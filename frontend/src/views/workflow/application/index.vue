<template>
    <div class="workflow-page">
        <section class="page-card summary-card">
            <div>
                <h1 class="page-title">审批任务</h1>
                <p class="page-subtitle">集中处理待办申请，并查看已办记录与流转轨迹。</p>
            </div>

            <div class="summary-grid">
                <article class="summary-item">
                    <span class="summary-label">我的待办</span>
                    <strong class="summary-value">{{ todoTotal }}</strong>
                </article>
                <article class="summary-item">
                    <span class="summary-label">我的已办</span>
                    <strong class="summary-value">{{ doneTotal }}</strong>
                </article>
                <article class="summary-item">
                    <span class="summary-label">当前视图</span>
                    <strong class="summary-value">{{ activeTab === 'todo' ? '待办清单' : '已办清单' }}</strong>
                </article>
            </div>
        </section>

        <section class="page-card toolbar-card">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange">
                <el-tab-pane label="我的待办" name="todo" />
                <el-tab-pane label="我的已办" name="done" />
            </el-tabs>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-input v-model="query.keyword" clearable placeholder="申请人或对象" />
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.applicationType" clearable placeholder="申请类型" style="width: 160px;">
                        <el-option label="课程调整" value="课程调整" />
                        <el-option label="公告发布" value="公告发布" />
                        <el-option label="部门调整" value="部门调整" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadTasks">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="taskList" stripe>
                <el-table-column prop="applicantName" label="申请人" min-width="120" />
                <el-table-column prop="applicationType" label="申请类型" min-width="120" />
                <el-table-column prop="targetName" label="申请对象" min-width="180" show-overflow-tooltip />
                <el-table-column label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getStatusTagType(row.status)">
                            {{ formatStatus(row.status) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" min-width="180" />
                <el-table-column prop="processTime" label="处理时间" min-width="180" />
                <el-table-column prop="approverName" label="处理人" min-width="120" />
                <el-table-column label="操作" width="240" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="activeTab === 'todo' && hasPermission('business:application:review')"
                            link
                            type="success"
                            @click="openReviewDialog(row.recordId, 'approve')"
                        >
                            通过
                        </el-button>
                        <el-button
                            v-if="activeTab === 'todo' && hasPermission('business:application:review')"
                            link
                            type="danger"
                            @click="openReviewDialog(row.recordId, 'reject')"
                        >
                            驳回
                        </el-button>
                        <el-button link type="info" @click="openRecordDialog(row.applicationId)">流转记录</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pager">
                <el-pagination
                    background
                    layout="total, prev, pager, next"
                    :current-page="query.pageNum"
                    :page-size="query.pageSize"
                    :total="total"
                    @current-change="handlePageChange"
                />
            </div>
        </section>

        <el-dialog v-model="reviewDialogVisible" :title="reviewMode === 'approve' ? '审批通过' : '审批驳回'" width="520px" destroy-on-close>
            <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="96px">
                <el-form-item :label="reviewMode === 'approve' ? '审批意见' : '驳回原因'" prop="commentText">
                    <el-input v-model="reviewForm.commentText" type="textarea" :rows="4" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="reviewDialogVisible = false">取消</el-button>
                <el-button type="primary" :loading="reviewSubmitting" @click="handleReviewSubmit">
                    提交
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="recordDialogVisible" title="流转记录" width="720px" destroy-on-close>
            <el-empty v-if="recordList.length === 0" description="暂无流转记录" />
            <el-timeline v-else class="record-timeline">
                <el-timeline-item
                    v-for="record in recordList"
                    :key="record.id"
                    :timestamp="record.actedAt || record.operateTime || '-'"
                    placement="top"
                >
                    <div class="timeline-card">
                        <div class="timeline-title">{{ record.actionName }}</div>
                        <div class="timeline-meta">{{ record.approverName || record.operatorName || '系统' }}</div>
                        <div class="timeline-remark">{{ record.commentText || record.remark || '无备注' }}</div>
                    </div>
                </el-timeline-item>
            </el-timeline>
        </el-dialog>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getApplicationRecords } from '@/api/business/application'
import { approveWorkflowTask, getWorkflowDone, getWorkflowTodo, rejectWorkflowTask } from '@/api/workflow/application'
import { usePermission } from '@/composables/use_permission'

const reviewFormRef = ref()
const activeTab = ref('todo')
const taskList = ref([])
const total = ref(0)
const todoTotal = ref(0)
const doneTotal = ref(0)
const reviewDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewTargetId = ref(null)
const reviewMode = ref('approve')
const recordList = ref([])
const { hasPermission } = usePermission()

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    applicationType: ''
})

