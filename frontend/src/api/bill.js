import request from '../utils/request'

export function getBills(params) {
  return request({
    url: '/bills',
    method: 'get',
    params
  })
}

export function getBillById(id) {
  return request({
    url: `/bills/${id}`,
    method: 'get'
  })
}

export function createBill(data) {
  return request({
    url: '/bills',
    method: 'post',
    data
  })
}

export function updateBill(id, data) {
  return request({
    url: `/bills/${id}`,
    method: 'put',
    data
  })
}

export function deleteBill(id) {
  return request({
    url: `/bills/${id}`,
    method: 'delete'
  })
}

export function getStatistics(month) {
  return request({
    url: '/bills/statistics',
    method: 'get',
    params: { month }
  })
}

export function getRecentBills(limit = 10) {
  return request({
    url: '/bills/recent',
    method: 'get',
    params: { limit }
  })
}

export function getYearlyStatistics(year) {
  return request({
    url: '/bills/yearly',
    method: 'get',
    params: { year }
  })
}
