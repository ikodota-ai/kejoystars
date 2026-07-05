<template>
    <div class="article-edit">
        <el-card class="!border-none" shadow="never">
            <el-page-header :content="$route.meta.title" @back="$router.back()" />
        </el-card>
        <el-card class="mt-4 !border-none" shadow="never">
            <el-form
                ref="formRef"
                class="ls-form"
                :model="formData"
                label-width="85px"
                :rules="rules"
            >
                <div class="xl:flex">
                    <div>
                        <el-form-item label="文章标题" prop="title">
                            <div class="w-80">
                                <el-input
                                    v-model="formData.title"
                                    placeholder="请输入文章标题"
                                    :autosize="{ minRows: 3, maxRows: 3 }"
                                    maxlength="64"
                                    show-word-limit
                                    clearable
                                />
                            </div>
                        </el-form-item>
                        <!-- <el-form-item label="关联影视" prop="movieName">
                            <el-input
                                    v-model="formData.movieName"
                                    placeholder="请输入关联影视"
                                    :autosize="{ minRows: 3, maxRows: 3 }"
                                    maxlength="64"
                                    show-word-limit
                                    clearable
                                    @blur="searchMovie" 
                                />
                            <el-input
                                    v-model="formData.movieId"
                                    placeholder="请输入关联影视"
                                    type="hidden"
                                    clearable
                                />
                        </el-form-item> -->
                        <el-form-item label="关联影视" prop="movieName">
                            <el-select v-model="formData.movieId" class="w-[320px]" 
                                    filterable
                                    remote
                                    reserve-keyword
                                    placeholder="请输入关联影视"
                                    :remote-method="remoteMethod2"
                                    :key="selectKey" 
                                >
                                    <el-option
                                        v-for="item in options2"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                        </el-form-item>
                        <el-form-item label="链接">
                            <div class="w-80">
                                <el-input
                                    v-model="formData.url"
                                    placeholder="请输入链接"
                                    :autosize="{ minRows: 3, maxRows: 3 }"
                                    maxlength="64"
                                    show-word-limit
                                    clearable
                                />
                            </div>
                        </el-form-item>
                        <!-- <el-form-item label="文章栏目" prop="cid">
                            <el-select
                                class="w-80"
                                v-model="formData.cid"
                                placeholder="请选择文章栏目"
                                clearable
                            >
                                <el-option
                                    v-for="item in optionsData.article_cate"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id"
                                />
                            </el-select>
                        </el-form-item> -->
                        <!-- <el-form-item label="文章简介" prop="desc">
                            <div class="w-80">
                                <el-input
                                    v-model="formData.desc"
                                    placeholder="请输入文章简介"
                                    type="textarea"
                                    :autosize="{ minRows: 3, maxRows: 6 }"
                                    :maxlength="200"
                                    show-word-limit
                                    clearable
                                />
                            </div>
                        </el-form-item>
                        <el-form-item label="摘要" prop="abstract">
                            <div class="w-80">
                                <el-input
                                    type="textarea"
                                    :autosize="{ minRows: 6, maxRows: 6 }"
                                    v-model="formData.abstract"
                                    maxlength="200"
                                    show-word-limit
                                    clearable
                                />
                            </div>
                        </el-form-item> -->
                        <el-form-item label="文章封面" prop="image">
                            <div>
                                <div>
                                    <material-picker v-model="formData.image" :limit="1" />
                                </div>
                                <div class="form-tips">建议尺寸：240*180px</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="发布时间">
                            <div class="w-80">
                                <el-date-picker
                                v-model="formData.createData"
                                type="datetime"
                                placeholder="选择日期时间">
                                </el-date-picker>
                            </div>
                        </el-form-item>
                        <!-- <el-form-item label="作者" prop="author">
                            <div class="w-80">
                                <el-input v-model="formData.author" placeholder="请输入作者名称" />
                            </div>
                        </el-form-item> -->
                        <!-- <el-form-item label="排序" prop="sort">
                            <div>
                                <el-input-number v-model="formData.sort" :min="0" :max="9999" />
                                <div class="form-tips">默认为0， 数值越大越排前</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="初始浏览量" prop="click_virtual">
                            <div>
                                <el-input-number v-model="formData.click_virtual" :min="0" />
                            </div>
                        </el-form-item> -->
                        <el-form-item label="文章状态" required prop="isShow">
                            <el-radio-group v-model="formData.isShow">
                                <el-radio :label="1">显示</el-radio>
                                <el-radio :label="0">隐藏</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </div>
                    <div class="xl:ml-20">
                        <el-form-item label="文章内容" prop="content">
                            <editor v-model="formData.content" :height="367" />
                        </el-form-item>
                    </div>
                </div>
            </el-form>
        </el-card>
        <footer-btns>
            <el-button type="primary" @click="handleSave">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup name="articleListsEdit">
