<template>
    <div @focus="getLists">
        <el-card class="!border-none" shadow="never">
            <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" :inline="true">
                
                <el-form-item label="名称">
                    <el-input
                        class="w-[200px]"
                        v-model="queryParams.movieName"
                        clearable
                        @keyup.enter="resetPage"
                    />
                </el-form-item>
                <el-form-item  style="width:310px; display:inline-block;">
                                <el-select v-model="queryParams.country" class="w-[300px]" 
                                    placeholder="国家区域"
                                >
                                    <el-option
                                        v-for="item in optionsCountry"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                    </el-form-item>
                <el-form-item label="类别">
                    <el-select v-model="queryParams.movieType" placeholder="请选择" class="w-[150px]">
                        <el-option
                        v-for="item in optionsType"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="状态">
                    <el-select v-model="queryParams.status" placeholder="请选择" class="w-[150px]">
                        <el-option
                        v-for="item in optionsStatus"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                        </el-option>
                    </el-select>
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
                    <router-link
                        v-perms="['k.movies/add']"
                        :to="{
                            path: getRoutePath('k.movies/add')
                        }"
                    >
                        <el-button type="primary" class="ml-4">
                            <template #icon>
                                <icon name="el-icon-Plus" />
                            </template>
                            添加影视
                        </el-button>
                    </router-link>
                </el-form-item>
            </el-form>
        </el-card>
        <el-card class="!border-none mt-4" shadow="never">
            <el-checkbox-group v-model="checkedMovies" @change="handleCheckedMoviesChange">
            <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                <el-table-column label="编号" min-width="25">
                    <template #default="scope">
                        <el-checkbox :label="scope.row.id" :key="scope.row.id">
                            {{ scope.row.id }}
                        </el-checkbox>
                    </template>
                </el-table-column>
                <el-table-column label="名称" prop="movieName" min-width="100">
                    <template #default="scope">
                        <span>{{ scope.row.movieName }}</span>
                        <span> - [{{ getCountryName(scope.row.country) }}]</span>
                    </template>
                </el-table-column>
                <el-table-column label="类别" prop="historyCount" min-width="80">
                    <template #default="scope">
                        <span>{{ getMovieType(scope.row.movieType) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="状态" prop="status" min-width="80" >
                    <template #default="scope">
                        <span>{{ getStatus(scope.row.status) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="添加日期" prop="createTime" min-width="100" />
                <el-table-column label="添加人" prop="createUser" min-width="100" />
                <el-table-column label="修改日期" prop="upTime" min-width="120" />
                <el-table-column label="修改人" prop="upUser" min-width="120" />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                        <el-button
                            v-perms="['k.movies/edit']"
                            type="primary"
                            link
                        >
                            <router-link
                                :to="{
                                    path: getRoutePath('k.movies/edit'),
                                    query: {
                                        id: row.id
                                    }
                                }"
                            >
                                编辑
                            </router-link>
                        </el-button>
                        <el-button
                            v-perms="['k.movies/del']"
                            type="danger"
                            link
                            @click="handleDelete([row.id])"
                        >
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
            </el-checkbox-group>
            <div class="flex justify-end mt-4">
                <div style="float: left;width: 50%;">
                    <el-checkbox :indeterminate="isMoviesIndeterminate" v-model="checkedMoviesAll" @change="handleCheckMoviesAllChange">全选</el-checkbox>
                    <el-button type="danger" v-perms="['k.star/del']" @click="handleDeleteAll()" style="margin-left: 20px;">删除</el-button>
                </div>
                <div style="float: right;width: 50%;">
                    <pagination v-model="pager" @change="getLists" />
                </div>
            </div>
        </el-card>
    </div>
</template>
<script lang="ts" setup name="consumerLists">
import { moviesLists,moviesDelete } from '@/api/k/Movies'
import { dictDataAll } from '@/api/setting/dict'
import { usePaging } from '@/hooks/usePaging'
import { getRoutePath } from '@/router'
import EditPopup from './edit.vue'
import feedback from '@/utils/feedback'

let checkedMovies = ref<number[]>([])
let isMoviesIndeterminate = ref(false)
let checkedMoviesAll = ref(false)

const queryParams = reactive({
    movieName: '',
    movieType: '',
    country :'',
    status: '',
    create_time_start: '',
    create_time_end: ''
})
const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const showEdit = ref(false)


const handleCheckMoviesAllChange = (val:any)=>{
    if(checkedMoviesAll.value) {
        const checked = pager.lists.map((item:any) => item.id)
        checkedMovies.value = checked // 修改ref的值而非重新赋值
    } else {
        checkedMovies.value = []
    }
    isMoviesIndeterminate.value = false;
}

const handleCheckedMoviesChange=(value:number)=> {
    console.log(checkedMovies);
}
const handleDeleteAll = async ()=>{
    handleDelete(checkedMovies.value);
}
const handleDelete = async (id: number[]) => {
    await feedback.confirm('确定要删除？')
    await moviesDelete(id)
    getLists()
}

let optionsType = [{"value":"TV", "label":"电视剧"}, {"value":"MOVIE", "label":"电影"}]
let optionsStatus = [{"value":"P", "label":"进行中"}, {"value":"W", "label":"将要播出"}, {"value":"D", "label":"已完结"}]

const getStatus=(code:string) => {
    switch(code) {
        case "P":
            return "进行中"
            break
        case "W":
            return "将要播出"
            break
        case "D":
            return "已完结"
            break
    }
    return ""
}

const getMovieType=(code:string) => {
    return code == "TV" ? "电视剧" : "电影"
}

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: moviesLists,
    params: queryParams
})

interface OptionItem {
  value: string | number;
  label: string;
  // 可扩展其他字段
}
let optionsCountry = ref<OptionItem[]>([])
const getCountry = async () => {
    const data = await dictDataAll({ type_id:6, dictType:"country_area" })
    if (data != null) {
        setTimeout(() => {
            optionsCountry.value = data.map((item:any) => ({
                value: item.value,
                label: item.name
            }))
        }, 200);
    }
}

const getCountryName = ( id : number ) =>{
    for( let i in optionsCountry.value) {
        if (optionsCountry.value[i].value == id) {
            return optionsCountry.value[i].label
        }
    }

    return "未知"
}

onActivated(() => {
    getCountry()
    getLists()
})
</script>
