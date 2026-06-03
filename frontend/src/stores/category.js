import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategories } from '../api/category'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const loading = ref(false)

  async function fetchCategories() {
    loading.value = true
    try {
      const res = await getCategories()
      categories.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  function getCategoriesByType(type) {
    return categories.value.filter(c => c.type === type)
  }

  function getCategoryById(id) {
    return categories.value.find(c => c.id === id)
  }

  return {
    categories,
    loading,
    fetchCategories,
    getCategoriesByType,
    getCategoryById
  }
})
