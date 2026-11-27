package com.elyjah.springalibabalearning.controller;


import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image")
public class ImagController {

    private final DashScopeImageModel imageModel;

    public ImagController(DashScopeImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @RequestMapping("/create")
    public String text2Imag(String prompt) {
        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wanx2.1-t2i-turbo")
                .build();

        ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,options)
        );

        return response.getResult().getOutput().getUrl();
    }
}