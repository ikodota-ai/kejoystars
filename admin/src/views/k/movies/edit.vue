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
                <el-row>
                    <el-col :span="24">
                        <div class="grid-content bg-purple-dark">
                            <el-form-item label="抓取链接">
                                <el-input
                                        v-model="url"
                                        placeholder="请输入文章标题"
                                        type="text"
                                        :autosize="{ minRows: 3, maxRows: 3 }"
                                        class="w-[90%]"
                                        show-word-limit
                                        clearable
                                    />
                                    <el-button type="primary" class="ml-[10px]" @click="grab()">开始抓取</el-button>
                            </el-form-item>
                        </div>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <div class="grid-content bg-purple">
                            <el-form-item label="影视名称" prop="movieName">
                                <el-input
                                        v-model="formData.movieName"
                                        placeholder="请输入影视名称"
                                        type="text"
                                        :autosize="{ minRows: 3, maxRows: 3 }"
                                        maxlength="64"
                                        show-word-limit
                                        clearable
                                    />
                            </el-form-item>
                            <el-form-item label="又名">
                                <el-input
                                        v-model="formData.movieEnName"
                                        placeholder="请输入影视别的名称"
                                        type="text"
                                        :autosize="{ minRows: 3, maxRows: 3 }"
                                        maxlength="64"
                                        show-word-limit
                                        clearable
                                    />
                            </el-form-item>
                            <el-form-item label="播出平台">
                                <el-input
                                        v-model="formData.playPlatform"
                                        placeholder="请输入播出平台"
                                        type="text"
                                        :autosize="{ minRows: 3, maxRows: 3 }"
                                        maxlength="64"
                                        show-word-limit
                                        clearable
                                    />
                            </el-form-item>
                            <el-form-item label="播出国家">
                                <el-radio-group v-model="formData.country">
                                    <el-radio-button v-for="item in countryOptions.value"
                                    :key="item.value" 
                                    :label="item.value">{{ item.name }}</el-radio-button>
                                </el-radio-group>
                            </el-form-item>
                            <el-row>
                                <el-col :span="12">
                                    <div class="grid-content bg-purple-light">
                                        <el-form-item label="开播时间">
                                            <el-date-picker
                                                v-model="formData.startPlayTime"
                                                type="date"
                                                format="YYYY 年 MM 月 DD 日"
                                                value-format="YYYY-MM-DD"
                                                @change="console.log(formData.startPlayTime)"
                                                placeholder="选择日期">
                                            </el-date-picker>
                                        </el-form-item>
                                    </div>
                                </el-col>
                                <el-col :span="12">
                                    <div class="grid-content bg-purple-light">
                                        <el-form-item label="更新每周几">
                                            <el-select v-model="formData.updateWeek" class="!w-[100px]" placeholder="(1-6=周一到周六, 0=周日)">
                                                <el-option
                                                v-for="item in weekOptions"
                                                :key="item.value"
                                                :label="item.label"
                                                :value="item.value">
                                                </el-option>
                                            </el-select>
                                            <el-time-picker
                                                class="w-[120px] ml-[10px]"
                                                arrow-control
                                                v-model="formData.updateTime"
                                                format="HH:mm:ss"
                                                value-format="HH:mm:ss"
                                                :picker-options="{
                                                    selectableRange: '00:00:00 - 23:59:59'
                                                }"
                                                placeholder="选择时间">
                                            </el-time-picker>
                                        </el-form-item>
                                    </div>
                                </el-col>
                            </el-row>
                            <el-row>
                                <el-col :span="12">
                                    <div class="grid-content bg-purple-light">
                                        <el-form-item label="关联明星">
                                            <el-select class="w-[211px]"
                                                v-model="selectedSid"
                                                filterable
                                                remote
                                                reserve-keyword
                                                clearable
                                                placeholder="搜索明星(中/英文名)"
                                                :remote-method="remoteStar"
                                                :loading="starLoading"
                                            >
                                                <el-option
                                                    v-for="item in starOptions"
                                                    :key="item.value"
                                                    :label="item.label"
                                                    :value="item.value"
                                                />
                                            </el-select>
                                        </el-form-item>
                                    </div>
                                </el-col>
                                <el-col :span="12">
                                    <div class="grid-content bg-purple-light">
                                        <el-form-item label="饰&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;演">
                                            <el-input  class="w-[160px]"
                                                v-model="cosplay"
                                                placeholder="请输入饰演角色"
                                                type="text"
                                                :autosize="{ minRows: 3, maxRows: 3 }"
                                                maxlength="64"
                                                show-word-limit
                                                clearable
                                            />
                                            <el-button type="info" class="ml-[9px]" @click="addActor()">增加</el-button>
                                        </el-form-item>
                                    </div>
                                </el-col>
                            </el-row>
                            <el-form-item label="">
                                <div style="height: 80px;max-height: 80px; overflow-x: auto; width: 100%;">
                                    <el-row class="w-[100%] p-[5px]" style="border-bottom: 1px dotted #cdcdcd;" v-for="item in actorList">
                                        <el-col :span="8">{{ item.star }}</el-col>
                                        <el-col :span="4" style="text-align:center">饰演</el-col>
                                        <el-col :span="8">{{ item.cosplay }}</el-col>
                                        <el-col :span="4" style="text-align:center; cursor: pointer;" @click="removeActor(item)">X</el-col>
                                    </el-row>
                                </div>
                            </el-form-item>
                        </div>
                    </el-col>
                    <el-col :span="12">
                        <div class="grid-content bg-purple-light">
                            <el-form-item label="封&nbsp;&nbsp;面&nbsp;&nbsp;照" prop="image">
                                <material-picker style="width: 120px;" v-if="formData.movieCover== ''" v-model="formData.image" />
                                <material-picker style="width: 150px;" v-else v-model="formData.movieCover" />
                            </el-form-item>
                            <el-row>
                                <el-col :span="14">
                                    <div class="grid-content bg-purple-light" prop="status">
                                        <el-form-item label="播出状态">
                                            <el-radio v-model="formData.status" label="W">将要播出</el-radio>
                                            <el-radio v-model="formData.status" label="P">热播</el-radio>
                                            <el-radio v-model="formData.status" label="D">已完结</el-radio>
                                        </el-form-item>
                                    </div>
                                </el-col>
                                <el-col :span="10">
                                    <div class="grid-content bg-purple-light">
                                        <el-form-item label="播出状态" prop="movieType">
                                            <el-radio v-model="formData.movieType" label="TV">电视剧</el-radio>
                                            <el-radio v-model="formData.movieType" label="MOVIE">电影</el-radio>
                                        </el-form-item>
                                    </div>
                                </el-col>
                            </el-row>
                            <el-form-item label="总共集数">
                                <el-input v-model="formData.quantity" clearable />
                            </el-form-item>
                            <el-form-item label="制作公司">
                                <el-input v-model="formData.company" clearable />
                            </el-form-item>
                            <el-form-item label="内容简介">
                                <el-input v-model="formData.content" type="textarea" clearable />
                            </el-form-item>
                        </div>
                    </el-col>
                </el-row>
            </el-form>
        </el-card>
        <footer-btns>
            <el-button type="primary" @click="handleSave">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup name="MovieListsEdit">
