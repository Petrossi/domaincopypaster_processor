package com.domainsurvey.crawler;

import com.domainsurvey.crawler.service.backend.BackendService;
import com.domainsurvey.crawler.service.crawler.CrawlerUtilsService;
import com.domainsurvey.crawler.service.crawler.CrawlerWorker;
import com.domainsurvey.crawler.service.crawler.finalizer.CrawlingFinalizerService;
import com.domainsurvey.crawler.service.dao.DomainDeleter;
import com.domainsurvey.crawler.thread.StartParsingThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.logging.LogManager;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static ConfigurableApplicationContext context;

    static {
        LogManager.getLogManager().reset();
    }

    public static void main(String[] args) {
        startApp(args);
    }

    private static void startApp(String[] args) {
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.addListeners(new PropertiesLogger());
        context = springApplication.run(args);

        startFunctions();
    }

    private static void startFunctions() {
        System.out.println("startFunctions");

        startParsing(context);

        startFinalizer(context);

//        startDeleter(context);
    }

    static void startParsing(ConfigurableApplicationContext context) {
        new StartParsingThread(() -> context.getBean(CrawlerUtilsService.class).startLastSession()).start();
        context.getBean(CrawlerWorker.class).startTaskMonitor();
        context.getBean(BackendService.class).start();
    }

    static void startDeleter(ConfigurableApplicationContext context) {
        context.getBean(DomainDeleter.class).start();
    }

    static void startFinalizer(ConfigurableApplicationContext context) {
        context.getBean(CrawlingFinalizerService.class).start();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**");
            }
        };
    }
}