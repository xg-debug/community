package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;



@Controller
@RequestMapping("/user")
public class UserController {

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

    /**
     * / 映射到templates文件夹
     * @return
     */
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 处理上传图片请求
     */
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
}
