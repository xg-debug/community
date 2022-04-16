package com.nowcoder.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    // 将路径注入进来
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 初始化注解
    @PostConstruct
    public void init() {
        // 在程序启动的时候，SpringBoot会认为这是一个配置类，会自动调用
        // init这个方法，创建一个目录
        // 创建wk图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("创建WK图片目录：" + wkImageStorage);
        }
    }
}
