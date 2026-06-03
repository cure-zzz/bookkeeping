import request from '../utils/request'

export function getCategories(type) {
  return request({
    url: '/categories',
    method: 'get',
    params: type ? { type } : {}
  })
}

export function getCategoryById(id) {
  return request({
    url: `/categories/${id}`,
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}
