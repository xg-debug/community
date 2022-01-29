package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询所有帖子
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit) {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(userId, offset, limit);
        return discussPosts;
    }

    /**
     * 查询所有帖子的数量
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 发布一个新的帖子
     * @param post
     * @return
     */
    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 转移HTML标记,有标签的话会把标签去掉
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 通过 用户id 查询帖子
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 修改帖子的数量
     * @param postId
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int postId, int commentCount) {
        return discussPostMapper.updateCommentCount(postId, commentCount);
    }
}
