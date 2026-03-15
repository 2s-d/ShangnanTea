package com.shangnantea.service;

import com.shangnantea.common.api.Result;
import com.shangnantea.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试店铺创建功能
 */
@SpringBootTest
@ActiveProfiles("local")
public class ShopServiceCreateShopTest {

    @Autowired
    private ShopService shopService;

    @Test
    public void testCreateShop() {
        // 准备测试数据
        Map<String, Object> shopData = new HashMap<>();
        shopData.put("userId", "cy345428"); // 使用一个已通过认证的用户ID
        shopData.put("name", "测试店铺_" + System.currentTimeMillis()); // 使用时间戳确保唯一
        shopData.put("contactPhone", "15912590710");
        shopData.put("province", "14");
        shopData.put("city", "1404");
        shopData.put("district", "140406");
        shopData.put("address", "测试地址");
        shopData.put("businessLicense", "http://localhost:8080/api/files/images/certifications/test.jpg");

        // 调用创建店铺方法
        System.out.println("========== 开始测试创建店铺 ==========");
        System.out.println("测试数据: " + shopData);
        
        Result<Object> result = shopService.createShop(shopData);
        
        // 输出结果
        System.out.println("========== 测试结果 ==========");
        System.out.println("返回码: " + result.getCode());
        System.out.println("返回消息: " + result.getMessage());
        System.out.println("返回数据: " + result.getData());
        System.out.println("是否成功: " + (result.getCode() == 4000));
        
        // 断言
        assert result.getCode() == 4000 : "创建店铺应该成功，但返回码是: " + result.getCode();
        System.out.println("========== 测试通过 ==========");
    }
}
