package com.elyjah.springzhipu.controller;


import org.springframework.ai.image.*;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/image")
public class ImageController {

//    private final OpenAiImageModel imageModel;
//
//    public ImageController(OpenAiImageModel imageModel) {
//        this.imageModel = imageModel;
//    }
//
//    @GetMapping("/generate")
//    public String generate(@RequestParam String prompt) {
//        ImageResponse response = imageModel.call(
//                new ImagePrompt(prompt,
//                OpenAiImageOptions.builder()
    /// /                        .model("cogview")
//                        .n(1)
//                        .responseFormat("url")
//                        .build())
//        );
//        return response.getResult().getOutput().getUrl();
//    }

    private final ImageModel imageModel;    //接口类型

    public ImageController(ImageModel imageModel) {
        this.imageModel = imageModel;        // 构造器注入
    }

    @RequestMapping("/draw")
    public String draw(@RequestParam(defaultValue = "一只戴墨镜的猫") String prompt) {
        ImagePrompt request = new ImagePrompt(prompt,
                OpenAiImageOptions.builder()
                        .model("cogview-3-flash")
                        .n(1)
                        .responseFormat("url")
                        .build()
        );

        ImageResponse response = imageModel.call(request);
        return response.getResult().getOutput().getUrl();
    }


    @GetMapping("/pd")
    public String promptDemo() {

        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model("cogview-3-flash")
                .n(1)
                .responseFormat("url")
                .build();

        ImagePrompt p1 = new ImagePrompt("一只哈基米");

        ImagePrompt p2 = new ImagePrompt("一只哈基米", options);

        ImagePrompt p3 = new ImagePrompt(new ImageMessage("一只哈基米"), options);

        // 打印出来看内部结构
        return "写法1: " + p1 + "\n\n"
                + "写法2: " + p2 + "\n\n"
                + "写法3: " + p3;
    }


    @RequestMapping("/md")
    public String messageDemo() {
        ImageMessage m1 = new ImageMessage("一只哈基米");
        ImageMessage m2 = new ImageMessage("一只哈基米", 1.2f);
        ImageMessage m3 = new ImageMessage(
                "一只哈基米",
                -0.5f,
                List.of(),// 附加的媒体（图片等）
                Map.of("note", "测试")// 元数据 给消息挂标签 不发模型
        );
        return "构造1: " + m1 + "\n\n"
                + "构造2: " + m2 + "\n\n"
                + "构造3: " + m3;
    }


    @RequestMapping("/od")
    public String optionDemo() {

        OpenAiImageOptions a = OpenAiImageOptions.builder()
                .n(1)
                .build();

        OpenAiImageOptions b = OpenAiImageOptions.builder()
                .model("cogview-3-flash")
                .n(1)
                .build();

        OpenAiImageOptions c = OpenAiImageOptions.builder().build();

        return "a.model=" + a.getModel() + ", a.n=" + a.getN() + "\n\n"
                + "b.model=" + b.getModel() + ", b.n=" + b.getN() + "\n\n"
                + "c.model=" + c.getModel();

    }

    @GetMapping("/rd")
    public String responseDemo() {
        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model("cogview-3-flash")
                .n(2)
                .responseFormat("url")
                .build();

        ImageResponse response = imageModel.call(new ImagePrompt("一只梳着银发中分，穿着背带裤，打着篮球的鸡", options));

        // getResults()：拿全部，遍历
        StringBuilder sb = new StringBuilder();
        List<ImageGeneration> all = response.getResults();
        sb.append("一共生成 ").append(all.size()).append(" 张\n\n");
        for (int i = 0; i < all.size(); i++) {
            Image img = all.get(i).getOutput();   // 每张 → Image
            sb.append("第 ").append(i + 1).append(" 张: ").append(img.getUrl()).append("\n");
        }

        //getResult()：只拿第一张
        Image first = response.getResult().getOutput();
        sb.append("\ngetResult() 拿到的: ").append(first.getUrl());

        return sb.toString();
    }

}