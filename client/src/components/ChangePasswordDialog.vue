<template>
  <el-dialog
    :model-value="modelValue"
    title="修改密码"
    width="420px"
    append-to-body
    :close-on-click-modal="false"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form :model="pwdForm" label-position="top" @submit.prevent>
      <el-form-item label="原密码">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="输入当前密码" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-32 位" />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input
          v-model="pwdForm.confirm"
          type="password"
          show-password
          @keyup.enter="changePwd"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="changing" @click="changePwd">修改密码</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/user'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [boolean] }>()

const changing = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

// 每次打开时清空表单
watch(
  () => props.modelValue,
  (v) => {
    if (v) {
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirm = ''
    }
  },
)

async function changePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整密码')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (pwdForm.newPassword.length < 6 || pwdForm.newPassword.length > 32) {
    ElMessage.warning('新密码长度需在 6-32 位之间')
    return
  }
  changing.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    emit('update:modelValue', false)
  } catch {
    // 拦截器已提示
  } finally {
    changing.value = false
  }
}
</script>
