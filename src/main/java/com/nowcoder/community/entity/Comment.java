package com.nowcoder.community.entity;

import lombok.*;

import java.util.Date;

@Data
public class Comment {
    private int id;
    // 发表评论的用户id
    private int userId;
    // 评论的实体类型 1:帖子 0:回复
    private int entityType;
    private int entityId;
    // 评论的目标用户
    private int targetId;
    // 评论的内容
    private String content;
    // 帖子的状态，0--正常，1--精华，2--拉黑
    private int status;
    // 评论的发布时间
    private Date createTime;
}