import type { FormInstance } from 'element-plus'
import { starLists } from '@/api/k/star'
import { moviesGrab, moviesAdd, moviesDetail, moviesEdit } from '@/api/k/Movies'
import useMultipleTabs from '@/hooks/useMultipleTabs'
import { dictDataAll } from '@/api/setting/dict'
import feedback from '@/utils/feedback'

const route = useRoute()
const router = useRouter()
interface Actor {
  star: string
  cosplay: string
  sid:number
}

const actorList = ref<Actor[]>([])
let cosplay = ref('')

interface StarOption {
  value: number
  label: string
  name: string
}
const starOptions = ref<StarOption[]>([])
const starLoading = ref(false)
const selectedSid = ref<number | ''>('')

const remoteStar = async (query: string) => {
    if (!query) {
        starOptions.value = []
        return
    }
    starLoading.value = true
    try {
        const data = await starLists({ name: query, page_size: 20 })
        starOptions.value = (data.lists || []).map((item: any) => ({
            value: item.id,
            name: item.name,
            label: item.enName ? `${item.name}（${item.enName}）` : item.name
        }))
    } finally {
        starLoading.value = false
    }
}
const formData = reactive({
    id: '',
    movieName: '',
    movieEnName: '',
    movieType : "",
    image: '',
    movieCover:'',
    company: '',
    country : 1,
    playPlatform: '',
    quantity: '',
    startPlayTime:'',
    content: '',
    updateWeek: '',
    updateTime:'',
    cp: 0,
    actorList: '',
    status: 'P'
})

