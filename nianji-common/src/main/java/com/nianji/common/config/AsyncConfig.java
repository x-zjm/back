package com.nianji.common.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    public AsyncConfig() {
        log.debug("AsyncConfig 被初始化了");
    }

    /**
     * 布隆过滤器专用的异步线程池
     */
    @Bean("bloomFilterExecutor")
    public Executor bloomFilterExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：即使空闲也保留的线程数
        executor.setCorePoolSize(2);
        // 最大线程数：线程池中允许的最大线程数
        executor.setMaxPoolSize(5);
        // 队列容量：任务队列的容量
        executor.setQueueCapacity(50);
        // 线程名前缀
        executor.setThreadNamePrefix("BloomFilter-Async-");
        // 拒绝策略：当线程池和队列都满了时的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程空闲时间：超过核心线程数的线程，空闲多久后被销毁
        executor.setKeepAliveSeconds(60);
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待任务结束的最大时间
        executor.setAwaitTerminationSeconds(60);
        // 初始化
        executor.initialize();
        return executor;
    }


    /**
     * 通用异步线程池
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Common-Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}