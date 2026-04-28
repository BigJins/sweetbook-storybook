package com.sweetbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/files/**")
            .addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath() + "/")
            .setCachePeriod(3600);

        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCachePeriod(86400);

        registry.addResourceHandler("/favicon.ico", "/vite.svg")
            .addResourceLocations("classpath:/static/")
            .setCachePeriod(86400);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/stories/new").setViewName("forward:/index.html");
        registry.addViewController("/stories/{id:[\\w-]+}").setViewName("forward:/index.html");
        registry.addViewController("/orders").setViewName("forward:/index.html");
        registry.addViewController("/orders/{id:[\\w-]+}").setViewName("forward:/index.html");
    }
}
