package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
@SpringBootTest
class CommunityApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private DiscussPostMapper discussPostMapper;

	@Test
	void testSelectById() {
		System.out.println(userMapper.selectById(101));
	}

	@Test
	void testSelectByName() {
		System.out.println(userMapper.selectByName("liubei"));
	}

	@Test
	void testSelectByEmail() {
		System.out.println(userMapper.selectByEmail("nowcoder112@sina.com"));
	}
	@Test
	void testInsertUser() {

	}
	@Test
	void testUpdateStatus() {

	}
	@Test
	void testUpdateHeader() {

	}
	@Test
	void testUpdatePassword() {

	}

	@Test
	void testSelectDiscussPosts() {
		List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
		for (DiscussPost discussPost : discussPosts) {
			System.out.println(discussPost);
		}
	}

	@Test
	void testDiscussPostRows() {
		int rows = discussPostMapper.selectDiscussPostRows(0);
		System.out.println(rows);
	}

}
