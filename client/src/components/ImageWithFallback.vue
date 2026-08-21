<template>
  <el-image
    v-if="src"
    :src="src"
    :alt="alt"
    :fit="fit"
    class="img-fallback"
    @error="failed = true"
  >
    <template #error>
      <div class="img-placeholder">
        <el-icon :size="24"><Picture /></el-icon>
        <span v-if="!compact" class="ph-text">图片加载失败</span>
      </div>
    </template>
  </el-image>
  <div v-else class="img-placeholder">
    <el-icon :size="24"><Picture /></el-icon>
    <span v-if="!compact" class="ph-text">暂无图片</span>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    src?: string | null
    alt?: string
    fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
    /** 紧凑模式：只显示图标，不显示文字 */
    compact?: boolean
  }>(),
  {
    src: '',
    alt: '',
    fit: 'cover',
    compact: false,
  },
)

const failed = ref(false)

watch(
  () => props.src,
  () => {
    failed.value = false
  },
)
</script>

<style scoped>
.img-fallback,
.img-placeholder {
  width: 100%;
  height: 100%;
  display: block;
}
.img-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: var(--fth-bg-muted);
  color: var(--fth-text-tertiary);
  border-radius: var(--fth-radius-sm);
  min-height: 60px;
}
.ph-text {
  font-size: var(--fth-text-xs);
}
</style>
