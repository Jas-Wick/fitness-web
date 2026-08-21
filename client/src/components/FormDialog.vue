<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :append-to-body="true"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <slot />
    <template #footer>
      <slot name="footer">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" :loading="loading" @click="$emit('confirm')">
          {{ confirmText }}
        </el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    width?: string | number
    loading?: boolean
    confirmText?: string
  }>(),
  {
    title: '',
    width: '520px',
    loading: false,
    confirmText: '保存',
  },
)

defineEmits<{
  'update:modelValue': [boolean]
  confirm: []
}>()
</script>
