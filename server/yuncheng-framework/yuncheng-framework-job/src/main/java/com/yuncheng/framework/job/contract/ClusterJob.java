package com.yuncheng.framework.job.contract;

import com.yuncheng.framework.job.context.JobContext;

/** 多实例环境下由平台统一调度的业务任务。 */
public interface ClusterJob {

    void execute(JobContext context);
}