const { removeTab } = useMultipleTabs()
const formRef = shallowRef<FormInstance>()
const rules = reactive({
    movieName: [{ required: true, message: '请输入电影名称', trigger: 'blur' }],
    image: [{ required: true, message: '请选上传电影封面照', trigger: 'blur' }],
    country: [{ required: true, message: '请选择播出国家', trigger: 'blur' }],
    status: [{ required: true, message: '请选择影视状态', trigger: 'blur' }],
    movieType: [{ required: true, message: '请选择影视类型', trigger: 'blur' }]
})

const getDetails = async () => {
    const data = await moviesDetail({
        id: route.query.id
    })
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        if(data[key]!==undefined) formData[key] = data[key]
    })

    actorList.value = JSON.parse(data.actorList);
}


let url = ref('');
const grab = async()=>{
    const data = await moviesGrab({url:url.value})

    formData.movieName = data.movieName
    formData.content = data.description
    formData.image = data.image
    formData.movieCover = data.movieCover
    if(data.country < 24 ) {
        formData.country = data.country -13
    } else if(data.country == 24 ) {
        formData.country = 0
    } else if(data.country > 24 ) {
        formData.country = data.country -14
    }
    
    formData.startPlayTime = data.releaseDate
    actorList.value = []
    for(let i in data.femaleActors) {
        actorList.value.push({"star": data.femaleActors[i].name, "cosplay": data.femaleActors[i].cosplay, "sid":data.femaleActors[i].sid});
    }
}

// 监听 movieCover 值的变化
watch(
  () => formData.image,
  (newVal, oldVal) => {
    console.log('movieCover 值已改变'+formData.movieCover)
    console.log('旧值:', oldVal)
    console.log('新值:', newVal)
  }
)

const handleSave = async () => {
    await formRef.value?.validate()
    if(actorList.value.length > 0) {
        formData.actorList = JSON.stringify(actorList.value)
    } else {
        formData.actorList = "[]"
    }

    if(formData.startPlayTime != null && formData.startPlayTime!='')formData.startPlayTime = formData.startPlayTime + " 00:00:00"
    // let newData = Object.fromEntries(Object.entries(formData).filter(([key, value]) => value !== ""));

    if (route.query.id) {
        await moviesEdit(formData)
    } else {
        if(formData.image == null || formData.image=='') {
            if(formData.movieCover!=null && formData.movieCover!='')formData.image=formData.movieCover
        }
        await moviesAdd(formData)
    }
    removeTab()
    router.back()
}

const countryOptions = reactive<{
    value: any[]
}>({
    value: []
})
const getCountry = async () => {
    const data = await dictDataAll({ type_id:6, dictType:"country_area" })
    countryOptions.value = data;
}

const addActor = async()=>{
    if (!selectedSid.value) {
        await feedback.confirm('请先搜索并选择明星！')
        return
    }
    const selected = starOptions.value.find(item => item.value === selectedSid.value)
    if (!selected) {
        await feedback.confirm('请先搜索并选择明星！')
        return
    }
    if (actorList.value.some(item => item.sid === selected.value)) {
        await feedback.confirm('该明星已添加！')
        return
    }
    actorList.value.push({ star: selected.name, cosplay: cosplay.value, sid: selected.value })
    setTimeout(() => {
        selectedSid.value = ''
        starOptions.value = []
        cosplay.value = ""
    }, 200) // 防抖300ms
}

const removeActor = ( value : Actor)=>{
    const index = actorList.value.indexOf(value);
    if (index > -1) {
        actorList.value.splice(index, 1);
    }
}

getCountry()

route.query.id && getDetails()

const weekOptions = [
    {
        value: "1",
        label: "周一"
    },
    {
        value: "2",
        label: "周二"
    },
    {
        value: "3",
        label: "周三"
    },
    {
        value: "4",
        label: "周四"
    },
    {
        value: "5",
        label: "周五"
    },
    {
        value: "6",
        label: "周六"
    },
    {
        value: "0",
        label: "周天"
    }
]
</script>
