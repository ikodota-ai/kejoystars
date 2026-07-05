<template>
    <div class="edit-popup">
        <popup ref="popupRef" :title="popupTitle" :async="true" width="350px" @confirm="handleSubmit"
            @close="handleClose">
            <el-form ref="formRef" :model="formData" label-width="80px" :rules="formRules">
                <div class="grid-content bg-purple">
                    <el-form-item label="账号" prop="account">
                        <el-input v-model="formData.account" placeholder="请输入充值账号" clearable />
                    </el-form-item>
                    <el-form-item label="金额" prop="amount">
                        <el-input v-model="formData.amount" type="number" @input="handleMoneyInput" clearable />
                    </el-form-item>
                </div>
            </el-form>
        </popup>
    </div>
</template>

<script lang="ts" setup>
import type { FormInstance } from 'element-plus'
import { add } from '@/api/finance'
import Popup from '@/components/popup/index.vue'
import { getModulesKey } from '@/router'
import feedback from '@/utils/feedback'

const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')
const popupTitle = computed(() => {
    return '手动充值'
})
const componentsOptions = ref(getModulesKey())
let movies = ref([])
const querySearch = (queryString: string, cb: any) => {
    const results = queryString
        ? componentsOptions.value.filter((item) =>
            item.toLowerCase().includes(queryString.toLowerCase())
        )
        : componentsOptions.value
    cb(results.map((item) => ({ value: item })))
}
const formData = reactive({
    account: '',
    amount: 0,
})

const handleMoneyInput = (value:string) => {
  // 限制只能输入数字和小数点，最多两位小数
  formData.amount = value.replace(/[^\d.]/g, '')
    .replace(/^\./g, '')  // 不能以小数点开头
    .replace(/\.{2,}/g, '.')  // 不能有连续多个小数点
    .replace(/^(\d+\.\d{2}).*$/, '$1');  // 限制最多两位小数
}

const formRules = {
    account: [
        {
            required: true,
            message: '主输入充值账号',
            trigger: ['blur', 'change']
        }
    ],
    amount: [
        {
            required: true,
            message: '请填写充值金额',
            trigger: 'blur'
        }
    ]
}


const handleSubmit = async () => {
    await formRef.value?.validate()
    await feedback.confirm('确认是否给['+ formData.account +']充值？')
    const result = await add(formData)
    if(result == true) {
        formData.account = ''
        formData.amount = 0
    }
    // popupRef.value?.close()
    // emit('success')
}

const open = (type = 'add') => {
    mode.value = type
    popupRef.value?.open()
}


const handleClose = () => {
    emit('close')
}

defineExpose({
    open
})

</script>