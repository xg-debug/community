package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)// 说明该注解可以写在方法上
@Retention(RetentionPolicy.RUNTIME)// 声明注解有效的时间
public @interface LoginRequired {

}
