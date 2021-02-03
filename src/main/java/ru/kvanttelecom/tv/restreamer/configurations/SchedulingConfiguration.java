package ru.kvanttelecom.tv.restreamer.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfiguration implements SchedulingConfigurer {

    // By default all @Scheduled methods share a single thread (of same TaskPool).
    // https://stackoverflow.com/questions/29796651/what-is-the-default-scheduler-pool-size-in-spring-boot
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        final CustomizableThreadFactory threadFactory = new CustomizableThreadFactory();
        threadFactory.setDaemon(true);
        threadFactory.setThreadNamePrefix("SchedulingPool-");

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadFactory(threadFactory);
        taskScheduler.setPoolSize(5);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    // This TaskExecuter used for @Async @Scheduled methods
    // Same @Async method can execute in new thread while previous call has been not finished yet.
    // @Scheduled without @Async will wait till previous execution will finished
    @Bean
    public TaskExecutor threadPoolTaskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        //executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("AsyncSchedulingPool-");
        //executor.setQueueCapacity(0);
        executor.initialize();
        return executor;
    }

}