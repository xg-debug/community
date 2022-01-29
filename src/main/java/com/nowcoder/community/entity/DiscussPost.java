package com.nowcoder.community.entity;

import lombok.*;

import java.util.Date;

@Data
public class DiscussPost {
    private int id;
    private int userId;
    private String title;
    private String content;
    // type为帖子类型，0--普通帖子，1--置顶帖子

    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;
}
