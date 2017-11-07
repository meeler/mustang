package com.dimogo.open.myjobs.manager.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.dimogo.open.myjobs.dto.*;
import com.dimogo.open.myjobs.manager.admin.service.MyJobsService;
import com.dimogo.open.myjobs.types.NotificationParaType;
import com.dimogo.open.myjobs.types.NotificationType;
import com.dimogo.open.myjobs.utils.JobUtils;
import com.dimogo.open.myjobs.utils.ListUtils;
import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Ethan Xiao on 2017/4/19.
 */
@Service("clusteredJobService")
public class MyJobsServiceImpl implements MyJobsService {

	private static final Logger logger = Logger.getLogger(MyJobsServiceImpl.class);

	private static ExecutionHistoryComparator executionHistoryComparator = new ExecutionHistoryComparator();

	private ZkClient zkClient;

	public MyJobsServiceImpl() {
		zkClient = ZKUtils.newClient();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		zkClient.close();
	}

	public List<ClusteredJobInfo> listJobs(int start, int pageSize) {
		List<String> allJobs = zkClient.getChildren(ZKUtils.Path.Jobs.build());
		if (CollectionUtils.isEmpty(allJobs)) {
			return new ArrayList<ClusteredJobInfo>(0);
		}
		List<String> jobs = ListUtils.subList(allJobs, start, (start + 1) * pageSize);
		if (CollectionUtils.isEmpty(allJobs)) {
			return new ArrayList<ClusteredJobInfo>(0);
		}
		List<ClusteredJobInfo> jobInfos = new ArrayList<ClusteredJobInfo>(jobs.size());
		for (String job : jobs) {
			int executors = zkClient.countChildren(ZKUtils.buildJobExecutorsPath(job));
			int executions = zkClient.countChildren(ZKUtils.buildJobExecutionsPath(job));
			Integer instances = zkClient.readData(ZKUtils.buildJobInstancesPath(job), true);
			String cron = zkClient.readData(ZKUtils.buildJobCronPath(job), true);
			String paras = zkClient.readData(ZKUtils.buildJobParasPath(job), true);
			boolean pauseTrigger = zkClient.exists(ZKUtils.buildJobPauseTrigger(job));
			boolean exists = zkClient.exists(ZKUtils.buildJobPath(job));
			int historyCount = 0;
			try {
				historyCount = zkClient.countChildren(ZKUtils.buildJobHistoriesPath(job));
			} catch (Throwable e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e);
				}
			}

