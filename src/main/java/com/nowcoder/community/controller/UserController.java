package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;



@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String  uploadPath;

    @Value("${community.path.domain}")
    private String  domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * / 映射到templates文件夹
     * 账号设置
     * @return
     */
    @LoginRequired
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 处理上传头像请求
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error","您还未选择图片！");
            return "/site/setting";
        }

        // 先读取文件名并截取后缀名
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (suffix == null) {
            model.addAttribute("error","文件的格式不正确！");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！");
        }

        // 成功后更新当前用户的投降的路径(web访问路径)
        // eg. http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     *  处理获取图像请求
     */
    @GetMapping("/header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 找到图片存放的路径和图片的文件名
        fileName = uploadPath + "/" + fileName;
        // 截取图片后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 设置并响应图片
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(fileName);
             OutputStream outputStream = response.getOutputStream();) {
            // fis输入流用于读取文件
            byte[] buffer = new byte[1024 * 100];
            int b = 0;
            while (((b = fis.read(buffer)) != -1)) {
                outputStream.write(buffer,0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败：" + e.getMessage());
        }

    }

    /**
     * 处理用户修改密码的请求
     */
    @LoginRequired
    @PostMapping("/updatePwd")
    public String updatePassword(String oldPwd, String newPwd, Model model, @CookieValue("ticket") String ticket) {
        // 验证原密码是否正确
        // 空值验证
        if (oldPwd == null) {
            model.addAttribute("passwordMsg","原密码不能为空！");
        }
        if (newPwd == null) {
            model.addAttribute("passwordMsg","新密码不能为空！");
        }
        // 获取当前用户
        User user = hostHolder.getUser();
        String oldPassword = CommunityUtil.md5(oldPwd + user.getSalt());
        if (user.getPassword().equals(oldPassword)) {
            // 正确则更新密码，并进行加密
            String newPassword = CommunityUtil.md5(newPwd + user.getSalt());
            userService.updatePassword(user.getId(), newPassword);
            // 修改完密码后,将此登陆凭证修改为失效，重定向到登陆页面
            userService.logout(ticket);
            return "redirect:/login";
        } else {
            // 原密码不正确，则提示相关信息
            model.addAttribute("passwordMsg","原密码不正确！");
            return "site/setting";
        }
    }

    /**
     * 去忘记密码页面
     */
    @GetMapping("/forget")
    public String getForgetPwdPage() {
        return "site/forget";
    }

    /**
     * 处理重置密码中的【获取验证码】操作
     */
    @GetMapping("/getCode")
    public void getCode(String email) {
        Context context = new Context();
        context.setVariable("username","牛客服务中心");
        // 随机生成验证码
        //String code = kaptchaProducer.createText();
        String code = "u5s6dt";
        context.setVariable("code",code);
        // 用html模板处理邮件
        String content = templateEngine.process("/mail/forget",context);
        mailClient.sendMail(email,"ReSetPassword",content);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset")
    public String reSetPwd(String email, String code, String newPwd, Model model) {
        // 空值验证
        // 验证邮箱是否为空
        if(email==null) {
            model.addAttribute("emailMsg","邮箱不能为空");
        }
        // 验证验证码是否为空
        if(code==null) {
            model.addAttribute("codeMsg","验证码不能为空");
        }
        // 验证新密码是否为空
        if(newPwd==null) {
            model.addAttribute("passwordMsg","新密码不能为空");
        }
        // 判断验证码是否正确
        if("u5s6dt".equals(code)==false) {
            model.addAttribute("codeMsg","验证码错误！");
        }
        // 根据email查询用户，重置密码
        User user = userService.findUserByEmail(email);
        userService.updatePassword(user.getId(),newPwd);

        return "redirect:/login";
    }

    /**
     * 个人主页
     * 不仅可以查看当前用户的主页，也能查看其他用户的主页
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 查询用户
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 查询关注的数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 查询粉丝的数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 查询点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 查询是否关注该用户
        boolean hasFollowed = false;
        // 要先判断当前用户是否登录
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
