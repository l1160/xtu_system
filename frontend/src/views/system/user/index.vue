<template>
    <div class="user-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">用户管理</h1>
                <p class="page-subtitle">支持用户状态切换、密码重置和角色分配，方便完成日常账号维护。</p>
            </div>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-input v-model="query.keyword" clearable placeholder="账号或姓名" />
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.deptId" clearable placeholder="部门" style="width: 180px;">
                        <el-option
                            v-for="option in flatDepartmentOptions"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px;">
                        <el-option :value="1" label="启用" />
                        <el-option :value="0" label="停用" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadUsers">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('system:user:create')" type="success" @click="openCreateDialog">新增用户</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="userList" stripe>
                <el-table-column prop="username" label="账号" min-width="140" />
                <el-table-column prop="realName" label="姓名" min-width="120" />
                <el-table-column label="类型" min-width="120">
                    <template #default="{ row }">
                        {{ formatUserType(row.userType) }}
                    </template>
                </el-table-column>
                <el-table-column prop="deptName" label="部门" min-width="140" />
                <el-table-column prop="phone" label="手机号" min-width="140" />
                <el-table-column prop="email" label="邮箱" min-width="200" />
                <el-table-column label="状态" width="140">
                    <template #default="{ row }">
                        <div v-if="hasPermission('system:user:update')" class="status-cell">
                            <el-switch
                                v-model="row.status"
                                :active-value="1"
                                :inactive-value="0"
                                :loading="Boolean(statusLoadingMap[row.id])"
                                @change="handleStatusChange(row)"
                            />
                            <span>{{ row.status === 1 ? '启用' : '停用' }}</span>
                        </div>
                        <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
                            {{ row.status === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="300" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-if="hasPermission('system:user:update')"
                            link
                            type="primary"
                            @click="openEditDialog(row.id)"
                        >
                            编辑
                        </el-button>
                        <el-button
                            v-if="hasPermission('system:user:update')"
                            link
                            type="warning"
                            @click="openRoleDialog(row)"
                        >
                            分配角色
                        </el-button>
                        <el-button
                            v-if="hasPermission('system:user:update')"
                            link
                            type="success"
                            @click="openResetPasswordDialog(row)"
                        >
                            重置密码
                        </el-button>
                        <el-button
                            v-if="hasPermission('system:user:delete')"
                            link
                            type="danger"
                            @click="handleDelete(row.id)"
                        >
                            删除
                        </el-button>
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
                <el-form-item label="账号" prop="username">
                    <el-input v-model="form.username" :disabled="Boolean(form.id)" />
                </el-form-item>
                <el-form-item v-if="!form.id" label="密码" prop="password">
                    <el-input v-model="form.password" show-password type="password" />
                </el-form-item>
                <el-form-item label="姓名" prop="realName">
                    <el-input v-model="form.realName" />
                </el-form-item>
                <el-form-item label="类型" prop="userType">
                    <el-select v-model="form.userType" placeholder="请选择用户类型" style="width: 100%;">
                        <el-option :value="1" label="管理员" />
                        <el-option :value="2" label="教师" />
                        <el-option :value="3" label="学生" />
                        <el-option :value="4" label="员工" />
                    </el-select>
                </el-form-item>
                <el-form-item label="部门" prop="deptId">
                    <el-select v-model="form.deptId" clearable placeholder="请选择部门" style="width: 100%;">
                        <el-option
                            v-for="option in flatDepartmentOptions"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="角色" prop="roleIds">
                    <el-select v-model="form.roleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色" style="width: 100%;">
                        <el-option
                            v-for="option in roleOptions"
                            :key="option.id"
                            :label="option.roleName"
                            :value="option.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                    <el-input v-model="form.phone" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.email" />
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :value="1">启用</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission(form.id ? 'system:user:update' : 'system:user:create')"
                    type="primary"
                    :loading="submitting"
                    @click="handleSubmit"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="520px" destroy-on-close>
            <el-form ref="roleFormRef" :model="roleForm" label-width="90px">
                <el-form-item label="角色选择">
                    <el-select
                        v-model="roleForm.roleIds"
                        multiple
                        collapse-tags
                        collapse-tags-tooltip
                        placeholder="请选择角色"
                        style="width: 100%;"
                    >
                        <el-option
                            v-for="option in roleOptions"
                            :key="option.id"
                            :label="option.roleName"
                            :value="option.id"
                        />
                    </el-select>
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="roleDialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission('system:user:update')"
                    type="primary"
                    :loading="roleSubmitting"
                    @click="handleAssignRoles"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="resetPasswordDialogVisible" :title="resetPasswordDialogTitle" width="520px" destroy-on-close>
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="90px">
                <el-form-item label="新密码" prop="password">
                    <el-input v-model="passwordForm.password" show-password type="password" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input v-model="passwordForm.confirmPassword" show-password type="password" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission('system:user:update')"
                    type="primary"
                    :loading="passwordSubmitting"
                    @click="handleResetPassword"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    assignUserRoles,
    createUser,
    deleteUser,
    getUserDetail,
    getUserPage,
    resetUserPassword,
    updateUser,
    updateUserStatus
} from '@/api/system/user'
import { getDepartmentOptions } from '@/api/organization/department'
import { getRoleOptions } from '@/api/system/role'
import { usePermission } from '@/composables/use_permission'

const formRef = ref()
const roleFormRef = ref()
const passwordFormRef = ref()
const userList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const submitting = ref(false)
const roleSubmitting = ref(false)
const passwordSubmitting = ref(false)
const departmentOptions = ref([])
const roleOptions = ref([])
const roleTargetId = ref(null)
const roleTargetName = ref('')
const passwordTargetId = ref(null)
const passwordTargetName = ref('')
const statusLoadingMap = reactive({})
const { hasPermission } = usePermission()

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    deptId: null,
    status: null
})

