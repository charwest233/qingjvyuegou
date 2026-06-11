import request from './request'

// 申请售后
export function applyRefund(data) {
  return request.post('/refund/apply', data)
}

// 查询我的售后列表
export function getRefundList() {
  return request.get('/refund/list')
}

// 查询售后详情
export function getRefundDetail(id) {
  return request.get(`/refund/${id}`)
}

// 填写退货物流
export function fillReturnLogistics(refundId, expressCompany, expressNo) {
  return request.put('/refund/return-logistics', { refundId, expressCompany, expressNo })
}

// 撤销申请
export function cancelRefund(id) {
  return request.put(`/refund/cancel/${id}`)
}


