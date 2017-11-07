package com.dimogo.open.myjobs.notification;

import com.dimogo.open.myjobs.quartz.SchedulerManager;
import com.dimogo.open.myjobs.types.NotificationParaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import java.util.Map;

/**
 * Created by ethanx on 2017/5/4.
 */
public class PauseTriggerProcessor implements NotificationProcessor {
	private static final Logger logger = Logger.getLogger(NotificationProcessor.class);

	public void dispatch(Map<String, String> paras) {
		String jobName = paras.get(NotificationParaType.JobName.name());
		if (StringUtils.isBlank(jobName)) {
			return;
		}
		try {
			SchedulerManager.getInstance().pauseTrigger(jobName);
		} catch (SchedulerException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}
}
