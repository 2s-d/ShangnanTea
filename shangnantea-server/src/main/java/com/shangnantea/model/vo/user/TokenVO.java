package com.shangnantea.model.vo.user;

/**
 * Token值对象，用于登录接口返回
 */
public class TokenVO {
    
    /**
     * JWT令牌
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserVO userInfo;
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserVO getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserVO userInfo) {
        this.userInfo = userInfo;
    }
} 