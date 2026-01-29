package com.shangnantea.service.impl;

import com.shangnantea.common.api.PageParam;
import com.shangnantea.common.api.PageResult;
import com.shangnantea.common.api.Result;
import com.shangnantea.mapper.ForumPostMapper;
import com.shangnantea.mapper.ForumReplyMapper;
import com.shangnantea.mapper.ForumTopicMapper;
import com.shangnantea.mapper.HomeContentMapper;
import com.shangnantea.mapper.TeaArticleMapper;
import com.shangnantea.mapper.UserFavoriteMapper;
import com.shangnantea.mapper.UserLikeMapper;
import com.shangnantea.mapper.UserMapper;
import com.shangnantea.model.dto.forum.CreatePostDTO;
import com.shangnantea.model.dto.forum.CreateTopicDTO;
import com.shangnantea.model.dto.forum.RejectPostDTO;
import com.shangnantea.model.dto.forum.UpdatePostDTO;
import com.shangnantea.model.dto.forum.UpdateTopicDTO;
import com.shangnantea.model.entity.forum.ForumPost;
import com.shangnantea.model.entity.forum.ForumReply;
import com.shangnantea.model.entity.forum.ForumTopic;
import com.shangnantea.model.entity.forum.HomeContent;
import com.shangnantea.model.entity.forum.TeaArticle;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.model.entity.user.UserFavorite;
import com.shangnantea.model.entity.user.UserLike;
import com.shangnantea.model.vo.forum.ArticleDetailVO;
import com.shangnantea.model.vo.forum.ArticleVO;
import com.shangnantea.model.vo.forum.ForumHomeVO;
import com.shangnantea.model.vo.forum.PostDetailVO;
import com.shangnantea.model.vo.forum.PostVO;
import com.shangnantea.model.vo.forum.TopicDetailVO;
import com.shangnantea.model.vo.forum.TopicVO;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.service.ForumService;
import com.shangnantea.utils.FileUploadUtils;
import com.shangnantea.utils.StatisticsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 论坛服务实现类
 */
@Service
public class ForumServiceImpl implements ForumService {

    private static final Logger logger = LoggerFactory.getLogger(ForumServiceImpl.class);

    @Autowired
    private ForumTopicMapper topicMapper;
    
    @Autowired
    private ForumPostMapper postMapper;
    
    @Autowired
    private ForumReplyMapper replyMapper;
    
    @Autowired
    private TeaArticleMapper articleMapper;
    
    @Autowired
    private HomeContentMapper homeContentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserLikeMapper userLikeMapper;
    
    @Autowired
    private UserFavoriteMapper userFavoriteMapper;
    
