<template>
    <el-dialog
        :title="dialogTitle"
        :model-value="modelValue"
        width="720px"
        :close-on-click-modal="false"
        destroy-on-close
        @close="handleClose"
        @update:modelValue="emitVisible"
    >
        <el-form
            ref="postFormRef"
            :model="postForm"
            :rules="postRules"
            label-width="80px"
        >
            <el-form-item label="标题" prop="title">
                <el-input
                    v-model="postForm.title"
                    placeholder="请输入帖子标题（5-100字）"
                    maxlength="100"
                    show-word-limit
                />
            </el-form-item>

            <el-form-item label="分类" prop="topicId">
                <el-select v-model="postForm.topicId" placeholder="请选择分类" style="width: 100%">
                    <el-option
                        v-for="topic in topicList"
                        :key="topic.id"
                        :label="topic.name"
                        :value="topic.id"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="摘要">
                <el-input
                    v-model="postForm.summary"
                    type="textarea"
                    :rows="3"
                    maxlength="200"
                    show-word-limit
                    placeholder="可选：不填时将自动从正文前200字生成"
                />
            </el-form-item>

            <el-form-item label="内容" prop="content">
                <QuillEditor
                    v-model:content="postForm.content"
                    contentType="html"
                    :options="postEditorOptions"
                    style="height: 320px;width: 100%;"
                    @ready="onEditorReady"
                />
            </el-form-item>

            <el-form-item label="封面图">
                <el-upload
                    class="cover-uploader"
                    :show-file-list="false"
                    :http-request="handleCoverUpload"
                    accept="image/*"
                >
                    <SafeImage
                        v-if="postForm.coverImage"
                        :src="postForm.coverImage"
                        type="post"
                        alt="封面图片"
                        class="cover-image-preview"
                    />
                    <template v-else>
                        <el-icon class="cover-uploader-icon"><Plus /></el-icon>
                        <div class="cover-uploader-text">上传封面（可选）</div>
                    </template>
                </el-upload>
                <div class="el-upload__tip">
                    建议比例 16:9，用于列表和详情页顶部展示
                </div>
            </el-form-item>
        </el-form>

        <template #footer>
            <span class="dialog-footer">
                <el-button @click="emitVisible(false)" :disabled="submitting">取消</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitting">
                    {{ submitText }}
                </el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script setup>
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import SafeImage from '@/components/common/form/SafeImage.vue'
import { useForumStore } from '@/stores/forum'
import { showByCode } from '@/utils/apiMessages'

defineOptions({
    name: 'PostEditorDialog'
})

const props = defineProps({
    modelValue: {
        type: Boolean,
        required: true
    },
    mode: {
        type: String,
        default: 'create', // 'create' | 'edit'
        validator: (v) => ['create', 'edit'].includes(v)
    },
    postId: {
        type: [String, Number],
        default: null
    },
    topicList: {
        type: Array,
        default: () => []
    },
    /**
     * 打开弹窗时预填数据（编辑模式会优先用 postId 拉取详情覆盖它）
     * 支持字段：title, content, summary, topicId, coverImage
     */
    initialData: {
        type: Object,
        default: () => ({})
    },
    /**
     * 发布时默认选中的 topicId（比如当前处于某个版块）
     */
    defaultTopicId: {
        type: [String, Number, null],
        default: null
    }
})

const emit = defineEmits(['update:modelValue', 'submitted'])

const forumStore = useForumStore()

const postFormRef = ref(null)
const submitting = ref(false)
let quillInstance = null

const postForm = reactive({
    title: '',
    topicId: null,
    summary: '',
    content: '',
    coverImage: ''
})

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑帖子' : '发布新帖'))
const submitText = computed(() => (props.mode === 'edit' ? '保存' : '发布'))

const postRules = {
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 5, max: 100, message: '标题长度应为5-100字', trigger: 'blur' }
    ],
    topicId: [
        { required: true, message: '请选择分类', trigger: 'change' }
    ],
    content: [
        { required: true, message: '请输入内容', trigger: 'blur' }
    ]
}

const emitVisible = (val) => {
    emit('update:modelValue', val)
}

const onEditorReady = (quill) => {
    quillInstance = quill
}

const extractPlainText = (html) => {
    if (!html) return ''
    const div = document.createElement('div')
    div.innerHTML = html
    return div.textContent || div.innerText || ''
}

const extractImageUrls = (html) => {
    if (!html) return []
    const div = document.createElement('div')
    div.innerHTML = html
    const imgs = Array.from(div.querySelectorAll('img'))
    const urls = imgs.map(img => img.getAttribute('src')).filter(u => !!u)
    return Array.from(new Set(urls))
}

const handleCoverUpload = async ({ file }) => {
    if (!file) return
    try {
        const res = await forumStore.uploadPostImage(file)
        if (res?.code) showByCode(res.code)
        const url = res?.data?.url
        const path = res?.data?.path
        // 存库使用相对路径，预览仍然可以使用完整URL
        if (path) {
            postForm.coverImage = path
        } else if (url) {
            postForm.coverImage = url
        }
    } catch (e) {
        ElMessage.error('封面上传失败，请重试')
    }
}

