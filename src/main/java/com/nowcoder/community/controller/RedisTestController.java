package com.nowcoder.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redisTest")
public class RedisTestController {

    @Autowired
    protected RedisTemplate redisTemplate;

    @GetMapping
    public String testRedis() {
        // 设置值到redis
        redisTemplate.opsForValue().set("name","lucy");
        // 从redis获取值
        String name = (String) redisTemplate.opsForValue().get("name");
        return name;
    }
}
