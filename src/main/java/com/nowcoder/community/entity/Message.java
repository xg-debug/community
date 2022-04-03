package com.nowcoder.community.entity;

import lombok.*;

import java.util.Date;

@Data
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    // 0--未读 1--已读 2--删除
    private int status;
    private Date createTime;
}
