package com.example.medibookandroid.data.remote;

import java.util.Map;

public class FCMRequestV1 {
    public Message message;

    public FCMRequestV1(String token, String title, String body, Map<String, String> dataMap) {
        this.message = new Message();
        this.message.token = token;

        this.message.notification = new Notification();
        this.message.notification.title = title;
        this.message.notification.body = body;

        this.message.data = dataMap;
    }

    public static class Message {
        public String token;
        public Notification notification;
        public Map<String, String> data;
    }

    public static class Notification {
        public String title;
        public String body;
    }
}