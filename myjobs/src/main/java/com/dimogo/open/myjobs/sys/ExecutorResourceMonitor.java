package com.dimogo.open.myjobs.sys;

import com.alibaba.fastjson.JSON;
import com.dimogo.open.myjobs.dto.RuntimeInfo;
import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;

/**
 * Created by Ethan Xiao on 2017/4/27.
 */
public class ExecutorResourceMonitor implements Runnable {

	private static final Logger logger = Logger.getLogger(ExecutorResourceMonitor.class);

	private static class ExecutorResourceMonitorHolder {
		private static ExecutorResourceMonitor instance = new ExecutorResourceMonitor();
	}

	public static ExecutorResourceMonitor getInstance() {
		return ExecutorResourceMonitorHolder.instance;
	}

	private ExecutorResourceMonitor() {

	}

	public void run() {
		ZkClient zkClient = ZKUtils.newClient();
		String path = ZKUtils.buildExecutorIDPath();
		try {
			while (true) {
				try {
					Thread.sleep(Config.getResourceMonitorInterval());
					RuntimeInfo runtimeInfo = new RuntimeInfo();
					runtimeInfo.setup();
					String data = JSON.toJSONString(runtimeInfo);
					zkClient.writeData(path, data);
				} catch (InterruptedException e) {
					if (logger.isDebugEnabled()) {
						logger.debug(e);
					}
					break;
				} catch (Throwable e) {
					//catch exception
				}
			}
		} finally {
			zkClient.close();
		}
	}
}
