<template>
    <div class="Upload-popup">
        <popup ref="popupRef" :title="popupTitle" :async="true" width="850px" @confirm="handleSubmit"
            @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
                <div class="grid-content bg-purple">
                    <el-form-item label="明星演员" prop="starId">
                        <el-select v-model="formData.starId" class="w-[408px]" filterable remote
                            reserve-keyword placeholder="请输入明星演员）" :remote-method="remoteMethod2">
                            <el-option v-for="item in options2" :key="item.value" :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="图片来源" prop="source">
                        <el-select v-model="formData.source" placeholder="请选择图片来源">
                            <el-option
                            v-for="item in [{label:'x', value:'x'},{label:'ins', value:'ins'}]"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="明星照片" prop="images">
                        <material-picker v-model="formData.images" :limit="50" />
                    </el-form-item>
                </div>
            </el-form>
        </popup>
    </div>
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import Popup from '@/components/popup/index.vue'
import { starLists } from '@/api/k/star'
import { instagramAdd } from '@/api/k/instagram'
const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const popupTitle = computed(() => {
    return '上传明星/演员照片'
})
const formData = reactive({
    starId: '',
    name: '',
    images: [],
    source:''
})

const formRules = {
    starId: [
        {
            required: true,
            message: '请输入明星姓名',
            trigger: ['blur', 'change']
        }
    ],
    images: [
        {
            required: true,
            message: '请上传明星照片',
            trigger: 'blur'
        }
    ],
    source: [
        {
            required: true,
            message: '请选择图片来源！',
            trigger: 'blur'
        }
    ]
}


const handleSubmit = async () => {
    await formRef.value?.validate()
    for(let i in formData.images){
        let data = {starId: formData.starId, name: formData.name, image: formData.images[i], source:formData.source}
        await instagramAdd(data)
    }
    popupRef.value?.close()
    emit('success')
}

const open = (type = 'add') => {
    popupRef.value?.open()
}
let options2 = [] as OptionItem[]

const setFormData = (data: Record<any, any>) => {
    options2.push({value:data.starId, label:data.name})
    for (const key in formData) {
        if (data[key] != null && data[key] != undefined) {
            //@ts-ignore
            formData[key] = data[key]
        }
    }
}


const handleClose = () => {
    emit('close')
}


interface OptionItem {
    value: string | number;
    label: string;
    // 可扩展其他字段
}

const remoteMethod2 = async (query: string) => {
    if (query !== '') {
        const data: any = await starLists({ "name": query });
        options2 = data.lists.map((item: any) => ({
            value: item.id,
            label: item.name
        }))
    } else {
        options2 = [];
    }
}

defineExpose({
    open,
    setFormData
})

</script>