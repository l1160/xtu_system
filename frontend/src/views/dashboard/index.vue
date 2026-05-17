<template>
    <div class="dashboard">
        <section class="overview-grid">
            <article v-for="item in cards" :key="item.label" class="overview-card page-card">
                <div class="card-label">{{ item.label }}</div>
                <div class="card-value">{{ item.value }}</div>
                <div class="card-tip">{{ item.tip }}</div>
            </article>
        </section>

        <section class="content-grid">
            <div class="todo-card page-card">
                <div class="section-header">
                    <div>
                        <h2>待办事项</h2>
                        <p>优先处理审批和基础资料维护任务。</p>
                    </div>
                </div>
                <el-table :data="todoList" stripe>
                    <el-table-column prop="title" label="标题" min-width="220" />
                    <el-table-column prop="type" label="类型" width="120" />
                    <el-table-column prop="status" label="状态" width="120" />
                    <el-table-column prop="createdAt" label="创建时间" width="180" />
                </el-table>
            </div>
        </section>
    </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getSummary, getTodoList } from '@/api/dashboard'

const summary = ref({
    userCount: 0,
    studentCount: 0,
    teacherCount: 0,
    courseCount: 0,
    pendingApplicationCount: 0
})
const todoList = ref([])

const cards = computed(() => [
    { label: '用户总数', value: summary.value.userCount, tip: '含管理员、教师、学生和员工账号' },
    { label: '学生总数', value: summary.value.studentCount, tip: '用于支撑档案与审批业务' },
    { label: '教师总数', value: summary.value.teacherCount, tip: '教师档案与课程主数据' },
    { label: '课程总数', value: summary.value.courseCount, tip: '课程基础信息与学期统计' },
    { label: '待审批', value: summary.value.pendingApplicationCount, tip: '建议作为首页重点指标' }
])

async function loadPageData() {
    const [summaryResponse, todoResponse] = await Promise.all([getSummary(), getTodoList()])
    summary.value = summaryResponse.data
    todoList.value = todoResponse.data
}

onMounted(() => {
    loadPageData()
})
</script>

<style scoped>
.dashboard {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.overview-grid {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 16px;
}

.overview-card {
    padding: 22px;
}

.card-label {
    font-size: 13px;
    color: #64748b;
}

.card-value {
    margin-top: 12px;
    font-size: 32px;
    font-weight: 700;
}

.card-tip {
    margin-top: 12px;
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
}

.content-grid {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
}

.todo-card {
    padding: 20px;
}

.section-header {
    margin-bottom: 16px;
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

@media (max-width: 1200px) {
    .overview-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 720px) {
    .overview-grid {
        grid-template-columns: 1fr;
    }
}
</style>
