<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-section">
        <el-avatar :size="80" :icon="UserFilled" />
        <div class="user-info">
          <h2>{{ userStore.user.nickname || userStore.user.username }}</h2>
          <p class="username">@{{ userStore.user.username }}</p>
        </div>
      </div>
    </div>

    <el-card class="profile-card" shadow="never">
      <h3 class="section-title">基本信息</h3>
      
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="注册时间">
          <el-input :model-value="formatDate(userStore.user.createdAt)" disabled />
        </el-form-item>
      </el-form>
      
      <div class="form-actions">
        <el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button>
      </div>
    </el-card>

    <el-card class="profile-card" shadow="never">
      <h3 class="section-title">修改密码</h3>
      
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      
      <div class="form-actions">
        <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
      </div>
    </el-card>

    <el-card class="profile-card danger-card" shadow="never">
      <h3 class="section-title">账户统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-value number-font">{{ stats.billCount || 0 }}</span>
          <span class="stat-label">账单数量</span>
        </div>
        <div class="stat-item">
          <span class="stat-value number-font">{{ stats.categoryCount || 0 }}</span>
          <span class="stat-label">分类数量</span>
        </div>
        <div class="stat-item">
          <span class="stat-value number-font">{{ formatAmount(stats.totalIncome || 0) }}</span>
          <span class="stat-label">累计收入</span>
        </div>
        <div class="stat-item">
          <span class="stat-value number-font">{{ formatAmount(stats.totalExpense || 0) }}</span>
          <span class="stat-label">累计支出</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const saving = ref(false)
const passwordLoading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  email: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const stats = ref({})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为 2-20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const loadUserInfo = () => {
  form.username = userStore.user.username || ''
  form.nickname = userStore.user.nickname || ''
  form.email = userStore.user.email || ''
}

const loadStats = async () => {
  try {
    const res = await request.get('/user/stats')
    stats.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  saving.value = true
  try {
    await request.put('/user/profile', {
      nickname: form.nickname,
      email: form.email
    })
    userStore.user.nickname = form.nickname
    userStore.user.email = form.email
    ElMessage.success('修改成功')
  } catch (error) {
    // Error handled
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  passwordLoading.value = true
  try {
    await request.put('/user/password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    // Error handled
  } finally {
    passwordLoading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const formatAmount = (amount) => {
  return '¥' + Number(amount || 0).toFixed(2)
}

onMounted(() => {
  loadUserInfo()
  loadStats()
})
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.username {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

.profile-card {
  border-radius: 16px;
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.form-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.danger-card .section-title {
  color: #606266;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-value {
    font-size: 20px;
  }
}
</style>
