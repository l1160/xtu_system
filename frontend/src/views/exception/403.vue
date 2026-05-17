<template>
    <div class="exception-page">
        <div class="exception-card page-card">
            <h1>403</h1>
            <p>当前账号没有访问该页面的权限。</p>
            <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
        </div>
    </div>
</template>

<style scoped>
.exception-page {
    min-height: 100vh;
    display: grid;
    place-items: center;
    padding: 24px;
}

.exception-card {
    width: min(420px, 100%);
    text-align: center;
    padding: 48px 32px;
}

.exception-card h1 {
    margin: 0;
    font-size: 64px;
}

.exception-card p {
    margin: 12px 0 24px;
    color: #64748b;
}
</style>
