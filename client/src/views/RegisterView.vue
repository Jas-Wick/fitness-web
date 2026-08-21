<template>
  <AuthLayout>
    <h1 class="form-title">注册</h1>
    <p class="form-sub">创建账号，开启你的健身管理之旅</p>

    <el-form class="auth-form-box" @submit.prevent="onSubmit">
      <el-form-item>
        <el-input v-model="form.username" placeholder="用户名" size="large">
          <template #prefix><el-icon><User /></el-icon></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input v-model="form.nickname" placeholder="昵称（可选）" size="large">
          <template #prefix><el-icon><Postcard /></el-icon></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码（6-32 位）"
          size="large"
          show-password
          @keyup.enter="onSubmit"
        >
          <template #prefix><el-icon><Lock /></el-icon></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="w-full submit-btn" size="large" :loading="loading" @click="onSubmit">
          注册
        </el-button>
      </el-form-item>
    </el-form>

    <div class="tip">
      已有账号？
      <router-link to="/login" class="link">去登录</router-link>
    </div>
  </AuthLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import AuthLayout from '@/components/AuthLayout.vue'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '' })

async function onSubmit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (form.password.length < 6 || form.password.length > 32) {
    ElMessage.warning('密码长度需在 6-32 位之间')
    return
  }
  loading.value = true
  try {
    await auth.register({
      username: form.username,
      nickname: form.nickname || undefined,
      password: form.password,
    })
    ElMessage.success('注册成功')
    router.push('/home')
  } catch {
    // 错误提示由拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.form-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--fth-text-primary);
}
.form-sub {
  margin: 6px 0 24px;
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
}
.submit-btn {
  margin-top: 4px;
}
.w-full {
  width: 100%;
}
.tip {
  text-align: center;
  font-size: 13px;
  color: var(--fth-text-tertiary);
  margin-top: 8px;
}
.link {
  color: var(--fth-primary);
  text-decoration: none;
  font-weight: 500;
}
.link:hover {
  text-decoration: underline;
}
</style>
