package org.n3r.quartz.glass.web.form;

import org.quartz.Trigger;

import java.text.ParseException;

public interface TriggerForm {
    public Trigger getTrigger(Trigger trigger) throws ParseException;
}
