import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo, logout as logoutApi } from '../utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    setToken(res.data.token)
    setUser(res.data)
    return res.data
  }

  async function register(registerForm) {
    const res = await registerApi(registerForm)
    // 注册成功后不自动登录，让用户手动登录
    return res.data
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      setUser(res.data)
      return res.data
    } catch (error) {
      logout()
      throw error
    }
  }

  function logout() {
    try {
      logoutApi()
    } catch (e) {
      // ignore
    }
    token.value = ''
    user.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function isLoggedIn() {
    return !!token.value
  }

  return {
    token,
    user,
    login,
    register,
    fetchUserInfo,
    logout,
    isLoggedIn,
    setToken,
    setUser
  }
})