			ClusteredJobInfo jobInfo = new ClusteredJobInfo();
			jobInfo.setExists(exists);
			jobInfo.setPauseTrigger(pauseTrigger);
			jobInfo.setJobName(job);
			jobInfo.setExecutors(executors);
			jobInfo.setExecutions(executions);
			jobInfo.setHistoryCount(historyCount);
			if (instances != null) {
				jobInfo.setMaxInstances(instances);
			}
			if (cron != null) {
				jobInfo.setCron(cron);
			}
			if (paras != null) {
				jobInfo.setParas(paras);
			}
			jobInfos.add(jobInfo);
		}
		return jobInfos;
	}

	public int countJobs() {
		return zkClient.countChildren(ZKUtils.Path.Jobs.build());
	}

	public List<ExecutorInfo> listExecutors(int start, int pageSize) {
		List<String> allExecutors = zkClient.getChildren(ZKUtils.Path.Executors.build());
		if (CollectionUtils.isEmpty(allExecutors)) {
			return new ArrayList<ExecutorInfo>(0);
		}
		List<String> executors = ListUtils.subList(allExecutors, start, (start + 1) * pageSize);
		if (CollectionUtils.isEmpty(executors)) {
			return new ArrayList<ExecutorInfo>(0);
		}
		List<ExecutorInfo> executorInfos = new ArrayList<ExecutorInfo>(executors.size());
		for (String executor : executors) {
			ExecutorInfo e = new ExecutorInfo();
			e.setId(executor);

			String runtimeJson = zkClient.readData(ZKUtils.buildExecutorIDPath(executor), true);
			if (StringUtils.isNotBlank(runtimeJson)) {
				RuntimeInfo runtimeInfo = JSON.parseObject(runtimeJson, RuntimeInfo.class);
				e.setHost(runtimeInfo.getHostName());
				e.setIp(runtimeInfo.getIp());
				e.setArch(runtimeInfo.getOsArch());
				e.setCpuUsedPercent(runtimeInfo.getCpusUsedPercent());
				e.setDiskUsedPercent(runtimeInfo.getDisksUsedPercent());
				e.setOsVendorName(runtimeInfo.getOsVendorName());
				e.setOsVersion(runtimeInfo.getOsVersion());
			}

			executorInfos.add(e);
		}
		return executorInfos;
	}

	public ExecutorDetails findExecutor(String executorId) {
		ExecutorDetails details = new ExecutorDetails();
		details.setId(executorId);

		String runtimeJson = zkClient.readData(ZKUtils.buildExecutorIDPath(executorId), true);
		if (StringUtils.isNotBlank(runtimeJson)) {
			RuntimeInfo runtimeInfo = JSON.parseObject(runtimeJson, RuntimeInfo.class);
			details.setRuntime(runtimeInfo);
		}
		return details;
	}

	public int countExecutors() {
		return zkClient.countChildren(ZKUtils.Path.Executors.build());
	}

	public List<NotificationInfo> listNotifications(int start, int pageSize) {
		List<String> allNotifications = zkClient.getChildren(ZKUtils.Path.Notifications.build());
		if (CollectionUtils.isEmpty(allNotifications)) {
			return new ArrayList<NotificationInfo>(0);
		}
		List<String> notifications = ListUtils.subList(allNotifications, start, start + pageSize);
		if (CollectionUtils.isEmpty(notifications)) {
			return new ArrayList<NotificationInfo>(0);
		}
		List<NotificationInfo> executorInfos = new ArrayList<NotificationInfo>(notifications.size());
		for (String notification : notifications) {
			NotificationInfo notificationInfo = new NotificationInfo();
			notificationInfo.setName(notification);
			Map<String, String> paras = zkClient.readData(ZKUtils.buildNotificationPath(notification), true);
			if (MapUtils.isNotEmpty(paras)) {
				notificationInfo.setParas(JSON.toJSONString(paras));
			}
			executorInfos.add(notificationInfo);
		}
		return executorInfos;
	}

	public int countNotifications() {
		return zkClient.countChildren(ZKUtils.Path.Notifications.build());
	}

	public ClusteredJobInfo findJob(String jobName) {
		boolean exists = zkClient.exists(ZKUtils.buildJobPath(jobName));
		int executors = zkClient.countChildren(ZKUtils.buildJobExecutorsPath(jobName));
		int executions = zkClient.countChildren(ZKUtils.buildJobExecutionsPath(jobName));
		Integer instances = zkClient.readData(ZKUtils.buildJobInstancesPath(jobName), true);
		String cron = zkClient.readData(ZKUtils.buildJobCronPath(jobName), true);
		String paras = zkClient.readData(ZKUtils.buildJobParasPath(jobName), true);
		boolean pauseTrigger = zkClient.exists(ZKUtils.buildJobPauseTrigger(jobName));
		int historyCount = 0;
		try {
			historyCount = zkClient.countChildren(ZKUtils.buildJobHistoriesPath(jobName));
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		ClusteredJobInfo jobInfo = new ClusteredJobInfo();
		jobInfo.setPauseTrigger(pauseTrigger);
		jobInfo.setExists(exists);
		jobInfo.setJobName(jobName);
		jobInfo.setExecutors(executors);
		jobInfo.setExecutions(executions);
		jobInfo.setHistoryCount(historyCount);
		if (instances != null) {
			jobInfo.setMaxInstances(instances);
		}
		if (cron != null) {
			jobInfo.setCron(StringUtils.trim(cron));
		}
		if (paras != null) {
			jobInfo.setParas(paras);
		}
		return jobInfo;
	}

	public boolean deleteJob(String jobName) {
		int executors = zkClient.countChildren(ZKUtils.buildJobExecutorsPath(jobName));
		int executions = zkClient.countChildren(ZKUtils.buildJobExecutionsPath(jobName));
		if (executions != 0 || executors != 0) {
			return false;
		}
		try {
			return zkClient.deleteRecursive(ZKUtils.buildJobPath(jobName));
		} catch (Throwable e) {
			return false;
		}
	}

	public void updateJob(ClusteredJobInfo jobInfo) {
		String paras = JSON.toJSONString(JobUtils.parameterListToJson(jobInfo.getParas()));
		jobInfo.setParas(paras);
		jobInfo.setCron(StringUtils.trim(jobInfo.getCron()));

		JobUtils.setConfJobParas(jobInfo.getJobName(), jobInfo.getParas());
		String instancePath = ZKUtils.buildJobInstancesPath(jobInfo.getJobName());
		if (zkClient.exists(instancePath)) {
			zkClient.writeData(ZKUtils.buildJobInstancesPath(jobInfo.getJobName()), jobInfo.getMaxInstances());
		} else {
			zkClient.create(ZKUtils.buildJobInstancesPath(jobInfo.getJobName()), jobInfo.getMaxInstances(), CreateMode.PERSISTENT);
		}
		if (StringUtils.isNoneBlank(jobInfo.getCron())) {
			JobUtils.setJobCronExpression(jobInfo.getJobName(), jobInfo.getCron());
		} else {
			JobUtils.cleanJobCronExpression(jobInfo.getJobName());
		}
	}

	public List<ExecutorInfo> listJobExecutors(String job) {
		List<ExecutorInfo> executors = new LinkedList<ExecutorInfo>();
		try {
			List<String> executorNames = zkClient.getChildren(ZKUtils.buildJobExecutorsPath(job));
			for (String executor : executorNames) {
				ExecutorInfo e = new ExecutorInfo();
				e.setId(executor);

				String runtimeJson = zkClient.readData(ZKUtils.buildExecutorIDPath(executor), true);
				if (StringUtils.isNotBlank(runtimeJson)) {
					RuntimeInfo runtimeInfo = JSON.parseObject(runtimeJson, RuntimeInfo.class);
					e.setHost(runtimeInfo.getHostName());
					e.setIp(runtimeInfo.getIp());
					e.setArch(runtimeInfo.getOsArch());
					e.setCpuUsedPercent(runtimeInfo.getCpusUsedPercent());
					e.setDiskUsedPercent(runtimeInfo.getDisksUsedPercent());
					e.setOsVendorName(runtimeInfo.getOsVendorName());
					e.setOsVersion(runtimeInfo.getOsVersion());
				}
				executors.add(e);
			}
			return executors;
		} catch (Exception e) {
			return executors;
		}
	}

	public List<JobExecutionDTO> listJobExecutions(String job) {
		List<JobExecutionDTO> executions = new LinkedList<JobExecutionDTO>();
		try {
			List<String> executionIDs = zkClient.getChildren(ZKUtils.buildJobExecutionsPath(job));
			for (String id : executionIDs) {
				JobExecutionDTO e = zkClient.readData(ZKUtils.buildJobExecutionPath(job, id), true);
				if (e != null) {
					executions.add(e);
				}
			}
			return executions;
		} catch (Exception e) {
			return executions;
		}
	}

	public MasterInfo findMaster() {
		UUID masterId = zkClient.readData(ZKUtils.Path.MasterNode.build(), true);
		if (masterId == null) {
			return null;
		}
		MasterInfo masterInfo = new MasterInfo();
		masterInfo.setId(masterId.toString());
		return masterInfo;
	}

	public void stopJob(String jobName) {
		Map<String, String> message = new LinkedHashMap<String, String>();
		message.put(NotificationParaType.NotificationType.name(), NotificationType.StopJob.name());
		message.put(NotificationParaType.JobName.name(), jobName);

		try {
			List<String> jobExecutions = zkClient.getChildren(ZKUtils.buildJobExecutionsPath(jobName));
			Set<String> jobExecutors = new HashSet<String>(jobExecutions.size());
			for (String execution : jobExecutions) {
				JobExecutionDTO executionDTO = zkClient.readData(ZKUtils.buildJobExecutionPath(jobName, execution), true);
				if (executionDTO == null) {
					continue;
				}
				jobExecutors.add(executionDTO.getExecutorId());
			}
			JobUtils.sendJobNotification(jobName, NotificationType.StopJob.name(), message, jobExecutors);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public void pauseTrigger(String jobName) {
		Map<String, String> message = new LinkedHashMap<String, String>();
		message.put(NotificationParaType.NotificationType.name(), NotificationType.PauseTrigger.name());
		message.put(NotificationParaType.JobName.name(), jobName);

		try {
			JobUtils.sendJobNotification(jobName, NotificationType.PauseTrigger.name(), message, null);
			zkClient.create(ZKUtils.buildJobPauseTrigger(jobName), null, CreateMode.PERSISTENT);
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public void resumeTrigger(String jobName) {
		Map<String, String> message = new LinkedHashMap<String, String>();
		message.put(NotificationParaType.NotificationType.name(), NotificationType.ResumeTrigger.name());
		message.put(NotificationParaType.JobName.name(), jobName);

		try {
			JobUtils.sendJobNotification(jobName, NotificationType.ResumeTrigger.name(), message, null);
			zkClient.deleteRecursive(ZKUtils.buildJobPauseTrigger(jobName));
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public List<JobHistoryDTO> listExecutionHistory(String jobName, int start, int pageSize) {
		List<String> histories = null;
		try {
			histories = zkClient.getChildren(ZKUtils.buildJobHistoriesPath(jobName));
		} catch (Throwable e) {
			if (e instanceof ZkNoNodeException) {
				return new ArrayList<JobHistoryDTO>(0);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			throw new RuntimeException("get history ndoes error", e);
		}
		if (CollectionUtils.isEmpty(histories)) {
			return new ArrayList<JobHistoryDTO>(0);
		}
		Collections.sort(histories, executionHistoryComparator);
		List<String> selectedHistories = ListUtils.subList(histories, start, start + pageSize);
		if (CollectionUtils.isEmpty(selectedHistories)) {
			return new ArrayList<JobHistoryDTO>(0);
		}
		List<JobHistoryDTO> historyList = new ArrayList<JobHistoryDTO>(selectedHistories.size());
		for (String history : selectedHistories) {
			JobHistoryDTO historyDTO = zkClient.readData(ZKUtils.buildJobHistoryPath(jobName, history), true);
			if (historyDTO == null) {
				continue;
			}
			historyList.add(historyDTO);
		}
		return historyList;
	}

	public int countExecutionHistory(String jobName) {
		try {
			return zkClient.countChildren(ZKUtils.buildJobHistoriesPath(jobName));
		} catch (Throwable e) {
			if (e instanceof ZkNoNodeException) {
				return 0;
			}
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			throw new RuntimeException("count history error", e);
		}
	}

	public void cleanExecutionHistory(String jobName) {
		try {
			zkClient.deleteRecursive(ZKUtils.buildJobHistoriesPath(jobName));
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public boolean updateUser(String userName, String password, List<String> roles) {
		UserDTO user = new UserDTO();
		user.setUserName(userName);
		user.setPassword(password);
		user.setRoles(roles);
		try {
			zkClient.create(ZKUtils.buildUserPath(user.getUserName()), user, CreateMode.PERSISTENT);
			return true;
		} catch (ZkNodeExistsException e) {
			zkClient.writeData(ZKUtils.buildUserPath(user.getUserName()), user);
			return true;
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
			return false;
		}
	}

	public void deleteUser(String userName) {
		zkClient.deleteRecursive(ZKUtils.buildUserPath(userName));
	}

	public UserDTO findUser(String userName) {
		return zkClient.readData(ZKUtils.buildUserPath(userName), true);
	}

	public List<User2DTO> listUsers() {
		List<String> userNames = zkClient.getChildren(ZKUtils.Path.Users.build());
		List<User2DTO> users = new ArrayList<User2DTO>(userNames.size());
		for (String userName : userNames) {
			UserDTO userDTO = findUser(userName);
			User2DTO user2DTO = new User2DTO();
			user2DTO.setUsername(userDTO.getUserName());
			user2DTO.setPassword(userDTO.getPassword());
			user2DTO.setRoles(userDTO.getRoles().toString());
			users.add(user2DTO);
		}
		return users;
	}

	private static class ExecutionHistoryComparator implements Comparator<String> {

		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2) < 0 ? 1 : -1;
		}
	}
}
