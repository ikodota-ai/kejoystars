<template>
    <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="明星/演员" name="star">
        <div>
            <el-card class="!border-none" shadow="never">
                <!-- <el-form ref="formRef" class="mb-[-16px]" :model="queryParams" :inline="true"> -->
                    <el-form-item style="width:310px; display:inline-block;">
                        <el-input
                            class="w-[300px]"
                            v-model="queryParams.name"
                            placeholder="明星/演员"
                            clearable
                            @keyup.enter="resetPage"
                        />
                    </el-form-item>
                    <el-form-item  style="width:310px; display:inline-block;">
                                <el-select v-model="queryParams.country" class="w-[300px]" 
                                    placeholder="国家区域"
                                >
                                    <el-option
                                        v-for="item in optionsCountry"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                    </el-form-item>
                    <el-form-item style="width:300px; display:inline-block;">
                        <el-button type="primary" @click="resetPage">查询</el-button>
                        <el-button @click="resetParams">重置</el-button>
                        <el-button type="primary" v-perms="['k.star/add']" @click="handleAdd()">
                            <template #icon>
                                <icon name="el-icon-Plus" />
                            </template>
                            添加明星
                        </el-button>
                    </el-form-item>
                <!-- </el-form> -->
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <el-checkbox-group v-model="checkedStars" @change="handleCheckedStarChange">
                <el-table size="large" v-loading="pager.loading" :data="pager.lists">
                    <el-table-column label="编号" min-width="25">
                        <template #default="scope">
                            <el-checkbox :label="scope.row.id" :key="scope.row.id">
                                {{ scope.row.id }}
                            </el-checkbox>
                        </template>
                    </el-table-column>
                    <el-table-column label="明星" prop="name" min-width="100" />
                    <el-table-column label="糖果" prop="candy" min-width="50" />
                    <el-table-column label="国家" prop="countryName" min-width="50">
                        <!-- <template #default="scope">
                            <span>{{ getCountryName(scope.row.country) }}</span>
                        </template> -->
                    </el-table-column>
                    <el-table-column label="状态" prop="status" min-width="50">
                        <template #default="scope">
                            <span>{{ scope.row.updateTime == null ? "资料不全":"正常" }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="添加日期" prop="createTime" min-width="100" />
                    <el-table-column label="添加人" prop="createUser" min-width="100" />
                    <el-table-column label="修改日期" prop="updateTime" min-width="120" />
                    <el-table-column label="修改人" prop="updateUser" min-width="120" />
                    <el-table-column label="操作" width="160" fixed="right">
                        <template #default="{ row }">
                            <el-button
                                v-perms="['k.instagram/list']"
                                type="primary"
                                link
                                @click="loadImage('photo', row)"
                            >
                                照片
                            </el-button>
                            <el-button
                                v-perms="['k.instagram/add']"
                                type="primary"
                                link
                                @click="handleEdit(row)"
                            >
                                编辑
                            </el-button>
                            <el-button
                                v-perms="['k.star/del']"
                                type="danger"
                                link
                                @click="handleDelete([row.id])"
                            >
                                删除
                            </el-button>
                        </template>
                    </el-table-column>
                </el-table>
                </el-checkbox-group>
                <div class="flex justify-end mt-4">
                    <div style="float: left;width: 50%;">
                        <el-checkbox :indeterminate="isStarIndeterminate" v-model="checkedStarAll" @change="handleCheckStarsAllChange">全选</el-checkbox>
                        <el-button type="danger" v-perms="['k.star/del']" @click="handleDeleteAll()" style="margin-left: 20px;">删除</el-button>
                    </div>
                    <div style="float: right;width: 50%;">
                        <pagination v-model="pager" @change="getLists" />
                    </div>
                </div>
            </el-card>
            <edit-popup v-if="showEdit" ref="editRef" @success="getLists" @close="showEdit = false" />
        </div>
        </el-tab-pane>
        <el-tab-pane label="明星照片" name="photo" v-perms="['k.instagram/list']">
            <el-card class="!border-none" shadow="never">
                <el-form ref="formRef" class="mb-[-16px]" :model="queryPhotoParams" :inline="true">
                    <el-form-item label="明星/演员">
                        <el-select v-model="queryPhotoParams.starId" placeholder="请输入明星名字">
                            <el-option
                            v-for="item in options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="图片来源">
                        <el-select v-model="queryPhotoParams.source" placeholder="请选择图片来源">
                            <el-option
                            v-for="item in [{label:'x', value:'x'},{label:'ins', value:'ins'}]"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="photos.resetPage">查询</el-button>
                        <el-button @click="photos.resetParams">重置</el-button>
                        <el-button type="primary"  v-perms="['k.instagram/add']" @click="handleUpload">
                            <template #icon>
                                <icon name="el-icon-Plus" />
                            </template>
                            添加照片
                        </el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never">
                <el-checkbox-group v-model="checkedPhotos" @change="handleCheckedPhotosChange">
                    <div class="demo-image__preview" style="width: 100px; height: 120px; margin:5px;float: left;" v-for="item in photos.pager.lists">
                        <el-image 
                            style="width: 100px; height: 100px;"
                            :src="item.image" 
                            :preview-src-list="[item.image]">
                        </el-image>
                        <div style="padding: 2px;overflow: hidden; width: 100px;">
                            <el-checkbox :label="item.id" :key="item.id">{{item.starName}}</el-checkbox>
                        </div>
                        {{ pushPhotoToList(item.image) }}
                    </div>
                </el-checkbox-group>
            </el-card>
            <div class="flex justify-end mt-4">
                <div style="float: left;width: 50%;">
                    <el-checkbox :indeterminate="isIndeterminate" v-model="checkedAll" @change="handleCheckAllChange">全选</el-checkbox>
                    <el-button type="danger" v-perms="['k.instagram/del']" @click="handlePhoto('del')" style="margin-left: 10px;">删除</el-button>
                </div>
                <div style="float: right;width: 50%;">
                    <pagination v-model="photos.pager" @change="photos.getLists();photoList = [];" />
                </div>
            </div>
            <upload v-if="showUpload" ref="uploadRef" @success="photos.getLists" @close="showUpload = false" />
        </el-tab-pane>
        <el-tab-pane label="待核照片" name="verify" v-perms="['k.instagram/list']">
            <el-card class="!border-none" shadow="never">
                <el-form ref="formRef" class="mb-[-16px]" :model="queryPhotoParams" :inline="true">
                    <el-form-item label="明星/演员">
                        <el-select v-model="queryPhotoParams.starId" placeholder="请输入明星名字">
                            <el-option
                            v-for="item in options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="图片来源">
                        <el-select v-model="queryPhotoParams.source" placeholder="请选择图片来源">
                            <el-option
                            v-for="item in [{label:'x', value:'x'},{label:'ins', value:'ins'}]"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            </el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="photos.resetPage">查询</el-button>
                        <el-button @click="photos.resetParams">重置</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
            <el-card class="!border-none mt-4" shadow="never" style="padding: 10px;">
                <el-checkbox-group v-model="checkedPhotos" @change="handleCheckedPhotosChange">
                <div class="demo-image__preview" style="width: 100px; height: 120px; margin:5px;float: left;" v-for="item in photos.pager.lists">
                    <el-image 
                        style="width: 100px; height: 100px;"
                        :src="item.image" 
                        :preview-src-list="[item.image]">
                    </el-image>
                    <div style="padding: 2px;overflow: hidden; width: 100px;">
                        <el-checkbox :label="item.id" :key="item.id">{{item.starName}}</el-checkbox>
                    </div>
                </div>
                </el-checkbox-group>
            </el-card>
            <div class="flex justify-end mt-4">
                <div style="float: left;width: 50%;">
                    <el-checkbox :indeterminate="isIndeterminate" v-model="checkedAll" @change="handleCheckAllChange">全选</el-checkbox>
                    <el-button type="success" class="ml-[10px]" v-perms="['k.instagram/edit']" @click="handlePhoto('verify')">通过</el-button>
                    <el-button type="danger" v-perms="['k.instagram/del']" @click="handlePhoto('del')">删除</el-button>
                </div>
                <div style="float: right;width: 50%;">
                    <pagination v-model="photos.pager" @change="photos.getLists();photoList = []; checkedAll = false;" style="float: right;" />
                </div>
            </div>
        </el-tab-pane>
    </el-tabs>
</template>
<style>
.selection-overlay {
  position: absolute;
  inset: 0;
  background: rgba(64,158,255,0.2);
}
</style>
<script lang="ts" setup name="starInfo">
import { ref, watch } from 'vue'
import { starLists, starDelete } from '@/api/k/star'
import { instagramDelete, instagramEdit, instagramLists } from '@/api/k/instagram'
import { dictDataAll } from '@/api/setting/dict'
import feedback from '@/utils/feedback'
import { usePaging } from '@/hooks/usePaging'
import EditPopup from './edit.vue'
import Upload from './upload.vue'

let checkedStars = ref<number[]>([])
let isStarIndeterminate = ref(false)
let checkedStarAll = ref(false)
let checkedPhotos = ref([])
let isIndeterminate = ref(false)
let checkedAll = ref(false)
let  photoList:any[] = [] // 初始化空数组
const queryParams = reactive({
    name: '',
    country:'',
    channel: '',
    create_time_start: '',
    create_time_end: ''
})

const handleCheckStarsAllChange = (val:any)=>{
    if(checkedStarAll.value) {
        const checked = pager.lists.map((item:any) => item.id)
        checkedStars.value = checked // 修改ref的值而非重新赋值
    } else {
        checkedStars.value = []
    }
    isStarIndeterminate.value = false;
}

const handleCheckedStarChange=(value:number)=> {
    console.log(checkedPhotos);
}

const handleCheckAllChange = (val:any)=>{
    if(checkedAll.value) {
        const checked = photos.pager.lists.map((photo:any) => photo.id)
        checkedPhotos.value = checked // 修改ref的值而非重新赋值
    } else {
        checkedPhotos.value = []
    }
    isIndeterminate.value = false;
}
const handleCheckedPhotosChange=(value:number)=> {
    console.log(checkedPhotos);
}
const editRef = shallowRef<InstanceType<typeof EditPopup>>()
const showEdit = ref(false)
const showUpload = ref(false)
const uploadRef = shallowRef<InstanceType<typeof Upload>>()
const handleUpload = async (id?: any, name?:any) => {
    showUpload.value = true
    await nextTick()
    if (id) {
        uploadRef.value?.setFormData({
            starId: queryPhotoParams.starId, name: queryPhotoParams.name
        })
        
    }
    uploadRef.value?.open('add')
}
const handleAdd = async (id?: number) => {
    showEdit.value = true
    await nextTick()
    if (id) {
        editRef.value?.setFormData({
            pid: id
        })
    }
    editRef.value?.open('add')
}

const handlePhoto = async (cmd: string) => {
    if(cmd == 'verify'){
        await feedback.confirm('确认审核照片没有问题？')

        await instagramEdit({id:checkedPhotos.value.join(','), status:'Y'});
    } else if(cmd == 'del'){
        await feedback.confirm('确定要删除,删除后将不能恢复？')
        await instagramDelete({id:1, ids:checkedPhotos.value.join(',')});
    }
    if(checkedAll.value) checkedAll.value = false;

    photos.getLists();
}

const handleEdit = async (data: any) => {
    showEdit.value = true
    await nextTick()
    editRef.value?.open('edit')
    editRef.value?.getDetail(data)
}

const handleDeleteAll = async () => {
    handleDelete(checkedStars.value);
    if(checkedStarAll.value) checkedStarAll.value = false;
}

const handleDelete = async (id:number[]) => {
    await feedback.confirm('确定要删除？')
    await starDelete(id)
    getLists()
}

const pushPhotoToList = (image:string)=>{
    photoList.push(image)
}

interface OptionItem {
  value: string | number;
  label: string;
  // 可扩展其他字段
}
let optionsCountry = ref<OptionItem[]>([])
const getCountry = async () => {
    const data = await dictDataAll({ type_id:6, dictType:"country_area" })
    if (data != null) {
        setTimeout(() => {
            optionsCountry.value = data.map((item:any) => ({
                value: item.value,
                label: item.name
            }))
        }, 200);
    }
}

// const getCountryName=(code:number) => {
//     return countryOptions.value[code] || code
//   }

const { pager, getLists, resetPage, resetParams } = usePaging({
    fetchFun: starLists,
    params: queryParams
})
onActivated(() => {
    photos.pager.size = 50
    getCountry()
    getLists()
})

const queryPhotoParams = reactive({
    starId: '',
    name : '',
    status: '',
    source:''
})

interface Images {
    pager:any;
    getLists:any
    resetPage: any
    resetParams : any
}
let activeName = ref('star');
let options:any = [];
const photos:Images = usePaging({
            fetchFun: instagramLists,
            params: queryPhotoParams
        })
const handleClick = (tab: string, event: any) => {
    options = []
    photoList = []
    setTimeout(() => {
        if (activeName.value == 'photo' || activeName.value == 'verify') {
            loadImage('', { id: queryPhotoParams.starId, name: queryPhotoParams.name })
        } else {
            queryPhotoParams.starId = ''
            name: queryPhotoParams.name = ''
        }
    }, 10);
}

const loadImage = (tab:string, data:any)=>{
    photoList = []
    if(tab != '') {
        activeName.value = tab
        options.push({label:data.name, value:data.id});
    }

    //加载图片
    queryPhotoParams.starId = data.id
    queryPhotoParams.name = data.name
    queryPhotoParams.status = "Y"
    if( activeName.value == 'verify' ) queryPhotoParams.status = "N"
    photos.getLists()
    checkedAll.value = false
    isIndeterminate.value = false
    handleCheckAllChange(null);
}
</script>