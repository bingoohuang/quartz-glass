package org.n3r.quartz.glass.web.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.configuration.Version;
import org.n3r.quartz.glass.tools.FormatTool;
import org.n3r.quartz.glass.tools.UtilsTool;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class AddToModelInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private Scheduler quartzScheduler;

    @Autowired
    private Configuration configuration;

    @Autowired
    private Version version;

    private UtilsTool utilsTool = new UtilsTool();

    private FormatTool formatTool = new FormatTool();

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView == null) return;

        if (StringUtils.startsWith(modelAndView.getViewName(), "redirect:")) return;

        ModelMap model = modelAndView.getModelMap();

        model.addAttribute("standby", quartzScheduler.isInStandbyMode());
        model.addAttribute("root", configuration.getRoot());
        String current = request.getServletPath() + request.getPathInfo();
        model.addAttribute("current", URLEncoder.encode(current, "UTF-8"));
        model.addAttribute("utils", utilsTool);
        model.addAttribute("format", formatTool);
        model.addAttribute("version", version);
    }
}
