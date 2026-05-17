<template>
    <div class="log-page">
        <section class="page-card summary-card">
            <div>
                <h1 class="page-title">日志管理</h1>
                <p class="page-subtitle">查看登录轨迹与系统操作留痕，辅助排查权限与数据变更问题。</p>
            </div>

            <div class="summary-grid">
                <article class="summary-item">
                    <span class="summary-label">登录日志</span>
                    <strong class="summary-value">{{ loginTotal }}</strong>
                </article>
                <article class="summary-item">
                    <span class="summary-label">操作日志</span>
                    <strong class="summary-value">{{ operationTotal }}</strong>
                </article>
            </div>
        </section>

        <section class="page-card table-card">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane v-if="hasPermission('system:log:login:view')" label="登录日志" name="login">
                    <el-form :inline="true" :model="loginQuery" class="query-form">
                        <el-form-item>
                            <el-input v-model="loginQuery.keyword" clearable placeholder="账号或姓名" />
                        </el-form-item>
                        <el-form-item>
                            <el-select v-model="loginQuery.loginStatus" clearable placeholder="登录结果" style="width: 120px;">
                                <el-option :value="1" label="成功" />
                                <el-option :value="0" label="失败" />
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="loadLoginLogs">查询</el-button>
                            <el-button @click="resetLoginQuery">重置</el-button>
                        </el-form-item>
                    </el-form>

                    <el-table :data="loginLogList" stripe>
                        <el-table-column prop="username" label="登录账号" min-width="140" />
                        <el-table-column prop="realName" label="姓名" min-width="120" />
                        <el-table-column prop="loginIp" label="登录IP" min-width="140" />
                        <el-table-column prop="browser" label="浏览器" min-width="140" />
                        <el-table-column prop="os" label="操作系统" min-width="120" />
                        <el-table-column label="结果" width="100">
                            <template #default="{ row }">
                                <el-tag :type="row.loginStatus === 1 ? 'success' : 'danger'">
                                    {{ row.loginStatus === 1 ? '成功' : '失败' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="loginAt" label="登录时间" min-width="180" />
                    </el-table>

                    <div class="pager">
                        <el-pagination
                            background
                            layout="total, prev, pager, next"
                            :current-page="loginQuery.pageNum"
                            :page-size="loginQuery.pageSize"
                            :total="loginTotal"
                            @current-change="handleLoginPageChange"
                        />
                    </div>
                </el-tab-pane>

                <el-tab-pane v-if="hasPermission('system:log:operation:view')" label="操作日志" name="operation">
                    <el-form :inline="true" :model="operationQuery" class="query-form">
                        <el-form-item>
                            <el-input v-model="operationQuery.keyword" clearable placeholder="操作人、请求地址或业务类型" style="width: 260px;" />
                        </el-form-item>
                        <el-form-item>
                            <el-select v-model="operationQuery.moduleName" clearable placeholder="模块" style="width: 160px;">
                                <el-option label="用户" value="users" />
                                <el-option label="部门" value="departments" />
                                <el-option label="学生" value="students" />
                                <el-option label="教师" value="teachers" />
                                <el-option label="课程" value="courses" />
                                <el-option label="公告" value="notices" />
                                <el-option label="申请" value="applications" />
                                <el-option label="附件" value="attachment" />
                                <el-option label="流程" value="workflow" />
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-select v-model="operationQuery.resultStatus" clearable placeholder="执行结果" style="width: 120px;">
                                <el-option :value="1" label="成功" />
                                <el-option :value="0" label="失败" />
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-button type="primary" @click="loadOperationLogs">查询</el-button>
                            <el-button @click="resetOperationQuery">重置</el-button>
                        </el-form-item>
                    </el-form>

                    <el-table :data="operationLogList" stripe>
                        <el-table-column prop="moduleName" label="模块" min-width="140" />
                        <el-table-column prop="bizType" label="业务类型" min-width="120" />
                        <el-table-column prop="operationType" label="操作类型" min-width="120" />
                        <el-table-column prop="operatorName" label="操作人" min-width="120" />
                        <el-table-column prop="operatorIp" label="操作IP" min-width="140" />
                        <el-table-column label="结果" width="100">
                            <template #default="{ row }">
                                <el-tag :type="row.resultStatus === 1 ? 'success' : 'danger'">
                                    {{ row.resultStatus === 1 ? '成功' : '失败' }}
                                </el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column prop="createdAt" label="操作时间" min-width="180" />
                    </el-table>

                    <div class="pager">
                        <el-pagination
                            background
                            layout="total, prev, pager, next"
                            :current-page="operationQuery.pageNum"
                            :page-size="operationQuery.pageSize"
                            :total="operationTotal"
                            @current-change="handleOperationPageChange"
                        />
                    </div>
                </el-tab-pane>
            </el-tabs>
        </section>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getLoginLogPage, getOperationLogPage } from '@/api/system/log'
import { usePermission } from '@/composables/use_permission'

const loginLogList = ref([])
const operationLogList = ref([])
const loginTotal = ref(0)
const operationTotal = ref(0)
const { hasPermission } = usePermission()
const activeTab = ref(hasPermission('system:log:login:view') ? 'login' : 'operation')

const loginQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    loginStatus: null
})

