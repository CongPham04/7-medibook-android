package com.example.medibookandroid.data.remote;
import java.util.Map;

public class FCMRequest {
    public String to;
    public Map<String, String> data;

    public FCMRequest(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }
}