package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
@SpringBootTest
public class EsTests {

    // 注入ElasticsearchRestTemplate
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 插入一条数据
    @Test
    public void testInsert() {
        // 查询帖子实体
        DiscussPost post = discussPostMapper.selectDiscussPostById(241);
        restTemplate.save(post);
    }

    // 插入多条数据
    public void testInsertList() {
        List<DiscussPost> posts = new ArrayList<>();
        posts.add(discussPostMapper.selectDiscussPostById(241));
        posts.add(discussPostMapper.selectDiscussPostById(242));
        posts.add(discussPostMapper.selectDiscussPostById(243));
        Iterator<DiscussPost> discussPosts = posts.iterator();
        restTemplate.save(discussPosts);
    }
}