    @Autowired
    private StatisticsUtils statisticsUtils;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    @Override
    public Result<ForumHomeVO> getHomeData() {
        try {
            logger.info("获取论坛首页数据请求");
            
            ForumHomeVO homeVO = new ForumHomeVO();
            
            // 1. 获取Banner列表（优化：直接查询section='banner'且status=1的记录）
            List<HomeContent> bannerContents = homeContentMapper.selectBySection("banner", 1);
            List<ForumHomeVO.BannerVO> banners = bannerContents.stream()
                    .map(content -> {
                        ForumHomeVO.BannerVO bannerVO = new ForumHomeVO.BannerVO();
                        bannerVO.setId(content.getId());
                        // 生成图片访问URL
                        String imageUrl = content.getContent();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            imageUrl = FileUploadUtils.generateAccessUrl(imageUrl, baseUrl);
                        }
                        bannerVO.setImageUrl(imageUrl);
                        bannerVO.setTitle(content.getTitle());
                        bannerVO.setLinkUrl(content.getLinkUrl());
                        return bannerVO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            homeVO.setBanners(banners);
            
            // 2. 获取热门帖子（按浏览量降序，取前10条）
            List<ForumPost> allPosts = postMapper.selectAll();
            List<ForumHomeVO.HotPostVO> hotPosts = allPosts.stream()
                    .filter(post -> post.getStatus() != null && post.getStatus() == 1)
                    .sorted((a, b) -> {
                        Integer viewA = a.getViewCount() != null ? a.getViewCount() : 0;
                        Integer viewB = b.getViewCount() != null ? b.getViewCount() : 0;
                        return viewB.compareTo(viewA); // 降序
                    })
                    .limit(10)
                    .map(post -> {
                        ForumHomeVO.HotPostVO hotPostVO = new ForumHomeVO.HotPostVO();
                        hotPostVO.setId(post.getId());
                        hotPostVO.setTitle(post.getTitle());
                        hotPostVO.setViewCount(post.getViewCount() != null ? post.getViewCount() : 0);
                        return hotPostVO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            homeVO.setHotPosts(hotPosts);
            
            // 3. 获取最新文章（按创建时间降序，取前10条）
            List<TeaArticle> allArticles = articleMapper.selectAll();
            List<ForumHomeVO.LatestArticleVO> latestArticles = allArticles.stream()
                    .filter(article -> article.getStatus() != null && article.getStatus() == 1)
                    .sorted((a, b) -> {
                        if (a.getCreateTime() == null && b.getCreateTime() == null) {
                            return 0;
                        }
                        if (a.getCreateTime() == null) {
                            return 1;
                        }
                        if (b.getCreateTime() == null) {
                            return -1;
                        }
                        return b.getCreateTime().compareTo(a.getCreateTime()); // 降序
                    })
                    .limit(10)
                    .map(article -> {
                        ForumHomeVO.LatestArticleVO articleVO = new ForumHomeVO.LatestArticleVO();
                        articleVO.setId(article.getId());
                        articleVO.setTitle(article.getTitle());
                        articleVO.setSummary(article.getSummary());
                        return articleVO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            homeVO.setLatestArticles(latestArticles);
            
            logger.info("获取论坛首页数据成功");
            return Result.success(200, homeVO); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取论坛首页数据失败: 系统异常", e);
            return Result.failure(6100); // 失败码6100
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateHomeData(Map<String, Object> data) {
        try {
            logger.info("更新论坛首页数据请求");
            
            // 1. 解析请求数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> banners = (List<Map<String, Object>>) data.get("banners");
            @SuppressWarnings("unchecked")
            List<Long> hotPostIds = (List<Long>) data.get("hotPostIds");
            @SuppressWarnings("unchecked")
            List<Long> latestArticleIds = (List<Long>) data.get("latestArticleIds");
            
            // 2. 更新Banner配置（如果提供）
            if (banners != null && !banners.isEmpty()) {
                for (Map<String, Object> bannerData : banners) {
                    Integer id = bannerData.get("id") != null ? 
                            Integer.parseInt(bannerData.get("id").toString()) : null;
                    String imageUrl = (String) bannerData.get("imageUrl");
                    String title = (String) bannerData.get("title");
                    String linkUrl = (String) bannerData.get("linkUrl");
                    Integer sortOrder = bannerData.get("sortOrder") != null ? 
                            Integer.parseInt(bannerData.get("sortOrder").toString()) : 0;
                    
                    if (id != null) {
                        // 更新现有Banner
                        HomeContent content = homeContentMapper.selectById(id);
                        if (content != null && "banner".equals(content.getSection())) {
                            content.setContent(imageUrl);
                            content.setTitle(title);
                            content.setLinkUrl(linkUrl);
                            content.setSortOrder(sortOrder);
                            content.setUpdateTime(new Date());
                            homeContentMapper.updateById(content);
                        }
                    }
                }
            }
            
            // 3. 热门帖子和最新文章由算法实时计算，不需要持久化存储
            // 前端调用 getHomeData 接口时会自动获取实时推荐数据
            if (hotPostIds != null && !hotPostIds.isEmpty()) {
                logger.info("收到热门帖子ID列表（仅供参考）: {}", hotPostIds);
            }
            
            if (latestArticleIds != null && !latestArticleIds.isEmpty()) {
                logger.info("收到最新文章ID列表（仅供参考）: {}", latestArticleIds);
            }
            
            logger.info("更新论坛首页数据成功");
            return Result.success(6000, null); // 成功码6000：区块内容已保存
            
        } catch (Exception e) {
            logger.error("更新论坛首页数据失败: 系统异常", e);
            return Result.failure(6101); // 失败码6101：保存失败
        }
    }
    
    @Override
    public Result<List<ForumHomeVO.BannerVO>> getBanners() {
        try {
            logger.info("获取Banner列表请求");
            
            // 优化：直接查询section='banner'且status=1的记录
            List<HomeContent> bannerContents = homeContentMapper.selectBySection("banner", 1);
            List<ForumHomeVO.BannerVO> banners = bannerContents.stream()
                    .map(content -> {
                        ForumHomeVO.BannerVO bannerVO = new ForumHomeVO.BannerVO();
                        bannerVO.setId(content.getId());
                        // 生成图片访问URL
                        String imageUrl = content.getContent();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            imageUrl = FileUploadUtils.generateAccessUrl(imageUrl, baseUrl);
                        }
                        bannerVO.setImageUrl(imageUrl);
                        bannerVO.setTitle(content.getTitle());
                        bannerVO.setLinkUrl(content.getLinkUrl());
                        bannerVO.setSortOrder(content.getSortOrder());
                        return bannerVO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            logger.info("获取Banner列表成功，共{}条", banners.size());
            return Result.success(200, banners); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取Banner列表失败: 系统异常", e);
            return Result.failure(6102); // 失败码6102
        }
    }
    
    @Override
    public Result<Map<String, Object>> uploadPostImage(MultipartFile image) {
        try {
            logger.info("上传帖子图片请求, 文件名: {}", image.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"posts"）
            String relativePath = FileUploadUtils.uploadImage(image, "posts");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 直接返回，不存数据库（场景2：先返回URL，稍后存储）
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            
            logger.info("帖子图片上传成功: path: {}", relativePath);
            return Result.success(6028, responseData); // 帖子图片上传成功
            
        } catch (Exception e) {
            logger.error("帖子图片上传失败: 系统异常", e);
            return Result.failure(6140); // 帖子图片上传失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> uploadBanner(MultipartFile file, String title, String subtitle, String linkUrl) {
        try {
            logger.info("上传论坛轮播图请求, title: {}, 文件名: {}", title, file.getOriginalFilename());
            
            // 1. 调用工具类上传（硬编码type为"forum-banners"）
            String relativePath = FileUploadUtils.uploadImage(file, "forum-banners");
            
            // 2. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(relativePath, baseUrl);
            
            // 3. 创建首页内容记录（存储到home_contents表）
            HomeContent content = new HomeContent();
            content.setSection("banner"); // 轮播图板块
            content.setTitle(title);
            content.setSubTitle(subtitle);
            content.setContent(relativePath); // 存储相对路径
            content.setLinkUrl(linkUrl);
            content.setType("image");
            content.setStatus(1); // 启用状态
            content.setCreateTime(new Date());
            content.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            int result = homeContentMapper.insert(content);
            if (result <= 0) {
                logger.error("论坛轮播图上传失败: 数据库插入失败");
                return Result.failure(6104); // Banner上传失败
            }
            
            // 5. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", content.getId());
            responseData.put("url", accessUrl);
            responseData.put("path", relativePath);
            responseData.put("title", title);
            responseData.put("subtitle", subtitle);
            responseData.put("linkUrl", linkUrl);
            
            logger.info("论坛轮播图上传成功: id: {}, path: {}", content.getId(), relativePath);
            return Result.success(6001, responseData); // Banner上传成功
            
        } catch (Exception e) {
            logger.error("论坛轮播图上传失败: 系统异常", e);
            return Result.failure(6103); // Banner上传失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateBanner(String id, MultipartFile file, String title, String linkUrl, Integer sortOrder) {
        try {
            logger.info("更新Banner请求: id={}, hasFile={}", id, file != null && !file.isEmpty());
            
            // 1. 验证Banner是否存在
            Integer bannerId = Integer.parseInt(id);
            HomeContent content = homeContentMapper.selectById(bannerId);
            if (content == null || !"banner".equals(content.getSection())) {
                logger.warn("更新Banner失败: Banner不存在, id: {}", id);
                return Result.failure(6106); // 保存失败
            }
            
            // 2. 如果提供了新图片文件，则替换图片
            if (file != null && !file.isEmpty()) {
                // 2.1 保存旧图片路径（用于删除）
                String oldImagePath = content.getContent();
                
                // 2.2 上传新图片
                String newRelativePath = FileUploadUtils.uploadImage(file, "forum-banners");
                
                // 2.3 删除旧图片文件
                if (oldImagePath != null && !oldImagePath.isEmpty()) {
                    boolean deleted = FileUploadUtils.deleteFile(oldImagePath);
                    if (deleted) {
                        logger.info("旧Banner图片删除成功: path={}", oldImagePath);
                    } else {
                        logger.warn("旧Banner图片删除失败或文件不存在: path={}", oldImagePath);
                    }
                }
                
                // 2.4 更新图片路径
                content.setContent(newRelativePath);
                logger.info("Banner图片已更新: oldPath={}, newPath={}", oldImagePath, newRelativePath);
            }
            
            // 3. 更新其他Banner信息
            if (title != null && !title.trim().isEmpty()) {
                content.setTitle(title);
            }
            if (linkUrl != null) {
                content.setLinkUrl(linkUrl);
            }
            if (sortOrder != null) {
                content.setSortOrder(sortOrder);
            }
            content.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            int result = homeContentMapper.updateById(content);
            if (result <= 0) {
                logger.error("更新Banner失败: 数据库更新失败, id: {}", id);
                return Result.failure(6106); // 保存失败
            }
            
            // 5. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", content.getId());
            responseData.put("title", content.getTitle());
            responseData.put("linkUrl", content.getLinkUrl());
            responseData.put("sortOrder", content.getSortOrder());
            // 生成图片访问URL
            String imageUrl = content.getContent();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageUrl = FileUploadUtils.generateAccessUrl(imageUrl, baseUrl);
            }
            responseData.put("imageUrl", imageUrl);
            
            logger.info("更新Banner成功: id={}", id);
            return Result.success(6002, responseData); // Banner更新成功
            
        } catch (NumberFormatException e) {
            logger.error("更新Banner失败: ID格式错误, id: {}", id, e);
            return Result.failure(6106); // 保存失败
        } catch (Exception e) {
            logger.error("更新Banner失败: 系统异常, id: {}", id, e);
            return Result.failure(6106); // 保存失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteBanner(String id) {
        try {
            logger.info("删除Banner请求: id={}", id);
            
            // 1. 验证Banner是否存在
            Integer bannerId = Integer.parseInt(id);
            HomeContent content = homeContentMapper.selectById(bannerId);
            if (content == null || !"banner".equals(content.getSection())) {
                logger.warn("删除Banner失败: Banner不存在, id: {}", id);
                return Result.failure(6107); // 删除失败
            }
            
            // 2. 删除关联的图片文件
            String relativePath = content.getContent();
            if (relativePath != null && !relativePath.isEmpty()) {
                boolean fileDeleted = FileUploadUtils.deleteFile(relativePath);
                if (fileDeleted) {
                    logger.info("Banner图片文件删除成功: path={}", relativePath);
                } else {
                    logger.warn("Banner图片文件删除失败或文件不存在: path={}", relativePath);
                }
            }
            
            // 3. 删除数据库记录
            int result = homeContentMapper.deleteById(bannerId);
            if (result <= 0) {
                logger.error("删除Banner失败: 数据库删除失败, id: {}", id);
                return Result.failure(6107); // 删除失败
            }
            
            logger.info("删除Banner成功: id={}", id);
            return Result.success(6003, true); // 删除成功
            
        } catch (NumberFormatException e) {
            logger.error("删除Banner失败: ID格式错误, id: {}", id, e);
            return Result.failure(6107); // 删除失败
        } catch (Exception e) {
            logger.error("删除Banner失败: 系统异常, id: {}", id, e);
            return Result.failure(6107); // 删除失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateBannerOrder(Map<String, Object> data) {
        try {
            logger.info("更新Banner顺序请求");
            
            // 1. 解析bannerIds数组
            @SuppressWarnings("unchecked")
            List<Integer> bannerIds = (List<Integer>) data.get("bannerIds");
            
            if (bannerIds == null || bannerIds.isEmpty()) {
                logger.warn("更新Banner顺序失败: bannerIds为空");
                return Result.failure(6108); // 排序更新失败
            }
            
            // 2. 批量查询Banner（优化：一次查询所有）
            List<HomeContent> contents = homeContentMapper.selectByIds(bannerIds);
            
            // 3. 验证所有Banner是否存在且类型正确
            if (contents.size() != bannerIds.size()) {
                logger.warn("更新Banner顺序失败: 部分Banner不存在");
                return Result.failure(6108, "部分Banner不存在"); // 排序更新失败
            }
            
            for (HomeContent content : contents) {
                if (!"banner".equals(content.getSection())) {
                    logger.warn("更新Banner顺序失败: ID {} 不是Banner记录", content.getId());
                    return Result.failure(6108, "ID " + content.getId() + " 不是Banner"); // 排序更新失败
                }
            }
            
            // 4. 更新sortOrder（按bannerIds数组顺序）
            Map<Integer, HomeContent> contentMap = contents.stream()
                    .collect(java.util.stream.Collectors.toMap(HomeContent::getId, c -> c));
            
            List<HomeContent> toUpdate = new ArrayList<>();
            for (int i = 0; i < bannerIds.size(); i++) {
                Integer bannerId = bannerIds.get(i);
                HomeContent content = contentMap.get(bannerId);
                if (content != null) {
                    content.setSortOrder(i + 1); // 从1开始排序
                    content.setUpdateTime(new Date());
                    toUpdate.add(content);
                }
            }
            
            // 5. 批量更新（优化：一次更新所有）
            if (!toUpdate.isEmpty()) {
                homeContentMapper.batchUpdate(toUpdate);
            }
            
            logger.info("更新Banner顺序成功，共更新{}个Banner", toUpdate.size());
            return Result.success(6004, null); // 排序更新成功
            
        } catch (Exception e) {
            logger.error("更新Banner顺序失败: 系统异常", e);
            return Result.failure(6108); // 排序更新失败
        }
    }
    
    @Override
    public Result<Object> getArticles(Map<String, Object> params) {
        try {
            logger.info("获取文章列表请求: {}", params);
            
            // 1. 解析查询参数
            String categoryId = (String) params.get("categoryId");
            String keyword = (String) params.get("keyword");
            Integer page = params.get("page") != null ? 
                    Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                    Integer.parseInt(params.get("pageSize").toString()) : 10;
            
            // 2. 使用优化的查询方法（数据库层面过滤和排序）
            List<TeaArticle> allArticles = articleMapper.selectPublishedArticles(categoryId, keyword, 1);
            
            // 3. 内存分页处理
            int total = allArticles.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<ArticleVO> articleVOList = new ArrayList<>();
            if (startIndex < total) {
                articleVOList = allArticles.subList(startIndex, endIndex).stream()
                        .map(article -> {
                            ArticleVO vo = new ArticleVO();
                            vo.setId(article.getId());
                            vo.setTitle(article.getTitle());
                            vo.setSummary(article.getSummary());
                            // 生成封面图片访问URL
                            String coverImage = article.getCoverImage();
                            if (coverImage != null && !coverImage.isEmpty()) {
                                coverImage = FileUploadUtils.generateAccessUrl(coverImage, baseUrl);
                            }
                            vo.setCoverImage(coverImage);
                            vo.setAuthorName(article.getAuthor());
                            vo.setCategory(article.getCategory());
                            vo.setViewCount(article.getViewCount() != null ? article.getViewCount() : 0);
                            // 使用动态计算获取点赞数
                            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
                            vo.setIsTop(article.getIsTop());
                            vo.setIsRecommend(article.getIsRecommend());
                            vo.setPublishTime(article.getPublishTime());
                            vo.setCreateTime(article.getCreateTime());
                            return vo;
                        })
                        .collect(Collectors.toList());
            }
            
            // 4. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", articleVOList);
            responseData.put("total", total);
            
            logger.info("获取文章列表成功，共{}条，返回{}条", total, articleVOList.size());
            return Result.success(200, responseData); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取文章列表失败: 系统异常", e);
            return Result.failure(6109); // 获取文章列表失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> getArticleDetail(String id) {
        try {
            logger.info("获取文章详情请求: id={}", id);
            
            // 1. 查询文章
            Long articleId = Long.parseLong(id);
            TeaArticle article = articleMapper.selectById(articleId);
            
            if (article == null || article.getStatus() == null || article.getStatus() != 1) {
                logger.warn("获取文章详情失败: 文章不存在或未发布, id: {}", id);
                return Result.failure(6110); // 获取文章详情失败
            }
            
            // 2. 增加阅读量
            article.setViewCount((article.getViewCount() != null ? article.getViewCount() : 0) + 1);
            article.setUpdateTime(new Date());
            articleMapper.updateById(article);
            
            // 3. 构造返回VO
            ArticleDetailVO vo = new ArticleDetailVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSubtitle(article.getSubtitle());
            vo.setContent(article.getContent());
            vo.setSummary(article.getSummary());
            // 生成封面图片访问URL
            String coverImage = article.getCoverImage();
            if (coverImage != null && !coverImage.isEmpty()) {
                coverImage = FileUploadUtils.generateAccessUrl(coverImage, baseUrl);
            }
            vo.setCoverImage(coverImage);
            vo.setAuthor(article.getAuthor());
            vo.setCategory(article.getCategory());
            vo.setTags(article.getTags());
            vo.setSource(article.getSource());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数和收藏数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            
            logger.info("获取文章详情成功: id={}", id);
            return Result.success(200, vo); // 成功码200
            
        } catch (NumberFormatException e) {
            logger.error("获取文章详情失败: ID格式错误, id: {}", id, e);
            return Result.failure(6110); // 获取文章详情失败
        } catch (Exception e) {
            logger.error("获取文章详情失败: 系统异常, id: {}", id, e);
            return Result.failure(6110); // 获取文章详情失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createArticle(Map<String, Object> data) {
        try {
            logger.info("创建文章请求: {}", data);
            
            // 1. 解析请求数据
            String title = (String) data.get("title");
            String subtitle = (String) data.get("subtitle");
            String content = (String) data.get("content");
            String summary = (String) data.get("summary");
            String coverImage = (String) data.get("coverImage");
            String category = (String) data.get("category");
            String tags = (String) data.get("tags");
            String source = (String) data.get("source");
            
            // 2. 参数验证
            if (title == null || title.trim().isEmpty()) {
                logger.warn("创建文章失败: 标题不能为空");
                return Result.failure(6111, "标题不能为空");
            }
            if (title.length() > 100) {
                logger.warn("创建文章失败: 标题长度超过限制");
                return Result.failure(6111, "标题长度不能超过100个字符");
            }
            if (content == null || content.trim().isEmpty()) {
                logger.warn("创建文章失败: 内容不能为空");
                return Result.failure(6111, "内容不能为空");
            }
            if (subtitle != null && subtitle.length() > 200) {
                logger.warn("创建文章失败: 副标题长度超过限制");
                return Result.failure(6111, "副标题长度不能超过200个字符");
            }
            if (summary != null && summary.length() > 500) {
                logger.warn("创建文章失败: 摘要长度超过限制");
                return Result.failure(6111, "摘要长度不能超过500个字符");
            }
            
            // 3. 创建文章实体
            TeaArticle article = new TeaArticle();
            article.setTitle(title.trim());
            article.setSubtitle(subtitle);
            article.setContent(content);
            article.setSummary(summary);
            article.setCoverImage(coverImage);
            article.setCategory(category);
            article.setTags(tags);
            article.setSource(source);
            
            // 获取当前用户作为作者
            String currentUserId = UserContext.getCurrentUserId();
            String authorName = "管理员"; // 默认值
            if (currentUserId != null) {
                User author = userMapper.selectById(currentUserId);
                if (author != null && author.getUsername() != null) {
                    authorName = author.getUsername();
                }
            }
            article.setAuthor(authorName);
            
            article.setViewCount(0);
            // likeCount和favoriteCount已从数据库删除，使用动态计算
            article.setIsTop(0);
            article.setIsRecommend(0);
            article.setStatus(1); // 1=已发布
            article.setPublishTime(new Date());
            article.setCreateTime(new Date());
            article.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            articleMapper.insert(article);
            
            // 5. 构造返回VO
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCoverImage(article.getCoverImage());
            vo.setAuthorName(article.getAuthor());
            vo.setCategory(article.getCategory());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            
            logger.info("创建文章成功: id={}", article.getId());
            return Result.success(6005, vo); // 文章创建成功
            
        } catch (Exception e) {
            logger.error("创建文章失败: 系统异常", e);
            return Result.failure(6111); // 文章创建失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateArticle(String id, Map<String, Object> data) {
        try {
            logger.info("更新文章请求: id={}, data={}", id, data);
            
            // 1. 查询文章是否存在
            Long articleId = Long.parseLong(id);
            TeaArticle article = articleMapper.selectById(articleId);
            
            if (article == null) {
                logger.warn("更新文章失败: 文章不存在, id: {}", id);
                return Result.failure(6112); // 文章更新失败
            }
            
            // 2. 参数验证并更新文章信息
            if (data.containsKey("title")) {
                String title = (String) data.get("title");
                if (title == null || title.trim().isEmpty()) {
                    logger.warn("更新文章失败: 标题不能为空");
                    return Result.failure(6112, "标题不能为空");
                }
                if (title.length() > 100) {
                    logger.warn("更新文章失败: 标题长度超过限制");
                    return Result.failure(6112, "标题长度不能超过100个字符");
                }
                article.setTitle(title.trim());
            }
            if (data.containsKey("subtitle")) {
                String subtitle = (String) data.get("subtitle");
                if (subtitle != null && subtitle.length() > 200) {
                    logger.warn("更新文章失败: 副标题长度超过限制");
                    return Result.failure(6112, "副标题长度不能超过200个字符");
                }
                article.setSubtitle(subtitle);
            }
            if (data.containsKey("content")) {
                String content = (String) data.get("content");
                if (content == null || content.trim().isEmpty()) {
                    logger.warn("更新文章失败: 内容不能为空");
                    return Result.failure(6112, "内容不能为空");
                }
                article.setContent(content);
            }
            if (data.containsKey("summary")) {
                String summary = (String) data.get("summary");
                if (summary != null && summary.length() > 500) {
                    logger.warn("更新文章失败: 摘要长度超过限制");
                    return Result.failure(6112, "摘要长度不能超过500个字符");
                }
                article.setSummary(summary);
            }
            if (data.containsKey("coverImage")) {
                article.setCoverImage((String) data.get("coverImage"));
            }
            if (data.containsKey("category")) {
                article.setCategory((String) data.get("category"));
            }
            if (data.containsKey("tags")) {
                article.setTags((String) data.get("tags"));
            }
            if (data.containsKey("source")) {
                article.setSource((String) data.get("source"));
            }
            article.setUpdateTime(new Date());
            
            // 3. 保存到数据库
            int result = articleMapper.updateById(article);
            if (result <= 0) {
                logger.error("更新文章失败: 数据库更新失败, id: {}", id);
                return Result.failure(6112); // 文章更新失败
            }
            
            // 4. 构造返回VO
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCoverImage(article.getCoverImage());
            vo.setAuthorName(article.getAuthor());
            vo.setCategory(article.getCategory());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            
            logger.info("更新文章成功: id={}", id);
            return Result.success(6006, vo); // 文章更新成功
            
        } catch (NumberFormatException e) {
            logger.error("更新文章失败: ID格式错误, id: {}", id, e);
            return Result.failure(6112); // 文章更新失败
        } catch (Exception e) {
            logger.error("更新文章失败: 系统异常, id: {}", id, e);
            return Result.failure(6112); // 文章更新失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteArticle(String id) {
        try {
            logger.info("删除文章请求: id={}", id);
            
            // 1. 查询文章是否存在
            Long articleId = Long.parseLong(id);
            TeaArticle article = articleMapper.selectById(articleId);
            
            if (article == null) {
                logger.warn("删除文章失败: 文章不存在, id: {}", id);
                return Result.failure(6113); // 文章删除失败
            }
            
            // 2. 软删除：更新状态为已删除
            article.setStatus(2); // 2=已删除
            article.setUpdateTime(new Date());
            int result = articleMapper.updateById(article);
            
            if (result <= 0) {
                logger.error("删除文章失败: 数据库更新失败, id: {}", id);
                return Result.failure(6113); // 文章删除失败
            }
            
            logger.info("删除文章成功: id={}", id);
            return Result.success(6007, true); // 文章已删除
            
        } catch (NumberFormatException e) {
            logger.error("删除文章失败: ID格式错误, id: {}", id, e);
            return Result.failure(6113); // 文章删除失败
        } catch (Exception e) {
            logger.error("删除文章失败: 系统异常, id: {}", id, e);
            return Result.failure(6113); // 文章删除失败
        }
    }
    
    @Override
    public Result<Object> getForumTopics() {
        try {
            logger.info("获取版块列表请求");
            
            // 1. 使用优化的查询方法（数据库层面过滤和排序）
            List<ForumTopic> topics = topicMapper.selectByStatus(1);
            List<TopicVO> topicVOList = topics.stream()
                    .map(topic -> {
                        TopicVO vo = new TopicVO();
                        vo.setId(topic.getId());
                        vo.setName(topic.getName());
                        vo.setDescription(topic.getDescription());
                        // 生成图标访问URL
                        String icon = topic.getIcon();
                        if (icon != null && !icon.isEmpty()) {
                            icon = FileUploadUtils.generateAccessUrl(icon, baseUrl);
                        }
                        vo.setIcon(icon);
                        // 生成封面访问URL
                        String cover = topic.getCover();
                        if (cover != null && !cover.isEmpty()) {
                            cover = FileUploadUtils.generateAccessUrl(cover, baseUrl);
                        }
                        vo.setCover(cover);
                        vo.setSortOrder(topic.getSortOrder());
                        vo.setPostCount(topic.getPostCount() != null ? topic.getPostCount() : 0);
                        vo.setCreateTime(topic.getCreateTime());
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            logger.info("获取版块列表成功，共{}个版块", topicVOList.size());
            return Result.success(200, topicVOList); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取版块列表失败: 系统异常", e);
            return Result.failure(6114); // 获取版块列表失败
        }
    }
    
    @Override
    public Result<Object> getTopicDetail(String id) {
        try {
            logger.info("获取版块详情请求: id={}", id);
            
            // 1. 查询版块
            Integer topicId = Integer.parseInt(id);
            ForumTopic topic = topicMapper.selectById(topicId);
            
            if (topic == null) {
                logger.warn("获取版块详情失败: 版块不存在, id: {}", id);
                return Result.failure(6115); // 获取版块详情失败
            }
            
            // 2. 构造返回VO
            TopicDetailVO vo = new TopicDetailVO();
            vo.setId(topic.getId());
            vo.setName(topic.getName());
            vo.setDescription(topic.getDescription());
            // 生成图标访问URL
            String icon = topic.getIcon();
            if (icon != null && !icon.isEmpty()) {
                icon = FileUploadUtils.generateAccessUrl(icon, baseUrl);
            }
            vo.setIcon(icon);
            // 生成封面访问URL
            String cover = topic.getCover();
            if (cover != null && !cover.isEmpty()) {
                cover = FileUploadUtils.generateAccessUrl(cover, baseUrl);
            }
            vo.setCover(cover);
            vo.setUserId(topic.getUserId());
            
            // 查询版主名称
            String moderatorName = "未设置版主";
            if (topic.getUserId() != null) {
                User moderator = userMapper.selectById(topic.getUserId());
                if (moderator != null && moderator.getUsername() != null) {
                    moderatorName = moderator.getUsername();
                }
            }
            vo.setModeratorName(moderatorName);
            
            vo.setSortOrder(topic.getSortOrder());
            vo.setPostCount(topic.getPostCount() != null ? topic.getPostCount() : 0);
            
            // 统计今日帖子数
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
            Date startTime = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            Date endTime = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
            int todayPostCount = postMapper.countTodayPosts(topicId, startTime, endTime, 1);
            vo.setTodayPostCount(todayPostCount);
            
            vo.setStatus(topic.getStatus());
            vo.setCreateTime(topic.getCreateTime());
            vo.setUpdateTime(topic.getUpdateTime());
            
            logger.info("获取版块详情成功: id={}", id);
            return Result.success(200, vo); // 成功码200
            
        } catch (NumberFormatException e) {
            logger.error("获取版块详情失败: ID格式错误, id: {}", id, e);
            return Result.failure(6115); // 获取版块详情失败
        } catch (Exception e) {
            logger.error("获取版块详情失败: 系统异常, id: {}", id, e);
            return Result.failure(6115); // 获取版块详情失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createTopic(CreateTopicDTO dto) {
        try {
            logger.info("创建版块请求: {}", dto);
            
            // 1. 使用优化的查询方法检查名称是否重复
            ForumTopic existingTopic = topicMapper.selectByName(dto.getName());
            if (existingTopic != null) {
                logger.warn("创建版块失败: 版块名称已存在, name: {}", dto.getName());
                return Result.failure(6116, "版块名称已存在");
            }
            
            // 2. 创建版块实体
            ForumTopic topic = new ForumTopic();
            topic.setName(dto.getName());
            topic.setDescription(dto.getDescription());
            topic.setIcon(dto.getIcon());
            topic.setCover(dto.getCover());
            topic.setUserId(dto.getUserId()); // 设置版主ID
            topic.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
            topic.setPostCount(0); // 初始帖子数为0
            topic.setStatus(1); // 1=启用
            topic.setCreateTime(new Date());
            topic.setUpdateTime(new Date());
            
            // 3. 保存到数据库
            int result = topicMapper.insert(topic);
            if (result <= 0) {
                logger.error("创建版块失败: 数据库插入失败");
                return Result.failure(6116); // 添加版块失败
            }
            
            logger.info("创建版块成功: id={}, name={}", topic.getId(), dto.getName());
            return Result.success(6008, null); // 添加版块成功
            
        } catch (Exception e) {
            logger.error("创建版块失败: 系统异常", e);
            return Result.failure(6116); // 添加版块失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateTopic(String id, UpdateTopicDTO dto) {
        try {
            logger.info("更新版块请求: id={}, dto={}", id, dto);
            
            // 1. 查询版块是否存在
            Integer topicId = Integer.parseInt(id);
            ForumTopic topic = topicMapper.selectById(topicId);
            
            if (topic == null) {
                logger.warn("更新版块失败: 版块不存在, id: {}", id);
                return Result.failure(6117); // 更新版块失败
            }
            
            // 2. 如果要更新名称，使用优化的查询方法验证名称是否与其他版块重复
            if (dto.getName() != null && !dto.getName().isEmpty()) {
                ForumTopic existingTopic = topicMapper.selectByNameExcludeId(dto.getName(), topicId);
                if (existingTopic != null) {
                    logger.warn("更新版块失败: 版块名称已存在, name: {}", dto.getName());
                    return Result.failure(6117, "版块名称已存在");
                }
                topic.setName(dto.getName());
            }
            
            // 3. 更新版块信息
            if (dto.getDescription() != null) {
                topic.setDescription(dto.getDescription());
            }
            if (dto.getIcon() != null) {
                topic.setIcon(dto.getIcon());
            }
            if (dto.getCover() != null) {
                topic.setCover(dto.getCover());
            }
            if (dto.getUserId() != null) {
                topic.setUserId(dto.getUserId());
            }
            if (dto.getSortOrder() != null) {
                topic.setSortOrder(dto.getSortOrder());
            }
            if (dto.getStatus() != null) {
                topic.setStatus(dto.getStatus());
            }
            topic.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            int result = topicMapper.updateById(topic);
            if (result <= 0) {
                logger.error("更新版块失败: 数据库更新失败, id: {}", id);
                return Result.failure(6117); // 更新版块失败
            }
            
            logger.info("更新版块成功: id={}", id);
            return Result.success(6009, null); // 更新版块成功
            
        } catch (NumberFormatException e) {
            logger.error("更新版块失败: ID格式错误, id: {}", id, e);
            return Result.failure(6117); // 更新版块失败
        } catch (Exception e) {
            logger.error("更新版块失败: 系统异常, id: {}", id, e);
            return Result.failure(6117); // 更新版块失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteTopic(String id) {
        try {
            logger.info("删除版块请求: id={}", id);
            
            // 1. 查询版块是否存在
            Integer topicId = Integer.parseInt(id);
            ForumTopic topic = topicMapper.selectById(topicId);
            
            if (topic == null) {
                logger.warn("删除版块失败: 版块不存在, id: {}", id);
                return Result.failure(6118); // 删除版块失败
            }
            
            // 2. 检查版块下是否还有帖子
            if (topic.getPostCount() != null && topic.getPostCount() > 0) {
                logger.warn("删除版块失败: 版块下还有{}个帖子, id: {}", topic.getPostCount(), id);
                return Result.failure(6118); // 删除版块失败（版块下还有帖子）
            }
            
            // 3. 删除版块
            int result = topicMapper.deleteById(topicId);
            if (result <= 0) {
                logger.error("删除版块失败: 数据库删除失败, id: {}", id);
                return Result.failure(6118); // 删除版块失败
            }
            
            logger.info("删除版块成功: id={}", id);
            return Result.success(6010, true); // 删除版块成功
            
        } catch (NumberFormatException e) {
            logger.error("删除版块失败: ID格式错误, id: {}", id, e);
            return Result.failure(6118); // 删除版块失败
        } catch (Exception e) {
            logger.error("删除版块失败: 系统异常, id: {}", id, e);
            return Result.failure(6118); // 删除版块失败
        }
    }
    
    @Override
    public Result<Object> getForumPosts(Map<String, Object> params) {
        try {
            logger.info("获取帖子列表请求: {}", params);
            
            // 1. 解析查询参数
            Integer topicId = params.get("topicId") != null ? 
                    Integer.parseInt(params.get("topicId").toString()) : null;
            String keyword = (String) params.get("keyword");
            Integer page = params.get("page") != null ? 
                    Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                    Integer.parseInt(params.get("pageSize").toString()) : 10;
            
            // 2. 使用优化的查询方法（数据库层面过滤和排序）
            List<ForumPost> allPosts = postMapper.selectPublishedPosts(topicId, keyword, 1);
            
            // 3. 批量查询用户和版块信息（优化N+1查询）
            List<String> userIds = allPosts.stream()
                    .map(ForumPost::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Integer> topicIds = allPosts.stream()
                    .map(ForumPost::getTopicId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<String, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectByIds(userIds);
                userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            }
            
            Map<Integer, ForumTopic> topicMap = new HashMap<>();
            if (!topicIds.isEmpty()) {
                List<ForumTopic> topics = topicMapper.selectByIds(topicIds);
                topicMap = topics.stream().collect(Collectors.toMap(ForumTopic::getId, t -> t));
            }
            
            // 4. 内存分页
            int total = allPosts.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<PostVO> postVOList = new ArrayList<>();
            if (startIndex < total) {
                final Map<String, User> finalUserMap = userMap;
                final Map<Integer, ForumTopic> finalTopicMap = topicMap;
                
                postVOList = allPosts.subList(startIndex, endIndex).stream()
                        .map(post -> {
                            PostVO vo = new PostVO();
                            vo.setId(post.getId());
                            vo.setUserId(post.getUserId());
                            // 从Map获取用户信息
                            User user = finalUserMap.get(post.getUserId());
                            vo.setUserName(user != null ? user.getUsername() : "未知用户");
                            vo.setUserAvatar(user != null ? user.getAvatar() : null);
                            vo.setTopicId(post.getTopicId());
                            // 从Map获取版块名称
                            ForumTopic topic = finalTopicMap.get(post.getTopicId());
                            vo.setTopicName(topic != null ? topic.getName() : "");
                            vo.setTitle(post.getTitle());
                            vo.setSummary(post.getSummary());
                            vo.setCoverImage(post.getCoverImage());
                            vo.setViewCount(post.getViewCount());
                            vo.setReplyCount(post.getReplyCount());
                            // 使用动态计算获取点赞数和收藏数
                            vo.setLikeCount(statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
                            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("post", String.valueOf(post.getId())));
                            vo.setIsSticky(post.getIsSticky());
                            vo.setIsEssence(post.getIsEssence());
                            vo.setStatus(post.getStatus());
                            vo.setLastReplyTime(post.getLastReplyTime());
                            vo.setCreateTime(post.getCreateTime());
                            return vo;
                        })
                        .collect(Collectors.toList());
            }
            
            // 5. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", postVOList);
            responseData.put("total", total);
            
            logger.info("获取帖子列表成功，共{}条，返回{}条", total, postVOList.size());
            return Result.success(200, responseData); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取帖子列表失败: 系统异常", e);
            return Result.failure(6119); // 获取帖子列表失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createPost(CreatePostDTO dto) {
        try {
            logger.info("创建帖子请求: {}", dto);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建帖子失败: 用户未登录");
                return Result.failure(6120); // 帖子发布失败
            }
            
            // 2. 验证版块是否存在
            ForumTopic topic = topicMapper.selectById(dto.getTopicId());
            if (topic == null) {
                logger.warn("创建帖子失败: 版块不存在, topicId: {}", dto.getTopicId());
                return Result.failure(6120); // 帖子发布失败
            }
            
            // 3. 创建帖子实体
            ForumPost post = new ForumPost();
            post.setUserId(userId);
            post.setTopicId(dto.getTopicId());
            post.setTitle(dto.getTitle());
            post.setContent(dto.getContent());
            post.setSummary(dto.getSummary());
            post.setCoverImage(dto.getCoverImage());
            post.setImages(dto.getImages());
            post.setViewCount(0);
            post.setReplyCount(0);
            // likeCount和favoriteCount已从数据库删除，使用动态计算
            post.setIsSticky(0);
            post.setIsEssence(0);
            post.setStatus(1); // 1=正常（直接发布，不需要审核）
            post.setLastReplyTime(new Date());
            post.setCreateTime(new Date());
            post.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            int result = postMapper.insert(post);
            if (result <= 0) {
                logger.error("创建帖子失败: 数据库插入失败");
                return Result.failure(6120); // 帖子发布失败
            }
            
            // 5. 更新版块的帖子数
            topic.setPostCount((topic.getPostCount() != null ? topic.getPostCount() : 0) + 1);
            topic.setUpdateTime(new Date());
            topicMapper.updateById(topic);
            
            logger.info("创建帖子成功: id={}, userId={}", post.getId(), userId);
            return Result.success(6011, null); // 帖子发布成功
            
        } catch (Exception e) {
            logger.error("创建帖子失败: 系统异常", e);
            return Result.failure(6120); // 帖子发布失败
        }
    }
    
    @Override
    public Result<Object> getPendingPosts(Map<String, Object> params) {
        try {
            logger.info("获取待审核帖子列表请求: {}", params);
            
            // 1. 解析查询参数
            Integer page = params.get("page") != null ? 
                    Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                    Integer.parseInt(params.get("pageSize").toString()) : 10;
            
            // 2. 使用优化的查询方法（数据库层面过滤和排序）
            List<ForumPost> allPosts = postMapper.selectByStatus(0);
            
            // 3. 批量查询用户和版块信息（优化N+1查询）
            List<String> userIds = allPosts.stream()
                    .map(ForumPost::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Integer> topicIds = allPosts.stream()
                    .map(ForumPost::getTopicId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<String, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectByIds(userIds);
                userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            }
            
            Map<Integer, ForumTopic> topicMap = new HashMap<>();
            if (!topicIds.isEmpty()) {
                List<ForumTopic> topics = topicMapper.selectByIds(topicIds);
                topicMap = topics.stream().collect(Collectors.toMap(ForumTopic::getId, t -> t));
            }
            
            // 4. 内存分页
            int total = allPosts.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<PostVO> postVOList = new ArrayList<>();
            if (startIndex < total) {
                final Map<String, User> finalUserMap = userMap;
                final Map<Integer, ForumTopic> finalTopicMap = topicMap;
                
                postVOList = allPosts.subList(startIndex, endIndex).stream()
                        .map(post -> {
                            PostVO vo = new PostVO();
                            vo.setId(post.getId());
                            vo.setUserId(post.getUserId());
                            // 从Map获取用户信息
                            User user = finalUserMap.get(post.getUserId());
                            vo.setUserName(user != null ? user.getUsername() : "未知用户");
                            vo.setUserAvatar(user != null ? user.getAvatar() : null);
                            vo.setTopicId(post.getTopicId());
                            // 从Map获取版块名称
                            ForumTopic topic = finalTopicMap.get(post.getTopicId());
                            vo.setTopicName(topic != null ? topic.getName() : "");
                            vo.setTitle(post.getTitle());
                            vo.setSummary(post.getSummary());
                            vo.setCoverImage(post.getCoverImage());
                            vo.setViewCount(post.getViewCount());
                            vo.setReplyCount(post.getReplyCount());
                            // 使用动态计算获取点赞数和收藏数
                            vo.setLikeCount(statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
                            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("post", String.valueOf(post.getId())));
                            vo.setIsSticky(post.getIsSticky());
                            vo.setIsEssence(post.getIsEssence());
                            vo.setStatus(post.getStatus());
                            vo.setLastReplyTime(post.getLastReplyTime());
                            vo.setCreateTime(post.getCreateTime());
                            return vo;
                        })
                        .collect(Collectors.toList());
            }
            
            // 5. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", postVOList);
            responseData.put("total", total);
            
            logger.info("获取待审核帖子列表成功，共{}条，返回{}条", total, postVOList.size());
            return Result.success(200, responseData); // 成功码200
            
        } catch (Exception e) {
            logger.error("获取待审核帖子列表失败: 系统异常", e);
            return Result.failure(6121); // 获取待审核帖子列表失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> getPostDetail(String id) {
        try {
            logger.info("获取帖子详情请求: id={}", id);
            
            // 1. 查询帖子
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("获取帖子详情失败: 帖子不存在或已删除, id: {}", id);
                return Result.failure(6122); // 获取帖子详情失败
            }
            
            // 2. 增加浏览量
            post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
            postMapper.updateById(post);
            
            // 3. 查询用户和版块信息
            User user = userMapper.selectById(post.getUserId());
            ForumTopic topic = topicMapper.selectById(post.getTopicId());
            
            // 4. 构造返回VO
            PostDetailVO vo = new PostDetailVO();
            vo.setId(post.getId());
            vo.setUserId(post.getUserId());
            vo.setUserName(user != null ? user.getUsername() : "未知用户");
            vo.setUserAvatar(user != null ? user.getAvatar() : null);
            vo.setTopicId(post.getTopicId());
            vo.setTopicName(topic != null ? topic.getName() : "");
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setSummary(post.getSummary());
            vo.setCoverImage(post.getCoverImage());
            vo.setImages(post.getImages());
            vo.setViewCount(post.getViewCount());
            vo.setReplyCount(post.getReplyCount());
            // 使用动态计算获取点赞数和收藏数
            vo.setLikeCount(statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("post", String.valueOf(post.getId())));
            vo.setIsSticky(post.getIsSticky());
            vo.setIsEssence(post.getIsEssence());
            vo.setStatus(post.getStatus());
            vo.setLastReplyTime(post.getLastReplyTime());
            vo.setCreateTime(post.getCreateTime());
            vo.setUpdateTime(post.getUpdateTime());
            
            logger.info("获取帖子详情成功: id={}", id);
            return Result.success(200, vo); // 成功码200
            
        } catch (NumberFormatException e) {
            logger.error("获取帖子详情失败: ID格式错误, id: {}", id, e);
            return Result.failure(6122); // 获取帖子详情失败
        } catch (Exception e) {
            logger.error("获取帖子详情失败: 系统异常, id: {}", id, e);
            return Result.failure(6122); // 获取帖子详情失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updatePost(String id, UpdatePostDTO dto) {
        try {
            logger.info("更新帖子请求: id={}, dto={}", id, dto);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("更新帖子失败: 用户未登录");
                return Result.failure(6123); // 帖子更新失败
            }
            
            // 2. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("更新帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6123); // 帖子更新失败
            }
            
            // 3. 验证用户是否有权限修改（作者本人或管理员）
            boolean isAdmin = UserContext.isAdmin();
            if (!userId.equals(post.getUserId()) && !isAdmin) {
                logger.warn("更新帖子失败: 无权限修改, userId: {}, postUserId: {}, isAdmin: {}", 
                        userId, post.getUserId(), isAdmin);
                return Result.failure(6123); // 帖子更新失败
            }
            
            // 4. 参数验证并更新帖子信息
            if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
                if (dto.getTitle().length() > 100) {
                    logger.warn("更新帖子失败: 标题长度超过限制");
                    return Result.failure(6123, "标题长度不能超过100个字符");
                }
                post.setTitle(dto.getTitle());
            }
            if (dto.getContent() != null) {
                if (dto.getContent().length() > 10000) {
                    logger.warn("更新帖子失败: 内容长度超过限制");
                    return Result.failure(6123, "内容长度不能超过10000个字符");
                }
                post.setContent(dto.getContent());
            }
            if (dto.getSummary() != null) {
                post.setSummary(dto.getSummary());
            }
            if (dto.getCoverImage() != null) {
                post.setCoverImage(dto.getCoverImage());
            }
            if (dto.getImages() != null) {
                post.setImages(dto.getImages());
            }
            post.setUpdateTime(new Date());
            
            // 5. 保存到数据库
            int result = postMapper.updateById(post);
            if (result <= 0) {
                logger.error("更新帖子失败: 数据库更新失败, id: {}", id);
                return Result.failure(6123); // 帖子更新失败
            }
            
            logger.info("更新帖子成功: id={}, userId={}", id, userId);
            return Result.success(6012, null); // 帖子更新成功
            
        } catch (NumberFormatException e) {
            logger.error("更新帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6123); // 帖子更新失败
        } catch (Exception e) {
            logger.error("更新帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6123); // 帖子更新失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deletePost(String id) {
        try {
            logger.info("删除帖子请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除帖子失败: 用户未登录");
                return Result.failure(6124); // 帖子删除失败
            }
            
            // 2. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("删除帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6124); // 帖子删除失败
            }
            
            // 3. 验证用户是否有权限删除（作者本人或管理员）
            boolean isAdmin = UserContext.isAdmin();
            if (!userId.equals(post.getUserId()) && !isAdmin) {
                logger.warn("删除帖子失败: 无权限删除, userId: {}, postUserId: {}, isAdmin: {}", 
                        userId, post.getUserId(), isAdmin);
                return Result.failure(6124); // 帖子删除失败
            }
            
            // 4. 软删除：更新状态为已删除
            post.setStatus(2); // 2=已删除
            post.setUpdateTime(new Date());
            int result = postMapper.updateById(post);
            
            if (result <= 0) {
                logger.error("删除帖子失败: 数据库更新失败, id: {}", id);
                return Result.failure(6124); // 帖子删除失败
            }
            
            // 5. 更新版块的帖子数
            ForumTopic topic = topicMapper.selectById(post.getTopicId());
            if (topic != null) {
                topic.setPostCount(Math.max(0, (topic.getPostCount() != null ? topic.getPostCount() : 0) - 1));
                topic.setUpdateTime(new Date());
                topicMapper.updateById(topic);
            }
            
            logger.info("删除帖子成功: id={}, userId={}", id, userId);
            return Result.success(6013, true); // 帖子删除成功
            
        } catch (NumberFormatException e) {
            logger.error("删除帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6124); // 帖子删除失败
        } catch (Exception e) {
            logger.error("删除帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6124); // 帖子删除失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> likePost(String id) {
        try {
            logger.info("点赞帖子请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("点赞帖子失败: 用户未登录");
                return Result.failure(6125); // 点赞失败
            }
            
            // 2. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("点赞帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6125); // 点赞失败
            }
            
            // 3. 检查是否已点赞
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, "post", id);
            if (existingLike != null) {
                logger.warn("点赞帖子失败: 已点赞, userId: {}, postId: {}", userId, id);
                return Result.failure(6125); // 点赞失败
            }
            
            // 4. 插入点赞记录
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setTargetType("post");
            userLike.setTargetId(id);
            userLike.setCreateTime(new Date());
            int insertResult = userLikeMapper.insert(userLike);
            
            if (insertResult <= 0) {
                logger.error("点赞帖子失败: 数据库插入失败, userId: {}, postId: {}", userId, id);
                return Result.failure(6125); // 点赞失败
            }
            
            // likeCount已从数据库删除，使用动态计算
            
            logger.info("点赞帖子成功: userId={}, postId={}", userId, id);
            return Result.success(6014, null); // 点赞成功
            
        } catch (NumberFormatException e) {
            logger.error("点赞帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6125); // 点赞失败
        } catch (Exception e) {
            logger.error("点赞帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6125); // 点赞失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> unlikePost(String id) {
        try {
            logger.info("取消点赞帖子请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消点赞帖子失败: 用户未登录");
                return Result.failure(6126); // 取消点赞失败
            }
            
            // 2. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("取消点赞帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6126); // 取消点赞失败
            }
            
            // 3. 检查是否已点赞
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, "post", id);
            if (existingLike == null) {
                logger.warn("取消点赞帖子失败: 未点赞, userId: {}, postId: {}", userId, id);
                return Result.failure(6126); // 取消点赞失败
            }
            
            // 4. 删除点赞记录
            int deleteResult = userLikeMapper.deleteById(existingLike.getId());
            if (deleteResult <= 0) {
                logger.error("取消点赞帖子失败: 数据库删除失败, userId: {}, postId: {}", userId, id);
                return Result.failure(6126); // 取消点赞失败
            }
            
            // likeCount已从数据库删除，使用动态计算
            
            logger.info("取消点赞帖子成功: userId={}, postId={}", userId, id);
            return Result.success(6015, null); // 已取消点赞
            
        } catch (NumberFormatException e) {
            logger.error("取消点赞帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6126); // 取消点赞失败
        } catch (Exception e) {
            logger.error("取消点赞帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6126); // 取消点赞失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> favoritePost(String id) {
        try {
            logger.info("收藏帖子请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("收藏帖子失败: 用户未登录");
                return Result.failure(6127); // 收藏失败
            }
            
            // 2. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("收藏帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6127); // 收藏失败
            }
            
            // 3. 检查是否已收藏
            UserFavorite existingFavorite = userFavoriteMapper.selectByUserIdAndItem(userId, "post", id);
            if (existingFavorite != null) {
                logger.warn("收藏帖子失败: 已收藏, userId: {}, postId: {}", userId, id);
                return Result.failure(6127); // 收藏失败
            }
            
            // 4. 插入收藏记录
            UserFavorite userFavorite = new UserFavorite();
            userFavorite.setUserId(userId);
            userFavorite.setItemType("post");
            userFavorite.setItemId(id);
            userFavorite.setTargetName(post.getTitle()); // 冗余字段：帖子标题
            userFavorite.setTargetImage(null); // 帖子没有封面图
            userFavorite.setCreateTime(new Date());
            int insertResult = userFavoriteMapper.insert(userFavorite);
            
            if (insertResult <= 0) {
                logger.error("收藏帖子失败: 数据库插入失败, userId: {}, postId: {}", userId, id);
                return Result.failure(6127); // 收藏失败
            }
            
            // favoriteCount已从数据库删除，使用动态计算
            
            logger.info("收藏帖子成功: userId={}, postId={}", userId, id);
            return Result.success(6016, null); // 收藏成功
            
        } catch (NumberFormatException e) {
            logger.error("收藏帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6127); // 收藏失败
        } catch (Exception e) {
            logger.error("收藏帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6127); // 收藏失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> unfavoritePost(String id) {
        try {
            logger.info("取消收藏帖子请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消收藏帖子失败: 用户未登录");
                return Result.failure(6128); // 取消收藏失败
            }
            
            // 2. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("取消收藏帖子失败: 帖子不存在, id: {}", id);
                return Result.failure(6128); // 取消收藏失败
            }
            
            // 3. 检查是否已收藏
            UserFavorite existingFavorite = userFavoriteMapper.selectByUserIdAndItem(userId, "post", id);
            if (existingFavorite == null) {
                logger.warn("取消收藏帖子失败: 未收藏, userId: {}, postId: {}", userId, id);
                return Result.failure(6128); // 取消收藏失败
            }
            
            // 4. 删除收藏记录
            int deleteResult = userFavoriteMapper.deleteById(existingFavorite.getId());
            if (deleteResult <= 0) {
                logger.error("取消收藏帖子失败: 数据库删除失败, userId: {}, postId: {}", userId, id);
                return Result.failure(6128); // 取消收藏失败
            }
            
            // favoriteCount已从数据库删除，使用动态计算
            
            logger.info("取消收藏帖子成功: userId={}, postId={}", userId, id);
            return Result.success(6017, null); // 已取消收藏
            
        } catch (NumberFormatException e) {
            logger.error("取消收藏帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6128); // 取消收藏失败
        } catch (Exception e) {
            logger.error("取消收藏帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6128); // 取消收藏失败
        }
    }
    
    @Override
    public Result<Object> getPostReplies(String id, Map<String, Object> params) {
        try {
            logger.info("获取帖子回复列表请求: id={}, params={}", id, params);
            
            // 1. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("获取回复列表失败: 帖子不存在或已删除, id: {}", id);
                return Result.failure(6129); // 获取回复列表失败
            }
            
            // 2. 解析分页参数
            Integer page = params.get("page") != null ? 
                    Integer.parseInt(params.get("page").toString()) : 1;
            Integer pageSize = params.get("pageSize") != null ? 
                    Integer.parseInt(params.get("pageSize").toString()) : 20;
            
            // 3. 查询所有回复（数据库层面已按时间升序排序）
            List<ForumReply> allReplies = replyMapper.selectByPostId(postId);
            
            // 4. 过滤正常状态的回复（status=1）
            List<ForumReply> filteredReplies = allReplies.stream()
                    .filter(reply -> reply.getStatus() != null && reply.getStatus() == 1)
                    .collect(Collectors.toList());
            
            // 5. 分页处理
            int total = filteredReplies.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<ForumReply> pagedReplies = startIndex < total ? 
                    filteredReplies.subList(startIndex, endIndex) : new ArrayList<>();
            
            // 6. 批量查询用户信息（优化N+1查询）
            List<String> userIds = pagedReplies.stream()
                    .map(ForumReply::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<String> toUserIds = pagedReplies.stream()
                    .map(ForumReply::getToUserId)
                    .filter(toUserId -> toUserId != null && !toUserId.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            
            userIds.addAll(toUserIds);
            userIds = userIds.stream().distinct().collect(Collectors.toList());
            
            Map<String, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectByIds(userIds);
                userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            }
            
            // 7. 获取当前用户ID（用于判断是否已点赞）
            String currentUserId = UserContext.getCurrentUserId();
            
            // 8. 批量查询当前用户的点赞记录（优化N+1查询）
            Map<String, Boolean> likeMap = new HashMap<>();
            if (currentUserId != null && !pagedReplies.isEmpty()) {
                List<String> replyIds = pagedReplies.stream()
                        .map(reply -> String.valueOf(reply.getId()))
                        .collect(Collectors.toList());
                
                for (String replyId : replyIds) {
                    UserLike like = userLikeMapper.selectByUserIdAndTarget(
                            currentUserId, "reply", replyId);
                    likeMap.put(replyId, like != null);
                }
            }
            
            // 9. 转换为VO
            final Map<String, User> finalUserMap = userMap;
            List<com.shangnantea.model.vo.forum.ReplyVO> replyVOs = pagedReplies.stream()
                    .map(reply -> {
                        com.shangnantea.model.vo.forum.ReplyVO vo = new com.shangnantea.model.vo.forum.ReplyVO();
                        vo.setId(String.valueOf(reply.getId()));
                        vo.setPostId(String.valueOf(reply.getPostId()));
                        vo.setUserId(reply.getUserId());
                        vo.setContent(reply.getContent());
                        vo.setParentId(reply.getParentId() != null ? String.valueOf(reply.getParentId()) : null);
                        vo.setToUserId(reply.getToUserId());
                        // 使用动态计算获取点赞数
                        vo.setLikeCount(statisticsUtils.getLikeCount("reply", String.valueOf(reply.getId())));
                        vo.setCreateTime(reply.getCreateTime() != null ? reply.getCreateTime().toString() : null);
                        
                        // 从Map获取用户信息
                        User user = finalUserMap.get(reply.getUserId());
                        if (user != null) {
                            vo.setUsername(user.getUsername());
                            vo.setAvatar(user.getAvatar());
                        }
                        
                        // 从Map获取目标用户信息
                        if (reply.getToUserId() != null) {
                            User toUser = finalUserMap.get(reply.getToUserId());
                            if (toUser != null) {
                                vo.setToUsername(toUser.getUsername());
                            }
                        }
                        
                        // 从Map获取点赞状态
                        vo.setIsLiked(likeMap.getOrDefault(String.valueOf(reply.getId()), false));
                        
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            // 10. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("list", replyVOs);
            responseData.put("total", total);
            
            logger.info("获取回复列表成功: postId={}, total={}, returned={}", id, total, replyVOs.size());
            return Result.success(200, responseData); // 成功码200
            
        } catch (NumberFormatException e) {
            logger.error("获取回复列表失败: ID格式错误, id: {}", id, e);
            return Result.failure(6129); // 获取回复列表失败
        } catch (Exception e) {
            logger.error("获取回复列表失败: 系统异常, id: {}", id, e);
            return Result.failure(6129); // 获取回复列表失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createReply(String id, com.shangnantea.model.dto.forum.CreateReplyDTO dto) {
        try {
            logger.info("创建回复请求: postId={}, content={}", id, dto.getContent());
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("创建回复失败: 用户未登录");
                return Result.failure(6130); // 创建回复失败
            }
            
            // 2. 验证帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            if (post == null) {
                logger.warn("创建回复失败: 帖子不存在, id: {}", id);
                return Result.failure(6130); // 创建回复失败
            }
            
            // 3. 如果是回复评论，验证父回复是否存在
            Long parentId = null;
            String toUserId = null;
            if (dto.getParentId() != null && !dto.getParentId().isEmpty()) {
                parentId = Long.parseLong(dto.getParentId());
                ForumReply parentReply = replyMapper.selectById(parentId);
                if (parentReply == null) {
                    logger.warn("创建回复失败: 父回复不存在, parentId: {}", dto.getParentId());
                    return Result.failure(6130); // 创建回复失败
                }
                toUserId = parentReply.getUserId();
            }
            
            // 4. 创建回复记录
            ForumReply reply = new ForumReply();
            reply.setPostId(postId);
            reply.setUserId(userId);
            reply.setContent(dto.getContent());
            reply.setParentId(parentId);
            reply.setToUserId(toUserId);
            // likeCount已从数据库删除，使用动态计算
            reply.setStatus(1); // 1=正常
            reply.setCreateTime(new Date());
            reply.setUpdateTime(new Date());
            
            int insertResult = replyMapper.insert(reply);
            if (insertResult <= 0) {
                logger.error("创建回复失败: 数据库插入失败, userId: {}, postId: {}", userId, id);
                return Result.failure(6130); // 创建回复失败
            }
            
            // 5. 更新帖子回复数和最后回复时间
            post.setReplyCount((post.getReplyCount() != null ? post.getReplyCount() : 0) + 1);
            post.setLastReplyTime(new Date());
            post.setUpdateTime(new Date());
            postMapper.updateById(post);
            
            logger.info("创建回复成功: replyId={}, userId={}, postId={}", reply.getId(), userId, id);
            return Result.success(6018, null); // 回复发布成功
            
        } catch (NumberFormatException e) {
            logger.error("创建回复失败: ID格式错误, id: {}", id, e);
            return Result.failure(6130); // 创建回复失败
        } catch (Exception e) {
            logger.error("创建回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6130); // 创建回复失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteReply(String id) {
        try {
            logger.info("删除回复请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("删除回复失败: 用户未登录");
                return Result.failure(6131); // 删除回复失败
            }
            
            // 2. 查询回复是否存在
            Long replyId = Long.parseLong(id);
            ForumReply reply = replyMapper.selectById(replyId);
            
            if (reply == null) {
                logger.warn("删除回复失败: 回复不存在, id: {}", id);
                return Result.failure(6131); // 删除回复失败
            }
            
            // 3. 验证用户是否有权限删除（作者本人或管理员）
            boolean isAdmin = UserContext.isAdmin();
            if (!userId.equals(reply.getUserId()) && !isAdmin) {
                logger.warn("删除回复失败: 无权限删除, userId: {}, replyUserId: {}, isAdmin: {}", 
                        userId, reply.getUserId(), isAdmin);
                return Result.failure(6131); // 删除回复失败
            }
            
            // 4. 软删除：更新状态为已删除
            reply.setStatus(2); // 2=已删除
            reply.setUpdateTime(new Date());
            int result = replyMapper.updateById(reply);
            
            if (result <= 0) {
                logger.error("删除回复失败: 数据库更新失败, id: {}", id);
                return Result.failure(6131); // 删除回复失败
            }
            
            // 5. 更新帖子的回复数
            ForumPost post = postMapper.selectById(reply.getPostId());
            if (post != null) {
                post.setReplyCount(Math.max(0, (post.getReplyCount() != null ? post.getReplyCount() : 0) - 1));
                post.setUpdateTime(new Date());
                postMapper.updateById(post);
            }
            
            logger.info("删除回复成功: id={}, userId={}", id, userId);
            return Result.success(6019, true); // 评论已删除
            
        } catch (NumberFormatException e) {
            logger.error("删除回复失败: ID格式错误, id: {}", id, e);
            return Result.failure(6131); // 删除回复失败
        } catch (Exception e) {
            logger.error("删除回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6131); // 删除回复失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> likeReply(String id) {
        try {
            logger.info("点赞回复请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("点赞回复失败: 用户未登录");
                return Result.failure(6132); // 点赞失败
            }
            
            // 2. 验证回复是否存在
            Long replyId = Long.parseLong(id);
            ForumReply reply = replyMapper.selectById(replyId);
            if (reply == null) {
                logger.warn("点赞回复失败: 回复不存在, id: {}", id);
                return Result.failure(6132); // 点赞失败
            }
            
            // 3. 检查是否已点赞
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, "reply", id);
            if (existingLike != null) {
                logger.warn("点赞回复失败: 已点赞, userId: {}, replyId: {}", userId, id);
                return Result.failure(6132); // 点赞失败
            }
            
            // 4. 插入点赞记录
            UserLike userLike = new UserLike();
            userLike.setUserId(userId);
            userLike.setTargetType("reply");
            userLike.setTargetId(id);
            userLike.setCreateTime(new Date());
            int insertResult = userLikeMapper.insert(userLike);
            
            if (insertResult <= 0) {
                logger.error("点赞回复失败: 数据库插入失败, userId: {}, replyId: {}", userId, id);
                return Result.failure(6132); // 点赞失败
            }
            
            // likeCount已从数据库删除，使用动态计算
            
            logger.info("点赞回复成功: userId={}, replyId={}", userId, id);
            return Result.success(6020, null); // 点赞成功
            
        } catch (NumberFormatException e) {
            logger.error("点赞回复失败: ID格式错误, id: {}", id, e);
            return Result.failure(6132); // 点赞失败
        } catch (Exception e) {
            logger.error("点赞回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6132); // 点赞失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> unlikeReply(String id) {
        try {
            logger.info("取消点赞回复请求: id={}", id);
            
            // 1. 获取当前用户ID
            String userId = UserContext.getCurrentUserId();
            if (userId == null) {
                logger.warn("取消点赞回复失败: 用户未登录");
                return Result.failure(6133); // 取消点赞失败
            }
            
            // 2. 验证回复是否存在
            Long replyId = Long.parseLong(id);
            ForumReply reply = replyMapper.selectById(replyId);
            if (reply == null) {
                logger.warn("取消点赞回复失败: 回复不存在, id: {}", id);
                return Result.failure(6133); // 取消点赞失败
            }
            
            // 3. 检查是否已点赞
            UserLike existingLike = userLikeMapper.selectByUserIdAndTarget(userId, "reply", id);
            if (existingLike == null) {
                logger.warn("取消点赞回复失败: 未点赞, userId: {}, replyId: {}", userId, id);
                return Result.failure(6133); // 取消点赞失败
            }
            
            // 4. 删除点赞记录
            int deleteResult = userLikeMapper.deleteById(existingLike.getId());
            if (deleteResult <= 0) {
                logger.error("取消点赞回复失败: 数据库删除失败, userId: {}, replyId: {}", userId, id);
                return Result.failure(6133); // 取消点赞失败
            }
            
            // likeCount已从数据库删除，使用动态计算
            
            logger.info("取消点赞回复成功: userId={}, replyId={}", userId, id);
            return Result.success(6021, null); // 已取消点赞
            
        } catch (NumberFormatException e) {
            logger.error("取消点赞回复失败: ID格式错误, id: {}", id, e);
            return Result.failure(6133); // 取消点赞失败
        } catch (Exception e) {
            logger.error("取消点赞回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6133); // 取消点赞失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> approvePost(String id) {
        try {
            logger.info("审核通过帖子请求: id={}", id);
            
            // 1. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("审核通过失败: 帖子不存在, id: {}", id);
                return Result.failure(6134); // 审核失败
            }
            
            // 2. 检查帖子状态
            if (post.getStatus() != 0) {
                logger.warn("审核通过失败: 帖子状态不是待审核, id: {}, status: {}", id, post.getStatus());
                return Result.failure(6134, "帖子状态不是待审核");
            }
            
            // 3. 更新帖子状态为已发布
            post.setStatus(1); // 1=已发布
            post.setUpdateTime(new Date());
            int result = postMapper.updateById(post);
            
            if (result <= 0) {
                logger.error("审核通过失败: 数据库更新失败, id: {}", id);
                return Result.failure(6134); // 审核失败
            }
            
            logger.info("审核通过成功: id={}", id);
            return Result.success(6022, null); // 审核通过
            
        } catch (NumberFormatException e) {
            logger.error("审核通过失败: ID格式错误, id: {}", id, e);
            return Result.failure(6134); // 审核失败
        } catch (Exception e) {
            logger.error("审核通过失败: 系统异常, id: {}", id, e);
            return Result.failure(6134); // 审核失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> rejectPost(String id, RejectPostDTO dto) {
        try {
            logger.info("审核拒绝帖子请求: id={}, reason={}", id, dto.getReason());
            
            // 1. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("审核拒绝失败: 帖子不存在, id: {}", id);
                return Result.failure(6135); // 审核拒绝失败
            }
            
            // 2. 检查帖子状态
            if (post.getStatus() != 0) {
                logger.warn("审核拒绝失败: 帖子状态不是待审核, id: {}, status: {}", id, post.getStatus());
                return Result.failure(6135, "帖子状态不是待审核");
            }
            
            // 3. 更新帖子状态为已拒绝
            post.setStatus(3); // 3=已拒绝
            post.setUpdateTime(new Date());
            // Note: 拒绝原因可以存储在单独的审核记录表中，这里暂不处理
            int result = postMapper.updateById(post);
            
            if (result <= 0) {
                logger.error("审核拒绝失败: 数据库更新失败, id: {}", id);
                return Result.failure(6135); // 审核拒绝失败
            }
            
            logger.info("审核拒绝成功: id={}, reason={}", id, dto.getReason());
            return Result.success(6023, null); // 审核已拒绝
            
        } catch (NumberFormatException e) {
            logger.error("审核拒绝失败: ID格式错误, id: {}", id, e);
            return Result.failure(6135); // 审核拒绝失败
        } catch (Exception e) {
            logger.error("审核拒绝失败: 系统异常, id: {}", id, e);
            return Result.failure(6135); // 审核拒绝失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> togglePostSticky(String id, Boolean isSticky) {
        try {
            logger.info("设置帖子置顶请求: id={}, isSticky={}", id, isSticky);
            
            // 1. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("设置置顶失败: 帖子不存在, id: {}", id);
                return Result.failure(isSticky ? 6136 : 6137); // 置顶失败 / 取消置顶失败
            }
            
            // 2. 更新置顶状态
            post.setIsSticky(isSticky ? 1 : 0);
            post.setUpdateTime(new Date());
            int result = postMapper.updateById(post);
            
            if (result <= 0) {
                logger.error("设置置顶失败: 数据库更新失败, id: {}", id);
                return Result.failure(isSticky ? 6136 : 6137); // 置顶失败 / 取消置顶失败
            }
            
            logger.info("设置置顶成功: id={}, isSticky={}", id, isSticky);
            return Result.success(isSticky ? 6024 : 6025, null); // 帖子已置顶 / 帖子已取消置顶
            
        } catch (NumberFormatException e) {
            logger.error("设置置顶失败: ID格式错误, id: {}", id, e);
            return Result.failure(isSticky ? 6136 : 6137); // 置顶失败 / 取消置顶失败
        } catch (Exception e) {
            logger.error("设置置顶失败: 系统异常, id: {}", id, e);
            return Result.failure(isSticky ? 6136 : 6137); // 置顶失败 / 取消置顶失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> togglePostEssence(String id, Boolean isEssence) {
        try {
            logger.info("设置帖子精华请求: id={}, isEssence={}", id, isEssence);
            
            // 1. 查询帖子是否存在
            Long postId = Long.parseLong(id);
            ForumPost post = postMapper.selectById(postId);
            
            if (post == null) {
                logger.warn("设置精华失败: 帖子不存在, id: {}", id);
                return Result.failure(isEssence ? 6138 : 6139); // 加精失败 / 取消加精失败
            }
            
            // 2. 更新精华状态
            post.setIsEssence(isEssence ? 1 : 0);
            post.setUpdateTime(new Date());
            int result = postMapper.updateById(post);
            
            if (result <= 0) {
                logger.error("设置精华失败: 数据库更新失败, id: {}", id);
                return Result.failure(isEssence ? 6138 : 6139); // 加精失败 / 取消加精失败
            }
            
            logger.info("设置精华成功: id={}, isEssence={}", id, isEssence);
            return Result.success(isEssence ? 6026 : 6027, null); // 帖子已加精 / 帖子已取消加精
            
        } catch (NumberFormatException e) {
            logger.error("设置精华失败: ID格式错误, id: {}", id, e);
            return Result.failure(isEssence ? 6138 : 6139); // 加精失败 / 取消加精失败
        } catch (Exception e) {
            logger.error("设置精华失败: 系统异常, id: {}", id, e);
            return Result.failure(isEssence ? 6138 : 6139); // 加精失败 / 取消加精失败
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadPostImage(MultipartFile image) {
        try {
            logger.info("上传帖子图片请求: filename={}", image.getOriginalFilename());
            
            // 1. 验证文件
            if (image == null || image.isEmpty()) {
                logger.warn("上传帖子图片失败: 文件为空");
                return Result.failure(6140, "文件不能为空");
            }
            
            // 2. 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("上传帖子图片失败: 文件类型不正确, contentType: {}", contentType);
                return Result.failure(6141); // 不支持的文件类型
            }
            
            // 3. 验证文件大小（限制5MB）
            if (image.getSize() > 5 * 1024 * 1024) {
                logger.warn("上传帖子图片失败: 文件大小超过限制, size: {}", image.getSize());
                return Result.failure(6142); // 文件大小超限
            }
            
            // 4. 上传文件
            String filePath = FileUploadUtils.uploadImage(image, "forum-posts");
            if (filePath == null || filePath.isEmpty()) {
                logger.error("上传帖子图片失败: 文件上传失败");
                return Result.failure(6140); // 图片上传失败
            }
            
            // 5. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(filePath, baseUrl);
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", filePath);
            
            logger.info("上传帖子图片成功: path={}, url={}", filePath, accessUrl);
            return Result.success(6028, responseData); // 图片上传成功
            
        } catch (Exception e) {
            logger.error("上传帖子图片失败: 系统异常", e);
            return Result.failure(6140); // 图片上传失败
        }
    }
}
            logger.error("取消点赞回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6133); // 取消点赞失败
        }
    }
} 