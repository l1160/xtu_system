<template>
    <div class="role-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">角色管理</h1>
                <p class="page-subtitle">维护角色主数据，并为角色分配菜单访问能力。</p>
            </div>

            <el-form :inline="true" :model="query" class="query-form">
                <el-form-item>
                    <el-input v-model="query.keyword" clearable placeholder="角色编码或名称" />
                </el-form-item>
                <el-form-item>
                    <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px;">
                        <el-option :value="1" label="启用" />
                        <el-option :value="0" label="停用" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="loadRoles">查询</el-button>
                    <el-button @click="resetQuery">重置</el-button>
                    <el-button v-if="hasPermission('system:role:create')" type="success" @click="openCreateDialog">新增角色</el-button>
                </el-form-item>
            </el-form>
        </section>

        <section class="page-card table-card">
            <el-table :data="roleList" stripe>
                <el-table-column prop="roleCode" label="角色编码" min-width="140" />
                <el-table-column prop="roleName" label="角色名称" min-width="140" />
                <el-table-column label="数据范围" min-width="160">
                    <template #default="{ row }">
                        {{ formatDataScope(row.dataScope) }}
                    </template>
                </el-table-column>
                <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
                <el-table-column label="状态" width="120">
                    <template #default="{ row }">
                        <el-switch
                            :model-value="row.status"
                            :active-value="1"
                            :inactive-value="0"
                            :disabled="!hasPermission('system:role:update')"
                            @change="(value) => handleStatusChange(row.id, value)"
                        />
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="hasPermission('system:role:update')" link type="primary" @click="openEditDialog(row.id)">编辑</el-button>
                        <el-button v-if="hasPermission('system:role:update')" link type="primary" @click="openMenuAssignDialog(row.id)">配置菜单</el-button>
                        <el-button v-if="hasPermission('system:role:delete')" link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="580px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="角色编码" prop="roleCode">
                    <el-input v-model="form.roleCode" />
                </el-form-item>
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="form.roleName" />
                </el-form-item>
                <el-form-item label="数据范围" prop="dataScope">
                    <el-select v-model="form.dataScope" style="width: 100%;">
                        <el-option :value="1" label="全部数据" />
                        <el-option :value="2" label="本部门数据" />
                        <el-option :value="3" label="本部门及下级" />
                        <el-option :value="4" label="仅本人" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :value="1">启用</el-radio>
                        <el-radio :value="0">停用</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" :rows="3" />
                </el-form-item>
            </el-form>

            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission(form.id ? 'system:role:update' : 'system:role:create')"
                    type="primary"
                    :loading="submitting"
                    @click="handleSubmit"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="menuDialogVisible" title="配置角色菜单" width="680px" destroy-on-close>
            <div class="menu-dialog-tip">选中菜单后保存，会覆盖当前角色已有菜单绑定。</div>
            <el-tree
                ref="menuTreeRef"
                node-key="id"
                show-checkbox
                default-expand-all
                :data="menuTreeData"
                :props="{ label: 'menuName', children: 'children' }"
            />

            <template #footer>
                <el-button @click="menuDialogVisible = false">取消</el-button>
                <el-button
                    v-if="hasPermission('system:role:update')"
                    type="primary"
                    :loading="menuSubmitting"
                    @click="handleSaveMenus"
                >
                    保存
                </el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
    assignRoleMenus,
    createRole,
    deleteRole,
    getRoleDetail,
    getRolePage,
    updateRole,
    updateRoleStatus
} from '@/api/system/role'
import { getMenuTree } from '@/api/system/menu'
import { usePermission } from '@/composables/use_permission'

const formRef = ref()
const menuTreeRef = ref()
const roleList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const submitting = ref(false)
const menuSubmitting = ref(false)
const currentRoleId = ref(null)
const menuTreeData = ref([])
const { hasPermission } = usePermission()

const query = reactive({
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    status: null
})

const form = reactive({
    id: null,
    roleCode: '',
    roleName: '',
    dataScope: 4,
    status: 1,
    remark: ''
})

const rules = {
    roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
    roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
}

const dialogTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))

async function loadRoles() {
    const response = await getRolePage(query)
    roleList.value = response.data.list
    total.value = response.data.total
}

function resetQuery() {
    query.keyword = ''
    query.status = null
    query.pageNum = 1
    loadRoles()
}

function resetForm() {
    Object.assign(form, {
        id: null,
        roleCode: '',
        roleName: '',
        dataScope: 4,
        status: 1,
        remark: ''
    })
}

function openCreateDialog() {
    resetForm()
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const { data } = await getRoleDetail(id)
    Object.assign(form, data)
    dialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateRole(form.id, form)
            ElMessage.success('角色更新成功')
        } else {
            await createRole(form)
            ElMessage.success('角色创建成功')
        }
        dialogVisible.value = false
        loadRoles()
    } finally {
        submitting.value = false
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除角色前会检查用户绑定，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteRole(id)
    ElMessage.success('角色删除成功')
    loadRoles()
}

async function handleStatusChange(id, status) {
    await updateRoleStatus(id, { status })
    ElMessage.success('角色状态已更新')
    loadRoles()
}

async function openMenuAssignDialog(id) {
    currentRoleId.value = id
    menuDialogVisible.value = true

    const [roleResponse, menuResponse] = await Promise.all([
        getRoleDetail(id),
        getMenuTree()
    ])

    menuTreeData.value = menuResponse.data

    await nextTick()
    const checkedKeys = roleResponse.data.menuIds || []
    menuTreeRef.value.setCheckedKeys(checkedKeys)
  }

async function handleSaveMenus() {
    menuSubmitting.value = true

    try {
        const checkedKeys = menuTreeRef.value.getCheckedKeys(false)
        const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
        const menuIds = Array.from(new Set([...checkedKeys, ...halfCheckedKeys]))
        await assignRoleMenus(currentRoleId.value, { menuIds })
        ElMessage.success('角色菜单配置成功')
        menuDialogVisible.value = false
    } finally {
        menuSubmitting.value = false
    }
}

function handlePageChange(page) {
    query.pageNum = page
    loadRoles()
}

function formatDataScope(dataScope) {
    const dataScopeMap = {
        1: '全部数据',
        2: '本部门数据',
        3: '本部门及下级',
        4: '仅本人'
    }
    return dataScopeMap[dataScope] || '未定义'
}

onMounted(() => {
    loadRoles()
})
</script>

<style scoped>
.role-page {
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

.menu-dialog-tip {
    margin-bottom: 16px;
    color: #64748b;
    font-size: 13px;
}
</style>
