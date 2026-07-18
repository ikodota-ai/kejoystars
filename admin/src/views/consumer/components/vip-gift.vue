<template>
    <popup
        ref="popupRef"
        title="赠送VIP"
        width="500px"
        @confirm="handleConfirm"
        :async="true"
        @close="popupClose"
    >
        <div class="pr-8">
            <el-form ref="formRef" :model="formData" label-width="120px" :rules="formRules">
                <el-form-item label="当前VIP剩余">{{ vipRemainText }}</el-form-item>
                <el-form-item label="赠送批次" prop="batch_key">
                    <el-select
                        v-model="formData.batch_key"
                        placeholder="请选择赠送批次(可在字典 gift_batch 维护)"
                        clearable
                        style="width: 100%"
                    >
                        <el-option
                            v-for="item in batchOptions"
                            :key="item.value"
                            :label="item.name"
                            :value="item.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="赠送天数" prop="days">
                    <el-input
                        v-model.number="formData.days"
                        placeholder="请输入赠送天数"
                        type="number"
                        :min="1"
                    />
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="formData.remark" type="textarea" :rows="3" />
                </el-form-item>
                <div class="text-tx-secondary text-xs">
                    * 同一用户同一批次仅可赠送一次;赠送记录会写入充值记录(支付方式=后台赠送)。
                </div>
            </el-form>
        </div>
    </popup>
</template>
<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus'
import Popup from '@/components/popup/index.vue'
import { dictDataAll } from '@/api/setting/dict'

const formRef = shallowRef<FormInstance>()
const props = defineProps({
    show: {
        type: Boolean,
        required: true
    },
    vipRemain: {
        type: [Number, String],
        default: 0
    }
})
const emit = defineEmits<{
    (event: 'update:show', value: boolean): void
    (event: 'confirm', value: any): void
}>()

const formData = reactive({
    batch_key: '',
    days: 30,
    remark: ''
})
const batchOptions = ref<any[]>([])
const popupRef = shallowRef<InstanceType<typeof Popup>>()

const vipRemainText = computed(() => {
    const n = Number(props.vipRemain) || 0
    return n > 0 ? `${n} 天` : '未开通'
})

const formRules: FormRules = {
    batch_key: [{ required: true, message: '请选择赠送批次' }],
    days: [{ required: true, message: '请输入赠送天数' }]
}

const loadBatch = async () => {
    try {
        const data: any = await dictDataAll({ type_id: 0, dictType: 'gift_batch' })
        batchOptions.value = data || []
    } catch (e) {
        batchOptions.value = []
    }
}

const handleConfirm = async () => {
    await formRef.value?.validate()
    emit('confirm', { ...formData })
}
const popupClose = () => {
    emit('update:show', false)
    formRef.value?.resetFields()
}
watch(
    () => props.show,
    (val) => {
        if (val) {
            loadBatch()
            popupRef.value?.open()
        } else {
            popupRef.value?.close()
        }
    }
)
</script>
