package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // offset为起始页码，limit为每页显示多少条数据，方便sql语句的拼接

    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    /**
     * 查询共有多少条帖子，@Param注解的作用是给参数起个别名
     * 如果方法只有一个参数，并且在<if>标签里使用，则必须加别名
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 添加帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 修改帖子评论的数量
     * @param postId
     * @param commentCount
     * @return
     */
    int updateCommentCount(int postId, int commentCount);

    /**
     * 修改帖子类型
     * @param id
     * @param type 0--普通帖子，1--置顶帖子
     * @return
     */
    int updateType(int id, int type);

    int updateStatus(int id, int sattus);

    int updateScore(int id, double score);
}
