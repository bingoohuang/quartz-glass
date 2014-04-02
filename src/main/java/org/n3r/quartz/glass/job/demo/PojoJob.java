package org.n3r.quartz.glass.job.demo;

import org.n3r.quartz.glass.job.annotation.GlassJob;
import org.n3r.quartz.glass.job.annotation.JobArgument;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.quartz.DisallowConcurrentExecution;

@GlassJob(description = "淘宝订单同步", team = "小牛队", created = "2013-10-16")
@DisallowConcurrentExecution
public class PojoJob {
    @JobArgument(description = "订单类型")
    private String orderType;

    public void execute() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JobLogs.info("淘宝订单:" + orderType);
    }
}
