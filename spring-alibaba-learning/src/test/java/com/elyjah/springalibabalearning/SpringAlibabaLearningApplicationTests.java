package com.elyjah.springalibabalearning;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringAlibabaLearningApplicationTests {

    @Autowired
    public DashScopeChatModel chatModel;

    @Test
    void testChat() {
        System.out.println(chatModel.call("介绍一下你自己"));
    }

}
