package com.elyjah.springalibabalearning;

import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechOptions;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionOptions;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisPrompt;
import com.alibaba.cloud.ai.dashscope.audio.synthesis.SpeechSynthesisResponse;
import com.alibaba.cloud.ai.dashscope.video.DashScopeVideoModel;
import com.alibaba.cloud.ai.dashscope.video.DashScopeVideoOptions;
import com.alibaba.cloud.ai.dashscope.video.VideoPrompt;
import com.alibaba.cloud.ai.dashscope.video.VideoResponse;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.audio.asr.transcription.Transcription;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionQueryParam;
import com.alibaba.dashscope.audio.asr.transcription.TranscriptionResult;
import com.alibaba.dashscope.audio.tts.SpeechSynthesisResult;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@SpringBootTest
public class SpeechSynthesisTest {

    @Autowired
    private DashScopeAudioSpeechModel speechModel;

    @Autowired
    private DashScopeAudioTranscriptionModel transcriptionModel;


    @Test
    void text2Audio() {
        DashScopeAudioSpeechOptions options = DashScopeAudioSpeechOptions.builder()
                .model("sambert-zhiqi-v1")
                .voice("longhua")
                .speed(0.5f)
                .build();

        SpeechSynthesisResponse response = speechModel.call(
                new SpeechSynthesisPrompt(
                        "哈基米南波南北绿豆"
                        , options)
        );

        ByteBuffer audio = response.getResult().getOutput().getAudio();

        File file = new File(System.getProperty("user.dir") + "/output.mp3");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audio.array());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void text2Audio2() {
//        Constants.baseWebsocketApiUrl = "wss://{WorkspaceId}.cn-beijing.maas.aliyuncs.com/api-ws/v1/inference";
        try (FileOutputStream audio = new FileOutputStream("output.mp3")) {
            SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen-audio-3.1-tts-flash")
                    .voice("longanhuan_v3.1")
                    .format(SpeechSynthesisAudioFormat.MP3_24000HZ_MONO_256KBPS)
                    .build();
            ResultCallback<SpeechSynthesisResult> callback = new ResultCallback<>() {
                @Override
                public void onEvent(SpeechSynthesisResult result) {
                    ByteBuffer buffer = result.getAudioFrame();
                    if (buffer != null) {
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        try {
                            audio.write(bytes);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void onComplete() {
                }

                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, callback);
            try {
                for (String text : new String[]{"哈基米，哈基米", "南北绿豆！。"}) {
                    synthesizer.streamingCall(text);
                }
                synthesizer.streamingComplete(60000);
            } finally {
                synthesizer.getDuplexApi().close(1000, "done");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void stt() {
//
//        Resource resource = new DefaultResourceLoader()
//                .getResource("https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav");
        Resource resource = new FileSystemResource(System.getProperty("user.dir") + "/output.mp3");
        AudioTranscriptionResponse response = transcriptionModel.call(
                new AudioTranscriptionPrompt(resource,
                        DashScopeAudioTranscriptionOptions.builder()
                                .withModel("paraformer-v2")
                                .build())
        );

        System.out.println("识别结果: " + response.getResult().getOutput());
    }

    @Test
    void sttDashscope() {
        // ① 构造参数
        TranscriptionParam param = TranscriptionParam.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model("paraformer-v2")              // 或 fun-asr / qwen3-asr-flash
                .fileUrls(Arrays.asList(
                        "https://dashscope.oss-cn-beijing.aliyuncs.com/samples/audio/paraformer/hello_world_female2.wav"
                ))
                .build();

        try {
            Transcription transcription = new Transcription();

            // ② 提交异步任务，拿到 taskId
            TranscriptionResult result = transcription.asyncCall(param);
            System.out.println("TaskId: " + result.getTaskId());

            // ③ 轮询等结果
            result = transcription.wait(
                    TranscriptionQueryParam.FromTranscriptionParam(param, result.getTaskId())
            );

            // ④ 打印结果（结果里是 transcriptionUrl，要去下载）
            System.out.println("任务状态: " + result.getTaskStatus());
            result.getResults().forEach(r -> {
                System.out.println("文件: " + r.getFileUrl());
                System.out.println("转写结果 URL: " + r.getTranscriptionUrl());

                //把转录结果JSON打印出来
                String transUrl = r.getTranscriptionUrl();
                try {
                    String jsonText = new String(
                            new URL(transUrl).openStream().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    JsonObject json = JsonParser.parseString(jsonText).getAsJsonObject();
                    String text = json.getAsJsonArray("transcripts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    System.out.println("识别文字: " + text);


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    void textToVideo() throws NoApiKeyException, InputRequiredException {
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .model(VideoSynthesis.Models.WAN_2_7_I2V)
                .prompt("一只正在哈气的橘猫")
                .size("1280*720")
                .duration(5)
                .build();

        System.out.println("任务提交中，请稍后。。");

        VideoSynthesis vs = new VideoSynthesis();
        VideoSynthesisResult result = vs.call(param);

        String url = result.getOutput().getVideoUrl();
        System.out.println("视频地址（24小时有效）: " + url);
    }

    @Autowired
    private DashScopeVideoModel videoModel;

    @Test
    void textToVideos() {
        DashScopeVideoOptions options = DashScopeVideoOptions.builder()
                .model(VideoSynthesis.Models.WAN_2_7_I2V)     // ⚠️ 见下方说明
                .prompt("一只小猫在月光下奔跑")
                .size("1280*720")
                .duration(5)
                .build();

        VideoResponse response = videoModel.call(new VideoPrompt(options));

        System.out.println("视频URL: " + response.getResult().getOutput().getVideoUrl());
    }
}