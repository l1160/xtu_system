import request from '@/utils/request'

export function getLoginLogPage(params) {
    return request({
        url: '/login-logs',
        method: 'get',
        params
    })
}

export function getOperationLogPage(params) {
    return request({
        url: '/operation-logs',
        method: 'get',
        params
    })
}
