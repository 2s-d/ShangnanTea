# UserController 接口对比清单

## 前端 user.js 接口列表（35个）

| 序号 | 前端函数名 | 请求方式 | 路径 | 对应Controller方法 |
|------|-----------|----------|------|-------------------|
| 1 | login | POST | /user/login | ✅ login |
| 2 | register | POST | /user/register | ✅ register |
| 3 | logout | POST | /user/logout | ✅ logout |
| 4 | getCurrentUser | GET | /user/me | ✅ getCurrentUser |
| 5 | refreshToken | POST | /user/refresh | ✅ refreshToken |
| 6 | getUserInfo | GET | /user/{userId} 或 /user/me | ✅ getUserInfo (GET /{userId}) |
| 7 | updateUserInfo | PUT | /user | ✅ updateUserInfo (PUT /) |
| 8 | uploadAvatar | POST | /user/avatar | ✅ uploadAvatar |
| 9 | changePassword | PUT | /user/password | ✅ changePassword |
| 10 | resetPassword | POST | /user/password/reset | ✅ resetPassword |
| 11 | getAddressList | GET | /user/addresses | ✅ getAddressList |
| 12 | addAddress | POST | /user/addresses | ✅ addAddress |
| 13 | updateAddress | PUT | /user/addresses/{id} | ✅ updateAddress |
| 14 | deleteAddress | DELETE | /user/addresses/{id} | ✅ deleteAddress |
| 15 | setDefaultAddress | PUT | /user/addresses/{id}/default | ✅ setDefaultAddress |
| 16 | submitShopCertification | POST | /user/shop-certification | ✅ submitShopCertification |
| 17 | getShopCertificationStatus | GET | /user/shop-certification | ✅ getShopCertificationStatus |
| 18 | getFollowList | GET | /user/follows | ✅ getFollowList |
| 19 | addFollow | POST | /user/follows | ✅ addFollow |
| 20 | removeFollow | DELETE | /user/follows/{id} | ✅ removeFollow |
| 21 | getFavoriteList | GET | /user/favorites | ✅ getFavoriteList |
| 22 | addFavorite | POST | /user/favorites | ✅ addFavorite |
| 23 | removeFavorite | DELETE | /user/favorites/{id} | ✅ removeFavorite |
| 24 | addLike | POST | /user/likes | ✅ addLike |
| 25 | removeLike | DELETE | /user/likes/{id} | ✅ removeLike |
| 26 | getAdminUserList | GET | /user/admin/users | ✅ getAdminUserList |
| 27 | createAdmin | POST | /user/admin/users | ✅ createAdmin |
| 28 | updateUser | PUT | /user/admin/users/{userId} | ✅ updateUser |
| 29 | deleteUser | DELETE | /user/admin/users/{userId} | ✅ deleteUser |
| 30 | updateUserRole | PUT | /user/admin/users/{userId}/role | ✅ updateUserRole |
| 31 | toggleUserStatus | PUT | /user/admin/users/{userId}/status | ✅ toggleUserStatus |
| 32 | getCertificationList | GET | /user/admin/certifications | ✅ getCertificationList |
| 33 | processCertification | PUT | /user/admin/certifications/{id} | ✅ processCertification |
| 34 | getUserPreferences | GET | /user/preferences | ✅ getUserPreferences |
| 35 | updateUserPreferences | PUT | /user/preferences | ✅ updateUserPreferences |

## Controller 方法统计

### 当前Controller中的方法（35个）：
1. login - POST /login
2. register - POST /register
3. logout - POST /logout
4. refreshToken - POST /refresh
5. getCurrentUser - GET /me
6. getUserInfo - GET /{userId}
7. updateUserInfo - PUT /
8. uploadAvatar - POST /avatar
9. changePassword - PUT /password
10. resetPassword - POST /password/reset
11. getAddressList - GET /addresses
12. addAddress - POST /addresses
13. updateAddress - PUT /addresses/{id}
14. deleteAddress - DELETE /addresses/{id}
15. setDefaultAddress - PUT /addresses/{id}/default
16. getShopCertificationStatus - GET /shop-certification
17. submitShopCertification - POST /shop-certification
18. getFollowList - GET /follows
19. addFollow - POST /follows
20. removeFollow - DELETE /follows/{id}
21. getFavoriteList - GET /favorites
22. addFavorite - POST /favorites
23. removeFavorite - DELETE /favorites/{id}
24. addLike - POST /likes
25. removeLike - DELETE /likes/{id}
26. getAdminUserList - GET /admin/users
27. createAdmin - POST /admin/users
28. updateUser - PUT /admin/users/{userId}
29. deleteUser - DELETE /admin/users/{userId}
30. updateUserRole - PUT /admin/users/{userId}/role
31. toggleUserStatus - PUT /admin/users/{userId}/status
32. getCertificationList - GET /admin/certifications
33. processCertification - PUT /admin/certifications/{id}
34. getUserPreferences - GET /preferences
35. updateUserPreferences - PUT /preferences

## 结论

✅ **接口数量一致**：前端35个接口，Controller 35个方法，数量匹配。

✅ **路径完全对应**：所有接口路径都与前端API定义一致。

✅ **方法命名对应**：Controller方法名与前端函数名语义一致。

## 注意事项

1. **getUserInfo 的特殊处理**：
   - 前端：`getUserInfo(userId)` 如果 userId 为空，则调用 `/user/me`
   - Controller：`GET /user/{userId}` 和 `GET /user/me` 是两个独立的方法
   - 这是正确的设计，因为路径不同，需要两个方法

2. **所有接口都已实现**，没有缺失的接口。

