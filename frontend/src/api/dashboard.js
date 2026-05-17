import request from '@/utils/request'

export function getSummary() {
    return request({
        url: '/dashboard/summary',
        method: 'get'
    })
}

export function getTodoList() {
    return request({
        url: '/dashboard/todo',
        method: 'get'
    })
}
