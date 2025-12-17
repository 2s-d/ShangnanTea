package com.shangnantea.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * 判断字符串是否为空白
     *
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 判断字符串是否不为空白
     *
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    /**
     * 生成UUID，去除横线
     *
     * @return UUID字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成指定长度的随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String generateRandomString(int length) {
        if (length < 1) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * 生成指定前缀和长度的ID
     *
     * @param prefix 前缀
     * @param length 长度
     * @return ID
     */
    public static String generateId(String prefix, int length) {
        if (isEmpty(prefix)) {
            return generateRandomString(length);
        }
        
        int randomLength = length - prefix.length();
        if (randomLength <= 0) {
            return prefix;
        }
        
        return prefix + generateRandomString(randomLength);
    }
    
    /**
     * 截取字符串，超出部分用省略号代替
     *
     * @param str    字符串
     * @param length 长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int length) {
        if (isEmpty(str) || length <= 0 || str.length() <= length) {
            return str;
        }
        
        return str.substring(0, length) + "...";
    }
    
    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 验证邮箱格式
     *
     * @param email 邮箱
     * @return 是否为有效邮箱
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     * @return 是否为有效手机号
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        
        String regex = "^1[3-9]\\d{9}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
    
    /**
     * 将字符串按逗号分割
     *
     * @param str 字符串
     * @return 分割后的列表
     */
    public static List<String> splitByComma(String str) {
        List<String> result = new ArrayList<>();
        if (isEmpty(str)) {
            return result;
        }
        
        String[] arr = str.split(",");
        for (String item : arr) {
            if (isNotEmpty(item)) {
                result.add(item.trim());
            }
        }
        
        return result;
    }
    
    /**
     * 将列表转换为逗号分隔的字符串
     *
     * @param list 列表
     * @return 逗号分隔的字符串
     */
    public static String joinByComma(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(list.get(i));
        }
        
        return sb.toString();
    }
    
    /**
     * 隐藏手机号中间4位
     *
     * @param phone 手机号
     * @return 隐藏后的手机号
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 7) {
            return phone;
        }
        
        int length = phone.length();
        int prefixLength = (length - 4) / 2;
        int suffixLength = length - 4 - prefixLength;
        
        String prefix = phone.substring(0, prefixLength);
        String suffix = phone.substring(length - suffixLength);
        
        return prefix + "****" + suffix;
    }
    
    /**
     * 隐藏邮箱用户名部分
     *
     * @param email 邮箱
     * @return 隐藏后的邮箱
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 2) {
            return "*" + domain;
        }
        
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
} 