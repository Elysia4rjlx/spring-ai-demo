package com.elyjah.springaidemo1.controller;


import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/sse")
@Slf4j
public class SseController {

    @RequestMapping("/data")
    public void data(HttpServletResponse response) throws IOException, InterruptedException {
        log.info("发起请求");
        response.setContentType("text/event-stream;charset=utf-8");
        PrintWriter writer = response.getWriter();

        for (int i = 0; i < 10; i++) {
            String s = "data: " + new Date() + "\n\n";
            writer.write(s);
            writer.flush();
            Thread.sleep(1000l);
        }

    }

    @RequestMapping("/retry")
    public void retry(HttpServletResponse response) throws IOException, InterruptedException {
        log.info("发起请求：retry");
        response.setContentType("text/event-stream;charset=utf-8");
        PrintWriter writer = response.getWriter();

        String s = "retry: 2000\n";
        s += "data: " + new Date() + "\n\n";
        writer.write(s);
        writer.flush();

    }

    @RequestMapping("/end")
    public void end(HttpServletResponse response) throws IOException, InterruptedException {
        log.info("发起请求:end");
        response.setContentType("text/event-stream;charset=utf-8");
        PrintWriter writer = response.getWriter();

        for (int i = 0; i < 10; i++) {
            String s = "event: foo\ndata: " + new Date() + "\n\n";
            writer.write(s);
            writer.flush();
            Thread.sleep(1000l);
        }

        //定义end事件，表示当前流传输结束
        writer.write("event: end\ndata: EOF\n\n");
        writer.flush();
    }

    @RequestMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1)).map(s->new Date().toString());
    }
}