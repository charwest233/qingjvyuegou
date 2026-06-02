import request from './request'

// 发送验证码
export function sendSmsCode(phone) {
  return request.post('/sms/send-code', { phone })
}

// 校验验证码
export function verifySmsCode(phone, code) {
  return request.post('/sms/verify-code', { phone, code })
}
