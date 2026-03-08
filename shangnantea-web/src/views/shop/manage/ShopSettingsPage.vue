<template>
  <div class="shop-settings-page">
    <div class="container">
      <!-- 上半部分：店铺基础信息 -->
      <el-card class="basic-info-card" v-loading="loading">
        <h2 class="section-title">店铺基础信息</h2>
        <el-form
          :model="basicForm"
          label-width="100px"
          class="basic-form"
        >
          <el-form-item label="店铺名称">
            <el-input v-model="basicForm.name" placeholder="请输入店铺名称" maxlength="50" />
          </el-form-item>
          <el-form-item label="店铺简介">
            <el-input
              v-model="basicForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入店铺简介"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSaveBasicInfo" :loading="savingBasic">
              保存基础信息
            </el-button>
            <el-button @click="handleResetBasicInfo">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 下半部分：导航 + 轮播图 / 公告管理 -->
      <el-card class="settings-card">
        <div class="settings-header">
          <el-menu
            :default-active="activeTab"
            mode="horizontal"
            class="settings-menu"
            @select="handleTabChange"
          >
            <el-menu-item index="banners">
              <el-icon><Picture /></el-icon>
              <span>轮播图管理</span>
            </el-menu-item>
            <el-menu-item index="announcements">
              <el-icon><Document /></el-icon>
              <span>公告管理</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="settings-body">
          <!-- 轮播图管理 -->
          <div v-if="activeTab === 'banners'">
            <div class="settings-section-header">
              <span class="section-sub-title">店铺轮播图</span>
              <el-button type="primary" size="small" @click="showAddBannerDialog">
                <el-icon><Plus /></el-icon>
                添加Banner
              </el-button>
            </div>

            <div class="banner-manage-content" v-loading="bannerLoading">
              <div v-if="shopBanners.length === 0" class="empty-banner">
                <el-empty description="暂无Banner，点击上方按钮添加" />
              </div>

              <div v-else class="banner-list">
                <div
                  v-for="(banner, index) in shopBanners"
                  :key="banner.id"
                  class="banner-item"
                >
                  <div class="banner-preview">
                    <SafeImage
                      :src="banner.imageUrl"
                      type="banner"
                      :alt="banner.title"
                      style="width: 100%; height: 150px; object-fit: cover; border-radius: 4px;"
                    />
                    <div class="banner-info">
                      <div class="banner-title">{{ banner.title || '未命名Banner' }}</div>
                      <div class="banner-link">{{ banner.linkUrl || '无链接' }}</div>
                      <div class="banner-order">排序：{{ banner.sortOrder || index + 1 }}</div>
                    </div>
                  </div>

                  <div class="banner-actions">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="showEditBannerDialog(banner)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteBanner(banner)"
                    >
                      删除
                    </el-button>
                    <div class="banner-order-controls">
                      <el-button
                        type="info"
                        link
                        size="small"
                        :disabled="index === 0"
                        @click="moveBanner(index, 'up')"
                      >
                        ↑
                      </el-button>
                      <el-button
                        type="info"
                        link
                        size="small"
                        :disabled="index === shopBanners.length - 1"
                        @click="moveBanner(index, 'down')"
                      >
                        ↓
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 公告管理 -->
          <div v-else-if="activeTab === 'announcements'">
            <div class="settings-section-header">
              <span class="section-sub-title">店铺公告</span>
              <el-button type="primary" size="small" @click="showAddAnnouncementDialog">
                <el-icon><Plus /></el-icon>
                添加公告
              </el-button>
            </div>

            <div class="announcement-manage-content" v-loading="announcementLoading">
              <div v-if="shopAnnouncements.length === 0" class="empty-announcement">
                <el-empty description="暂无公告，点击上方按钮添加" />
              </div>

              <div v-else class="announcement-list">
                <div
                  v-for="announcement in shopAnnouncements"
                  :key="announcement.id"
                  class="announcement-item"
                  :class="{ 'is-top': announcement.isTop }"
                >
                  <div class="announcement-header">
                    <div class="announcement-title">
                      <el-tag v-if="announcement.isTop" type="warning" size="small">置顶</el-tag>
                      <span>{{ announcement.title }}</span>
                    </div>
                    <div class="announcement-time">
                      {{ formatTime(announcement.createTime) }}
                    </div>
                  </div>

                  <div class="announcement-content">
                    {{ announcement.content }}
                  </div>

                  <div class="announcement-actions">
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="showEditAnnouncementDialog(announcement)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteAnnouncement(announcement)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Banner 表单对话框 -->
      <el-dialog
        v-model="bannerDialogVisible"
        :title="isEditBanner ? '编辑Banner' : '添加Banner'"
        width="600px"
        destroy-on-close
      >
        <el-form
          ref="bannerFormRef"
          :model="currentBanner"
          label-width="100px"
          v-if="currentBanner"
        >
          <el-form-item label="Banner标题" prop="title">
            <el-input v-model="currentBanner.title" placeholder="请输入Banner标题" maxlength="50" />
          </el-form-item>

          <el-form-item label="Banner图片" prop="image_url">
            <el-upload
              class="banner-uploader"
              :show-file-list="false"
              :http-request="handleBannerImageUpload"
              accept="image/*"
            >
              <img v-if="currentBanner.image_url" :src="currentBanner.image_url" class="banner-image" />
              <el-icon v-else class="banner-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item label="链接地址" prop="link_url">
            <el-input v-model="currentBanner.link_url" placeholder="请输入链接地址（可选）" />
          </el-form-item>

          <el-form-item label="排序" prop="order">
            <el-input-number v-model="currentBanner.order" :min="1" :max="10" />
          </el-form-item>

          <el-form-item label="启用状态" prop="is_enabled">
            <el-switch v-model="currentBanner.is_enabled" :active-value="1" :inactive-value="0" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="bannerDialogVisible = false" :disabled="savingBanner">取消</el-button>
          <el-button
            type="primary"
            :loading="savingBanner"
            :disabled="savingBanner"
            @click="handleSaveBanner"
          >
            保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 公告表单对话框 -->
      <el-dialog
        v-model="announcementDialogVisible"
        :title="isEditAnnouncement ? '编辑公告' : '添加公告'"
        width="700px"
        destroy-on-close
      >
        <el-form
          ref="announcementFormRef"
          :model="currentAnnouncement"
          label-width="100px"
          v-if="currentAnnouncement"
        >
          <el-form-item label="公告标题" prop="title">
            <el-input v-model="currentAnnouncement.title" placeholder="请输入公告标题" maxlength="100" />
          </el-form-item>

          <el-form-item label="公告内容" prop="content">
            <el-input
              v-model="currentAnnouncement.content"
              type="textarea"
              :rows="6"
              placeholder="请输入公告内容"
              maxlength="1000"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="置顶" prop="isTop">
            <el-switch v-model="currentAnnouncement.isTop" :active-value="true" :inactive-value="false" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="announcementDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveAnnouncement">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture, Document } from '@element-plus/icons-vue'

