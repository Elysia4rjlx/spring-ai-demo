package com.elyjah.springaidemo1;


import reactor.core.publisher.Flux;

import java.time.Duration;

public class FluxDemoTest {
    public static void main(String[] args) throws InterruptedException {
        //Flux.just() 创建一个包含多个元素的数据流。
        Flux<String> flux = Flux.just("Apple","Banana","Cherry","Pear").delayElements(Duration.ofSeconds(1));

        //map() 对数据进行加工
        flux.map(String::toUpperCase)
                .map(s -> s + "-1")
                //subscribe() 表示订阅数据流，告诉 Flux：数据来了之后，我要如何处理它。
                .subscribe(System.out::println);

        Thread.sleep(5000);
    }
}