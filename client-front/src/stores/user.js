import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, setUser, removeToken, getUser } from '@/utils/auth'
import { wxLogin } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || '用户')
  const avatar = computed(() => userInfo.value?.avatarUrl || '')

  // 微信小程序登录
  async function login(code) {
    try {
      const res = await wxLogin(code)
      if (res.code === 200 && res.data) {
        token.value = res.data.token
        userInfo.value = res.data.userInfo
        setToken(res.data.token)
        setUser(res.data.userInfo)
        return true
      }
      return false
    } catch (err) {
      console.error('登录失败:', err)
      return false
    }
  }

  // 直接设置 Token（模拟登录）
  function setLoginData(data) {
    token.value = data.token
    userInfo.value = data.userInfo
    setToken(data.token)
    setUser(data.userInfo)
  }

  // 退出登录
  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    nickname,
    avatar,
    login,
    setLoginData,
    logout
  }
})
