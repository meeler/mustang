package com.dimogo.open.myjobs.quartz;

import com.dimogo.open.myjobs.types.NotificationParaType;
import com.dimogo.open.myjobs.types.NotificationType;
import com.dimogo.open.myjobs.utils.ID;
import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Ethan Xiao on 2017/4/18.
 */
public class MyJobDispatcher implements Runnable {

	private static final Logger logger = Logger.getLogger(MyJobDispatcher.class);
	private BlockingQueue<List<String>> queue;

	public MyJobDispatcher(BlockingQueue<List<String>> queue) {
		this.queue = queue;
	}

	public void run() {
		ZkClient zkClient = ZKUtils.newClient();
		try {
			while (true) {
				try {
					List<String> notifications = this.queue.take();
					for (String notification : notifications) {
						String notificationPath = ZKUtils.buildNotificationPath(notification);
						if (!zkClient.exists(notificationPath)) {
							continue;
						}
						Map<String, String> notificationParas = zkClient.readData(notificationPath, true);
						if (MapUtils.isEmpty(notificationParas)) {
							continue;
						}
						String type = notificationParas.get(NotificationParaType.NotificationType.name());
						if (StringUtils.isBlank(type)) {
							continue;
						}

						NotificationType notificationType = NotificationType.valueOf(type);
						boolean deleteNotification = false;
						if (notificationType.isNeedLock()) {
							deleteNotification = lock(notificationType, notification, zkClient, notificationParas);
						} else {
							deleteNotification = nonLock(zkClient, notificationType, notification, notificationParas);
						}

						if (!deleteNotification) {
							continue;
						}
						if (!notificationType.isForAllSlaves() || notificationType.isForMaster()) {
							try {
								zkClient.deleteRecursive(notificationPath);
							} catch (Throwable e) {
								if (logger.isDebugEnabled()) {
									logger.debug(e);
								}
							}
							continue;
						}
						try {
							if (CollectionUtils.isNotEmpty(zkClient.getChildren(ZKUtils.buildNotificationSlavesPath(notification)))) {
								continue;
							}
						} catch (ZkNoNodeException e) {
							//delete notification
						} catch (Throwable e) {
							continue;
						}

						try {
							zkClient.deleteRecursive(notificationPath);
						} catch (Throwable e) {
							if (logger.isDebugEnabled()) {
								logger.debug(e);
							}
						}
					}
				} catch (InterruptedException e) {
					if (logger.isDebugEnabled()) {
						logger.debug(e);
					}
					break;
				}
			}
		} finally {
			zkClient.close();
		}
	}

	private boolean lock(NotificationType notificationType, String notification, ZkClient zkClient, Map<String, String> paras) {
		String lockPath = ZKUtils.buildNotificationLockPath(notification);
		try {
			return nonLock(zkClient, notificationType, notification, paras);
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			return false;
		} finally {
			zkClient.deleteRecursive(lockPath);
		}
	}

	private boolean nonLock(ZkClient zkClient, NotificationType notificationType, String notification, Map<String, String> paras) {
		if (notificationType.isForMaster()) {
			return forMaster(zkClient, notificationType, paras);
		}
		if (notificationType.isForAllSlaves()) {
			return forAllSlaves(zkClient, notificationType, notification, paras);
		}
		notificationType.dispatch(paras);
		return true;
	}

	private boolean forMaster(ZkClient zkClient, NotificationType notificationType, Map<String, String> paras) {
		UUID masterId = zkClient.readData(ZKUtils.Path.MasterNode.build(), true);
		if (!ID.ExecutorID.equals(masterId)) {
			return false;
		}
		notificationType.dispatch(paras);
		return true;
	}

	private boolean forAllSlaves(ZkClient zkClient, NotificationType notificationType, String notification, Map<String, String> paras) {
		List<String> salves = null;
		try {
			salves = zkClient.getChildren(ZKUtils.buildNotificationSlavesPath(notification));
		} catch (ZkNoNodeException e) {
			return true;
		} catch (Throwable e) {
			return false;
		}
		if (CollectionUtils.isEmpty(salves)) {
			return true;
		}
		if (!salves.contains(ID.ExecutorID.toString())) {
			return false;
		}
		try {
			zkClient.deleteRecursive(ZKUtils.buildNotificationSlavePath(notification, ID.ExecutorID.toString()));
		} catch (Throwable e) {
			return false;
		}
		notificationType.dispatch(paras);
		return true;
	}

}
