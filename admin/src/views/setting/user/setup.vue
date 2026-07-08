<!-- 网站信息 -->
<template>
    <div class="user-setup">
        <el-card shadow="never" class="!border-none">
            <div class="font-medium mb-7">基本设置</div>
            <el-form ref="formRef" :model="formData" label-width="120px">
                <el-form-item label="用户默认头像">
                    <div>
                        <material-picker v-model="formData.default_avatar" :limit="1" />
                    </div>
                </el-form-item>
                <el-form-item>
                    <div>
                        <div class="form-tips">
                            用户注册时给的默认头像，建议尺寸：400*400像素，支持jpg，jpeg，png格式
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="注册赠送会员天数">
                    <div class="w-80">
                        <el-input-number
                            v-model="formData.free_vip_days"
                            :min="0"
                            :step="1"
                            :precision="0"
                            controls-position="right"
                        />
                    </div>
                </el-form-item>
                <el-form-item>
                    <div>
                        <div class="form-tips">
                            新用户注册时赠送的免费会员天数，填 0 表示不赠送
                        </div>
                    </div>
                </el-form-item>
            </el-form>
        </el-card>

        <footer-btns v-perms="['setting.user.user/setConfig']">
            <el-button type="primary" @click="handleSubmit">保存</el-button>
        </footer-btns>
    </div>
</template>

<script lang="ts" setup name="userSetup">
import { getUserSetup, setUserSetup } from '@/api/setting/user'

// 表单数据
const formData = reactive({
    default_avatar: '', // 用户默认头像
    free_vip_days: 0 // 注册赠送会员天数
})

// 获取用户设置数据
const getData = async () => {
    try {
        const data = await getUserSetup()
        for (const key in formData) {
            //@ts-ignore
            formData[key] = data[key]
        }
    } catch (error) {
        console.log('获取=>', error)
    }
}

// 保存用户设置数据
const handleSubmit = async () => {
    try {
        await setUserSetup(formData)
        getData()
    } catch (error) {
        console.log('保存=>', error)
    }
}

getData()
</script>

<style lang="scss" scoped></style>
