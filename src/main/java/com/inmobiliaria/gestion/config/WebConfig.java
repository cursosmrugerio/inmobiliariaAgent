package com.inmobiliaria.gestion.config;

import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Cache static assets (CSS, JS, images) for 1 hour with versioning
    registry
        .addResourceHandler("/assets/**")
        .addResourceLocations("classpath:/static/assets/")
        .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());

    // Don't cache index.html to ensure users get the latest version
    registry
        .addResourceHandler("/", "/index.html")
        .addResourceLocations("classpath:/static/")
        .setCacheControl(CacheControl.noCache().noStore().mustRevalidate());
  }
}
