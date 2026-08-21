<template>
  <div class="auth-layout">
    <!-- 品牌区 -->
    <div class="auth-brand">
      <div class="brand-top">
        <div class="brand-mark">F</div>
        <div class="brand-text">
          <div class="brand-name">FitHub</div>
          <div class="brand-sub">智能健身管理平台</div>
        </div>
      </div>

      <div class="brand-slogan">用数据驱动每一次进步</div>

      <ul class="brand-features">
        <li>
          <el-icon><Calendar /></el-icon>
          <span>训练打卡、日历与数据分析</span>
        </li>
        <li>
          <el-icon><Dish /></el-icon>
          <span>饮食记录与营养构成洞察</span>
        </li>
        <li>
          <el-icon><MagicStick /></el-icon>
          <span>AI 个性化训练与饮食建议</span>
        </li>
      </ul>

      <div class="brand-foot">FitHub · 你的智能健身伙伴</div>
    </div>

    <!-- 表单区 -->
    <div class="auth-form">
      <div class="auth-form-inner">
        <slot />
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-layout {
  display: grid;
  grid-template-columns: minmax(360px, 44%) 1fr;
  min-height: 100vh;
}
@media (max-width: 900px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }
}

/* 品牌区 */
.auth-brand {
  background: var(--fth-sidebar-bg);
  color: #fff;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
}
@media (max-width: 900px) {
  .auth-brand {
    padding: 28px 32px;
  }
  .brand-slogan,
  .brand-features,
  .brand-foot {
    display: none;
  }
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--fth-primary);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}
.brand-sub {
  font-size: 12px;
  color: var(--fth-sidebar-text);
}
.brand-slogan {
  margin-top: 48px;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.4;
}
.brand-features {
  list-style: none;
  margin: 24px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.brand-features li {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--fth-sidebar-text);
  font-size: 14px;
}
.brand-features li .el-icon {
  color: var(--fth-primary);
  font-size: 18px;
}
.brand-foot {
  margin-top: auto;
  color: var(--fth-sidebar-text);
  font-size: 12px;
}

/* 表单区 */
.auth-form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: var(--fth-bg-page);
}
.auth-form-inner {
  width: 100%;
  max-width: 380px;
}
</style>
