import request from '@/utils/request'

// 【请填写功能名称】列表
export function instagramLists(params?: Record<string, any>) {
    return request.get({ url: '/k.instagram/list', params })
}

// 【请填写功能名称】详情
export function instagramDetail(params: Record<string, any>) {
    return request.get({ url: '/k.instagram/detail', params })
}

// 【请填写功能名称】新增
export function instagramAdd(params: Record<string, any>) {
    return request.post({ url: '/k.instagram/add', params })
}

// 【请填写功能名称】编辑
export function instagramEdit(params: Record<string, any>) {
    return request.post({ url: '/k.instagram/edit', params })
}

// 【请填写功能名称】删除
export function instagramDelete(params: Record<string, any>) {
    return request.post({ url: '/k.instagram/del', params })
}
