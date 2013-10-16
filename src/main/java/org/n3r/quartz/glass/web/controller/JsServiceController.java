package org.n3r.quartz.glass.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.job.annotation.JobBean;
import org.n3r.quartz.glass.log.joblog.JobLog;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class JsServiceController {
    /**
     * Gets job description for a job class.
     * Used as a js service from pages.
     */
    @RequestMapping("/jsapi/jobs/description")
    @ResponseBody
    public JobBean description(String className) {
        if (StringUtils.isEmpty(className)) return null;

        try {
            return JobBean.fromClass(Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @RequestMapping("/jsapi/logs")
    @ResponseBody
    public Page<JobLog> logs(@RequestParam Long executionId, @RequestParam(defaultValue = "1") Integer page) {
        return JobLogs.getLogs(executionId, Query.oneBasedIndex(page).withSize(LogsController.PAGE_SIZE));
    }
}
