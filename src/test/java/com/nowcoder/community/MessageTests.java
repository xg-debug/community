package com.nowcoder.community;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
@SpringBootTest
public class MessageTests {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void test01() {
        List<Message> list = messageMapper.selectConversations(111,0,10);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(111);

        List<Message> list1 = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list1) {
            System.out.println(message);
        }
        int count1 = messageMapper.selectLetterCount("111_112");
        System.out.println(count1);

        int count2 = messageMapper.selectLetterUnreadCount(111,"111_131");
        System.out.println(count2);
    }
}
