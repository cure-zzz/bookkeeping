<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <div class="logo">
          <el-icon :size="48"><Wallet /></el-icon>
        </div>
        <h1>记账本</h1>
        <p>开启你的财务自由之旅</p>
      </div>
      <div class="features">
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>简洁易用的界面设计</span>
        </div>
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>安全可靠的数据存储</span>
        </div>
        <div class="feature-item">
          <el-icon><Check /></el-icon>
          <span>专业的财务管理功能</span>
        </div>
      </div>
    </div>
    
    <div class="login-right">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>注册</h2>
            <p>创建你的账户</p>
          </div>
        </template>
        
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="email">
            <el-input 
              v-model="form.email" 
              type="email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
            />
          </el-form-item>
          
          <el-form-item prop="nickname">
            <el-input 
              v-model="form.nickname" 
              placeholder="请输入昵称（选填）"
              :prefix-icon="UserFilled"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="form.confirmPassword" 
              type="password" 
              placeholder="请确认密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              native-type="submit" 
              :loading="loading" 
              class="login-btn"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="footer">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Wallet, Check, UserFilled, Message } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { useCategoryStore } from '../../stores/category'

const router = useRouter()
const userStore = useUserStore()
const categoryStore = useCategoryStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!value) {
    callback(new Error('请输入邮箱'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    await userStore.register({
      username: form.username,
      email: form.email,
      nickname: form.nickname || form.username,
      password: form.password
    })
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // 验证失败或注册失败
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 50%, #A0D468 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  padding: 40px;
}

.brand {
  text-align: center;
  margin-bottom: 60px;
}

.logo {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
}

.brand h1 {
  font-size: 48px;
  font-weight: 600;
  margin-bottom: 12px;
}

.brand p {
  font-size: 18px;
  opacity: 0.9;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 24px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.feature-item .el-icon {
  font-size: 20px;
}

.login-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-card {
  width: 100%;
  border: none;
}

.login-card :deep(.el-card__header) {
  border-bottom: none;
  padding-bottom: 0;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.card-header p {
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.footer span {
  margin-right: 4px;
}

@media (max-width: 768px) {
  .login-left {
    display: none;
  }
  
  .login-right {
    width: 100%;
  }
}
</style>
