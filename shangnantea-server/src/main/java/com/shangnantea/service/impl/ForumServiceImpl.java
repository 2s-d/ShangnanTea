package com.shangnantea.service.impl;

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
import com.shangnantea.utils.NotificationUtils;
import com.shangnantea.utils.StatisticsUtils;
import java.util.Objects;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                    Integer sortOrder = bannerData.get("sortOrder") != null ? 
                            Integer.parseInt(bannerData.get("sortOrder").toString()) : 0;
                    
                    if (id != null) {
                        // 更新现有Banner
                        HomeContent content = homeContentMapper.selectById(id);
                        if (content != null && "banner".equals(content.getSection())) {
                            content.setContent(imageUrl);
                            content.setTitle(title);
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
                        // 生成图片访问URL（兼容已是完整URL的情况）
                        String imageUrl = content.getContent();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            if (!(imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                                imageUrl = FileUploadUtils.generateAccessUrl(imageUrl, baseUrl);
                            }
                        }
                        bannerVO.setImageUrl(imageUrl);
                        bannerVO.setTitle(content.getTitle());
                        // 轮播图跳转链接：按约定复用 home_contents.sub_title 字段存储
                        bannerVO.setLinkUrl(content.getSubTitle());
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
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> uploadBanner(MultipartFile file, String title, String subtitle, String linkUrl, Integer sortOrder) {
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
            // 轮播图跳转链接：按约定复用 home_contents.sub_title 字段存储
            // subtitle 参数保留仅用于兼容旧调用，不再写入数据库
            content.setSubTitle(linkUrl);
            content.setContent(relativePath); // 存储相对路径
            // 如果提供了排序值，则使用；否则默认0，后续可通过更新顺序接口调整
            content.setSortOrder(sortOrder != null ? sortOrder : 0);
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
                // 允许清空：传空字符串时也覆盖为 ""
                content.setSubTitle(linkUrl.trim());
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
            responseData.put("linkUrl", content.getSubTitle());
            responseData.put("sortOrder", content.getSortOrder());
            // 生成图片访问URL
            String imageUrl = content.getContent();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!(imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                    imageUrl = FileUploadUtils.generateAccessUrl(imageUrl, baseUrl);
                }
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
            // 返回 code=6003，data=null
            return Result.success(6003); // 删除成功
            
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
            // 修复：处理空字符串参数
            Integer page = 1;
            Integer pageSize = 10;
            if (params.get("page") != null && !params.get("page").toString().trim().isEmpty()) {
                page = Integer.parseInt(params.get("page").toString());
            }
            if (params.get("pageSize") != null && !params.get("pageSize").toString().trim().isEmpty()) {
                pageSize = Integer.parseInt(params.get("pageSize").toString());
            }
            
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
                            vo.setStatus(article.getStatus() != null ? article.getStatus() : 0);
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
            vo.setVideoUrl(article.getVideoUrl());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数和收藏数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            
            // 查询当前用户是否已点赞/收藏该文章
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null) {
                try {
                    UserLike like = userLikeMapper.selectByUserIdAndTarget(currentUserId, "article", id);
                    vo.setIsLiked(like != null);
                    
                    UserFavorite favorite = userFavoriteMapper.selectByUserIdAndItem(currentUserId, "article", id);
                    vo.setIsFavorited(favorite != null);
                } catch (Exception e) {
                    logger.warn("查询文章点赞/收藏状态失败, articleId: {}, userId: {}, 默认设置为未点赞/未收藏", id, currentUserId, e);
                    vo.setIsLiked(false);
                    vo.setIsFavorited(false);
                }
            } else {
                // 未登录用户默认未点赞/未收藏
                vo.setIsLiked(false);
                vo.setIsFavorited(false);
            }
            
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
            // tags 可能是前端传来的字符串，也可能是数组，这里统一转换为用逗号分隔的字符串，避免 ClassCastException
            Object rawTags = data.get("tags");
            String tags = null;
            if (rawTags instanceof String) {
                tags = ((String) rawTags).trim();
            } else if (rawTags instanceof java.util.List<?>) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) rawTags;
                tags = list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(","));
            }
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
            
            // 作者字段：允许前端显式填写作者名；为空时使用固定默认值“商南茶文化编辑部”
            String authorFromRequest = (String) data.get("author");
            String authorName;
            if (authorFromRequest != null && !authorFromRequest.trim().isEmpty()) {
                authorName = authorFromRequest.trim();
            } else {
                authorName = "商南茶文化编辑部";
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
            
            // 图片索引：文章保存时根据最终内容与封面统一抽取
            String articleImages = buildImageIndex(content, coverImage, null);
            article.setImages(articleImages);
            
            // 4. 保存到数据库
            articleMapper.insert(article);
            
            // 5. 构造返回VO
            ArticleVO vo = new ArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            // 处理封面图片URL
            String articleCoverImage = article.getCoverImage();
            if (articleCoverImage != null && !articleCoverImage.trim().isEmpty()) {
                if (articleCoverImage.startsWith("http://") || articleCoverImage.startsWith("https://")) {
                    vo.setCoverImage(articleCoverImage);
                } else {
                    vo.setCoverImage(FileUploadUtils.generateAccessUrl(articleCoverImage, baseUrl));
                }
            } else {
                vo.setCoverImage(null);
            }
            vo.setAuthorName(article.getAuthor());
            vo.setCategory(article.getCategory());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            vo.setStatus(article.getStatus() != null ? article.getStatus() : 1);
            
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
                
                // 当正文发生变更时，需要重新抽取图片索引
                String cover = article.getCoverImage();
                String imagesIndex = buildImageIndex(content, cover, null);
                article.setImages(imagesIndex);
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
                String coverImage = (String) data.get("coverImage");
                article.setCoverImage(coverImage);
                // 封面变更时，同步刷新图片索引（使用最新正文）
                String imagesIndex = buildImageIndex(article.getContent(), coverImage, null);
                article.setImages(imagesIndex);
            }
            if (data.containsKey("category")) {
                article.setCategory((String) data.get("category"));
            }
            if (data.containsKey("tags")) {
                Object tagsObj = data.get("tags");
                if (tagsObj != null) {
                    // 处理tags：可能是数组或字符串
                    if (tagsObj instanceof String) {
                        article.setTags((String) tagsObj);
                    } else if (tagsObj instanceof java.util.List) {
                        // 如果是数组，转换为逗号分隔的字符串
                        @SuppressWarnings("unchecked")
                        java.util.List<String> tagsList = (java.util.List<String>) tagsObj;
                        article.setTags(String.join(",", tagsList));
                    } else {
                        article.setTags(tagsObj.toString());
                    }
                }
            }
            if (data.containsKey("videoUrl")) {
                article.setVideoUrl((String) data.get("videoUrl"));
            }
            if (data.containsKey("source")) {
                article.setSource((String) data.get("source"));
            }
            if (data.containsKey("status")) {
                Object statusObj = data.get("status");
                if (statusObj != null) {
                    article.setStatus(statusObj instanceof Integer ? (Integer) statusObj : Integer.parseInt(statusObj.toString()));
                }
            }
            if (data.containsKey("is_top")) {
                Object isTopObj = data.get("is_top");
                if (isTopObj != null) {
                    article.setIsTop(isTopObj instanceof Integer ? (Integer) isTopObj : Integer.parseInt(isTopObj.toString()));
                }
            }
            if (data.containsKey("is_recommend")) {
                Object isRecommendObj = data.get("is_recommend");
                if (isRecommendObj != null) {
                    article.setIsRecommend(isRecommendObj instanceof Integer ? (Integer) isRecommendObj : Integer.parseInt(isRecommendObj.toString()));
                }
            }
            if (data.containsKey("author")) {
                String authorFromRequest = (String) data.get("author");
                if (authorFromRequest != null && !authorFromRequest.trim().isEmpty()) {
                    article.setAuthor(authorFromRequest.trim());
                } else {
                    // 编辑时清空作者，恢复为默认“商南茶文化编辑部”
                    article.setAuthor("商南茶文化编辑部");
                }
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
            // 处理封面图片URL
            String coverImage = article.getCoverImage();
            if (coverImage != null && !coverImage.trim().isEmpty()) {
                if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                    vo.setCoverImage(coverImage);
                } else {
                    vo.setCoverImage(FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                }
            } else {
                vo.setCoverImage(null);
            }
            vo.setAuthorName(article.getAuthor());
            vo.setCategory(article.getCategory());
            vo.setViewCount(article.getViewCount());
            // 使用动态计算获取点赞数
            vo.setLikeCount(statisticsUtils.getLikeCount("article", String.valueOf(article.getId())));
            vo.setIsTop(article.getIsTop());
            vo.setIsRecommend(article.getIsRecommend());
            vo.setPublishTime(article.getPublishTime());
            vo.setCreateTime(article.getCreateTime());
            vo.setStatus(article.getStatus() != null ? article.getStatus() : 0);
            
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
    
    /**
     * 从富文本内容与封面中抽取图片索引，生成用于存库的字符串。
     * 当前实现使用逗号分隔的URL列表，以兼容历史数据与读取逻辑。
     *
     * @param content    富文本内容（可能包含&lt;img src="..."&gt;）
     * @param coverImage 封面图片URL（可为空）
     * @param rawImages  旧实现传入的images字段（可为空，兼容用）
     * @return 逗号分隔的图片URL字符串，若无图片则返回null
     */
    private String buildImageIndex(String content, String coverImage, String rawImages) {
        Set<String> urlSet = new HashSet<>();

        // 1. 从富文本内容中解析 <img src="...">
        if (content != null && !content.isEmpty()) {
            Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\\\"]([^'\\\"]+)['\\\"]", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String url = matcher.group(1);
                if (url != null && !url.trim().isEmpty()) {
                    urlSet.add(normalizeImagePath(url.trim()));
                }
            }
        }

        // 2. 加入封面图片
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            urlSet.add(normalizeImagePath(coverImage.trim()));
        }

        // 3. 兼容旧实现：如果前端已经传入了images字段，则一并合并进来
        if (rawImages != null && !rawImages.trim().isEmpty()) {
            String[] parts = rawImages.split(",");
            for (String part : parts) {
                if (part != null && !part.trim().isEmpty()) {
                    urlSet.add(normalizeImagePath(part.trim()));
                }
            }
        }

        if (urlSet.isEmpty()) {
            return null;
        }

        return String.join(",", urlSet);
    }

    /**
     * 将图片URL统一转换为相对路径存库：
     * - 如果是本系统生成的完整访问URL（以baseUrl开头），则裁剪掉baseUrl前缀，只保留相对路径
     * - 如果是以 /files/ 开头的绝对路径，则去掉前导斜杠
     * - 其它第三方完整URL保持不变
     */
    private String normalizeImagePath(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        String trimmed = url.trim();

        // 1. 裁剪掉本系统的访问前缀（http://.../api）
        if (baseUrl != null && !baseUrl.isEmpty() && trimmed.startsWith(baseUrl)) {
            String relative = trimmed.substring(baseUrl.length());
            // 通常是 /files/xxx，去掉前导 /
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }
            return relative;
        }

        // 2. 已经是 /files/... 这种绝对路径，去掉前导 /
        if (trimmed.startsWith("/files/")) {
            return trimmed.substring(1);
        }

        // 3. 其它情况（第三方URL等）原样返回
        return trimmed;
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
            // 返回 code=6007，data=null（文章已删除）
            return Result.success(6007);
            
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
            // 返回 code=6010，data=null（删除版块成功）
            return Result.success(6010);
            
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
            // 修复：处理空字符串参数
            Integer topicId = null;
            if (params.get("topicId") != null && !params.get("topicId").toString().trim().isEmpty()) {
                topicId = Integer.parseInt(params.get("topicId").toString());
            }
            String userId = null;
            if (params.get("userId") != null && !params.get("userId").toString().trim().isEmpty()) {
                userId = params.get("userId").toString();
            }
            String keyword = (String) params.get("keyword");
            Integer page = 1;
            Integer pageSize = 10;
            if (params.get("page") != null && !params.get("page").toString().trim().isEmpty()) {
                page = Integer.parseInt(params.get("page").toString());
            }
            if (params.get("pageSize") != null && !params.get("pageSize").toString().trim().isEmpty()) {
                pageSize = Integer.parseInt(params.get("pageSize").toString());
            }
            
            // 2. 使用优化的查询方法（数据库层面过滤和排序）
            List<ForumPost> allPosts = postMapper.selectPublishedPosts(topicId, keyword, userId, 1);
            
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
            
            // 批量查询每个帖子的回复数（优化N+1查询）
            Map<Long, Integer> replyCountMap = new HashMap<>();
            if (!allPosts.isEmpty()) {
                List<Long> postIds = allPosts.stream().map(ForumPost::getId).collect(Collectors.toList());
                for (Long postId : postIds) {
                    List<ForumReply> replies = replyMapper.selectByPostId(postId);
                    replyCountMap.put(postId, replies != null ? replies.size() : 0);
                }
            }
            
            // 4. 内存分页
            int total = allPosts.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<PostVO> postVOList = new ArrayList<>();
            if (startIndex < total) {
                final Map<String, User> finalUserMap = userMap;
                final Map<Integer, ForumTopic> finalTopicMap = topicMap;
                final Map<Long, Integer> finalReplyCountMap = replyCountMap;
                
                postVOList = allPosts.subList(startIndex, endIndex).stream()
                        .map(post -> {
                            PostVO vo = new PostVO();
                            vo.setId(post.getId());
                            vo.setUserId(post.getUserId());
                            // 从Map获取用户信息
                            User user = finalUserMap.get(post.getUserId());
                            vo.setNickname(user != null ? user.getNickname() : "未知用户");
                            // 处理用户头像URL
                            String userAvatar = user != null ? user.getAvatar() : null;
                            if (userAvatar != null && !userAvatar.trim().isEmpty()) {
                                if (userAvatar.startsWith("http://") || userAvatar.startsWith("https://")) {
                                    vo.setUserAvatar(userAvatar);
                                } else {
                                    vo.setUserAvatar(FileUploadUtils.generateAccessUrl(userAvatar, baseUrl));
                                }
                            } else {
                                vo.setUserAvatar(null);
                            }
                            vo.setTopicId(post.getTopicId());
                            // 从Map获取版块名称
                            ForumTopic topic = finalTopicMap.get(post.getTopicId());
                            vo.setTopicName(topic != null ? topic.getName() : "");
                            vo.setTitle(post.getTitle());
                            vo.setSummary(post.getSummary());
                            // 处理封面图片URL
                            String coverImage = post.getCoverImage();
                            if (coverImage != null && !coverImage.trim().isEmpty()) {
                                if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                                    vo.setCoverImage(coverImage);
                                } else {
                                    vo.setCoverImage(FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                                }
                            } else {
                                vo.setCoverImage(null);
                            }
                            vo.setViewCount(post.getViewCount());
                            // 使用动态计算的回复数，而不是数据库字段
                            vo.setReplyCount(finalReplyCountMap.getOrDefault(post.getId(), 0));
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
            
            // 8. 构造返回数据
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
            // 图片索引：在后端根据最终内容与封面统一抽取，避免依赖前端拼接
            String imageIndex = buildImageIndex(dto.getContent(), dto.getCoverImage(), dto.getImages());
            post.setImages(imageIndex);
            post.setViewCount(0);
            post.setReplyCount(0);
            // likeCount和favoriteCount已从数据库删除，使用动态计算
            post.setIsSticky(0);
            post.setIsEssence(0);
            post.setStatus(0); // 0=待审核（新帖子需要管理员审核后才能发布）
            post.setLastReplyTime(new Date());
            post.setCreateTime(new Date());
            post.setUpdateTime(new Date());
            
            // 4. 保存到数据库
            int result = postMapper.insert(post);
            if (result <= 0) {
                logger.error("创建帖子失败: 数据库插入失败");
                return Result.failure(6120); // 帖子发布失败
            }
            
            // 5. 更新版块的帖子数（待审核的帖子也计入总数）
            topic.setPostCount((topic.getPostCount() != null ? topic.getPostCount() : 0) + 1);
            topic.setUpdateTime(new Date());
            topicMapper.updateById(topic);
            
            logger.info("创建帖子成功（待审核）: id={}, userId={}", post.getId(), userId);
            return Result.success(6011, null); // 帖子提交成功，等待审核
            
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
            // 修复：处理空字符串参数
            Integer page = 1;
            Integer pageSize = 10;
            if (params.get("page") != null && !params.get("page").toString().trim().isEmpty()) {
                page = Integer.parseInt(params.get("page").toString());
            }
            if (params.get("pageSize") != null && !params.get("pageSize").toString().trim().isEmpty()) {
                pageSize = Integer.parseInt(params.get("pageSize").toString());
            }
            
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
                            vo.setNickname(user != null ? user.getNickname() : "未知用户");
                            // 处理用户头像URL
                            String userAvatar = user != null ? user.getAvatar() : null;
                            if (userAvatar != null && !userAvatar.trim().isEmpty()) {
                                if (userAvatar.startsWith("http://") || userAvatar.startsWith("https://")) {
                                    vo.setUserAvatar(userAvatar);
                                } else {
                                    vo.setUserAvatar(FileUploadUtils.generateAccessUrl(userAvatar, baseUrl));
                                }
                            } else {
                                vo.setUserAvatar(null);
                            }
                            vo.setTopicId(post.getTopicId());
                            // 从Map获取版块名称
                            ForumTopic topic = finalTopicMap.get(post.getTopicId());
                            vo.setTopicName(topic != null ? topic.getName() : "");
                            vo.setTitle(post.getTitle());
                            vo.setSummary(post.getSummary());
                            // 处理封面图片URL
                            String coverImage = post.getCoverImage();
                            if (coverImage != null && !coverImage.trim().isEmpty()) {
                                if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                                    vo.setCoverImage(coverImage);
                                } else {
                                    vo.setCoverImage(FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                                }
                            } else {
                                vo.setCoverImage(null);
                            }
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
            
            // 1.1 校验访问权限：
            // - status=1（正常）: 所有人可见
            // - status=0（待审核）、status=3（已拒绝）: 仅作者和管理员可见
            // - status=2（已删除）: 任何人不可见
            String currentUserId = UserContext.getCurrentUserId();
            Integer status = post.getStatus();
            boolean isAdmin = UserContext.isAdmin();
            boolean isOwner = currentUserId != null && currentUserId.equals(post.getUserId());
            
            if (status != null) {
                // 已删除：一律不可见
                if (status == 2) {
                    logger.warn("获取帖子详情失败: 帖子已删除, id: {}", id);
                    return Result.failure(6122);
                }
                // 待审核或已拒绝：仅作者/管理员可见
                if ((status == 0 || status == 3) && !(isAdmin || isOwner)) {
                    logger.warn("获取帖子详情失败: 帖子处于非公开状态且当前用户无权限, id: {}, status: {}, currentUserId={}",
                            id, status, currentUserId);
                    return Result.failure(6122);
                }
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
            vo.setNickname(user != null ? user.getNickname() : "未知用户");
            // 处理用户头像URL
            String userAvatar = user != null ? user.getAvatar() : null;
            if (userAvatar != null && !userAvatar.trim().isEmpty()) {
                if (userAvatar.startsWith("http://") || userAvatar.startsWith("https://")) {
                    vo.setUserAvatar(userAvatar);
                } else {
                    vo.setUserAvatar(FileUploadUtils.generateAccessUrl(userAvatar, baseUrl));
                }
            } else {
                vo.setUserAvatar(null);
            }
            vo.setTopicId(post.getTopicId());
            vo.setTopicName(topic != null ? topic.getName() : "");
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setSummary(post.getSummary());
            // 处理封面图片URL
            String coverImage = post.getCoverImage();
            if (coverImage != null && !coverImage.trim().isEmpty()) {
                if (coverImage.startsWith("http://") || coverImage.startsWith("https://")) {
                    vo.setCoverImage(coverImage);
                } else {
                    vo.setCoverImage(FileUploadUtils.generateAccessUrl(coverImage, baseUrl));
                }
            } else {
                vo.setCoverImage(null);
            }
            // 处理图片列表URL（逗号分隔的字符串）
            String imagesStr = post.getImages();
            if (imagesStr != null && !imagesStr.trim().isEmpty()) {
                List<String> processedImages = new ArrayList<>();
                String[] imageArray = imagesStr.split(",");
                for (String imageUrl : imageArray) {
                    if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                        if (imageUrl.trim().startsWith("http://") || imageUrl.trim().startsWith("https://")) {
                            processedImages.add(imageUrl.trim());
                        } else {
                            processedImages.add(FileUploadUtils.generateAccessUrl(imageUrl.trim(), baseUrl));
                        }
                    }
                }
                vo.setImages(String.join(",", processedImages));
            } else {
                vo.setImages(null);
            }
            vo.setViewCount(post.getViewCount());
            vo.setReplyCount(post.getReplyCount());
            // 使用动态计算获取点赞数和收藏数
            vo.setLikeCount(statisticsUtils.getLikeCount("post", String.valueOf(post.getId())));
            vo.setFavoriteCount(statisticsUtils.getFavoriteCount("post", String.valueOf(post.getId())));
            vo.setIsSticky(post.getIsSticky());
            vo.setIsEssence(post.getIsEssence());
            vo.setStatus(post.getStatus());
            vo.setLastReplyTime(post.getLastReplyTime());
            vo.setUpdateTime(post.getUpdateTime());
            
            // 查询当前用户是否已点赞/收藏该帖子
            currentUserId = UserContext.getCurrentUserId();
            if (currentUserId != null) {
                try {
                    UserLike like = userLikeMapper.selectByUserIdAndTarget(currentUserId, "post", id);
                    vo.setIsLiked(like != null);
                    
                    UserFavorite favorite = userFavoriteMapper.selectByUserIdAndItem(currentUserId, "post", id);
                    vo.setIsFavorited(favorite != null);
                } catch (Exception e) {
                    logger.warn("查询帖子点赞/收藏状态失败, postId: {}, userId: {}, 默认设置为未点赞/未收藏", id, currentUserId, e);
                    vo.setIsLiked(false);
                    vo.setIsFavorited(false);
                }
            } else {
                // 未登录用户默认未点赞/未收藏
                vo.setIsLiked(false);
                vo.setIsFavorited(false);
            }
            
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
            
            // 3. 验证用户是否有权限修改（仅作者本人可以修改内容）
            if (!userId.equals(post.getUserId())) {
                logger.warn("更新帖子失败: 无权限修改, userId: {}, postUserId: {}", 
                        userId, post.getUserId());
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
            // 注意：帖子状态更新策略
            // 业务约定：只要作者修改过内容并提交，帖子必须重新进入待审核状态
            // - 作者编辑任何非删除状态的帖子：统统重置为 status=0（待审核）
            // - 删除状态(status=2)的帖子不允许通过此接口“复活”，仍保持已删除
            Integer oldStatus = post.getStatus();
            if (oldStatus != null && oldStatus != 2) {
                post.setStatus(0);
            }
            post.setUpdateTime(new Date());
            
            // 5. 保存到数据库
            int result = postMapper.updateById(post);
            if (result <= 0) {
                logger.error("更新帖子失败: 数据库更新失败, id: {}", id);
                return Result.failure(6123); // 帖子更新失败
            }
            
            logger.info("更新帖子成功: id={}, userId={}, status={}", id, userId, post.getStatus());
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
            // 返回 code=6013，data=null（帖子删除成功）
            return Result.success(6013);
            
        } catch (NumberFormatException e) {
            logger.error("删除帖子失败: ID格式错误, id: {}", id, e);
            return Result.failure(6124); // 帖子删除失败
        } catch (Exception e) {
            logger.error("删除帖子失败: 系统异常, id: {}", id, e);
            return Result.failure(6124); // 帖子删除失败
        }
    }
    
    // ⚠️ 已删除：帖子点赞/收藏相关方法实现（likePost, unlikePost, favoritePost, unfavoritePost）
    // 说明：帖子点赞和收藏功能已统一使用用户模块的通用接口（UserServiceImpl 中的 addLike/removeLike, addFavorite/removeFavorite）
    // 帖子详情接口（getPostDetail）已包含 isLiked 和 isFavorited 字段，无需单独调用点赞/收藏接口
    
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
            // 修复：处理空字符串参数
            Integer page = 1;
            Integer pageSize = 20;
            if (params.get("page") != null && !params.get("page").toString().trim().isEmpty()) {
                page = Integer.parseInt(params.get("page").toString());
            }
            if (params.get("pageSize") != null && !params.get("pageSize").toString().trim().isEmpty()) {
                pageSize = Integer.parseInt(params.get("pageSize").toString());
            }
            
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
                            // 前台仅展示昵称
                            vo.setNickname(user.getNickname());
                            String avatar = user.getAvatar();
                            if (avatar != null && !avatar.trim().isEmpty()) {
                                if (avatar.startsWith("http://") || avatar.startsWith("https://")) {
                                    vo.setAvatar(avatar);
                                } else {
                                    vo.setAvatar(FileUploadUtils.generateAccessUrl(avatar, baseUrl));
                                }
                            } else {
                                vo.setAvatar(null);
                            }
                        }
                        
                        // 从Map获取目标用户信息
                        if (reply.getToUserId() != null) {
                            User toUser = finalUserMap.get(reply.getToUserId());
                            if (toUser != null) {
                                // 目标用户同样仅展示昵称
                                vo.setToNickname(toUser.getNickname());
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
            
            // 6. 创建通知：给帖子作者发送回复通知
            String postAuthorId = post.getUserId();
            if (postAuthorId != null && !postAuthorId.equals(userId)) {
                NotificationUtils.createPostReplyNotification(
                    postAuthorId, userId, postId, reply.getId(), dto.getContent()
                );
            }
            
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
            // 返回 code=6019，data=null（评论已删除）
            return Result.success(6019);
            
        } catch (NumberFormatException e) {
            logger.error("删除回复失败: ID格式错误, id: {}", id, e);
            return Result.failure(6131); // 删除回复失败
        } catch (Exception e) {
            logger.error("删除回复失败: 系统异常, id: {}", id, e);
            return Result.failure(6131); // 删除回复失败
        }
    }
    
    // ⚠️ 已删除：回复点赞相关方法实现（likeReply, unlikeReply）
    // 说明：回复点赞功能已统一使用用户模块的通用接口（UserServiceImpl 中的 addLike/removeLike）
    // 回复列表接口（getPostReplies）已包含每个回复的 isLiked 字段，无需单独调用点赞接口
    
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

            // 4. 创建帖子审核通过通知（站内信）
            try {
                NotificationUtils.createPostAuditResultNotification(
                        post.getUserId(),
                        post.getId(),
                        post.getTitle(),
                        true,
                        null
                );
            } catch (Exception notifyEx) {
                logger.warn("审核通过后创建帖子审核结果通知失败, postId={}, userId={}", post.getId(), post.getUserId(), notifyEx);
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

            // 4. 创建帖子审核拒绝通知（站内信）
            try {
                NotificationUtils.createPostAuditResultNotification(
                        post.getUserId(),
                        post.getId(),
                        post.getTitle(),
                        false,
                        dto.getReason()
                );
            } catch (Exception notifyEx) {
                logger.warn("审核拒绝后创建帖子审核结果通知失败, postId={}, userId={}", post.getId(), post.getUserId(), notifyEx);
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
                return Result.failure(6140); // 图片上传失败
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> uploadArticleImage(MultipartFile image) {
        try {
            logger.info("上传文章图片请求: filename={}", image.getOriginalFilename());
            
            // 1. 验证文件
            if (image == null || image.isEmpty()) {
                logger.warn("上传文章图片失败: 文件为空");
                return Result.failure(6143); // 图片上传失败
            }
            
            // 2. 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("上传文章图片失败: 文件类型不正确, contentType: {}", contentType);
                return Result.failure(6144); // 不支持的文件类型
            }
            
            // 3. 验证文件大小（限制5MB）
            if (image.getSize() > 5 * 1024 * 1024) {
                logger.warn("上传文章图片失败: 文件大小超过限制, size: {}", image.getSize());
                return Result.failure(6145); // 文件大小超限
            }
            
            // 4. 上传文件
            String filePath = FileUploadUtils.uploadImage(image, "forum-articles");
            if (filePath == null || filePath.isEmpty()) {
                logger.error("上传文章图片失败: 文件上传失败");
                return Result.failure(6143); // 图片上传失败
            }
            
            // 5. 生成访问URL
            String accessUrl = FileUploadUtils.generateAccessUrl(filePath, baseUrl);
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("url", accessUrl);
            responseData.put("path", filePath);
            
            logger.info("上传文章图片成功: path={}, url={}", filePath, accessUrl);
            return Result.success(6029, responseData); // 图片上传成功
            
        } catch (Exception e) {
            logger.error("上传文章图片失败: 系统异常", e);
            return Result.failure(6143); // 图片上传失败
        }
    }
    
    // ==================== 分类管理 ====================
    
    @Override
    public Result<Object> getCategories() {
        try {
            logger.info("获取分类列表请求");
            
            // 查询 home_contents 表中 section='category' 的记录
            List<HomeContent> categories = homeContentMapper.selectBySection("category", 1);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> categoryList = categories.stream()
                    .map(category -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", category.getId());
                        map.put("name", category.getTitle()); // title 字段存储分类名称
                        map.put("description", category.getSubTitle()); // sub_title 字段存储描述
                        map.put("sort_order", category.getSortOrder());
                        map.put("status", category.getStatus());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            logger.info("获取分类列表成功，共{}条", categoryList.size());
            return Result.success(200, categoryList);
            
        } catch (Exception e) {
            logger.error("获取分类列表失败: 系统异常", e);
            return Result.failure(6100);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> createCategory(Map<String, Object> data) {
        try {
            logger.info("创建分类请求: {}", data);
            
            // 1. 解析请求数据
            String name = (String) data.get("name");
            String description = (String) data.get("description");
            Integer sortOrder = data.get("sort_order") != null ? 
                    Integer.parseInt(data.get("sort_order").toString()) : 0;
            
            // 2. 参数验证
            if (name == null || name.trim().isEmpty()) {
                logger.warn("创建分类失败: 分类名称不能为空");
                return Result.failure(6147, "分类名称不能为空");
            }
            
            // 3. 检查分类名称是否已存在
            List<HomeContent> existingCategories = homeContentMapper.selectBySection("category", null);
            boolean nameExists = existingCategories.stream()
                    .anyMatch(c -> name.trim().equals(c.getTitle()));
            if (nameExists) {
                logger.warn("创建分类失败: 分类名称已存在, name: {}", name);
                return Result.failure(6147, "分类名称已存在");
            }
            
            // 4. 创建分类实体
            HomeContent category = new HomeContent();
            category.setSection("category");
            category.setTitle(name.trim());
            category.setSubTitle(description);
            category.setSortOrder(sortOrder);
            category.setStatus(1); // 默认启用
            category.setCreateTime(new Date());
            category.setUpdateTime(new Date());
            
            // 5. 保存到数据库
            int result = homeContentMapper.insert(category);
            if (result <= 0) {
                logger.error("创建分类失败: 数据库插入失败");
                return Result.failure(6147);
            }
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", category.getId());
            responseData.put("name", category.getTitle());
            responseData.put("description", category.getSubTitle());
            responseData.put("sort_order", category.getSortOrder());
            
            logger.info("创建分类成功: id={}, name={}", category.getId(), name);
            return Result.success(6030, responseData);
            
        } catch (Exception e) {
            logger.error("创建分类失败: 系统异常", e);
            return Result.failure(6147);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> updateCategory(String id, Map<String, Object> data) {
        try {
            logger.info("更新分类请求: id={}, data={}", id, data);
            
            // 1. 查询分类是否存在
            Integer categoryId = Integer.parseInt(id);
            HomeContent category = homeContentMapper.selectById(categoryId);
            
            if (category == null || !"category".equals(category.getSection())) {
                logger.warn("更新分类失败: 分类不存在, id: {}", id);
                return Result.failure(6149);
            }
            
            // 2. 解析请求数据
            String name = (String) data.get("name");
            String description = (String) data.get("description");
            Integer sortOrder = data.get("sort_order") != null ? 
                    Integer.parseInt(data.get("sort_order").toString()) : null;
            
            // 3. 如果要更新名称，检查是否与其他分类重复
            if (name != null && !name.trim().isEmpty()) {
                List<HomeContent> existingCategories = homeContentMapper.selectBySection("category", null);
                boolean nameExists = existingCategories.stream()
                        .anyMatch(c -> !c.getId().equals(categoryId) && name.trim().equals(c.getTitle()));
                if (nameExists) {
                    logger.warn("更新分类失败: 分类名称已存在, name: {}", name);
                    return Result.failure(6149, "分类名称已存在");
                }
                category.setTitle(name.trim());
            }
            
            // 4. 更新其他字段
            if (description != null) {
                category.setSubTitle(description);
            }
            if (sortOrder != null) {
                category.setSortOrder(sortOrder);
            }
            category.setUpdateTime(new Date());
            
            // 5. 保存到数据库
            int result = homeContentMapper.updateById(category);
            if (result <= 0) {
                logger.error("更新分类失败: 数据库更新失败, id: {}", id);
                return Result.failure(6149);
            }
            
            // 6. 构造返回数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", category.getId());
            responseData.put("name", category.getTitle());
            responseData.put("description", category.getSubTitle());
            responseData.put("sort_order", category.getSortOrder());
            
            logger.info("更新分类成功: id={}", id);
            return Result.success(6031, responseData);
            
        } catch (NumberFormatException e) {
            logger.error("更新分类失败: ID格式错误, id: {}", id, e);
            return Result.failure(6149);
        } catch (Exception e) {
            logger.error("更新分类失败: 系统异常, id: {}", id, e);
            return Result.failure(6149);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteCategory(String id) {
        try {
            logger.info("删除分类请求: id={}", id);
            
            // 1. 查询分类是否存在
            Integer categoryId = Integer.parseInt(id);
            HomeContent category = homeContentMapper.selectById(categoryId);
            
            if (category == null || !"category".equals(category.getSection())) {
                logger.warn("删除分类失败: 分类不存在, id: {}", id);
                return Result.failure(6151);
            }
            
            // 2. 检查是否有文章使用该分类
            String categoryName = category.getTitle();
            List<TeaArticle> articlesInCategory = articleMapper.selectByCategory(categoryName);
            if (articlesInCategory != null && !articlesInCategory.isEmpty()) {
                logger.warn("删除分类失败: 该分类下还有{}篇文章, id: {}", articlesInCategory.size(), id);
                return Result.failure(6151);
            }
            
            // 3. 删除分类
            int result = homeContentMapper.deleteById(categoryId);
            if (result <= 0) {
                logger.error("删除分类失败: 数据库删除失败, id: {}", id);
                return Result.failure(6148);
            }
            
            logger.info("删除分类成功: id={}", id);
            return Result.success(6032);
            
        } catch (NumberFormatException e) {
            logger.error("删除分类失败: ID格式错误, id: {}", id, e);
            return Result.failure(6151);
        } catch (Exception e) {
            logger.error("删除分类失败: 系统异常, id: {}", id, e);
            return Result.failure(6151);
        }
    }
} 