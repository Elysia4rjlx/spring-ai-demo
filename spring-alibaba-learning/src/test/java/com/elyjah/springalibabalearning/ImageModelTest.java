package com.elyjah.springalibabalearning;


import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ImageModelTest {

    @Autowired
    private DashScopeImageModel imageModel;

    @Test
    void text2Img() {
        String key = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("KEY: " + (key == null ? "NULL" : key.length() + " 位, 开头 " + key.substring(0, Math.min(6, key.length()))));


        ImageResponse imageResponse = imageModel.call(
                new ImagePrompt("正在哈气的哈基米")
        );
        String imagUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imagUrl);
    }

    @Test
    void text2Img2() {
        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wanx2.1-t2i-plus")
                .build();
        ImageResponse imageResponse = imageModel.call(
                new ImagePrompt("科比正面打篮球",options)
        );
        String imagUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imagUrl);
    }

    @Test
    void text2Img3() {

        String key = System.getenv("DASHSCOPE_API_KEY");
        System.out.println("KEY: " + (key == null ? "NULL" : key.length() + " 位, 开头 " + key.substring(0, Math.min(6, key.length()))));

        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wanx2.1-t2i-turbo")
                .build();
        ImageResponse imageResponse = imageModel.call(
                new ImagePrompt("科比正面打篮球",options)
        );
        String imagUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imagUrl);
    }

    @Test
    void text2Img4() {
        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wanx2.1-t2i-turbo")
                .build();
        String prompt = "中国女孩，圆脸，看着镜头，优雅的民族服装，商业摄影，室外，电影级光照，半身特写，精致的淡妆，锐利的边缘";

        ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,options)
        );

        System.out.println(response.getResult().getOutput().getUrl());
    }

    @Test
    void text2Img5() {

        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wan2.5-t2i-preview")
                .withWatermark(true)
                .build();

        String prompt = "异瞳白猫，圆脸，一只蓝眼一只金眼，看着镜头，优雅的民族风小服饰，商业摄影，室外，电影级光照，半身特写，精致干净的面部细节，锐利的边缘";

        ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,options)
        );

        System.out.println(response.getResult().getOutput().getUrl());
    }



    @Test
    void text2img6() {
        DashScopeImageOptions options = DashScopeImageOptions.builder()
                .withModel("wan2.5-t2i-preview")
                .withN(3)
                .build();

        String propmt = "异瞳白猫，圆脸，一只蓝眼一只金眼，呼噜大睡";

        ImageResponse response = imageModel.call(
                new ImagePrompt(propmt,options)
        );

        List<ImageGeneration> results = response.getResults();

        List<String> urls = new ArrayList<>();

        for (ImageGeneration imageGeneration : results) {
            urls.add(imageGeneration.getOutput().getUrl());
        }

        System.out.println(urls);
    }
}