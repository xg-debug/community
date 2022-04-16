package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// 定时任务默认不启用，加上@EnableScheduling注解才会启用

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
