<template>
    <div class="profile-page">
        <section class="page-card intro-card">
            <div>
                <h1 class="page-title">个人中心</h1>
                <p class="page-subtitle">当前页面用于维护个人登录密码。修改成功后会自动退出，请使用新密码重新登录。</p>
            </div>
        </section>

        <section class="content-grid">
            <article class="page-card profile-card">
                <div class="section-header">
                    <div>
                        <h2>当前账号</h2>
                        <p>确认当前登录身份后再执行改密操作。</p>
                    </div>
                </div>

                <div class="profile-meta">
                    <div class="meta-item">
                        <span class="meta-label">账号</span>
                        <span class="meta-value">{{ authStore.user?.username || '-' }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">姓名</span>
                        <span class="meta-value">{{ authStore.user?.realName || '-' }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">部门</span>
                        <span class="meta-value">{{ authStore.user?.deptName || '-' }}</span>
                    </div>
                    <div class="meta-item">
                        <span class="meta-label">角色</span>
                        <span class="meta-value">{{ roleText }}</span>
                    </div>
                </div>
            </article>

            <article class="page-card password-card">
                <div class="section-header">
                    <div>
                        <h2>修改密码</h2>
                        <p>新密码不要与旧密码相同，确认后将立即更新。</p>
                    </div>
                </div>

                <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                    <el-form-item label="原密码" prop="oldPassword">
                        <el-input v-model="form.oldPassword" show-password type="password" />
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                        <el-input v-model="form.newPassword" show-password type="password" />
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmPassword">
                        <el-input v-model="form.confirmPassword" show-password type="password" />
                    </el-form-item>
                    <el-form-item>
                        <el-button @click="resetForm">重置</el-button>
                        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认修改</el-button>
                    </el-form-item>
                </el-form>
            </article>
        </section>
    </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout, updatePassword } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

const roleText = computed(() => {
    const roles = authStore.user?.roles || []
    return roles.length ? roles.join(' / ') : '-'
})

const rules = {
    oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (value !== form.newPassword) {
                    callback(new Error('两次输入的新密码不一致'))
                    return
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
}

function resetForm() {
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    formRef.value?.clearValidate()
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        await updatePassword(form)
        try {
            await logout()
        } catch (error) {
            // 改密成功后即使退出接口失败，也继续清理本地登录态。
        }
        authStore.logout()
        ElMessage.success('密码修改成功，请使用新密码重新登录')
        router.replace('/login')
    } finally {
        submitting.value = false
    }
}
</script>

<style scoped>
.profile-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.intro-card {
    padding: 24px 28px;
}

.content-grid {
    display: grid;
    grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
    gap: 20px;
}

.profile-card,
.password-card {
    padding: 24px;
}

.section-header {
    margin-bottom: 20px;
}

.section-header h2 {
    margin: 0;
    font-size: 20px;
}

.section-header p {
    margin: 8px 0 0;
    color: #64748b;
    font-size: 13px;
}

.profile-meta {
    display: grid;
    gap: 14px;
}

.meta-item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 14px 16px;
    border-radius: 16px;
    background: rgba(15, 118, 110, 0.06);
    border: 1px solid rgba(15, 118, 110, 0.12);
}

.meta-label {
    font-size: 12px;
    color: #64748b;
}

.meta-value {
    font-size: 15px;
    font-weight: 600;
    word-break: break-all;
}

@media (max-width: 960px) {
    .content-grid {
        grid-template-columns: 1fr;
    }
}
</style>
