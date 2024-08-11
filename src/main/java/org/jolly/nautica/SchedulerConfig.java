package org.jolly.nautica;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.stream.IntStream;

@Profile("scheduling")
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class SchedulerConfig implements SchedulingConfigurer {

    @Bean
    ScheduledTaskRegistrar gtfs(GtfsClient client,
                                @Value("${schedule.gtfs.thread-count}") int threadCount,
                                @Value("${schedule.gtfs.delay-in-millis}") long delayInMillis) {
        String taskGroupName = GtfsClient.class.getSimpleName();
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(threadCount);
        taskScheduler.setThreadGroupName(taskGroupName);
        taskScheduler.setThreadNamePrefix(taskGroupName.concat("-"));
        taskScheduler.initialize();
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(60);

        ScheduledTaskRegistrar taskRegistrar = new ScheduledTaskRegistrar();
        taskRegistrar.setTaskScheduler(taskScheduler);

        IntStream.range(0, threadCount)
                .forEach(__ -> taskRegistrar.addFixedDelayTask(client::run, Duration.ofMillis(delayInMillis)));

        return taskRegistrar;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

    }
}
