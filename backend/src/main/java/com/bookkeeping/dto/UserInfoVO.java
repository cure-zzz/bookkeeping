package com.bookkeeping.dto;

import lombok.Data;

@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
}