const operationQuery = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    moduleName: '',
    resultStatus: null
})

async function loadLoginLogs() {
    if (!hasPermission('system:log:login:view')) {
        return
    }
    const response = await getLoginLogPage(loginQuery)
    loginLogList.value = response.data.list
    loginTotal.value = response.data.total
}

async function loadOperationLogs() {
    if (!hasPermission('system:log:operation:view')) {
        return
    }
    const response = await getOperationLogPage(operationQuery)
    operationLogList.value = response.data.list
    operationTotal.value = response.data.total
}

function resetLoginQuery() {
    loginQuery.keyword = ''
    loginQuery.loginStatus = null
    loginQuery.pageNum = 1
    loadLoginLogs()
}

function resetOperationQuery() {
    operationQuery.keyword = ''
    operationQuery.moduleName = ''
    operationQuery.resultStatus = null
    operationQuery.pageNum = 1
    loadOperationLogs()
}

function handleLoginPageChange(page) {
    loginQuery.pageNum = page
    loadLoginLogs()
}

function handleOperationPageChange(page) {
    operationQuery.pageNum = page
    loadOperationLogs()
}

function handleTabChange(name) {
    if (name === 'login') {
        loadLoginLogs()
        return
    }
    loadOperationLogs()
}

onMounted(async () => {
    const tasks = []
    if (hasPermission('system:log:login:view')) {
        tasks.push(loadLoginLogs())
    }
    if (hasPermission('system:log:operation:view')) {
        tasks.push(loadOperationLogs())
    }
    await Promise.all(tasks)
})
</script>

<style scoped>
.log-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.summary-card,
.table-card {
    padding: 20px;
}

.summary-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
    margin-top: 20px;
}

.summary-item {
    border-radius: 16px;
    padding: 16px 18px;
    background: linear-gradient(135deg, #eff6ff, #f8fafc);
    border: 1px solid #dbeafe;
}

.summary-label {
    color: #1d4ed8;
    font-size: 13px;
}

.summary-value {
    display: block;
    margin-top: 8px;
    font-size: 24px;
    font-weight: 700;
}

.query-form {
    margin: 10px 0 18px;
}

.pager {
    display: flex;
    justify-content: flex-end;
    margin-top: 18px;
}

.page-title {
    margin: 0;
    font-size: 24px;
}

.page-subtitle {
    margin: 6px 0 0;
    color: #64748b;
}

@media (max-width: 960px) {
    .summary-grid {
        grid-template-columns: 1fr;
    }
}
</style>
