<template>
    <div class="edit-popup">
        <popup
            ref="popupRef"
            :title="popupTitle"
            :async="true"
            width="640px"
            @confirm="handleSubmit"
            @close="handleClose"
        >
            <el-form class="ls-form" ref="formRef" :rules="rules" :model="formData" label-width="110px">
                <el-form-item label="平台" prop="platform">
                    <el-radio-group v-model="formData.platform">
                        <el-radio label="android">安卓</el-radio>
                        <el-radio label="ios">苹果</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="通道" prop="channel">
                    <el-input v-model="formData.channel" placeholder="默认 production" clearable />
                </el-form-item>
                <el-form-item label="版本名称" prop="versionName">
                    <el-input v-model="formData.versionName" placeholder="如 1.4.2" clearable />
                </el-form-item>
                <el-form-item label="版本号" prop="versionCode">
                    <el-input-number v-model="formData.versionCode" :min="1" :step="1" />
                    <span class="text-gray-400 ml-[10px]">整数，客户端用它比较高低</span>
                </el-form-item>
                <el-form-item label="最低兼容版本" prop="minVersionCode">
                    <el-input-number v-model="formData.minVersionCode" :min="0" :step="1" />
                    <span class="text-gray-400 ml-[10px]">低于此版本号强制更新</span>
                </el-form-item>
                <el-form-item label="是否强更" prop="isForce">
                    <el-radio-group v-model="formData.isForce">
                        <el-radio :label="0">建议更新</el-radio>
                        <el-radio :label="1">强制更新</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="包名/BundleId" prop="bundleId">
                    <el-input v-model="formData.bundleId" placeholder="iOS BundleId / Android packageName" clearable />
                </el-form-item>
                <el-form-item label="安装地址" prop="installUrl">
                    <el-input
                        v-model="formData.installUrl"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 3 }"
                        placeholder="iOS: itms-services://... 或分发平台链接; Android: apk 直链"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="下载地址" prop="downloadUrl">
                    <el-input
                        v-model="formData.downloadUrl"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 3 }"
                        placeholder="apk/ipa 原始文件直链, 可选"
                        clearable
                    />
                </el-form-item>
                <el-form-item label="包大小(字节)" prop="packageSize">
                    <el-input-number v-model="formData.packageSize" :min="0" :step="1024" />
                </el-form-item>
                <el-form-item label="包MD5" prop="packageMd5">
                    <el-input v-model="formData.packageMd5" placeholder="apk 校验, 可选" clearable />
                </el-form-item>
                <el-form-item label="灰度百分比" prop="grayPercent">
                    <el-input-number v-model="formData.grayPercent" :min="0" :max="100" :step="10" />
                    <span class="text-gray-400 ml-[10px]">100 表示全量推送</span>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="formData.status">
                        <el-radio label="draft">草稿</el-radio>
                        <el-radio label="online">上线</el-radio>
                        <el-radio label="offline">下线</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="更新说明" prop="releaseNote">
                    <el-input
                        v-model="formData.releaseNote"
                        type="textarea"
                        :autosize="{ minRows: 4, maxRows: 8 }"
                        placeholder="每行一条, 客户端弹窗展示"
                        clearable
                        maxlength="1000"
                        show-word-limit
                    />
                </el-form-item>
            </el-form>
        </popup>
    </div>
</template>
<script lang="ts" setup>
import type { FormInstance } from 'element-plus'

import { appVersionAdd, appVersionDetail, appVersionEdit } from '@/api/app/version'
import Popup from '@/components/popup/index.vue'

const emit = defineEmits(['success', 'close'])
const formRef = shallowRef<FormInstance>()
const popupRef = shallowRef<InstanceType<typeof Popup>>()
const mode = ref('add')
const popupTitle = computed(() => {
    return mode.value == 'edit' ? '编辑APP版本' : '新增APP版本'
})
const formData = reactive({
    id: '',
    platform: 'android',
    channel: 'production',
    versionName: '',
    versionCode: 1,
    minVersionCode: 0,
    isForce: 0,
    bundleId: '',
    installUrl: '',
    downloadUrl: '',
    packageSize: 0,
    packageMd5: '',
    grayPercent: 100,
    status: 'draft',
    releaseNote: ''
})

const rules = {
    platform: [{ required: true, message: '请选择平台', trigger: ['change'] }],
    versionName: [{ required: true, message: '请输入版本名称', trigger: ['blur'] }],
    versionCode: [{ required: true, message: '请输入版本号', trigger: ['blur'] }],
    installUrl: [{ required: true, message: '请输入安装地址', trigger: ['blur'] }]
}

const handleSubmit = async () => {
    await formRef.value?.validate()
    mode.value == 'edit' ? await appVersionEdit(formData) : await appVersionAdd(formData)
    popupRef.value?.close()
    emit('success')
}

const handleClose = () => {
    emit('close')
}

const open = (type = 'add') => {
    mode.value = type
    popupRef.value?.open()
}

const setFormData = async (data: Record<any, any>) => {
    // 列表数据字段不全, 拉详情补齐
    const detail = data.id ? await appVersionDetail({ id: data.id }) : data
    for (const key in formData) {
        if (detail[key] != null && detail[key] != undefined) {
            //@ts-ignore
            formData[key] = detail[key]
        }
    }
}

defineExpose({
    open,
    setFormData
})
</script>
