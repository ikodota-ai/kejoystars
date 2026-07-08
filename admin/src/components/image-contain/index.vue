<template>
    <el-image
        :key="retryKey"
        :style="styles"
        v-bind="imageBind"
        @error="handleError"
    >
        <template v-if="$slots.error" #error>
            <slot name="error" />
        </template>
    </el-image>
</template>

<script lang="ts" setup>
import { imageProps } from 'element-plus'
import type { CSSProperties } from 'vue'
import { computed, ref } from 'vue'

import { addUnit } from '@/utils/util'

const props = defineProps({
    width: {
        type: [String, Number],
        default: 'auto'
    },
    height: {
        type: [String, Number],
        default: 'auto'
    },
    radius: {
        type: [String, Number],
        default: 0
    },
    // 加载失败自动重试次数
    maxRetry: {
        type: Number,
        default: 2
    },
    // 每次重试的延迟(毫秒)，逐次递增
    retryDelay: {
        type: Number,
        default: 800
    },
    ...imageProps
})

// 透传给 el-image 的属性(排除自定义项)
const imageBind = computed(() => {
    const { width, height, radius, maxRetry, retryDelay, ...rest } = props as any
    return rest
})

const retryKey = ref(0)
let retryCount = 0
let timer: ReturnType<typeof setTimeout> | null = null

const handleError = (evt?: Event) => {
    if (retryCount < props.maxRetry) {
        retryCount++
        if (timer) clearTimeout(timer)
        // 递增延迟后强制重新挂载 el-image, 触发重新请求(网络抖动/HTTP2断连时可恢复)
        timer = setTimeout(() => {
            retryKey.value++
        }, props.retryDelay * retryCount)
    }
}

const styles = computed<CSSProperties>(() => {
    return {
        width: addUnit(props.width),
        height: addUnit(props.height),
        borderRadius: addUnit(props.radius)
    }
})
</script>

<style lang="scss" scoped>
.el-image {
    display: block;
    .el-image__error {
        @apply text-xs;
    }
}
</style>
