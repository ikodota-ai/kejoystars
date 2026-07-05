<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            :title="popupTitle"
            :async="true"
            width="850px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
                <el-row>
                    <el-col :span="12">
                        <div class="grid-content bg-purple">
                            <el-form-item label="姓&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;名" prop="name">
                                <el-input v-model="formData.name" placeholder="请输入明星/演员姓名" clearable />
                            </el-form-item>
                            <el-form-item label="备&nbsp;&nbsp;用&nbsp;&nbsp;名">
                                <el-input v-model="formData.enName" clearable />
                            </el-form-item>
                            <el-form-item  label="基础信息" prop="birthday">
                                <el-date-picker
                                    v-model="formData.birthday"
                                    class="!w-[150px]"
                                    type="date"
                                    format="YYYY 年 MM 月 DD 日"
                                    value-format="YYYY-MM-DD"
                                    placeholder="生日">
                                </el-date-picker>
                                <el-input v-model="formData.height" class="w-[60px] pl-[5px]" placeholder="身高" clearable />&nbsp;CM
                                <el-input v-model="formData.weight" class="w-[60px] pl-[5px]" placeholder="体重KG" clearable />&nbsp;KG
                            </el-form-item>
                            <el-form-item label="明星国籍" prop="country">
                                <el-radio-group v-model="formData.country">
                                    <el-radio-button v-for="item in countryOptions.value"
                                    :key="item.value" 
                                    :label="item.value">{{ item.name }}</el-radio-button>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item label="毕业院校" prop="university">
                                <el-input v-model="formData.university" class="w-[50%]" placeholder="请输入毕业院校" clearable />
                                <el-input v-model="formData.universityDepartments" class="w-[50%]" placeholder="请输就读专业" clearable />
                            </el-form-item>
                            <el-form-item
                                label="关联影视"
                                prop="selected"
                            >
                                <el-select v-model="movies" class="w-[408px]" 
                                    filterable
                                    multiple
                                    remote
                                    reserve-keyword
                                    placeholder="请输入关联影视（可多选）"
                                    :remote-method="remoteMethod2"
                                >
                                    <el-option
                                        v-for="item in options2"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item
                                label=" "
                            >
                                <div style="width: 100%;height: 30px;line-height: 30px;"></div>
                            </el-form-item>
                        </div>
                    </el-col>
                    <el-col :span="12">
                        <div class="grid-content bg-purple-light">
                            <el-form-item label="明星照片" prop="avatar">
                                <material-picker v-model="formData.avatar" />
                            </el-form-item>
                            <el-form-item label="Instagram" prop="params">
                                <el-input v-model="formData.x" clearable />
                            </el-form-item>
                            <el-form-item label="Twitter" prop="params">
                                <el-input v-model="formData.twitter" clearable />
                            </el-form-item>
                            <el-form-item
                                label="Tiktok"
                                prop="selected"
                            >
                                <el-input v-model="formData.tiktok" clearable />
                            </el-form-item>
                        </div>
                    </el-col>
                </el-row>
            </el-form>
        </popup>
    </div>
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import { starAdd, starDetail, starEdit } from '@/api/k/star'
import { moviesDetail, moviesLists } from '@/api/k/Movies'
import Popup from '@/components/popup/index.vue'
import { dictDataAll } from '@/api/setting/dict'
import { getModulesKey } from '@/router'
import { fromPairs } from 'lodash'

const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')
const popupTitle = computed(() => {
    return mode.value == 'edit' ? '编辑明星/演员' : '新增明星/演员'
})
const componentsOptions = ref(getModulesKey())
let movies= ref([])
const querySearch = (queryString: string, cb: any) => {
    const results = queryString
        ? componentsOptions.value.filter((item) =>
              item.toLowerCase().includes(queryString.toLowerCase())
          )
        : componentsOptions.value
    cb(results.map((item) => ({ value: item })))
}

const formData = reactive({
    id: '',
    name: '',
    enName: '',
    birthday: '',
    height: '',
    weight: '',
    country: -1,
    university: '',
    universityDepartments: '',
    avatar: '',
    movies: '',
    x: '',
    twitter: '',
    tiktok: ''
})

const formRules = {
    name: [
        {
            required: true,
            message: '请输入明星姓名',
            trigger: ['blur', 'change']
        }
    ],
    country: [
        {
            required: true,
            message: '请选择明星国家',
            trigger: 'blur'
        }
    ],
    avatar: [
        {
            required: true,
            message: '请上传明星照片',
            trigger: 'blur'
        }
    ]
}


const handleSubmit = async () => {
    await formRef.value?.validate()
    formData.movies = JSON.stringify(movies.value)
    if(formData.birthday != null && formData.birthday!='') formData.birthday = formData.birthday + " 00:00:00"
    mode.value == 'edit' ? await starEdit(formData) : await starAdd(formData)
    popupRef.value?.close()
    emit('success')
}

const open = (type = 'add') => {
    mode.value = type
    popupRef.value?.open()
}

const setFormData = (data: Record<any, any>) => {
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            //@ts-ignore
            formData[key] = data[key]
        }
    }
}

const getDetail = async (row: Record<string, any>) => {
    const data = await starDetail({
        id: row.id
    })

    setFormData(data)
    if (data['movies'] != '') {
        let mids = JSON.parse(data['movies'])
        for (let i in mids) {
            let movie = await moviesDetail({ id: mids[i] })
            if (movie != null) {
                setTimeout(() => {
                    options2.value.push({
                        value: movie.id,
                        label: movie.movieName
                    })
                }, 200) // 防抖300msVB 
            }
        }
        setTimeout(() => {
            movies.value = mids
        }, 500)
    }
}

const handleClose = () => {
    emit('close')
}

const countryOptions = reactive<{
    value: any[]
}>({
    value: []
})
const getCountry = async () => {
    const data = await dictDataAll({ type_id:6, dictType:"country_area" })
    countryOptions.value = data
}
interface OptionItem {
  value: string | number;
  label: string;
  // 可扩展其他字段
}
let options2 = ref<OptionItem[]>([])
const remoteMethod2 = async (query:string) => {
    if (query !== '') {
        const data = await moviesLists({"movieName":query});
        setTimeout(() => {
            options2.value = data.lists.map((item:any) => ({
                value: item.id,
                label: item.movieName
            }))
        }, 200);
    } else {
        options2.value = [];
    }
}

getCountry()

defineExpose({
    open,
    setFormData,
    getDetail
})

</script>