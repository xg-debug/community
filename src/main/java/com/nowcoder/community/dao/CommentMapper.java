package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 查询评论列表
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询某个实体的评论数量
    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);


//    // 查询某个用户的评论列表
//    List<Comment> selectCommentsByUserId(int entityType, int entityId, int offset, int limit,int userId);

//    // 查询某个用户的评论数量
//    int selectCommentCountByUserId(int userId);

    // 查询我所评论的帖子的ids
    List<Integer> selectPostIdsByUserId(int userId);

    // 查询我的评论的id
    int selectCommentIdByUserId(int userId);

    // 根据帖子id查询评论
    List<Comment> selectCommentByPostId(int postId);

}
