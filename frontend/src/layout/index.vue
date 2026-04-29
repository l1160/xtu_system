<template>
    <div class="layout-shell">
        <aside class="sidebar page-card">
            <div class="brand">
                <div class="brand-mark">XTU</div>
                <div>
                    <div class="brand-title">综合信息系统</div>
                    <div class="brand-subtitle">Integrated Information System</div>
                </div>
            </div>

            <el-menu :default-active="route.path" class="menu" router unique-opened>
                <SidebarMenuItem
                    v-for="item in menuTree"
                    :key="item.id"
                    :item="item"
                />
            </el-menu>
        </aside>

        <section class="main-area">
            <header class="header page-card">
                <div>
                    <div class="header-title">{{ currentTitle }}</div>
                    <div class="header-subtitle">保持模块边界清晰，优先完成核心 CRUD 主链路。</div>
                </div>
                <div class="header-actions">
                    <div class="user-meta">
                        <span class="user-name">{{ authStore.user?.realName || '未登录用户' }}</span>
                        <span class="user-role">{{ authStore.user?.deptName || '系统管理部' }}</span>
                    </div>
                    <el-button plain @click="handleOpenProfile">修改密码</el-button>
                    <el-button type="primary" plain @click="handleLogout">退出</el-button>
                </div>
            </header>

            <main class="content">
                <router-view />
            </main>
        </section>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import SidebarMenuItem from '@/components/sidebar_menu_item.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const currentTitle = computed(() => route.meta.title || '综合信息系统')
const menuTree = computed(() => authStore.menus || [])

async function handleLogout() {
    try {
        await logout()
    } catch (error) {
        // 退出接口失败时仍清理本地状态，避免卡住页面。
    }
    authStore.logout()
    ElMessage.success('已退出登录')
    window.location.href = '/login'
}

function handleOpenProfile() {
    router.push('/profile')
}
</script>

<style scoped>
.layout-shell {
    display: grid;
    grid-template-columns: 280px 1fr;
    min-height: 100vh;
    gap: 20px;
    padding: 20px;
}

.sidebar {
    padding: 24px 16px;
}

.brand {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 28px;
    padding: 4px 8px;
}

.brand-mark {
    width: 52px;
    height: 52px;
    border-radius: 18px;
    background: linear-gradient(135deg, #0f766e, #14b8a6);
    color: #fff;
    display: grid;
    place-items: center;
    font-weight: 800;
    letter-spacing: 1px;
}

.brand-title {
    font-size: 18px;
    font-weight: 700;
}

.brand-subtitle {
    margin-top: 4px;
    font-size: 12px;
    color: #64748b;
}

.menu {
    border-right: none;
    background: transparent;
}

.main-area {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 22px 24px;
}

.header-title {
    font-size: 22px;
    font-weight: 700;
}

.header-subtitle {
    margin-top: 6px;
    font-size: 13px;
    color: #64748b;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.user-meta {
    display: flex;
    flex-direction: column;
    text-align: right;
}

.user-name {
    font-weight: 600;
}

.user-role {
    margin-top: 4px;
    font-size: 12px;
    color: #64748b;
}

.content {
    min-height: 0;
}

@media (max-width: 960px) {
    .layout-shell {
        grid-template-columns: 1fr;
    }
}
</style>
