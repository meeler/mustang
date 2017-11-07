package com.dimogo.open.myjobs.listener;

import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Ethan Xiao on 2017/4/6.
 */
public class InitSystemListener implements ServletContextListener {
	private static final Logger logger = Logger.getLogger(InitSystemListener.class);

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ZkClient zkClient = null;
		try {
			zkClient = ZKUtils.newClient();
			ZKUtils.create(zkClient, ZKUtils.Path.Root.build(), null, CreateMode.PERSISTENT);
			ZKUtils.create(zkClient, ZKUtils.Path.MyJobs.build(), null, CreateMode.PERSISTENT);
			ZKUtils.create(zkClient, ZKUtils.Path.Jobs.build(), null, CreateMode.PERSISTENT);
			ZKUtils.create(zkClient, ZKUtils.Path.Executors.build(), null, CreateMode.PERSISTENT);
			ZKUtils.create(zkClient, ZKUtils.Path.Notifications.build(), null, CreateMode.PERSISTENT);
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} finally {
			if (zkClient != null) {
				zkClient.close();
			}
		}
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
