<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" :inline="true">
                <el-form-item label="名称">
                    <el-input
                        class="w-[150px]"
                        v-model="queryParams.name"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="明星/演员">
                    <el-input
                        class="w-[150px]"
                        v-model="queryParams.star"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="添加时间">
                    <el-date-picker
                        v-model="queryParams.create_time_start"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期">
                    </el-date-picker>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                    <el-button type="primary" v-perms="['k.couple/add']" @click="handleAdd()">
                    <template #icon>
                        <icon name="el-icon-Plus" />
                    </template>
                    添加档案
                </el-button>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-checkbox-group v-model="checkedCps" @change="handleCheckedCpsChange">
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="编号" min-width="25">
                    <template #default="scope">
                        <el-checkbox :label="scope.row.id" :key="scope.row.id">
                            {{ scope.row.id }}
                        </el-checkbox>
                    </template>
                </el-table-column>
                <el-table-column label="名称" min-width="100">
                    <template #default="scope">
                        <el-image 
                            style="width: 50px; height: 50px;"
                            :src="scope.row.image" 
                            :preview-src-list="[scope.row.image]">
                        </el-image>
                        <span>{{ scope.row.name }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="明星">
                    
                </el-table-column>
                <el-table-column label="收藏人数" prop="historyCount" min-width="50" />
                <el-table-column label="添加日期" prop="createTime" min-width="100" />
                <el-table-column label="添加人" prop="createUser" min-width="100" />
                <el-table-column label="修改日期" prop="updateTime" min-width="120" />
                <el-table-column label="修改人" prop="updateUser" min-width="120" />
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['k.couple/edit']"
                            type="primary"
                            link
                            @click="handleEdit(row)"
                        >
                            编辑
                        </el-button>
                        <el-button
                            v-perms="['k.couple/del']"
                            type="danger"
                            link
                            @click="handleDelete([row.id])"
                        >
                            删除
                        </el-button>
                        <router-link
                            v-perms="['article.article/lists', 'article.article/add:edit']"
                            :to="{
                                path: getRoutePath('article.article/lists'),
                                query:{cpid:row.id, cp:row.name}
                            }"
                        >
                            <el-button link type="warning">
                                资讯
                            </el-button>
                        </router-link>
                    </template>
                </el-table-column>
            </el-table>
            </el-checkbox-group>
            <div class="flex justify-end mt-4">
                <div style="float: left;width: 50%;">
                    <el-checkbox :indeterminate="isCpsIndeterminate" v-model="checkedCpsAll" @change="handleCheckCpsAllChange">全选</el-checkbox>
                    <el-button type="danger" v-perms="['k.star/del']" @click="handleDeleteAll()" style="margin-left: 20px;">删除</el-button>
                </div>
                <div style="float: right;width: 50%;">
                    <pagination v-model="pager" @change="getLists" />
                </div>
            </div>
        </el-card>
        <edit-popup v-if="showEdit" ref="editRef" @success="getLists" @close="showEdit = false" />
    </div>
</template>
<script lang="ts" setup name="coupleLists">
import { coupleLists, coupleDelete } from '@/api/k/couple'
import { usePaging } from '@/hooks/usePaging'
import EditPopup from './edit.vue'
import feedback from '@/utils/feedback'
import { getRoutePath } from '@/router'

let checkedCps = ref<number[]>([])
let isCpsIndeterminate = ref(false)
let checkedCpsAll = ref(false)

const queryParams = reactive({
    name: '',
    star: '',
    create_time_start: '',
    create_time_end: ''
})
const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const showEdit = ref(false)

const handleCheckCpsAllChange = (val:any)=>{
    if(checkedCpsAll.value) {
        const checked = pager.lists.map((item:any) => item.id)
        checkedCps.value = checked // 修改ref的值而非重新赋值
    } else {
        checkedCps.value = []
    }
    isCpsIndeterminate.value = false;
}

const handleCheckedCpsChange=(value:number)=> {
    console.log(checkedCps);
}
const handleDeleteAll = async ()=>{
    handleDelete(checkedCps.value);
}

const handleAdd = async (id?: number) => {
    showEdit.value = true
    await nextTick()
    if (id) {
        editRef.value?.setFormData({
            pid: id
        })
    }
    editRef.value?.open('add')
}

const handleEdit = async (data: any) => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('edit')
    editRef.value?.getDetail(data)
}

const handleDelete = async (id: number[]) => {
    await feedback.confirm('确定要删除？')
    await coupleDelete(id)
    getLists()
}

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: coupleLists,
    params: queryParams
})
onActivated(() => {
    getLists()
})


</script>
