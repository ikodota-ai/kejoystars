<template>
	<view class="user" :style="pageStyle">
		<view v-for="(item, index) in state.pages" :key="index">
			<template v-if="item.name == 'user-info'">
				<w-user-info :pageMeta="state.meta" :content="item.content" :styles="item.styles" :user="userInfo"
					:is-login="isLogin" :navColor="navColor" />
			</template>
			<template v-if="item.name == 'my-service'">
				<w-my-service :content="item.content" :styles="item.styles" />
			</template>
			<template v-if="item.name == 'user-banner'">
				<w-user-banner :content="item.content" :styles="item.styles" />
			</template>
		</view>
		<tabbar />
	</view>
</template>

<script setup lang="ts">
	import { getDecorate } from '@/api/shop'
	import { useUserStore } from '@/stores/user'
	import { onShow, onLoad } from '@dcloudio/uni-app'
	import { storeToRefs } from 'pinia'
	import { computed, reactive } from 'vue'
	const state = reactive<{
		meta : any[]
		pages : any[]
	}>({
		meta: [],
		pages: []
	})
	const getData = async () => {
		
		console.log('asdasd')
		const data = await getDecorate({ id: 2 })
		state.meta = JSON.parse(data.meta)
		state.pages = JSON.parse(data.data)
		uni.setNavigationBarTitle({
			title: state.meta[0].content.title,
		})
		uni.setNavigationBarColor({
			 frontColor: '#000000',
		})
	}
	const userStore = useUserStore()
	const { userInfo, isLogin } = storeToRefs(userStore)
	// 根页面样式
	const pageStyle = computed(() => {
		const { bg_type, bg_color, bg_image } = state.meta[0]?.content ?? {}
		if (bg_type != undefined) {
			return bg_type == 1 ? { 'background-color': bg_color } : { 'background-image': `url(${bg_image})` };
		} else {
			return ""
		}
	})
	const navColor = computed(() => {
		const { text_color } = state.meta[0]?.content ?? {}
		if (text_color == 2) {
			return '#000000';
		} else {
			return '#ffffff'
		}
	})
	onShow(() => {
		userStore.getUser()
	})
	getData()
	
</script>

<style lang="scss" scoped>
	.user {
		position: relative;
		background-repeat: no-repeat;
		background-size: 100% auto;
		overflow: hidden;
		width: 100%;
		transition: all 1s;
		min-height: calc(100vh - env(safe-area-inset-bottom));
	}
</style>