import request from '@/utils/request'

// 获取支付方式
export function getPayWay() {
    return request.get({ url: '/setting.pay.pay_way/getPayWay' })
}

// 设置支付方式
export function setPayWay(params: any) {
    return request.post({ url: '/setting.pay.pay_way/setPayWay', params })
}

// 获取支付方式
export function getPayConfigLists() {
    return request.get({ url: '/setting.pay.pay_config/lists' })
}

// 获取充值选项设置
export function getRechargeOption() {
    return request.get({ url: '/setting.web.web_setting/getRechargeOption' })
}

// 设置支付方式
export function setRechargeOption(params: any) {
    return request.post({ url: '/setting.web.web_setting/setRechargeOption', params })
}

// 设置支付方式
export function setPayConfig(params: any) {
    return request.post({ url: '/setting.pay.pay_config/setConfig', params })
}

// 设置支付方式
export function getPayConfig(params: any) {
    return request.get({ url: '/setting.pay.pay_config/getConfig', params })
}
