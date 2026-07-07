<template>
    <div class="app-version">
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" inline>
                <el-form-item label="平台">
                    <el-select class="w-[160px]" v-model="queryParams.platform">
                        <el-option label="全部" value />
                        <el-option label="安卓" value="android" />
                        <el-option label="苹果" value="ios" />
                    </el-select>
                </el-form-item>
                <el-form-item label="通道">
                    <el-input class="w-[160px]" v-model="queryParams.channel" clearable @keyup.enter="resetPage" />
                </el-form-item>
                <el-form-item label="版本名称">
                    <el-input class="w-[160px]" v-model="queryParams.versionName" clearable @keyup.enter="resetPage" />
                </el-form-item>
                <el-form-item label="状态">
                    <el-select class="w-[160px]" v-model="queryParams.status">
                        <el-option label="全部" value />
                        <el-option label="草稿" value="draft" />
                        <el-option label="上线" value="online" />
                        <el-option label="下线" value="offline" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <div>
                <el-button v-perms="['app.version/add']" type="primary" @click="handleAdd">
                    <template #icon>
                        <icon name="el-icon-Plus" />
                    </template>
                    新增版本
                </el-button>
            </div>
            <div class="mt-4" v-loading="pager.loading">
                <el-table :data="pager.lists" size="large">
                    <el-table-column label="ID" prop="id" width="70" />
                    <el-table-column label="平台" min-width="80">
                        <template #default="{ row }">
                            <el-tag v-if="row.platform == 'ios'" type="info">苹果</el-tag>
                            <el-tag v-else type="success">安卓</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="通道" prop="channel" min-width="90" />
                    <el-table-column label="版本" min-width="120">
                        <template #default="{ row }">
                            {{ row.versionName }} ({{ row.versionCode }})
                        </template>
                    </el-table-column>
                    <el-table-column label="强更" min-width="70">
                        <template #default="{ row }">
                            <el-tag v-if="row.isForce == 1" type="danger">强制</el-tag>
                            <el-tag v-else type="info">建议</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="灰度" prop="grayPercent" min-width="70">
                        <template #default="{ row }">{{ row.grayPercent }}%</template>
                    </el-table-column>
                    <el-table-column label="状态" min-width="90">
                        <template #default="{ row }">
                            <el-tag v-if="row.status == 'online'" type="success">上线</el-tag>
                            <el-tag v-else-if="row.status == 'offline'" type="danger">下线</el-tag>
                            <el-tag v-else type="info">草稿</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="发布时间" prop="publishTime" min-width="170" />
                    <el-table-column label="创建时间" prop="createTime" min-width="170" />
                    <el-table-column label="操作" width="220" fixed="right">
                        <template #default="{ row }">
                            <el-button
                                v-if="row.status != 'online'"
                                v-perms="['app.version/publish']"
                                link
                                type="success"
                                @click="handlePublish(row, 'online')"
                            >
                                上线
                            </el-button>
                            <el-button
                                v-if="row.status == 'online'"
                                v-perms="['app.version/publish']"
                                link
                                type="warning"
                                @click="handlePublish(row, 'offline')"
                            >
                                下线
                            </el-button>
                            <el-button v-perms="['app.version/edit']" link type="primary" @click="handleEdit(row)">
                                编辑
                            </el-button>
                            <el-button v-perms="['app.version/del']" link type="danger" @click="handleDelete(row.id)">
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
                <div class="flex justify-end mt-4">
                    <pagination v-model="pager" @change="getLists" />
                </div>
            </div>
        </el-card>
        <edit-popup v-if="showEdit" ref="editRef" @success="getLists" @close="showEdit = false" />
    </div>
</template>

<script lang="ts" setup name="appVersion">
import { appVersionDelete, appVersionLists, appVersionPublish } from '@/api/app/version'
import { usePaging } from '@/hooks/usePaging'
import feedback from '@/utils/feedback'

import EditPopup from './edit.vue'

const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const showEdit = ref(false)
const queryParams = reactive({
    platform: '',
    channel: '',
    versionName: '',
    status: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: appVersionLists,
    params: queryParams
})

const handleAdd = async () => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('add')
}

const handleEdit = async (data: any) => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('edit')
    editRef.value?.setFormData(data)
}

const handlePublish = async (row: any, status: string) => {
    await feedback.confirm(status == 'online' ? '确定要上线该版本？(同平台同通道其它版本将自动下线)' : '确定要下线该版本？')
    await appVersionPublish({ id: row.id, status })
    getLists()
}

const handleDelete = async (id: number) => {
    await feedback.confirm('确定要删除？')
    await appVersionDelete({ id })
    getLists()
}

getLists()
</script>