const handleEditorImageUpload = async () => {
    if (!quillInstance) return
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = async () => {
        const file = input.files && input.files[0]
        if (!file) return
        try {
            const res = await forumStore.uploadPostImage(file)
            if (res?.code) showByCode(res.code)
            const url = res?.data?.url
            if (!url) return
            const range = quillInstance.getSelection(true)
            const index = range && range.index != null ? range.index : quillInstance.getLength() - 1
            quillInstance.insertEmbed(index, 'image', url)
            quillInstance.setSelection(index + 1)
            quillInstance.update()
        } catch (e) {
            ElMessage.error('图片上传失败，请重试')
        }
    }
    input.click()
}

const postEditorOptions = computed(() => ({
    theme: 'snow',
    modules: {
        toolbar: {
            container: [
                ['bold', 'italic', 'underline', 'strike'],
                [{ header: 1 }, { header: 2 }],
                [{ list: 'ordered' }, { list: 'bullet' }],
                [{ indent: '-1' }, { indent: '+1' }],
                [{ size: ['small', false, 'large', 'huge'] }],
                [{ color: [] }, { background: [] }],
                [{ align: [] }],
                ['link', 'image'],
                ['clean']
            ],
            handlers: {
                image: handleEditorImageUpload
            }
        }
    },
    placeholder: '请输入帖子内容，可在正文中插入图片...'
}))

const applyInitialData = () => {
    const base = props.initialData || {}
    postForm.title = base.title || ''
    postForm.summary = base.summary || ''
    postForm.content = base.content || ''
    postForm.coverImage = base.coverImage || ''
    postForm.topicId = base.topicId ?? (props.defaultTopicId && props.defaultTopicId !== 'all' ? props.defaultTopicId : null)
}

const loadPostDetailForEdit = async () => {
    if (props.mode !== 'edit') return
    if (!props.postId) return
    const res = await forumStore.fetchPostDetail(props.postId)
    const post = res?.data || forumStore.currentPost || {}
    // 后端字段：topicId/title/content/summary/coverImage
    postForm.title = post.title || ''
    postForm.summary = post.summary || ''
    postForm.content = post.content || ''
    postForm.coverImage = post.coverImage || ''
    postForm.topicId = post.topicId ?? post.topic?.id ?? null
}

watch(
    () => props.modelValue,
    async (visible) => {
        if (!visible) return
        // 打开弹窗：先填一轮初始数据，再在编辑模式下拉取详情覆盖
        applyInitialData()
        await nextTick()
        if (props.mode === 'edit') {
            try {
                await loadPostDetailForEdit()
            } catch (e) {
                showByCode(6122)
            }
        }
    }
)

const handleSubmit = async () => {
    if (!postFormRef.value) return
    const valid = await postFormRef.value.validate().catch(() => false)
    if (!valid) return

    submitting.value = true
    try {
        const imageUrls = extractImageUrls(postForm.content)
        const plainText = extractPlainText(postForm.content)
        const autoSummary = plainText.slice(0, 200)

        const payload = {
            title: postForm.title,
            topicId: postForm.topicId ? parseInt(String(postForm.topicId), 10) : null,
            content: postForm.content,
            summary: postForm.summary || autoSummary || null,
            coverImage: postForm.coverImage || (imageUrls.length > 0 ? imageUrls[0] : null),
            images: imageUrls.length > 0 ? JSON.stringify(imageUrls) : null
        }

        let res
        if (props.mode === 'edit') {
            if (!props.postId) {
                ElMessage.error('帖子ID缺失，无法保存')
                return
            }
            res = await forumStore.updatePost(props.postId, payload)
        } else {
            res = await forumStore.createPost(payload)
        }

        if (res?.code) showByCode(res.code)

        // 成功：create=6011（提交审核），edit=6012（更新成功）
        if ((props.mode === 'create' && res?.code === 6011) || (props.mode === 'edit' && res?.code === 6012)) {
            emit('submitted', { mode: props.mode, postId: props.postId || null })
            emitVisible(false)
        }
    } finally {
        submitting.value = false
    }
}

const resetState = () => {
    quillInstance = null
    if (postFormRef.value) {
        postFormRef.value.resetFields()
    }
    postForm.title = ''
    postForm.topicId = null
    postForm.summary = ''
    postForm.content = ''
    postForm.coverImage = ''
}

const handleClose = () => {
    resetState()
}
</script>

<style lang="scss" scoped>
.cover-uploader {
    .cover-image-preview {
        width: 160px;
        height: 90px;
        border-radius: 6px;
        border: 1px solid #ebeef5;
        object-fit: cover;
        display: block;
    }

    .cover-uploader-icon {
        font-size: 24px;
        color: #909399;
    }

    .cover-uploader-text {
        margin-top: 6px;
        font-size: 12px;
        color: #909399;
    }
}
</style>
