package com.dimogo.open.myjobs.context;

import com.dimogo.open.myjobs.exception.JobRegisterException;
import com.dimogo.open.myjobs.listener.JobStatusListener;
import com.dimogo.open.myjobs.utils.ID;
import com.dimogo.open.myjobs.utils.ZKUtils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.util.Assert;

import java.util.*;

public class MyJobRegistry implements JobRegistry {
    private static final Logger logger = Logger.getLogger(MyJobRegistry.class);
    private Map<String, JobFactory> map = new HashMap<String, JobFactory>();
    private ZkClient zkClient;

    public MyJobRegistry() {
        zkClient = ZKUtils.newClient();
    }

    @Override
    protected void finalize() throws Throwable {
        if (zkClient != null) {
            zkClient.close();
        }
    }

    public void register(JobFactory jobFactory) throws DuplicateJobException {
        Assert.notNull(jobFactory);
        String name = jobFactory.getJobName();
        Assert.notNull(name, "Job configuration must have a name.");
        synchronized (map) {
            if (map.containsKey(name)) {
                throw new DuplicateJobException("A job configuration with this name [" + name
                        + "] was already registered");
            }

            String path = ZKUtils.buildJobPath(name);
            try {
                ZKUtils.create(zkClient, path, null, CreateMode.PERSISTENT);
                ZKUtils.create(zkClient, ZKUtils.buildJobExecutorsPath(name), null, CreateMode.PERSISTENT);
                zkClient.createEphemeral(ZKUtils.buildJobExecutorPath(name, ID.ExecutorID.toString()));
            } catch (Exception e) {
            	if (logger.isDebugEnabled()) {
            	    logger.debug(e);
                }
                throw new JobRegisterException("Create ZK node " + path + " error", e);
            }

            //为Job实例增加容器的监听
            AbstractJob job =  (AbstractJob) jobFactory.createJob();
            job.registerJobExecutionListener(new JobStatusListener());

            map.put(name, jobFactory);
        }
    }

    public void unregister(String name) {
        Assert.notNull(name, "Job configuration must have a name.");
        synchronized (map) {
            map.remove(name);
        }
    }

    public Job getJob(String name) throws NoSuchJobException {
        synchronized (map) {
            if (!map.containsKey(name)) {
                throw new NoSuchJobException("No job configuration with the name [" + name + "] was registered");
            }
            return map.get(name).createJob();
        }
    }

    public Collection<String> getJobNames() {
        synchronized (map) {
            return Collections.unmodifiableCollection(new HashSet<String>(map.keySet()));
        }
    }

}
