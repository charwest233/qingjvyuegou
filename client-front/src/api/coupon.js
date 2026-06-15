import request from './request'

export function getDrawStatus() { return request.get('/coupon/draw-status') }

export function draw() { return request.post('/coupon/draw') }

export function getMyCoupons() { return request.get('/coupon/my') }

export function getAvailableCoupons(amount) { return request.get(`/coupon/available?amount=${amount}`) }
