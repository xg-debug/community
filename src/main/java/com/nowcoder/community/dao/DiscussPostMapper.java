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
     * 查询共有多少条帖子，这里注解的作用是给参数起个别名
     * 如果方法只有一个参数，并且在<if>标签里使用，则必须加别名
     */
    int selectDiscussPostRows(@Param("userId") int userId);
}
