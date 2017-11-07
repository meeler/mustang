package com.dimogo.open.myjobs.utils;

import com.dimogo.open.myjobs.dto.UserDTO;
import com.dimogo.open.myjobs.sys.Config;
import com.dimogo.open.myjobs.types.UserRoleType;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;

/**
 * Created by Ethan Xiao on 2017/4/6.
 */
public class ZKUtils {

	private static final Logger logger = Logger.getLogger(ZKUtils.class);

	private static class SerializableSerializerHolder {
		private static SerializableSerializer serializableSerializer = new SerializableSerializer();
	}

	public enum Path {
		Root("/mustang", null),
		MyJobs("/myjobs", Root),
		Jobs("/jobs", MyJobs),
		Executors("/executors", MyJobs),
		Notifications("/notifications", MyJobs),
		Master("/master", MyJobs),
		MasterNode("/node", Master),
		Users("/users", MyJobs),;

		private String path;
		private Path parent;

		Path(String path, Path parent) {
			this.parent = parent;
			this.path = path;
		}

		static void buildPath(Path path, StringBuilder builder) {
			if (path.parent != null) {
				buildPath(path.parent, builder);
			}
			builder.append(path.path);
		}

		public String build() {
			StringBuilder builder = new StringBuilder();
			this.buildPath(this, builder);
			return builder.toString();
		}
	}

	public static SerializableSerializer getSerializableSerializer() {
		return SerializableSerializerHolder.serializableSerializer;
	}

	public static ZkClient newClient() {
		return new ZkClient(Config.getZKServers(), Config.getZKSessionTimeout(), Config.getZKConnTimeout(), getSerializableSerializer());
	}

	public static void create(ZkClient zkClient, String path, Object data, CreateMode mode) throws Exception {
		try {
			zkClient.create(path, data, mode);
		} catch (Exception e) {
			if (e instanceof ZkNodeExistsException) {
				return;
			}
			throw e;
		}
	}

	public static String buildJobPath(String job) {
		return Path.Jobs.build() + "/" + job;
	}

	public static String buildJobParasPath(String job) {
		return buildJobPath(job) + "/paras";
	}

	public static String buildJobCronPath(String job) {
		return buildJobPath(job) + "/cron";
	}

	public static String buildNotificationPath(String notification) {
		return Path.Notifications.build() + "/" + notification;
	}

	public static String buildNotificationLockPath(String notification) {
		return buildNotificationPath(notification) + "/locked";
	}

	public static String buildNotificationSlavesPath(String notification) {
		return buildNotificationPath(notification) + "/slaves";
	}

	public static String buildNotificationSlavePath(String notification, String slave) {
		return buildNotificationPath(notification) + "/slaves/" + slave;
	}

	public static String buildJobExecutorsPath(String job) {
		return Path.Jobs.build() + "/" + job + "/executors";
	}

	public static String buildJobExecutorPath(String job, String executor) {
		return buildJobExecutorsPath(job) + "/" + executor;
	}

	public static String buildJobHistoriesPath(String job) {
		return Path.Jobs.build() + "/" + job + "/histories";
	}

	public static String buildJobHistoryPath(String job, String executionId) {
		return buildJobHistoriesPath(job) + "/" + executionId;
	}

	public static String buildJobPauseTrigger(String job) {
		return Path.Jobs.build() + "/" + job + "/pause";
	}

	public static String buildExecutorIDPath() {
		return Path.Executors.build() + "/" + ID.ExecutorID;
	}

	public static String buildExecutorIDPath(String executorId) {
		return Path.Executors.build() + "/" + executorId;
	}

	public static String buildJobExecutionsPath(String job) {
		return Path.Jobs.build() + "/" + job + "/executions";
	}

	public static String buildJobExecutionPath(String job, String id) {
		return buildJobExecutionsPath(job) + "/" + id;
	}

	public static String buildJobInstancesPath(String job) {
		return buildJobPath(job) + "/instances";
	}

	public static String buildUserPath(String userName) {
		return Path.Users.build() + "/" + userName;
	}

	public static void initSupperUser(ZkClient zkClient) {
		try {
			create(zkClient, Path.Users.build(), null, CreateMode.PERSISTENT);
			UserDTO user = new UserDTO();
			user.setUserName("admin");
			user.setPassword("admin");
			user.setRole(UserRoleType.ROLE_SUPPER.name());
			create(zkClient, buildUserPath(user.getUserName()), user, CreateMode.PERSISTENT);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			return;
		}
	}

	public static int countJobExecutions(ZkClient zkClient, String job) {
		return zkClient.countChildren(buildJobExecutionsPath(job));
	}

	public static class ZkExclusiveLock {

		private ZkClient zkClient;

		public ZkExclusiveLock() {
			this.zkClient = newClient();
		}

		public void unlock() {
			try {
				this.zkClient.close();
			} catch (Throwable e) {

			}
		}

		public ZkClient getZkClient() {
			return zkClient;
		}

		public boolean lock(String lockPath, String lockedPath) {
			return tryLock(lockPath, lockedPath, -1);
		}

		public boolean tryLock(String lockPath, String lockedPath, long timeout) {
			final long exp = timeout > 0 ? System.currentTimeMillis() + timeout : -1;
			while (exp == -1 || System.currentTimeMillis() < exp) {
				try {
					create(zkClient, lockPath, null, CreateMode.PERSISTENT);
					zkClient.createEphemeral(lockedPath, ID.ExecutorID);
					return true;
				} catch (ZkNodeExistsException e) {
					zkClient.watchForChilds(lockPath);
					continue;
				} catch (InterruptedException e) {
					break;
				} catch (Throwable e) {
					//throw e;
					continue;
				}
			}
			return false;
		}
	}

}
