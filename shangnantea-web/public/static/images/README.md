# 前端静态图片资源目录

此目录存放前端的默认图片（兜底图片），当后端图片加载失败时使用。

## 目录结构

```
static/images/
├── avatars/
│   └── default.jpg      # 默认头像
├── teas/
│   └── default.jpg      # 默认茶叶图片
├── banners/
│   └── default.jpg      # 默认轮播图
└── posts/
    └── default.jpg      # 默认帖子图片
```

## 路径说明

- **后端图片路径**：`/images/xxx.jpg` → 代理到 `http://localhost:8080/images/xxx.jpg`
- **前端默认图片路径**：`/static/images/xxx.jpg` → 访问前端静态资源

## 使用方式

在 `SafeImage.vue` 组件中，当后端图片加载失败时，会自动使用此目录下的默认图片。

```vue
<SafeImage :src="userInfo.avatar" type="avatar" />
```

如果 `userInfo.avatar` 加载失败，会自动使用 `/static/images/avatars/default.jpg`。

## 注意事项

1. 默认图片必须放在前端，不能放在后端
2. 这样即使后端服务挂了，默认图片也能正常显示
3. 这是真正的"兜底"机制