const reviewForm = reactive({
    commentText: ''
})

const reviewRules = {
    commentText: [
        {
            validator: (_, value, callback) => {
                if (reviewMode.value === 'reject' && !value) {
                    callback(new Error('请输入驳回原因'))
                    return
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
}

async function loadTasks() {
    const requestFn = activeTab.value === 'todo' ? getWorkflowTodo : getWorkflowDone
    const response = await requestFn(query)
    taskList.value = response.data.list
    total.value = response.data.total
}

async function loadSummary() {
    const [todoResponse, doneResponse] = await Promise.all([
        getWorkflowTodo({
            pageNum: 1,
            pageSize: 1
        }),
        getWorkflowDone({
            pageNum: 1,
            pageSize: 1
        })
    ])
    todoTotal.value = todoResponse.data.total
    doneTotal.value = doneResponse.data.total
}

function resetQuery() {
    query.keyword = ''
    query.applicationType = ''
    query.pageNum = 1
    loadTasks()
}

function handleTabChange() {
    query.pageNum = 1
    loadTasks()
}

function handlePageChange(page) {
    query.pageNum = page
    loadTasks()
}

function openReviewDialog(recordId, mode) {
    reviewTargetId.value = recordId
    reviewMode.value = mode
    reviewForm.commentText = ''
    reviewDialogVisible.value = true
}

async function handleReviewSubmit() {
    await reviewFormRef.value.validate()
    reviewSubmitting.value = true

    try {
        const requestFn = reviewMode.value === 'approve' ? approveWorkflowTask : rejectWorkflowTask
        await requestFn(reviewTargetId.value, {
            commentText: reviewForm.commentText
        })
        ElMessage.success(reviewMode.value === 'approve' ? '审批已通过' : '审批已驳回')
        reviewDialogVisible.value = false
        await Promise.all([loadTasks(), loadSummary()])
    } finally {
        reviewSubmitting.value = false
    }
}

async function openRecordDialog(applicationId) {
    const response = await getApplicationRecords(applicationId)
    recordList.value = response.data
    recordDialogVisible.value = true
}

function formatStatus(status) {
    const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已驳回',
        3: '已撤回'
    }
    return statusMap[status] || '未知'
}

function getStatusTagType(status) {
    const tagTypeMap = {
        0: 'warning',
        1: 'success',
        2: 'danger',
        3: 'info'
    }
    return tagTypeMap[status] || 'info'
}

onMounted(async () => {
    await Promise.all([loadSummary(), loadTasks()])
})
</script>

<style scoped>
.workflow-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.summary-card,
.toolbar-card,
.table-card {
    padding: 20px;
}

.summary-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
    margin-top: 20px;
}

.summary-item {
    border-radius: 16px;
    padding: 16px 18px;
    background: linear-gradient(135deg, #f0fdfa, #ecfeff);
    border: 1px solid #ccfbf1;
}

.summary-label {
    color: #0f766e;
    font-size: 13px;
}

.summary-value {
    display: block;
    margin-top: 8px;
    font-size: 24px;
    font-weight: 700;
}

.query-form {
    margin-top: 10px;
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

.record-timeline {
    padding: 8px 8px 8px 4px;
}

.timeline-card {
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 14px 16px;
    background: #fff;
}

.timeline-title {
    font-weight: 700;
}

.timeline-meta {
    margin-top: 6px;
    color: #0f766e;
    font-size: 13px;
}

.timeline-remark {
    margin-top: 8px;
    color: #475569;
}

@media (max-width: 960px) {
    .summary-grid {
        grid-template-columns: 1fr;
    }
}
</style>
