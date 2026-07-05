<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            :title="popupTitle"
            :async="true"
            width="550px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
                <el-form-item label="名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称" prop="name">
                    <el-input v-model="formData.name" class="w-[408px]" placeholder="请输入名称" clearable />
                </el-form-item>
                <el-form-item label="播出国家" prop="country">
                    <el-radio-group v-model="formData.country">
                                    <el-radio-button v-for="item in countryOptions.value"
                                    :key="item.value" 
                                    :label="item.value">{{ item.name }}</el-radio-button>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="头像上传" prop="name">
                    <material-picker v-model="formData.image" />
                </el-form-item>
                <el-form-item label="明&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;星">
                    <div class="flex-1">
                        <el-select v-model="star" class="w-[408px]" 
                            filterable
                            multiple
                            remote
                            reserve-keyword
                            placeholder="明星名字（可多选）"
                            :remote-method="remoteMethod1"
                            :loading="couple.loading"
                        >
                            <el-option
                                v-for="item in couple.options1.value"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                    </div>
                </el-form-item>
                <el-form-item label="关联影视" prop="name">
                    <el-select v-model="movies" class="w-[408px]" 
                            filterable
                            multiple
                            remote
                            reserve-keyword
                            placeholder="请输入关联影视（可多选）"
                            :remote-method="remoteMethod2"
                        >
                            <el-option
                                v-for="item in couple.options2.value"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup name="coupleEdit">
import type { FormInstance } from 'element-plus'
import { coupleAdd, coupleDetail, coupleEdit } from '@/api/k/couple'
import { starLists,starDetail } from '@/api/k/star'
import { moviesLists,moviesDetail } from '@/api/k/Movies'
import config from '@/config'
import { dictDataAll } from '@/api/setting/dict'
import Popup from '@/components/popup/index.vue'
import { getModulesKey } from '@/router'
const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')
const countryOptions = reactive<{
    value: any[]
}>({
    value: []
})
let star = ref([]);
let movies = ref([]);
const popupTitle = computed(() => {
    return mode.value == 'edit' ? '编辑Couple' : '新增Couple'
})

const componentsOptions = ref(getModulesKey())
const querySearch = (queryString: string, cb: any) => {
    const results = queryString
        ? componentsOptions.value.filter((item) =>
              item.toLowerCase().includes(queryString.toLowerCase())
          )
        : componentsOptions.value
    cb(results.map((item) => ({ value: item })))
}

const uploadUrl = config.baseUrl + 'adminapi/upload/image?cid=0';
const formData = reactive({
    id: '',
    //父级id
    name: '',
    //图标
    country: 1,
    //名称
    image: '',
    //排序号
    star: '',
    //权限链接
    movies: '',
})

const formRules = {
    name: [
        {
            required: true,
            message: '请输入couple名称',
            trigger: 'blur'
        }
    ],
    country: [
        {
            required: true,
            message: '请选择couple国家',
            trigger: 'blur'
        }
    ],
    image: [
        {
            required: true,
            message: '请上传封面图片',
            trigger: 'blur'
        }
    ]
}

const handleSubmit = async () => {
    formData.star = JSON.stringify(star.value)
    formData.movies = JSON.stringify(movies.value);
    await formRef.value?.validate()
    mode.value == 'edit' ? await coupleEdit(formData) : await coupleAdd(formData)
    popupRef.value?.close()
    emit('success')
}

const open = (type = 'add') => {
    mode.value = type
    popupRef.value?.open()
}

const setFormData = async(data: Record<any, any>) => {
    star.value = []
    movies.value = []
    couple.options1.value = []
    couple.options2.value = []
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            //@ts-ignore
            formData[key] = data[key]
        }
    }
}

const getDetail = async (row: Record<string, any>) => {
    const data = await coupleDetail({
        id: row.id
    })

    setFormData(data)
    if (data['star'] != '') {
        let sids = JSON.parse(data['star'])
        for (let i in sids) {
            let s = await starDetail({ id: sids[i] })
            if (s != null) {
                setTimeout(() => {
                    couple.options1.value.push({
                        value: s.id,
                        label: s.name
                    })
                }, 200) // 防抖300ms
            }
        }
        setTimeout(() => {
            star.value = sids
        }, 500)

    }
    if (data['movies'] != '') {
        let mids = JSON.parse(data['movies'])
        for (let i in mids) {
            let movie = await moviesDetail({ id: mids[i] })
            if (movie != null) {
                setTimeout(() => {
                    couple.options2.value.push({
                        value: movie.id,
                        label: movie.movieName
                    })
                }, 200) // 防抖300ms
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

const getCountry = async () => {
    const data = await dictDataAll({ type_id:6, dictType:"country_area" })
    setTimeout(() => {
        countryOptions.value = data;
    }, 200) // 防抖300ms
}
interface OptionItem {
  value: string | number
  label: string
  // 可扩展其他字段
}
const couple = {
    options1 : ref<OptionItem[]>([]),
    options2 : ref<OptionItem[]>([]),
    list1 :  [] as OptionItem[],
    list2 :  [] as OptionItem[],
    loading : false
}

const remoteMethod1 = async (query:string) => {
    if (query !== '') {
        const data = await starLists({"name":query, page_size:50});
            setTimeout(() => {
                couple.options1.value = data.lists.map((item:any) => ({
                value: item.id,
                label: item.name
            }))
            console.log(couple.options1.value)
        }, 200) // 防抖300ms
    } else {
        couple.options1.value = [];
    }
}

const remoteMethod2 = async (query:string) => {
    if (query !== '') {
        const data = await moviesLists({"movieName":query});
        couple.options2.value = data.lists.map((item:any) => ({
              value: item.id,
              label: item.movieName
        }))
    } else {
        couple.options2.value = [];
    }
}

getCountry();

defineExpose({
    open,
    setFormData,
    getDetail
})

</script>