import SafeImage from '@/components/common/form/SafeImage.vue'
import { useShopStore } from '@/stores/shop'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { shopPromptMessages } from '@/utils/promptMessages'

const router = useRouter()
const shopStore = useShopStore()

// 店铺基础信息
const loading = computed(() => shopStore.loading)
const shop = computed(() => shopStore.myShop)

const basicForm = reactive({
  name: '',
  description: ''
})

const savingBasic = ref(false)

const initBasicForm = () => {
  if (!shop.value) return
  basicForm.name = shop.value.name || shop.value.shopName || ''
  basicForm.description = shop.value.description || shop.value.desc || ''
}

const handleSaveBasicInfo = async () => {
  if (!shop.value || !shop.value.id) {
    shopPromptMessages.showShopInfoLoadFirst()
    return
  }

  try {
    savingBasic.value = true
    const res = await shopStore.updateShop({
      id: shop.value.id,
      name: basicForm.name,
      description: basicForm.description
    })
    showByCode(res.code)
    if (isSuccess(res.code)) {
      await shopStore.fetchMyShop()
      initBasicForm()
    }
  } catch (error) {
    console.error('保存店铺基础信息失败:', error)
  } finally {
    savingBasic.value = false
  }
}

const handleResetBasicInfo = () => {
  initBasicForm()
}

