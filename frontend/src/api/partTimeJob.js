import request from '../utils/request'

export function getPartTimeJobs() {
    return request({
        url: '/part-time-job',
        method: 'get'
    })
}

export function getMonthlyStats() {
    return request({
        url: '/part-time-job/monthly',
        method: 'get'
    })
}

export function getStats() {
    return request({
        url: '/part-time-job/stats',
        method: 'get'
    })
}

export function getPartTimeJobById(id) {
    return request({
        url: `/part-time-job/${id}`,
        method: 'get'
    })
}

export function createPartTimeJob(data) {
    return request({
        url: '/part-time-job',
        method: 'post',
        data
    })
}

export function updatePartTimeJob(id, data) {
    return request({
        url: `/part-time-job/${id}`,
        method: 'put',
        data
    })
}

export function deletePartTimeJob(id) {
    return request({
        url: `/part-time-job/${id}`,
        method: 'delete'
    })
}

export function queryByDateRange(params) {
    return request({
        url: '/part-time-job/query',
        method: 'get',
        params: {
            ...params,
            page: params.page || 1,
            size: params.size || 20
        }
    })
}
