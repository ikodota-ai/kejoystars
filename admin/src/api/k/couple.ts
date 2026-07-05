import request from '@/utils/request'

// 明星列表
export function coupleLists(params?: any) {
    return request.get({ url: '/k.couple/list', params })
}

// 添加明星
export function coupleAdd(params?: any) {
    return request.post({ url: '/k.couple/add', params })
}

// 编辑明星
export function coupleEdit(params?: any) {
    return request.post({ url: '/k.couple/edit', params })
}

//  明星详情
export function coupleDetail(params?: any) {
    return request.get({ url: '/k.couple/detail', params })
}

//  明星详情
export function coupleDelete(params?: any) {
    return request.post({ url: '/k.couple/del', params })
}