// 下半部分导航
const activeTab = ref('banners')

const handleTabChange = key => {
  activeTab.value = key
}

// Banner 管理
const bannerLoading = ref(false)
const savingBanner = ref(false)
const shopBanners = computed(() => shopStore.shopBanners || [])
const bannerDialogVisible = ref(false)
const currentBanner = ref(null)
const isEditBanner = ref(false)
const bannerFormRef = ref(null)
const bannerUploadFile = ref(null)

const loadBanners = async () => {
  if (!shop.value || !shop.value.id) return
  bannerLoading.value = true
  try {
    const res = await shopStore.fetchShopBanners(shop.value.id)
    if (res && !isSuccess(res.code)) {
      showByCode(res.code)
    }
  } catch (error) {
    console.error('加载Banner列表失败:', error)
  } finally {
    bannerLoading.value = false
  }
}

const showAddBannerDialog = () => {
  isEditBanner.value = false
  bannerUploadFile.value = null
  currentBanner.value = {
    image_url: '',
    link_url: '',
    title: '',
    order: shopBanners.value.length + 1,
    is_enabled: 1
  }
  bannerDialogVisible.value = true
}

const showEditBannerDialog = banner => {
  isEditBanner.value = true
  bannerUploadFile.value = null
  // 将后端 BannerVO 映射为表单使用的数据结构
  currentBanner.value = {
    id: banner.id,
    image_url: banner.imageUrl,
    link_url: banner.linkUrl,
    title: banner.title,
    order: banner.sortOrder ?? 0,
    is_enabled: 1
  }
  bannerDialogVisible.value = true
}

const handleBannerImageUpload = async ({ file }) => {
  bannerUploadFile.value = file
  if (currentBanner.value) {
    currentBanner.value.image_url = URL.createObjectURL(file)
  }
}

const handleSaveBanner = async () => {
  if (!shop.value || !shop.value.id) {
    showByCode(4103)
    return
  }
  // 防止重复点击
  if (savingBanner.value) {
    return
  }
  try {
    savingBanner.value = true
    let res
    if (isEditBanner.value) {
      // 后端更新接口只支持 title/linkUrl/sortOrder
      res = await shopStore.updateShopBanner({
        bannerId: currentBanner.value.id,
        bannerData: {
          title: currentBanner.value.title || '',
          linkUrl: currentBanner.value.link_url || '',
          sortOrder: currentBanner.value.order || 0
        }
      })
    } else {
      if (!bannerUploadFile.value) {
        ElMessage.warning('请上传Banner图片')
        return
      }
      res = await shopStore.uploadShopBanner({
        file: bannerUploadFile.value,
        title: currentBanner.value.title || '',
        linkUrl: currentBanner.value.link_url || '',
        sortOrder: currentBanner.value.order || null
      })
    }
    showByCode(res.code)
    if (isSuccess(res.code)) {
      bannerDialogVisible.value = false
      bannerUploadFile.value = null
      await loadBanners()
    }
  } catch (error) {
    console.error('保存Banner失败:', error)
  } finally {
    savingBanner.value = false
  }
}

// dialog 关闭时清理本地文件引用
watch(bannerDialogVisible, val => {
  if (!val) {
    bannerUploadFile.value = null
  }
})

