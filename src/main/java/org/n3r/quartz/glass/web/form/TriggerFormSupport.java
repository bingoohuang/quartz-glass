package org.n3r.quartz.glass.web.form;

import org.n3r.quartz.glass.SpringConfig;
import org.n3r.quartz.glass.util.Dates;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import java.util.Date;

/**
 * Base class for trigger forms.
 *
 * @author damien bourdette
 */
public class TriggerFormSupport {
    @DateTimeFormat(pattern = SpringConfig.DATE_FORMAT)
    @Future
    protected Date startTime;

    @DateTimeFormat(pattern = SpringConfig.DATE_FORMAT)
    protected Date endTime;

    protected String dataMap;

    public Date getStartTime() {
        return Dates.copy(startTime);
    }

    public void setStartTime(Date startTime) {
        this.startTime = Dates.copy(startTime);
    }

    public Date getEndTime() {
        return Dates.copy(endTime);
    }

    public void setEndTime(Date endTime) {
        this.endTime = Dates.copy(endTime);
    }

    public String getDataMap() {
        return dataMap;
    }

    public void setDataMap(String dataMap) {
        this.dataMap = dataMap;
    }
}
