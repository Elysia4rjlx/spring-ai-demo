package com.elyjah.springaidemo1.controller;

import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ds")
public class DeepSeekController {

    @Autowired
    private DeepSeekChatModel deepSeekChatModel;

    @RequestMapping("/chat")
    public String chat(String message) {
        return deepSeekChatModel.call(message);
    }
}