package com.dimogo.open.myjobs.quartz;

import com.dimogo.open.myjobs.utils.ID;
import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 注册Job Executor实例，实例ID在每次服务重启后会改变
 * Created by ethanx on 2017/5/3.
 */
public class MyJobExecutor implements Runnable {

	private static class MyJobExecutorHolder {
		private static MyJobExecutor instance = new MyJobExecutor();
	}

	public static MyJobExecutor getInstance() {
		return MyJobExecutorHolder.instance;
	}

	public void run() {
		ZkClient zkClient = ZKUtils.newClient();
		try {
			String executorsPath = ZKUtils.Path.Executors.build();
			String executorPath = ZKUtils.buildExecutorIDPath();
			String id = ID.ExecutorID.toString();
			while (true) {
				try {
					//注册当前实例
					zkClient.createEphemeral(executorPath);

					while (true) {
						//监听Executors列表实例，当发生变化时如果当前实例不在列表中结束循环重新注册结点
						List<String> executors = zkClient.watchForChilds(executorsPath);
						if (CollectionUtils.isEmpty(executors) || !executors.contains(id)) {
							//executor not in list
							break;
						}
					}
				} catch (ZkNodeExistsException e) {
					//executor registered
				} catch (Throwable e) {
					//注册失败时重新注册
					continue;
				}
			}
		} finally {
			if (zkClient != null) {
				zkClient.close();
			}
		}
	}
}
