package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer producer;

    /**
     * 关注或者取消关注只是局部刷新，发送的是异步请求
     * 当前用户去关注某一个实体,用户可以从HostHolder获取，要传入entityType,entityId
     */
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        // 获取当前登录用户
        User user = hostHolder.getUser();
        // 关注某个实体
        followService.follow(user.getId(), entityType, entityId);
        // 异步请求给前端返回数据(json字符串)

        // 触发关注事件(系统发送通知)
        Event event = new Event().setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        producer.fireEvent(event);

        return CommunityUtil.getJSONString(0,"已关注!");
    }

    /**
     * 某人取消对某实体的关注
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        // 获取当前登录用户
        User user = hostHolder.getUser();
        // 取消关注某个实体
        followService.unfollow(user.getId(), entityType, entityId);
        // 异步请求给前端返回数据(json字符串)
        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

    // 查询关注列表(这里只查询某用户关注的人)
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        // 分页设置
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(ENTITY_TYPE_USER, userId));

        // 查询关注列表
        List<Map<String, Object>> followeesList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(followeesList != null) {
            for (Map<String, Object> map : followeesList) {
                User u = (User) map.get("user");
                // 判断是否关注
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followeesList", followeesList);
        return "/site/followee";
    }

    // 查询粉丝列表
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        // 分页设置
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        // 查询关注列表
        List<Map<String, Object>> followersList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followersList != null) {
            for (Map<String, Object> map : followersList) {
                User u = (User) map.get("user");
                // 判断是否关注
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("followersList", followersList);
        return "/site/follower";
    }


    private boolean hasFollowed(int userId) {
        // 当前用户没有登录的-- 显示没关注
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
