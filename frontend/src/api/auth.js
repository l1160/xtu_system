import request from '@/utils/request'

export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

export function getCurrentUser() {
    return request({
        url: '/auth/me',
        method: 'get'
    })
}

export function getMenus() {
    return request({
        url: '/auth/menus',
        method: 'get'
    })
}

export function updatePassword(data) {
    return request({
        url: '/auth/password',
        method: 'put',
        data
    })
}

export function logout() {
    return request({
        url: '/auth/logout',
        method: 'post'
    })
}
