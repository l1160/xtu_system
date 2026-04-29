import request from '@/utils/request'

export function getMenuTree() {
    return request({
        url: '/menus/tree',
        method: 'get'
    })
}

export function getMenuDetail(id) {
    return request({
        url: `/menus/${id}`,
        method: 'get'
    })
}

export function createMenu(data) {
    return request({
        url: '/menus',
        method: 'post',
        data
    })
}

export function updateMenu(id, data) {
    return request({
        url: `/menus/${id}`,
        method: 'put',
        data
    })
}

export function deleteMenu(id) {
    return request({
        url: `/menus/${id}`,
        method: 'delete'
    })
}
