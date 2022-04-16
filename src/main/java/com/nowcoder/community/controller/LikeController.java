package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer producer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @param entityUserId
     * @return
     * 注意：like() 方法中的参数名 要与发起异步请求时携带的参数名一致，否则会报错
     */
    @LoginRequired
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();
        // 加拦截器拦截，对于没登陆的用户要先进行登录才能进行点赞
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 查询点赞的数量
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 查询点赞的状态 0--未点赞 1--已点赞
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 把返回的结果封装到map集合中
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);
        // 返回给前端的数据data

        // 触发点赞事件(系统发送通知)
        if (likeStatus == 1) {
            Event event = new Event().setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setData("postId", postId)
                    .setEntityUserId(entityUserId);
            producer.fireEvent(event);
        }

        // 如果是对帖子点的赞，需要计算帖子分数
        if (entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            // 为了防止重复计算某个帖子分数的现象出现，使用set集合去重
            redisTemplate.opsForSet().add(redisKey, postId);

        }
        return CommunityUtil.getJSONString(0,null, map);
    }
}
