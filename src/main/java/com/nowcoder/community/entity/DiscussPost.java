package com.nowcoder.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
