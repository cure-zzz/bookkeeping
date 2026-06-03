package com.bookkeeping.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String icon;
    private Integer type;  // 1:收入 2:支出
    private String color;
    private Long userId;   // NULL表示系统预设
    private LocalDateTime createdAt;
}
