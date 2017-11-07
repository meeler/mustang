package com.dimogo.open.myjobs.quartz;

import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Job Slave负责分发、执行Job，每个实例都是slave角色
 * Created by Ethan Xiao on 2017/4/17.
 */
public class MyJobSlave implements Runnable {

	private static Logger logger = Logger.getLogger(MyJobSlave.class);

	private static class MyJobSlaveHolder {
		private static MyJobSlave instance = new MyJobSlave();
	}

	public static MyJobSlave getInstance() {
		return MyJobSlaveHolder.instance;
	}

	private MyJobSlave() {

	}

	public void run() {
		BlockingQueue<List<String>> queue = new LinkedBlockingQueue<List<String>>(100);
		MyJobDispatcher dispatcher = new MyJobDispatcher(queue);
		new Thread(dispatcher).start();
		ZkClient zkClient = ZKUtils.newClient();
		try {
			String path = ZKUtils.Path.Notifications.build();
			while (true) {
				try {
					//监听JOB通知列表
					List<String> notifications = zkClient.watchForChilds(path);
					if (CollectionUtils.isEmpty(notifications)) {
						continue;
					}
					//转发通知
					queue.offer(notifications);
				} catch (Throwable e) {
					if (logger.isDebugEnabled()) {
						logger.debug("MyJobs slave error", e);
					}
					if (e instanceof InterruptedException) {
						throw (InterruptedException) e;
					}
					continue;
				}
			}
		} catch (InterruptedException e) {
			//break;
		} finally {
			zkClient.close();
		}
	}
}
