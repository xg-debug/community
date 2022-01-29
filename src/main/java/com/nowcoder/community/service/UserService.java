package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private LoginTicketMapper loginTicketMapper;


    public User findUserById(int userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    public Map<String,Object> register(User user) {
        Map<String,Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("passwordMsg","密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }

        // 以上都不为空，再开始验证账号是否已经被注册过
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1!=null) {
            // 要注册的账号已经存在,不能注册
            map.put("usernameMsg","该账号已存在!");
            return map;
        }

        // 验证邮箱
        user1 = userMapper.selectByEmail(user.getEmail());
        if (user1!=null) {
            map.put("emailMsg","该邮箱已被注册!");
            return map;
        }

        // 以上都没有问题，才能注册;保存之前要对密码加密：对(用户输入的密码+salt)进行加密
        // 使用generateUUID()生成随机字符串并截取前5位
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        // 再设置一下其他字段,因为传进来的只有用户名，密码，邮箱
        user.setType(0);
        user.setStatus(0);
        // 设置激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 设置随机头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 给用户发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // 设置url,http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号",content);
        return map;
    }

    /**
     * 激活用户
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        // 先查询用户
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            // 激活码正确则进行激活
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        } else {
            // 以上都不满足则激活失败
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 用户登录账号
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        // 因为登陆时传入的是用户的明文密码，而在MySQL数据库中存储的是用户加密后的密码
        // 所以在这里先要对传进来的密码进行同样的加密    expired--过期秒数
        Map<String ,Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        // 验证账号是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg","该账号不存在");
            return map;
        }

        // 判断账号是否激活
        if (user.getStatus()==0) {
            // 0--注册但未激活
            map.put("usernameMsg","该账号尚未激活！");
            return map;
        }

        // 加密明文密码并验证
        String key = password + user.getSalt();

        // 加密后的算法
        String pwd = CommunityUtil.md5(key);
        if (!user.getPassword().equals(pwd)) {
            // 密码不正确
            map.put("passwordMsg","密码不正确！");
            return map;
        }                                                                                                       // e10adc3949ba59abbe56e057f20f883e20d0a
        // 以上都没问题，则生成登陆凭证，用户可以登录
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        // 有效状态
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 用户退出登录
     * @param ticket
     */
    public void logout(String ticket) {
        // 0--代表登陆凭证无效
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    /**
     * 用户上传头像后修改头像路径
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId, headerUrl);
    }

    /**
     * 用户修改密码
     * @param userId
     * @param password
     * @return
     */
    public int updatePassword(int userId, String password) {
        return userMapper.updatePassword(userId, password);
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
}
