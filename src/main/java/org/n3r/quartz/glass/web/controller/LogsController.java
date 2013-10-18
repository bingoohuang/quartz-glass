package org.n3r.quartz.glass.web.controller;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.log.execution.JobExecutions;
import org.n3r.quartz.glass.log.joblog.JobLogs;
import org.n3r.quartz.glass.util.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LogsController {
    public static final int PAGE_SIZE = 100;

    @Autowired
    protected JobExecutions executions;

    @Autowired
    protected Configuration configuration;

    @RequestMapping("/logs")
    public String logs(@RequestParam(defaultValue = "0") int index, Model model) {
        model.addAttribute("page", executions.find(Query.oneBasedIndex(index)));

        return "logs";
    }

    @RequestMapping("/logs/{result}")
    public String logs(@PathVariable String result, @RequestParam(defaultValue = "0") int index, Model model) {
        Query query = Query.oneBasedIndex(index).withResult(result);

        model.addAttribute("page", executions.find(query));

        return "logs";
    }

    @RequestMapping("/logs/{jobGroup}/{jobName}")
    public String logs(@PathVariable String jobGroup, @PathVariable String jobName, @RequestParam(defaultValue = "1") int index, Model model) {
        model.addAttribute("page", executions.find(jobGroup, jobName, Query.oneBasedIndex(index)));

        return "logs";
    }

    /**
     * Used for details popup from log list
     */
    @RequestMapping("/traces/{executionId}")
    public String traces(@PathVariable Long executionId, @RequestParam(defaultValue = "1") Integer index, Model model) {
        model.addAttribute("page", JobLogs.getLogs(executionId, Query.oneBasedIndex(index).withSize(PAGE_SIZE)));

        return "traces";
    }

    @RequestMapping("/logs/clear")
    public String clear(@RequestParam(defaultValue = "0") int index, Model model) {
        JobLogs.clear();
        executions.clear();

        return "redirect:/glass/logs";
    }
}
