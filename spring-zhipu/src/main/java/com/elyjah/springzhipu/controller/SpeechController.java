package com.elyjah.springzhipu.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    private final TextToSpeechModel textToSpeechModel;

    public SpeechController(TextToSpeechModel textToSpeechModel) {
        this.textToSpeechModel = textToSpeechModel;
    }


    @GetMapping(value = "/tts",produces = "audio/wav")
    public void tts(@RequestParam(defaultValue = "你好喵，关注塔菲喵")String text,
                    HttpServletResponse response) throws IOException {

        byte[] audio = textToSpeechModel.call(text); //  一行出音频

        response.setContentType("audio/wav");
        response.setContentLength(audio.length);
        response.getOutputStream().write(audio);
        response.getOutputStream().flush();
    }
}