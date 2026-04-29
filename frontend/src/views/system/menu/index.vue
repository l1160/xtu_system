<template>
    <div class="menu-page">
        <section class="page-card toolbar-card">
            <div>
                <h1 class="page-title">菜单管理</h1>
                <p class="page-subtitle">维护系统目录、菜单和按钮权限结构。</p>
            </div>
            <div class="toolbar-actions">
                <el-button v-if="hasPermission('system:menu:create')" type="primary" @click="openCreateDialog(0)">新增顶级菜单</el-button>
                <el-button @click="loadMenus">刷新</el-button>
            </div>
        </section>

        <section class="page-card table-card">
            <el-table
                :data="menuList"
                row-key="id"
                default-expand-all
                :tree-props="{ children: 'children' }"
            >
                <el-table-column prop="menuName" label="菜单名称" min-width="220" />
                <el-table-column label="类型" width="100">
                    <template #default="{ row }">
                        <el-tag>{{ formatMenuType(row.menuType) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="routePath" label="路由路径" min-width="180" />
                <el-table-column prop="permissionCode" label="权限标识" min-width="180" />
                <el-table-column prop="sortNo" label="排序" width="90" />
                <el-table-column label="显示" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.visible === 1 ? 'success' : 'info'">
                            {{ row.visible === 1 ? '显示' : '隐藏' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'">
                            {{ row.status === 1 ? '启用' : '停用' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="hasPermission('system:menu:create')" link type="primary" @click="openCreateDialog(row.id)">新增下级</el-button>
                        <el-button v-if="hasPermission('system:menu:update')" link type="primary" @click="openEditDialog(row.id)">编辑</el-button>
                        <el-button v-if="hasPermission('system:menu:delete')" link type="danger" @click="handleDelete(row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </section>

        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
            <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
                <el-form-item label="上级菜单" prop="parentId">
                    <el-select v-model="form.parentId" style="width: 100%;">
                        <el-option label="顶级菜单" :value="0" />
                        <el-option
                            v-for="option in flatMenuOptions"
                            :key="option.value"
                            :label="option.label"
                            :value="option.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="菜单名称" prop="menuName">
                    <el-input v-model="form.menuName" />
                </el-form-item>
                <el-form-item label="菜单类型" prop="menuType">
                    <el-select v-model="form.menuType" style="width: 100%;">
                        <el-option label="目录" value="M" />
                        <el-option label="菜单" value="C" />
                        <el-option label="按钮" value="B" />
                    </el-select>
                </el-form-item>
                <el-form-item label="路由路径" prop="routePath">
                    <el-input v-model="form.routePath" placeholder="例如 /system/users" />
                </el-form-item>
                <el-form-item label="组件路径" prop="componentPath">
                    <el-input v-model="form.componentPath" placeholder="例如 views/system/user/index.vue" />
                </el-form-item>
                <el-form-item label="权限标识" prop="permissionCode">
                    <el-input v-model="form.permissionCode" placeholder="例如 system:user:view" />
                </el-form-item>
                <el-form-item label="图标" prop="icon">
                    <el-input v-model="form.icon" placeholder="例如 User、Menu" />
                </el-form-item>
                <el-form-item label="排序号" prop="sortNo">
                    <el-input-number v-model="form.sortNo" :min="0" style="width: 100%;" />
                </el-form-item>
                <el-form-item label="是否显示" prop="visible">
                    <el-radio-group v-model="form.visible">
                        <el-radio :value="1">显示</el-radio>
                        <el-radio :value="0">隐藏</el-radio>
                    </el-radio-group>
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
                    v-if="hasPermission(form.id ? 'system:menu:update' : 'system:menu:create')"
                    type="primary"
                    :loading="submitting"
                    @click="handleSubmit"
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
import { createMenu, deleteMenu, getMenuDetail, getMenuTree, updateMenu } from '@/api/system/menu'
import { usePermission } from '@/composables/use_permission'

const formRef = ref()
const menuList = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const { hasPermission } = usePermission()

const form = reactive({
    id: null,
    parentId: 0,
    menuName: '',
    menuType: 'C',
    routePath: '',
    componentPath: '',
    permissionCode: '',
    icon: '',
    sortNo: 0,
    visible: 1,
    status: 1,
    remark: ''
})

const rules = {
    parentId: [{ required: true, message: '请选择上级菜单', trigger: 'change' }],
    menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
    menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const dialogTitle = computed(() => (form.id ? '编辑菜单' : '新增菜单'))
const flatMenuOptions = computed(() => flattenMenuOptions(menuList.value))

async function loadMenus() {
    const response = await getMenuTree()
    menuList.value = response.data
}

function resetForm(parentId = 0) {
    Object.assign(form, {
        id: null,
        parentId,
        menuName: '',
        menuType: 'C',
        routePath: '',
        componentPath: '',
        permissionCode: '',
        icon: '',
        sortNo: 0,
        visible: 1,
        status: 1,
        remark: ''
    })
}

function openCreateDialog(parentId = 0) {
    resetForm(parentId)
    dialogVisible.value = true
}

async function openEditDialog(id) {
    const { data } = await getMenuDetail(id)
    Object.assign(form, data)
    dialogVisible.value = true
}

async function handleSubmit() {
    await formRef.value.validate()
    submitting.value = true

    try {
        if (form.id) {
            await updateMenu(form.id, form)
            ElMessage.success('菜单更新成功')
        } else {
            await createMenu(form)
            ElMessage.success('菜单创建成功')
        }
        dialogVisible.value = false
        loadMenus()
    } finally {
        submitting.value = false
    }
}

async function handleDelete(id) {
    await ElMessageBox.confirm('删除前会检查下级菜单和角色绑定，是否继续？', '确认删除', {
        type: 'warning'
    })
    await deleteMenu(id)
    ElMessage.success('菜单删除成功')
    loadMenus()
}

function flattenMenuOptions(tree, level = 0) {
    return tree.flatMap((item) => {
        const prefix = level === 0 ? '' : `${'　'.repeat(level)}└ `
        const current = {
            label: `${prefix}${item.menuName}`,
            value: item.id
        }
        return [current, ...flattenMenuOptions(item.children || [], level + 1)]
    })
}

function formatMenuType(menuType) {
    const menuTypeMap = {
        M: '目录',
        C: '菜单',
        B: '按钮'
    }
    return menuTypeMap[menuType] || '未定义'
}

onMounted(() => {
    loadMenus()
})
</script>

<style scoped>
.menu-page {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.toolbar-card,
.table-card {
    padding: 20px;
}

.toolbar-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.toolbar-actions {
    display: flex;
    gap: 12px;
}

@media (max-width: 960px) {
    .toolbar-card {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
    }
}
</style>
