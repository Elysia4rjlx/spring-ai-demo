package com.elyjah.springchatbot.tools;


import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeTools {


//    @Tool(description = "Get the current date and time in the user's timezone")
//    String getCurrentDate() {
//        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
//    }

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        // 获取当前的本地时间
        LocalDateTime now = LocalDateTime.now();
        // 获取当前时区的时间
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime nowWithZone = now.atZone(zoneId).toLocalDateTime();

        // 将当前时间格式化为所需的格式
        String formattedDateTime = nowWithZone.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return "当前时间: " + formattedDateTime;
    }

    @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}