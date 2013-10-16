package org.n3r.quartz.glass.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author olivier lagache
 */

@Component
public class Version {
    private static final String APPLICATION_VERSION_NAME = "application.version";
    private static final String COMPILATION_DATE_NAME = "compilation.date";
    private static final String COMPILATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private String applicationVersion;

    private Date compilationDate;

    @PostConstruct
    public void initialize() throws IOException, ParseException {
        Properties properties = new Properties();

        InputStream propertyStream = getClass().getResourceAsStream("/glass-version.txt");
        properties.load(propertyStream);
        propertyStream.close();

        applicationVersion = properties.getProperty(APPLICATION_VERSION_NAME);

        String compilationDateAsString = properties.getProperty(COMPILATION_DATE_NAME);

        if (StringUtils.isNotEmpty(compilationDateAsString)) {
            compilationDate = new SimpleDateFormat(COMPILATION_DATE_FORMAT).parse(compilationDateAsString);
        }
    }

    public Date getCompilationDate() {
        return compilationDate;
    }

    public String getApplicationVersion() {

        return applicationVersion;
    }

}
