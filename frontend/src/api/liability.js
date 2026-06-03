import request from '../utils/request'

export function getLiabilities() {
  return request({
    url: '/liabilities',
    method: 'get'
  })
}

export function getLiabilityById(id) {
  return request({
    url: `/liabilities/${id}`,
    method: 'get'
  })
}

export function createLiability(data) {
  return request({
    url: '/liabilities',
    method: 'post',
    data
  })
}

export function updateLiability(id, data) {
  return request({
    url: `/liabilities/${id}`,
    method: 'put',
    data
  })
}

export function deleteLiability(id) {
  return request({
    url: `/liabilities/${id}`,
    method: 'delete'
  })
}

// 还款相关
export function createRepayment(data) {
  return request({
    url: '/liabilities/repayments',
    method: 'post',
    data
  })
}

export function getRepayments(liabilityId) {
  return request({
    url: `/liabilities/${liabilityId}/repayments`,
    method: 'get'
  })
}

export function deleteRepayment(id) {
  return request({
    url: `/liabilities/repayments/${id}`,
    method: 'delete'
  })
}
