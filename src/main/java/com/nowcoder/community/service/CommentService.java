package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 通过实体类型查询评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    /**
     * 查询评论的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    /**
     * 添加评论(注意敏感词的过滤)
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {

        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 添加评论

        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

//    // 查询某个用户的评论列表
//    public List<Comment> selectCommentsByUserId(int entityType, int entityId, int offset, int limit,int userId) {
//        return commentMapper.selectCommentsByUserId(entityType, entityId, offset, limit, userId);
//    }

//    // 查询某个用户的评论数量
//    public int selectCommentCountByUserId(int userId) {
//        return commentMapper.selectCommentCountByUserId(userId);
//    }

    // 查询我所评论的帖子的ids
    public List<Integer> selectPostIdsByUserId(int userId) {
        return commentMapper.selectPostIdsByUserId(userId);
    }

    // 根据帖子id查询评论
    public List<Comment> selectCommentByPostId(int postId) {
        return commentMapper.selectCommentByPostId(postId);
    }

}
