package com.example.medibookandroid.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    public static String removeAccent(String s) {
        if (s == null) return "";
        // 1. Chuẩn hóa chuỗi unicode (tách dấu ra khỏi chữ)
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        // 2. Dùng Regex để xóa các dấu
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        // 3. Chuyển về chữ thường và đổi 'đ' thành 'd'
        return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d");
    }
}