const handleDeleteBanner = async banner => {
  try {
    await ElMessageBox.confirm(
      `确定要删除Banner"${banner.title || '未命名'}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await shopStore.deleteBanner({
      bannerId: banner.id,
      shopId: shop.value.id
    })
    showByCode(res.code)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除Banner失败:', error)
    }
  }
}

const moveBanner = async (index, direction) => {
  if (!shop.value || !shop.value.id) return
  const banners = [...shopBanners.value]
  const newIndex = direction === 'up' ? index - 1 : index + 1
  if (newIndex < 0 || newIndex >= banners.length) return
  ;[banners[index], banners[newIndex]] = [banners[newIndex], banners[index]]
  const orderData = banners.map((banner, i) => ({
    id: banner.id,
    order: i + 1
  }))
  try {
    const res = await shopStore.updateBannerOrder({
      orderData,
      shopId: shop.value.id
    })
    showByCode(res.code)
  } catch (error) {
    console.error('更新Banner排序失败:', error)
  }
}

// 公告管理
const announcementLoading = ref(false)
const shopAnnouncements = computed(() => shopStore.shopAnnouncements || [])
const announcementDialogVisible = ref(false)
const currentAnnouncement = ref(null)
const isEditAnnouncement = ref(false)
const announcementFormRef = ref(null)

const loadAnnouncements = async () => {
  if (!shop.value || !shop.value.id) return
  announcementLoading.value = true
  try {
    const res = await shopStore.fetchShopAnnouncements(shop.value.id)
    if (res && !isSuccess(res.code)) {
      showByCode(res.code)
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
  } finally {
    announcementLoading.value = false
  }
}

const showAddAnnouncementDialog = () => {
  isEditAnnouncement.value = false
  currentAnnouncement.value = {
    title: '',
    content: '',
    isTop: false
  }
  announcementDialogVisible.value = true
}

const showEditAnnouncementDialog = announcement => {
  isEditAnnouncement.value = true
  currentAnnouncement.value = { ...announcement }
  announcementDialogVisible.value = true
}

const handleSaveAnnouncement = async () => {
  if (!shop.value || !shop.value.id) {
    showByCode(4103)
    return
  }
  try {
    const announcementData = {
      title: currentAnnouncement.value.title,
      content: currentAnnouncement.value.content,
      isTop: !!currentAnnouncement.value.isTop
    }
    let res
    if (isEditAnnouncement.value) {
      res = await shopStore.updateAnnouncement({
        announcementId: currentAnnouncement.value.id,
        announcementData
      })
    } else {
      res = await shopStore.createAnnouncement({
        shopId: shop.value.id,
        announcementData
      })
    }
    showByCode(res.code)
    if (isSuccess(res.code)) {
      announcementDialogVisible.value = false
      await loadAnnouncements()
    }
  } catch (error) {
    console.error('保存公告失败:', error)
  }
}

const handleDeleteAnnouncement = async announcement => {
  try {
    await ElMessageBox.confirm(
      `确定要删除公告"${announcement.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await shopStore.deleteAnnouncement(announcement.id)
    showByCode(res.code)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
    }
  }
}

const formatTime = time => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(async () => {
  // 确保店铺信息已加载
  if (!shop.value) {
    await shopStore.fetchMyShop()
  }
  initBasicForm()
  await loadBanners()
  await loadAnnouncements()
})
</script>

<style lang="scss" scoped>
.shop-settings-page {
  padding: 40px 0;
  min-height: 100vh;
  background-color: #f5f7fa;

  .container {
    width: 85%;
    max-width: 1920px;
    margin: 0 auto;
  }
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.section-sub-title {
  font-size: 16px;
  font-weight: 500;
}

.basic-info-card {
  margin-bottom: 24px;

  .basic-form {
    max-width: 600px;
  }
}

.settings-card {
  .settings-header {
    margin-bottom: 16px;
  }

  .settings-menu {
    border-bottom: none;
  }

  .settings-body {
    margin-top: 8px;
  }
}

.settings-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.banner-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.banner-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}

.banner-info {
  margin-top: 8px;
}

.banner-title {
  font-weight: 500;
}

.banner-link {
  font-size: 12px;
  color: #909399;
}

.banner-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.banner-uploader {
  .banner-image {
    width: 100%;
    max-height: 260px;
    object-fit: cover;
    border-radius: 4px;
    display: block;
  }

  .banner-uploader-icon {
    font-size: 32px;
    color: #909399;
  }
}

.banner-order-controls {
  display: flex;
  gap: 4px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;

  &.is-top {
    border-color: #f0ad4e;
    background-color: #fff8e6;
  }
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.announcement-title span {
  margin-left: 4px;
  font-weight: 500;
}

.announcement-time {
  font-size: 12px;
  color: #909399;
}

.announcement-actions {
  margin-top: 8px;
}
</style>

