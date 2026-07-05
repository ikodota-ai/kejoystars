<template>
    <div>
        <el-card class="!border-none" shadow="never">
            <el-alert
                type="warning"
                title="温馨提示：设置系统支持的支付方式"
                :closable="false"
                show-icon
            />
        </el-card>
        <el-card shadow="never" class="mt-4 !border-none">
            <div>
                <el-table :data="payConfigList">
                    <el-table-column prop="pay_way_name" label="支付方式" min-width="150" />
                    <el-table-column prop="name" label="显示名称" min-width="150" />
                    <el-table-column label="图标" min-width="150">
                        <template #default="{ row }">
                            <el-image
                                :src="row.icon"
                                alt="图标"
                                style="width: 34px; height: 34px"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column prop="sort" label="排序" min-width="150" />
                    <el-table-column label="操作" min-width="80" fixed="right">
                        <!-- 操作 -->
                        <template #default="{ row }">
                            <el-button
                                v-perms="['setting.pay.pay_config/setConfig']"
                                link
                                type="primary"
                                @click="handleEdit(row)"
                            >
                                配置
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-card>
              <el-card shadow="never" class="mt-4 !border-none">
            <div>
                <el-table :data="RechargeOption">
                    <el-table-column prop="id" label="编号" min-width="50" />
                    <el-table-column prop="title" label="显示名称" min-width="100" />
                    <el-table-column label="金额" min-width="50">
                       <template #default="{ row }">
                        <el-input type="number" v-model="row.amount" placeholder="金额" oninput="value=value.replace(/[^\d]/g,'')"></el-input>
                       </template> 
                    </el-table-column>
                    <el-table-column label="糖果" min-width="50">
                       <template #default="{ row }">
                        <el-input type="number" v-model="row.candy" placeholder="糖果" oninput="value=value.replace(/[^\d]/g,'')"></el-input>
                       </template> 
                    </el-table-column>
                    <el-table-column label="有效天数" min-width="50">
                       <template #default="{ row }">
                        <el-input type="number" v-model="row.expired" placeholder="糖果" oninput="value=value.replace(/[^\d]/g,'')"></el-input>
                       </template> 
                    </el-table-column>
                    <el-table-column label="下载次数(每天)" min-width="50">
                       <template #default="{ row }">
                        <el-input type="number" v-model="row.expired" placeholder="下载次数" oninput="value=value.replace(/[^\d]/g,'')"></el-input>
                       </template> 
                    </el-table-column>
                    <el-table-column label="状态" min-width="50">
                       <template #default="{ row }">
                        <el-switch
                            v-model="row.status"
                            active-text="开"
                            inactive-text="关">
                        </el-switch>
                       </template> 
                    </el-table-column>
                </el-table>
                <div style="line-height: 50px;padding-top: 10px;text-align: center;">
                    <el-button
                                v-perms="['setting.pay.pay_config/setConfig']"
                                type="primary"
                                @click="setRechargeOptions"
                            >
                                保存
                            </el-button>
                </div>
            </div>
        </el-card>
        <edit-popup v-if="showEdit" ref="editRef" @success="getConfig" @close="showEdit = false" />
    </div>
</template>

<script lang="ts" setup>
import { getPayConfigLists, getRechargeOption, setRechargeOption } from '@/api/setting/pay'
import feedback from '@/utils/feedback'
import EditPopup from './edit.vue'

const payConfigList = ref<any[]>([])
const RechargeOption = ref<[]>([])

const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const showEdit = ref(false)
const getConfig = async () => {
    const { lists } = await getPayConfigLists()
    payConfigList.value = lists
}

const getRechargeOptions = async () => {
    const data = await getRechargeOption()
    for(let i in data) {
        data[i].status = data[i].status == 1 ? true : false
    }
    RechargeOption.value = data
}

const setRechargeOptions = async () => {
    await feedback.confirm('请确认充值选项设置？')
    for(let i in RechargeOption.value) {
        RechargeOption.value[i].status = RechargeOption.value[i].status ? 1 : 0
    }
    await setRechargeOption({"c":JSON.stringify(RechargeOption.value)})
    getRechargeOptions()
}

const handleEdit = async (data: any) => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open()
    editRef.value?.getDetail(data)
}

getConfig()
getRechargeOptions()
</script>
