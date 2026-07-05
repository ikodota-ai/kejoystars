import request from '@/utils/request'

// 明星列表
export function starLists(params: Record<string, any>) {
    return request.get({ url: '/k.star/list', params })
}

// 添加明星
export function starAdd(params: Record<string, any>) {
    return request.post({ url: '/k.star/add', params })
}

// 编辑明星
export function starEdit(params: Record<string, any>) {
    return request.post({ url: '/k.star/edit', params })
}

//  明星详情
export function starDetail(params: Record<string, any>) {
    return request.get({ url: '/k.star/detail', params })
}

//  明星详情
export function starDelete(params: Record<string, any>) {
    return request.post({ url: '/k.star/del', params })
}