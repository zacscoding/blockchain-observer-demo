package org.blocksync.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author zacconding
 * @Date 2018-05-13
 * @GitHub : https://github.com/zacscoding
 */
@Configuration
public class ThreadConfiguration {

    @Bean
    public TaskExecutor eventHandlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //executor.setCorePoolSize(4);
        executor.setMaxPoolSize(50);
        executor.initialize();
        return executor;
    }
}
