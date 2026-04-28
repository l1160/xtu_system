import request from '@/utils/request'

export function getRolePage(params) {
    return request({
        url: '/roles',
        method: 'get',
        params
    })
}

export function getRoleDetail(id) {
    return request({
        url: `/roles/${id}`,
        method: 'get'
    })
}

export function createRole(data) {
    return request({
        url: '/roles',
        method: 'post',
        data
    })
}

export function updateRole(id, data) {
    return request({
        url: `/roles/${id}`,
        method: 'put',
        data
    })
}

export function deleteRole(id) {
    return request({
        url: `/roles/${id}`,
        method: 'delete'
    })
}

export function updateRoleStatus(id, data) {
    return request({
        url: `/roles/${id}/status`,
        method: 'put',
        data
    })
}

export function assignRoleMenus(id, data) {
    return request({
        url: `/roles/${id}/menus`,
        method: 'put',
        data
    })
}

export function getRoleOptions() {
    return request({
        url: '/roles/options',
        method: 'get'
    })
}
