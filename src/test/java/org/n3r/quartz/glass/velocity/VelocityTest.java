package org.n3r.quartz.glass.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n3r.quartz.glass.SpringConfig;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author damien bourdette
 */
public class VelocityTest {

    private VelocityEngine velocityEngine;

    @Before
    public void init() throws IOException {
        velocityEngine = new SpringConfig().velocityConfig().getVelocityEngine();
    }

    @Test
    public void merge() {
        Context context = new VelocityContext();
        context.put("message", "hi");

        Assert.assertEquals("hi", merge("/org/n3r/quartz/glass/velocity/velocity-test.vm", context));
    }

    @Test
    public void equals() {
        Context context = new VelocityContext();
        context.put("value", new Dummy("hi"));
        context.put("message", "hi");

        Assert.assertEquals(" hi ", merge("/org/n3r/quartz/glass/velocity/velocity-test-equals.vm", context));
    }

    private String merge(String template, Context context) {
        Template velocityTemplate = velocityEngine.getTemplate(template);

        StringWriter writer = new StringWriter();

        velocityTemplate.merge(context, writer);

        return writer.toString();
    }

    public static class Dummy {
        private String message;

        private Dummy(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