const form = reactive({
    id: null,
    username: '',
    password: '',
    realName: '',
    userType: 1,
    deptId: null,
    roleIds: [],
    phone: '',
    email: '',
    status: 1
})

const roleForm = reactive({
    roleIds: []
})

const passwordForm = reactive({
    password: '',
    confirmPassword: ''
})

const rules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }]
}

const passwordRules = {
    password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
    confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
            validator: (_, value, callback) => {
                if (value !== passwordForm.password) {
                    callback(new Error('两次输入的密码不一致'))
                    return
                }
                callback()
            },
            trigger: 'blur'
        }
    ]
}

const dialogTitle = computed(() => (form.id ? '编辑用户' : '新增用户'))
const roleDialogTitle = computed(() => `为 ${roleTargetName.value} 分配角色`)
const resetPasswordDialogTitle = computed(() => `重置 ${passwordTargetName.value} 的密码`)
const flatDepartmentOptions = computed(() => flattenOptions(departmentOptions.value))

function resetForm() {
    Object.assign(form, {
        id: null,
        username: '',
        password: '',
        realName: '',
        userType: 1,
        deptId: null,
        roleIds: [],
        phone: '',
        email: '',
        status: 1
    })
}

function resetRoleForm() {
    roleForm.roleIds = []
}

function resetPasswordForm() {
    passwordForm.password = ''
    passwordForm.confirmPassword = ''
}

async function loadUsers() {
    const response = await getUserPage(query)
    userList.value = response.data.list
    total.value = response.data.total
}

function resetQuery() {
    query.keyword = ''
    query.deptId = null
    query.status = null
    query.pageNum = 1
    loadUsers()
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const response = await getUserDetail(id)
    Object.assign(form, response.data, {
        password: '',
        roleIds: response.data.roleIds || []
    })
    dialogVisible.value = true
}

async function openRoleDialog(row) {
    const response = await getUserDetail(row.id)
    roleTargetId.value = row.id
    roleTargetName.value = row.realName
    roleForm.roleIds = response.data.roleIds || []
    roleDialogVisible.value = true
}

function openResetPasswordDialog(row) {
    passwordTargetId.value = row.id
    passwordTargetName.value = row.realName
    resetPasswordForm()
    resetPasswordDialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateUser(form.id, form)
            ElMessage.success('用户更新成功')
        } else {
            await createUser(form)
            ElMessage.success('用户创建成功')
        }
        dialogVisible.value = false
        loadUsers()
    } finally {
        submitting.value = false
    }
}

async function handleAssignRoles() {
    roleSubmitting.value = true
    try {
        await assignUserRoles(roleTargetId.value, {
            roleIds: roleForm.roleIds
        })
        ElMessage.success('角色分配成功')
        roleDialogVisible.value = false
        loadUsers()
    } finally {
        roleSubmitting.value = false
    }
}

async function handleResetPassword() {
    await passwordFormRef.value.validate()
    passwordSubmitting.value = true
    try {
        await resetUserPassword(passwordTargetId.value, {
            password: passwordForm.password
        })
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
    } finally {
        passwordSubmitting.value = false
    }
}

async function handleStatusChange(row) {
    const nextStatus = row.status
    const previousStatus = nextStatus === 1 ? 0 : 1

    try {
        await ElMessageBox.confirm(
            `确认${nextStatus === 1 ? '启用' : '停用'}用户 ${row.realName}？`,
            '确认状态变更',
            { type: 'warning' }
        )
        statusLoadingMap[row.id] = true
        await updateUserStatus(row.id, {
            status: nextStatus
        })
        ElMessage.success('状态更新成功')
    } catch (error) {
        row.status = previousStatus
    } finally {
        delete statusLoadingMap[row.id]
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除后将进入逻辑删除状态，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteUser(id)
    ElMessage.success('删除成功')
    loadUsers()
}

function handlePageChange(page) {
    query.pageNum = page
    loadUsers()
}

async function loadBaseOptions() {
    const [departmentResponse, roleResponse] = await Promise.all([
        getDepartmentOptions(),
        getRoleOptions()
    ])
    departmentOptions.value = departmentResponse.data
    roleOptions.value = roleResponse.data
}

function flattenOptions(tree, level = 0) {
    return tree.flatMap((item) => {
        const prefix = level === 0 ? '' : `${'　'.repeat(level)}└ `
        const current = {
            label: `${prefix}${item.label}`,
            value: item.value
        }
        return [current, ...flattenOptions(item.children || [], level + 1)]
    })
}

function formatUserType(userType) {
    const userTypeMap = {
        1: '管理员',
        2: '教师',
        3: '学生',
        4: '员工'
    }
    return userTypeMap[userType] || '未定义'
}

onMounted(() => {
    loadBaseOptions()
    loadUsers()
})
</script>

<style scoped>
.user-page {
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

.status-cell {
    display: flex;
    align-items: center;
    gap: 8px;
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
