package com.nowcoder.community.entity;

import lombok.*;

import java.util.Date;

@Data
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    // 0--有效 1--无效
    private int status;
    private Date expired;
}
