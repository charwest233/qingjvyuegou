// 认证工具：Token 存取管理

const TOKEN_KEY = 'token'
const USER_KEY = 'user_info'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser() {
  const userStr = localStorage.getItem(USER_KEY)
  try {
    return userStr ? JSON.parse(userStr) : null
  } catch {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function isLoggedIn() {
  return !!getToken()
}

export function logout() {
  removeToken()
  window.location.href = '/login'
}
