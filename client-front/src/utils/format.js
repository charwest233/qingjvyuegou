// 格式化工具

/**
 * 格式化金额（分转元，保留两位小数）
 */
export function formatPrice(price) {
  const num = Number(price)
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

/**
 * 格式化时间
 */
export function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

/**
 * 手机号脱敏
 */
export function maskPhone(phone) {
  if (!phone || phone.length !== 11) return phone || ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 订单状态中文映射
 */
export const orderStatusMap = {
  '-1': '已取消',
  '0': '待支付',
  '1': '已支付',
  '2': '已发货',
  '3': '已完成'
}

export function getOrderStatusText(status) {
  return orderStatusMap[String(status)] || '未知'
}

/**
 * 订单状态类型（用于标签样式）
 */
export function getOrderStatusType(status) {
  const map = {
    '-1': 'danger',
    '0': 'warning',
    '1': 'info',
    '2': 'primary',
    '3': 'success'
  }
  return map[String(status)] || 'info'
}
