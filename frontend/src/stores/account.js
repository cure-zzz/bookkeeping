import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLiabilities } from '../api/liability'

export const useAccountStore = defineStore('account', () => {
  const accounts = ref([])
  const loading = ref(false)

  async function fetchAccounts() {
    loading.value = true
    try {
      const res = await getLiabilities()
      // 只获取资产类型（type=1）的账户
      accounts.value = (res.data || []).filter(a => a.type === 1)
      return accounts.value
    } finally {
      loading.value = false
    }
  }

  function getAccountById(id) {
    return accounts.value.find(a => a.id === id)
  }

  return {
    accounts,
    loading,
    fetchAccounts,
    getAccountById
  }
})
