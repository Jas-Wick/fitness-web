<template>
  <div class="md-body" v-html="html" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps<{ content: string }>()

// 同步解析 + 消毒，防止 AI 回复中的 HTML/脚本注入
const html = computed(() => {
  const raw = marked.parse(props.content || '', { async: false }) as string
  return DOMPurify.sanitize(raw)
})
</script>