import type { FormInstance } from 'element-plus'

import { articleAdd, articleCateAll, articleDetail, articleEdit } from '@/api/article'
import { useDictOptions } from '@/hooks/useDictOptions'
import useMultipleTabs from '@/hooks/useMultipleTabs'
import { moviesLists } from '@/api/k/Movies'
import feedback from '@/utils/feedback'

let selectKey = 0;
const route = useRoute()
const router = useRouter()
const formData = reactive({
    id: '',
    title: '',
    url:'',
    movieId : '',
    movieName:'',
    image: '',
    cid: 3,
    cpid:0,
    desc: '',
    author: '',
    content: '',
    click_virtual: 0,
    sort: 0,
    isShow: 1,
    abstract: '',
    createData:'',
    createTime:0
})

const { removeTab } = useMultipleTabs()
const formRef = shallowRef<FormInstance>()
const rules = reactive({
    title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
    // movieId: [{ required: true, message: '关联影视不存在', trigger: 'blur' }],
    // movieName: [{ required: true, message: '关联影视不存在', trigger: 'blur' }],
    image: [{ required: true, message: '请上传封面照片', trigger: 'blur' }]
})

// const searchMovie = async() => {
//     const data = await moviesLists({movieName:formData.movieName});
//     if(data != null && data.count>0) {
//         if(data.count > 1 ){
//             await feedback.confirm('找到多个影视，请确认影视名称是否正确！')
//             return;
//         } else {
//             formData.movieId = data.lists[0].id
//             return
//         }
//     } 

//     await feedback.confirm('未找到影视，请确认影视名称是否正确！')
// }
let options2: OptionItem[]
const getDetails = async () => {
    const data = await articleDetail({
        id: route.query.id
    })
    if(data.movies != '' && data.movies != 'null') {
        let movies = JSON.parse(data.movies)
        formData.movieId = movies.id
        formData.movieName = movies.name
        options2 = [{"value": formData.movieId, "label":formData.movieName}]
    }
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })

    formData.createData = data.createTime
}

const { optionsData } = useDictOptions<{
    article_cate: any[]
}>({
    article_cate: {
        api: articleCateAll
    }
})
interface OptionItem {
  value: string | number
  label: string
  // 可扩展其他字段
}

const handleSave = async () => {
    await formRef.value?.validate()
    if(formData.createData!='' && formData.createData != null) {
        formData.createTime = parseDateToTimestamp(formData.createData)
    }
    for(const i in options2) {console.log(options2[i])
        if(options2[i].value == formData.movieId) {
            formData.movieName = options2[i].label
        }
    }
    
    if (route.query.id) {
        await articleEdit(formData)
    } else {
        await articleAdd(formData)
    }

    removeTab()
    router.back()
}

// 方法2：兼容ISO格式和自定义格式
const parseDateToTimestamp=(input: string | Date): number => {
    const date = typeof input === 'string' ? new Date(input) : input;
    if (isNaN(date.getTime())) {
        throw new Error('Invalid date format');
    }
    return Math.floor(date.getTime() / 1000); // 返回秒级时间戳
}

const remoteMethod2 = async (query:string) => {
    if (query !== '') {
        const data = await moviesLists({"movieName":query});
        setTimeout(() => {
            options2 = data.lists.map((item:any) => ({
                value: item.id,
                label: item.movieName
            }))
        }, 200)
        selectKey++
    } else {
        options2 = [];
    }
}

onMounted(() => {
  formData.cpid = Number(route.query.cpid) ;
})

route.query.id && getDetails()
</script>
