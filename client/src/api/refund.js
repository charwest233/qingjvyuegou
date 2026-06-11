import request from '@/utils/request'

export const refundAdminApi = {
  /** 获取售后列表 */
  getList(status) {
    return request.get('/admin/refund/list', { params: { status } })
  },

  /** 获取售后详情 */
  getDetail(id) {
    return request.get(`/admin/refund/${id}`)
  },

  /** 审核售后 */
  audit(refundId, approved, remark) {
    return request.put('/admin/refund/audit', { refundId, approved, remark })
  },

  /** 确认收货（完成退款） */
  confirmReceipt(id) {
    return request.put(`/admin/refund/confirm-receipt/${id}`)
  }
}
