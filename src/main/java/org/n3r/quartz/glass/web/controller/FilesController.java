package org.n3r.quartz.glass.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serves all static files : css, images, js files...
 *
 * @author damien bourdette
 */
@Controller
public class FilesController {

    public static final int BUFFER_SIZE = 1024;

    @RequestMapping("/css/style.css")
    public void ccs(HttpServletResponse response) throws IOException {
        response.setContentType("text/css");

        serveResource("/org/n3r/quartz/glass/css/style.css", response);
    }

    @RequestMapping("/image/{name}.png")
    public void image(@PathVariable String name, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");

        serveResource("/org/n3r/quartz/glass/image/" + name + ".png", response);
    }

    @RequestMapping("/js/{name}.js")
    public void javascript(@PathVariable String name, HttpServletResponse response) throws IOException {
        response.setContentType("application/javascript");

        serveResource("/org/n3r/quartz/glass/js/" + name + ".js", response);
    }

    private void serveResource(String name, HttpServletResponse response) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(name);
        OutputStream outputStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];

        int size = 0;
        while ((size = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, size);
        }

        inputStream.close();
    }
}
