<template>
    <div class="application-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">申请管理</h1>
                <p class="page-subtitle">管理业务申请单，支持提交、编辑、删除和审核。</p>
            </div>

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
                    <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px;">
                        <el-option :value="0" label="待审核" />
                        <el-option :value="1" label="已通过" />
                        <el-option :value="2" label="已驳回" />
                        <el-option :value="3" label="已撤回" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadApplications">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('business:application:create')" type="success" @click="openCreateDialog">新增申请</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="applicationList" stripe>
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
                <el-table-column label="附件" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.attachmentCount > 0 ? 'primary' : 'info'">
                            {{ row.attachmentCount || 0 }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" min-width="180" />
                <el-table-column prop="processTime" label="处理时间" min-width="180" />
                <el-table-column prop="approverName" label="审核人" min-width="120" />
                <el-table-column label="操作" width="360" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="row.status === 0 && hasPermission('business:application:update')"
                            link
                            type="primary"
                            @click="openEditDialog(row.id)"
                        >
                            编辑
                        </el-button>
                        <el-button
                            v-if="row.status === 0 && hasPermission('business:application:delete')"
                            link
                            type="danger"
                            @click="handleDelete(row.id)"
                        >
                            删除
                        </el-button>
                        <el-button
                            v-if="row.status === 0 && hasPermission('business:application:review')"
                            link
                            type="success"
                            @click="openReviewDialog(row.id)"
                        >
                            审核
                        </el-button>
                        <el-button
                            v-if="row.status === 0 && hasPermission('business:application:update')"
                            link
                            type="warning"
                            @click="handleWithdraw(row.id)"
                        >
                            撤回
                        </el-button>
                        <el-button
                            v-if="hasPermission('file:attachment:view')"
                            link
                            type="info"
                            @click="openAttachmentDialog(row)"
                        >
                            附件
                        </el-button>
                        <el-button link type="info" @click="openRecordDialog(row.id)">记录</el-button>
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="申请人" prop="applicantUserId">
                    <el-select v-model="form.applicantUserId" placeholder="请选择申请人" style="width: 100%;">
                        <el-option
                            v-for="user in userOptions"
                            :key="user.id"
                            :label="`${user.realName} (${user.username})`"
                            :value="user.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="申请类型" prop="applicationType">
                    <el-select v-model="form.applicationType" placeholder="请选择申请类型" style="width: 100%;">
                        <el-option label="课程调整" value="课程调整" />
                        <el-option label="公告发布" value="公告发布" />
                        <el-option label="部门调整" value="部门调整" />
                    </el-select>
                </el-form-item>
                <el-form-item label="申请对象" prop="targetName">
                    <el-input v-model="form.targetName" />
                </el-form-item>
                <el-form-item label="申请原因" prop="reason">
                    <el-input v-model="form.reason" type="textarea" :rows="5" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission(form.id ? 'business:application:update' : 'business:application:create')"
                    type="primary"
                    :loading="submitting"
                    @click="handleSubmit"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="reviewDialogVisible" title="审核申请" width="520px" destroy-on-close>
            <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="96px">
                <el-form-item label="审核结果" prop="status">
                    <el-radio-group v-model="reviewForm.status">
                        <el-radio :value="1">通过</el-radio>
                        <el-radio :value="2">驳回</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="审核备注" prop="reviewRemark">
                    <el-input v-model="reviewForm.reviewRemark" type="textarea" :rows="4" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="reviewDialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission('business:application:review')"
                    type="primary"
                    :loading="reviewSubmitting"
                    @click="handleReview"
                >
                    提交审核
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="recordDialogVisible" title="审批记录" width="680px" destroy-on-close>
            <el-table :data="recordList" stripe>
                <el-table-column prop="stepNo" label="步骤" width="80" />
                <el-table-column prop="actionName" label="动作" min-width="120" />
                <el-table-column prop="approverName" label="处理人" min-width="120" />
                <el-table-column prop="commentText" label="备注" min-width="180" show-overflow-tooltip />
                <el-table-column prop="actedAt" label="处理时间" min-width="180" />
            </el-table>
        </el-dialog>

        <BusinessAttachmentDialog
            v-model="attachmentDialogVisible"
            biz-type="application"
            :biz-id="attachmentTarget.id"
            :biz-label="attachmentTarget.label"
        />
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createApplication, deleteApplication, getApplicationDetail, getApplicationPage, getApplicationRecords, reviewApplication, updateApplication, withdrawApplication } from '@/api/business/application'
import { getUserOptions } from '@/api/system/user'
import { usePermission } from '@/composables/use_permission'
import BusinessAttachmentDialog from '@/components/business_attachment_dialog.vue'

