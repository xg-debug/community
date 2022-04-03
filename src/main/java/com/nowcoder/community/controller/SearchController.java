package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // /search?keyword=
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) {
        // 设置分页信息
        page.setLimit(10);
        page.setPath("/search?keyword=" + keyword);
        int count = (int) elasticsearchService.searchDiscussPostCount(keyword);
        page.setRows(count != 0 ? count : 0);

        // 搜索帖子
        List<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword);
        // 聚合数据(添加帖子作者信息，点赞数量信息)
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }

        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        return "/site/search";
    }
}
