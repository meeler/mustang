package com.dimogo.open.myjobs.notification;

import com.dimogo.open.myjobs.types.NotificationParaType;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Ethan Xiao on 2017/4/18.
 */
public class RunJobProcessor implements NotificationProcessor {

	private static final Logger logger = Logger.getLogger(NotificationProcessor.class);
	private JobLocator jobLocator;
	private JobLauncher jobLauncher;
	private JobRepository jobRepository;

	public void dispatch(Map<String, String> paras) {
		try {
			String jobName = paras.get(NotificationParaType.JobName.name());
			Map<String, JobParameter> jobParas = new LinkedHashMap<String, JobParameter>();
			JobParameters jobParameters = new JobParameters(jobParas);
			Job job = jobLocator.getJob(jobName);
			jobLauncher.run(job, jobParameters);
		} catch (NoSuchJobException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (JobInstanceAlreadyCompleteException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (JobExecutionAlreadyRunningException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (JobParametersInvalidException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		} catch (JobRestartException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}

	public void setJobLocator(JobLocator jobLocator) {
		this.jobLocator = jobLocator;
	}

	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}
}