const formRef = ref()
const reviewFormRef = ref()
const applicationList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const attachmentDialogVisible = ref(false)
const submitting = ref(false)
const reviewSubmitting = ref(false)
const reviewTargetId = ref(null)
const userOptions = ref([])
const recordList = ref([])
const { hasPermission } = usePermission()
const attachmentTarget = reactive({
    id: null,
    label: ''
})

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    applicationType: '',
    status: null
})

const form = reactive({
    id: null,
    applicantUserId: null,
    applicationType: '',
    targetName: '',
    reason: ''
})

const reviewForm = reactive({
    status: 1,
    reviewRemark: ''
})

const rules = {
    applicantUserId: [{ required: true, message: '请选择申请人', trigger: 'change' }],
    applicationType: [{ required: true, message: '请选择申请类型', trigger: 'change' }],
    reason: [{ required: true, message: '请输入申请原因', trigger: 'blur' }]
}

const reviewRules = {
    status: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

const dialogTitle = computed(() => (form.id ? '编辑申请' : '新增申请'))

function resetForm() {
    Object.assign(form, {
        id: null,
        applicantUserId: null,
        applicationType: '',
        targetName: '',
        reason: ''
    })
}

function resetReviewForm() {
    Object.assign(reviewForm, {
        status: 1,
        reviewRemark: ''
    })
}

async function loadApplications() {
    const response = await getApplicationPage(query)
    applicationList.value = response.data.list
    total.value = response.data.total
}

function resetQuery() {
    query.keyword = ''
    query.applicationType = ''
    query.status = null
    query.pageNum = 1
    loadApplications()
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const response = await getApplicationDetail(id)
    Object.assign(form, response.data)
    dialogVisible.value = true
}

function openReviewDialog(id) {
    reviewTargetId.value = id
    resetReviewForm()
    reviewDialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateApplication(form.id, form)
            ElMessage.success('申请更新成功')
        } else {
            await createApplication(form)
            ElMessage.success('申请创建成功')
        }
        dialogVisible.value = false
        loadApplications()
    } finally {
        submitting.value = false
    }
}

async function handleReview() {
    await reviewFormRef.value.validate()
    reviewSubmitting.value = true

    try {
        await reviewApplication(reviewTargetId.value, reviewForm)
        ElMessage.success('申请审核成功')
        reviewDialogVisible.value = false
        loadApplications()
    } finally {
        reviewSubmitting.value = false
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除后将进入逻辑删除状态，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteApplication(id)
    ElMessage.success('申请删除成功')
    loadApplications()
}

async function handleWithdraw(id) {
    await ElMessageBox.confirm('撤回后申请将不再进入当前审批流，是否继续？', '确认撤回', {
        type: 'warning'
    })
    await withdrawApplication(id)
    ElMessage.success('申请撤回成功')
    loadApplications()
}

function handlePageChange(page) {
    query.pageNum = page
    loadApplications()
}

async function loadUserOptions() {
    const response = await getUserOptions({ status: 1 })
    userOptions.value = response.data
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

async function openRecordDialog(id) {
    const response = await getApplicationRecords(id)
    recordList.value = response.data
    recordDialogVisible.value = true
}

function openAttachmentDialog(row) {
    attachmentTarget.id = row.id
    attachmentTarget.label = `${row.applicationType} · ${row.targetName}`
    attachmentDialogVisible.value = true
}

onMounted(() => {
    loadUserOptions()
    loadApplications()
})
</script>

<style scoped>
.application-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.toolbar-card,
.table-card {
    padding: 20px;
}

.query-form {
    margin-top: 18px;
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
</style>
