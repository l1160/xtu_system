import request from '@/utils/request'

export function getWorkflowTodo(params) {
    return request({
        url: '/workflow/todo',
        method: 'get',
        params
    })
}

export function getWorkflowDone(params) {
    return request({
        url: '/workflow/done',
        method: 'get',
        params
    })
}

export function approveWorkflowTask(recordId, data) {
    return request({
        url: `/workflow/tasks/${recordId}/approve`,
        method: 'put',
        data
    })
}

export function rejectWorkflowTask(recordId, data) {
    return request({
        url: `/workflow/tasks/${recordId}/reject`,
        method: 'put',
        data
    })
}
