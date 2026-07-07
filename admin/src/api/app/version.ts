import request from '@/utils/request'

// APP版本列表
export function appVersionLists(params?: Record<string, any>) {
    return request.get({ url: '/app.version/list', params })
}

// APP版本详情
export function appVersionDetail(params: Record<string, any>) {
    return request.get({ url: '/app.version/detail', params })
}

// APP版本新增
export function appVersionAdd(params: Record<string, any>) {
    return request.post({ url: '/app.version/add', params })
}

// APP版本编辑
export function appVersionEdit(params: Record<string, any>) {
    return request.post({ url: '/app.version/edit', params })
}

// APP版本删除
export function appVersionDelete(params: Record<string, any>) {
    return request.post({ url: '/app.version/del', params })
}

// APP版本发布/上下线
export function appVersionPublish(params: Record<string, any>) {
    return request.post({ url: '/app.version/publish', params })
}
