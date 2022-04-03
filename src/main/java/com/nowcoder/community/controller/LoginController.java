package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 去注册页面
     * @return
     */
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {
        return "site/register";
    }

    /**
     * 去登录页面
     * @return
     */
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage() {
        return "site/login";
    }

    /**
     * 提交表单数据进行注册
     * @return
     */
    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map==null || map.isEmpty()) {
            // 注册成功
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        // 也可以写成@PathVariable int userId 这种格式
        // 进行激活账号
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的帐号已经可以正常使用了!");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT) {
            // 重复激活
            model.addAttribute("msg","无效操作，该账号已经激活过了!");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","激活失败，您提供的激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        //重构,用Redis,原来是存放在session中，在分布式部署时会存在session共享的问题
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 把验证码存入session，以便后续进行登录验证----现在进行重构
        //session.setAttribute("kaptcha",text);

        // 验证码的归属(随机生成一个字符串，用于标识一个用户)，发给客户端用cookie保存
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);// 验证码60秒失效
        cookie.setPath(contextPath);// 验证码整个路径都有效
        response.addCookie(cookie);
        // 将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);


        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("响应验证码失败！"+e.getMessage());
        }

    }


    /**
     * 登陆验证
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme, Model model, /*HttpSession session,*/
                        HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码是否正确(从redis中取)
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        // 从redis中获取kaptcha
        if (StringUtils.isNoneBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码不正确！");
            return "site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> loginMap = userService.login(username, password, expiredSeconds);
        if (loginMap.containsKey("ticket")) {
            // 成功
            // 从服务器取出ticket,发送给客户端
            Cookie cookie = new Cookie("ticket",loginMap.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:index";
        } else {
            // 失败
            model.addAttribute("usernameMsg",loginMap.get("usernameMsg"));
            model.addAttribute("passwordMsg",loginMap.get("passwordMsg"));
            return "site/login";
        }

    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:login";
    }
}
