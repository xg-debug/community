package com.nowcoder.community.entity;

import lombok.*;

import java.util.Date;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    /**
     * status 0--已经注册但未激活，1--已经激活
     */
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;// 关注时间
}
