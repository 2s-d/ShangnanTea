package com.shangnantea.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {

    /**
     * 标准日期格式
     */
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    
    /**
     * 标准日期时间格式
     */
    public static final String STANDARD_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /**
     * 简短日期格式
     */
    public static final String SHORT_DATE_FORMAT = "yyyy/MM/dd";
    
    /**
     * 简短日期时间格式
     */
    public static final String SHORT_DATETIME_FORMAT = "yyyy/MM/dd HH:mm";
    
    /**
     * 中文日期格式
     */
    public static final String CHINESE_DATE_FORMAT = "yyyy年MM月dd日";
    
    /**
     * 将Date格式化为字符串，使用标准日期时间格式
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(STANDARD_DATETIME_FORMAT).format(date);
    }
    
    /**
     * 将Date格式化为字符串，使用标准日期格式
     *
     * @param date 日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(STANDARD_DATE_FORMAT).format(date);
    }
    
    /**
     * 将Date格式化为字符串，使用指定格式
     *
     * @param date   日期
     * @param format 日期格式
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }
    
    /**
     * 将LocalDateTime格式化为字符串，使用标准日期时间格式
     *
     * @param localDateTime 本地日期时间
     * @return 格式化后的字符串
     */
    public static String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DateTimeFormatter.ofPattern(STANDARD_DATETIME_FORMAT));
    }
    
    /**
     * 将LocalDate格式化为字符串，使用标准日期格式
     *
     * @param localDate 本地日期
     * @return 格式化后的字符串
     */
    public static String formatDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT));
    }
    
    /**
     * 将字符串转换为Date，使用标准日期时间格式
     *
     * @param dateStr 日期字符串
     * @return 转换后的Date
     */
    public static Date parseDateTime(String dateStr) {
        return parse(dateStr, STANDARD_DATETIME_FORMAT);
    }
    
    /**
     * 将字符串转换为Date，使用标准日期格式
     *
     * @param dateStr 日期字符串
     * @return 转换后的Date
     */
    public static Date parseDate(String dateStr) {
        return parse(dateStr, STANDARD_DATE_FORMAT);
    }
    
    /**
     * 将字符串转换为Date，使用指定格式
     *
     * @param dateStr 日期字符串
     * @param format  日期格式
     * @return 转换后的Date
     */
    public static Date parse(String dateStr, String format) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(format).parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("日期解析错误: " + dateStr, e);
        }
    }
    
    /**
     * 将Date转换为LocalDateTime
     *
     * @param date 日期
     * @return 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * 将LocalDateTime转换为Date
     *
     * @param localDateTime 本地日期时间
     * @return 日期
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * 获取当前日期时间字符串，使用标准日期时间格式
     *
     * @return 当前日期时间字符串
     */
    public static String getCurrentDateTimeStr() {
        return formatDateTime(new Date());
    }
    
    /**
     * 获取当前日期字符串，使用标准日期格式
     *
     * @return 当前日期字符串
     */
    public static String getCurrentDateStr() {
        return formatDate(new Date());
    }
    
    /**
     * 计算两个日期之间的天数差
     *
     * @param start 开始日期
     * @param end   结束日期
     * @return 天数差
     */
    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("开始日期和结束日期不能为空");
        }
        LocalDate startLocalDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return java.time.temporal.ChronoUnit.DAYS.between(startLocalDate, endLocalDate);
    }
} 