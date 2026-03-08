package com.shangnantea.model.vo.user;

import java.util.Date;

/**
 * 关注信息值对象，用于前端展示关注列表
 */
public class FollowVO {
    
    /**
     * 关注ID
     */
    private Integer id;
    
    /**
     * 被关注对象ID
     */
    private String targetId;
    
    /**
     * 关注类型(shop, user)
     */
    private String targetType;
    
    /**
     * 关注目标名称
     */
    private String targetName;
    
    /**
     * 关注目标头像
     */
    private String targetAvatar;
    
    /**
     * 店铺Logo（仅店铺类型有效）
     */
    private String targetLogo;
    
    /**
     * 店铺描述（仅店铺类型有效）
     */
    private String targetDescription;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    
    public String getTargetAvatar() {
        return targetAvatar;
    }
    
    public void setTargetAvatar(String targetAvatar) {
        this.targetAvatar = targetAvatar;
    }
    
    public String getTargetLogo() {
        return targetLogo;
    }
    
    public void setTargetLogo(String targetLogo) {
        this.targetLogo = targetLogo;
    }
    
    public String getTargetDescription() {
        return targetDescription;
    }
    
    public void setTargetDescription(String targetDescription) {
        this.targetDescription = targetDescription;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
