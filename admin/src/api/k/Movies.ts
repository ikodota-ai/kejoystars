import request from '@/utils/request'

// 【请填写功能名称】列表
export function moviesLists(params?: Record<string, any>) {
    return request.get({ url: '/k.movies/list', params })
}

// 【请填写功能名称】详情
export function moviesDetail(params: Record<string, any>) {
    return request.get({ url: '/k.movies/detail', params })
}

// 【请填写功能名称】新增
export function moviesAdd(params: Record<string, any>) {
    return request.post({ url: '/k.movies/add', params })
}

// 【请填写功能名称】编辑
export function moviesEdit(params: Record<string, any>) {
    return request.post({ url: '/k.movies/edit', params })
}

// 【请填写功能名称】删除
export function moviesDelete(params: Record<string, any>) {
    return request.post({ url: '/k.movies/del', params })
}

export function moviesGrab(params: Record<string, any>) {
    return request.get({ url: '/k.movies/grab', params })
}

