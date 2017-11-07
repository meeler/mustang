package com.dimogo.open.myjobs.quartz;

import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Ethan Xiao on 2017/4/13.
 */
public class MyJobMaster implements Runnable {

	private static final Logger logger = Logger.getLogger(MyJobMaster.class);

	private static class MyJobMasterHolder {
		private static MyJobMaster instance = new MyJobMaster();
	}

	public static MyJobMaster getInstance() {
		return MyJobMasterHolder.instance;
	}

	private MyJobMaster() {

	}

	public void run() {
		while (true) {
			ZKUtils.ZkExclusiveLock lock = new ZKUtils.ZkExclusiveLock();
			try {
				if (!lock.lock(ZKUtils.Path.Master.build(), ZKUtils.Path.MasterNode.build())) {
					Thread.sleep(10 * 1000);
				}
				schedule(lock.getZkClient());
			} catch (InterruptedException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e);
				}
				break;
			} catch (SchedulerException e) {
				if (logger.isDebugEnabled()) {
					logger.debug(e);
				}
				continue;
			} catch (Throwable e) {
				if (logger.isDebugEnabled()) {
					logger.debug("MyJobs master error", e);
				}
				continue;
			} finally {
				lock.unlock();
			}
		}
	}

	private void schedule(ZkClient zkClient) throws SchedulerException {
		logger.info("master startup");
		Scheduler scheduler = SchedulerManager.getInstance().start();
		try {
			scheduler.start();
			while (true) {
				//load all job names
				List<String> jobNames = zkClient.getChildren(ZKUtils.Path.Jobs.build());

				//clean jobs
				Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
				Set<String> scheduledJobs = new HashSet<String>(jobKeys.size());
				for (JobKey jobKey : jobKeys) {
					scheduledJobs.add(jobKey.getName());
					if (CollectionUtils.isEmpty(jobNames) || !jobNames.contains(jobKey.getName())) {
						if (unscheduledJob(scheduler, jobKey.getName())) {
							if (logger.isDebugEnabled()) {
								logger.debug("removed job " + jobKey.getName());
							}
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("remove job " + jobKey.getName() + " failed");
							}
						}
					}
				}

				if (CollectionUtils.isNotEmpty(jobNames)) {
					for (String jobName : jobNames) {
						if (zkClient.exists(ZKUtils.buildJobPauseTrigger(jobName))) {
							//paused trigger
							continue;
						}
						String cronExpression = zkClient.readData(ZKUtils.buildJobCronPath(jobName), true);
						try {
							if (StringUtils.isBlank(cronExpression)) {
								if (!scheduledJobs.contains(jobName)) {
									continue;
								}
								//no cron expression
								if (unscheduledJob(scheduler, jobName)) {
									if (logger.isDebugEnabled()) {
										logger.debug("unscheduled job " + jobName + ", cron expression is blank");
									}
								} else {
									if (logger.isDebugEnabled()) {
										logger.debug("unscheduled job " + jobName + " failed, cron expression is blank");
									}
								}
								continue;
							}
							JobKey jobKey = new JobKey(jobName);
							if (!scheduler.checkExists(jobKey)) {
								//new cron job
								if (scheduleJob(scheduler, jobName, cronExpression) != null) {
									if (logger.isDebugEnabled()) {
										logger.debug("schedule job " + jobName);
									}
								} else {
									if (logger.isDebugEnabled()) {
										logger.debug("schedule job " + jobName + " failed");
									}
								}
								continue;
							}
							CronTrigger oldTrigger = (CronTrigger) scheduler.getTrigger(new TriggerKey(jobName));
							if (!oldTrigger.getCronExpression().equals(cronExpression)) {
								//update cron of job
								if (rescheduleJob(scheduler, jobName, cronExpression) != null) {
									if (logger.isDebugEnabled()) {
										logger.debug("reschedule job " + jobName);
									}
								} else {
									if (logger.isDebugEnabled()) {
										logger.debug("reschedule job " + jobName + " failed");
									}
								}
								continue;
							}
						} catch (ParseException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(e);
							}
						} catch (SchedulerException e) {
							if (logger.isDebugEnabled()) {
								logger.debug(e);
							}
						}
					}
				}
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		} finally {
			scheduler.shutdown(false);
		}
	}

	public Date scheduleJob(Scheduler scheduler, String jobName, String cronExpression) throws SchedulerException, ParseException {
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(jobName)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

		JobDetail jobDetail = JobBuilder.newJob(MyJobber.class).withIdentity(jobName).build();

		return scheduler.scheduleJob(jobDetail, trigger);
	}

	public Date rescheduleJob(Scheduler scheduler,String jobName, String cronExpression) throws ParseException, SchedulerException {
		CronTrigger trigger = TriggerBuilder.newTrigger()
				.withIdentity(jobName)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
		return scheduler.rescheduleJob(trigger.getKey(), trigger);
	}

	public boolean unscheduledJob(Scheduler scheduler, String jobName) throws SchedulerException {
		return scheduler.unscheduleJob(new TriggerKey(jobName));
	}

}
