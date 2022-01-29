package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 用于生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    // MD5算法加密
    public static String md5(String key) {
        // 首先判断key字符串是否为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        // 此方法要求传入的字符串为字节数组
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key,map.get(key));
            }
        }
        // 返回json格式的字符串
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code,msg);
    }

    public static String getJSONString(int code) {
        return getJSONString(code);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",20);
        System.out.println(getJSONString(0,"ok",map));
    }
}
