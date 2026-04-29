<template>
    <div class="login-page">
        <div class="login-panel page-card">
            <section class="intro">
                <div class="eyebrow">XTU SYSTEM</div>
                <h1>综合信息管理系统</h1>
                <p>
                    统一账号、统一权限、统一数据入口。当前脚手架已接入登录、工作台和用户管理示例模块。
                </p>
                <ul>
                    <li>账号：`admin`</li>
                    <li>密码：`admin123`</li>
                </ul>
            </section>

            <section class="form-section">
                <h2>登录系统</h2>
                <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
                    <el-form-item label="账号" prop="username">
                        <el-input v-model="form.username" placeholder="请输入账号" />
                    </el-form-item>
                    <el-form-item label="密码" prop="password">
                        <el-input
                            v-model="form.password"
                            type="password"
                            show-password
                            placeholder="请输入密码"
                            @keyup.enter="handleSubmit"
                        />
                    </el-form-item>
                    <el-button type="primary" class="submit-button" :loading="submitting" @click="handleSubmit">
                        立即登录
                    </el-button>
                </el-form>
            </section>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref()
const submitting = ref(false)

const form = reactive({
    username: 'admin',
    password: 'admin123'
})

const rules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        const loginResponse = await login(form)
        authStore.saveToken(loginResponse.data.token)
        await authStore.fetchCurrentUser()
        ElMessage.success('登录成功')
        router.push(route.query.redirect || '/dashboard')
    } finally {
        submitting.value = false
    }
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 24px;
}

.login-panel {
    width: min(1080px, 100%);
    display: grid;
    grid-template-columns: 1.15fr 0.85fr;
    overflow: hidden;
}

.intro {
    padding: 56px;
    background:
        radial-gradient(circle at top right, rgba(255, 255, 255, 0.32), transparent 36%),
        linear-gradient(135deg, #115e59 0%, #0f766e 45%, #14b8a6 100%);
    color: #f8fafc;
}

.eyebrow {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.18);
    font-size: 12px;
    letter-spacing: 1.5px;
}

.intro h1 {
    margin: 18px 0 12px;
    font-size: 40px;
    line-height: 1.15;
}

.intro p {
    max-width: 420px;
    line-height: 1.8;
    color: rgba(248, 250, 252, 0.84);
}

.intro ul {
    margin: 28px 0 0;
    padding-left: 20px;
    line-height: 1.9;
}

.form-section {
    padding: 56px 48px;
    background: rgba(255, 255, 255, 0.92);
}

.form-section h2 {
    margin: 0 0 28px;
    font-size: 28px;
}

.submit-button {
    width: 100%;
    margin-top: 8px;
}

@media (max-width: 900px) {
    .login-panel {
        grid-template-columns: 1fr;
    }
}
</style>
