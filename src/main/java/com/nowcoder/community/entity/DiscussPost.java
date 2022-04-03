package com.nowcoder.community.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(indexName = "discusspost", shards = 6, replicas = 3)
public class DiscussPost {

    @Id
    private int id;

    // 用户id
    @Field(type = FieldType.Integer)
    private int userId;

    // 帖子的标题 eg.互联网校招
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    // 帖子的内容
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    // type为帖子类型，0--普通帖子，1--置顶帖子，2--精华帖子
    @Field(type = FieldType.Integer)
    private int type;

    // 帖子的状态，0--正常，1--精华，2--拉黑
    @Field(type = FieldType.Integer)
    private int status;

    // 帖子创建的时间
    @Field(type = FieldType.Date)
    private Date createTime;

    // 帖子评论的数量
    @Field(type = FieldType.Integer)
    private int commentCount;

    // 记录帖子的分数
    @Field(type = FieldType.Double)
    private double score;
}
