<template>
    <div class="article-lists">
        <el-card class="!border-none" shadow="never">CP: {{ queryParams.cp }}
            <!-- <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" :inline="true">
                <el-form-item label="文章标题">
                    <el-input
                        class="w-[280px]"
                        v-model="queryParams.title"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item label="栏目名称">
                    <el-select class="w-[280px]" v-model="queryParams.cid">
                        <el-option label="全部" value />
                        <el-option
                            v-for="item in optionsData.article_cate"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="文章状态">
                    <el-select class="w-[280px]" v-model="queryParams.is_show">
                        <el-option label="全部" value />
                        <el-option label="显示" :value="1" />
                        <el-option label="隐藏" :value="0" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="resetPage">查询</el-button>
                    <el-button @click="resetParams">重置</el-button>
                </el-form-item>
            </el-form> -->
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <div>  
                <router-link
                    v-perms="['article.article/add','article.article/add:edit']"
                    :to="{
                        path : getRoutePath('article.article/add:edit'),
                        query:{cpid:queryParams.cpid}
                    }"
                >
                    <el-button type="primary" class="mb-4">
                        <template #icon>
                            <icon name="el-icon-Plus" />
                        </template>
                        发布资讯
                    </el-button>
                </router-link>
            </div>
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="ID" prop="id" min-width="80" />
                <el-table-column label="封面" min-width="100">
                    <template #default="{ row }">
                        <image-contain
                            v-if="row.image"
                            :src="row.image"
                            :width="60"
                            :height="45"
                            :preview-src-list="[row.image]"
                            preview-teleported
                            fit="contain"
                        />
                    </template>
                </el-table-column>
                <el-table-column
                    label="关联影视"
                    prop="movieName"
                    min-width="160"
                    show-tooltip-when-overflow
                />
                <el-table-column
                    label="标题"
                    prop="title"
                    min-width="160"
                    show-tooltip-when-overflow
                />
                <el-table-column label="栏目" prop="cateName" min-width="100" />
                <el-table-column label="作者" prop="author" min-width="120" />
                <!-- <el-table-column label="浏览量" prop="click" min-width="100" /> -->
                <el-table-column label="状态" min-width="100">
                    <template #default="{ row }">
                        <el-switch
                            v-perms="['article.article/updateStatus']"
                            v-model="row.isShow"
                            :active-value="1"
                            :inactive-value="0"
                            @change="changeStatus($event, row.id)"
                        />
                    </template>
                </el-table-column>
                <!--
                <el-table-column label="排序" prop="sort" min-width="100" />
                -->
                <el-table-column label="发布时间" prop="createTime" min-width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['article.article/edit','article.article/add:edit']"
                            type="primary"
                            link
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('article.article/add:edit'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                编辑
                            </router-link>
                        </el-button>
                        <el-button
                            v-perms="['article.article/delete']"
                            type="danger"
                            link
                            @click="handleDelete(row.id)"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div class="flex justify-end mt-4">
                <pagination v-model="pager" @change="getLists" />
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup name="articleLists">
import { articleCateAll, articleDelete, articleLists, articleStatus } from '@/api/article'
import { useDictOptions } from '@/hooks/useDictOptions'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import feedback from '@/utils/feedback'

const route = useRoute()
const queryParams = reactive({
    title: '',
    cp:'',
    cid: 3,
    cpid:0,
    is_show: ''
})

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: articleLists,
    params: queryParams
})

const { optionsData } = useDictOptions<{
    article_cate: any[]
}>({
    article_cate: {
        api: articleCateAll
    }
})

const changeStatus = async (is_show: any, id: number) => {
    try {
        await articleStatus({ id, is_show })
        getLists()
    } catch (error) {
        getLists()
    }
}

const handleDelete = async (id: number) => {
    await feedback.confirm('确定要删除？')
    await articleDelete({ id })
    getLists()
}
onMounted(() => {
  queryParams.cpid = Number(route.query.cpid);
  queryParams.cp = route.query.cp+'';
})
onActivated(() => {
    getLists()
})
</script>
