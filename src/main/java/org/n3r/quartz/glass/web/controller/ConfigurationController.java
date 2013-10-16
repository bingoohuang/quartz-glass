package org.n3r.quartz.glass.web.controller;

import org.n3r.quartz.glass.configuration.Configuration;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The home page !
 */
@Controller
public class ConfigurationController {

    @Autowired
    private Configuration configuration;

    @RequestMapping("/configuration")
    public String configuration(Model model) throws SchedulerException {
        model.addAttribute("configuration", configuration);

        return "configuration";
    }

}
