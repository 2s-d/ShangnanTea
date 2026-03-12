package com.shangnantea.service.impl;

/** Session id: user-user -> min_max_private; shop-user -> shopId_userId_customer */
public final class ChatSessionIdBuilder {
    private ChatSessionIdBuilder() {}
    public static String privateSessionId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) throw new IllegalArgumentException("userId must not be null");
        String a = userId1.compareTo(userId2) <= 0 ? userId1 : userId2;
        String b = userId1.compareTo(userId2) <= 0 ? userId2 : userId1;
        return a + "_" + b + "_private";
    }
    public static String customerSessionId(String shopId, String userId) {
        if (shopId == null || userId == null) throw new IllegalArgumentException("shopId and userId must not be null");
        return shopId + "_" + userId + "_customer";
    }
}
