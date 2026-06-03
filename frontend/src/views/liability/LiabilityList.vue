<template>
  <div class="liability-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>账户管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加账户
      </el-button>
    </div>

    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="12">
        <div class="stat-card asset-card">
          <div class="stat-icon bg-gradient-success">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">账户总额</p>
            <p class="stat-value number-font">¥{{ totalUsed?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="stat-card debt-card">
          <div class="stat-icon bg-gradient-danger">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">剩余负债</p>
            <p class="stat-value number-font">¥{{ totalLiability?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 负债列表 -->
    <el-card class="list-card" shadow="never">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="资金账户" name="asset" />
        <el-tab-pane label="负债" name="liability" />
      </el-tabs>

      <el-table :data="filteredList" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="名称" min-width="180" prop="name" />
        
        <el-table-column label="类型" min-width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'danger'" size="small">
              {{ row.type === 1 ? '资金账户' : '负债' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="总额" min-width="140" align="right">
          <template #default="{ row }">
            <span class="amount-cell number-font">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="剩余" min-width="140" align="right">
          <template #default="{ row }">
            <span class="amount-cell number-font" :class="{ 'negative': (row.remainingAmount || 0) > 0 }">
              ¥{{ row.remainingAmount?.toFixed(2) || '0.00' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column v-if="activeTab === 'liability'" label="到期日" min-width="120">
          <template #default="{ row }">
            <span class="date-cell">{{ row.dueDate || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="备注" min-width="160">
          <template #default="{ row }">
            <span class="desc-cell">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="180" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.type === 2" 
              type="success" 
              link 
              @click="openRepaymentDialog(row)"
            >
              还款
            </el-button>
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="filteredList.length === 0 && !loading" description="暂无记录">
        <el-button type="primary" @click="handleAdd">添加第一条记录</el-button>
      </el-empty>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑账户' : '添加账户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：现金、支付宝、银行账户" />
        </el-form-item>
        
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">资金账户</el-radio>
            <el-radio :label="2">负债</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="总额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        
        <el-form-item v-if="form.type === 2" label="到期日" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="date" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="备注" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 还款对话框 -->
    <el-dialog v-model="repaymentDialogVisible" :title="`还款 - ${currentLiability?.name}`" width="600px">
      <!-- 还款表单 -->
      <el-form :model="repaymentForm" :rules="repaymentRules" ref="repaymentFormRef" label-width="100px">
        <el-form-item label="剩余负债">
          <span class="amount-cell number-font">¥{{ currentLiability?.remainingAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="还款金额" prop="amount">
          <el-input-number 
            v-model="repaymentForm.amount" 
            :min="0.01" 
            :max="currentLiability?.remainingAmount || 0"
            :precision="2" 
            :controls="false" 
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="repaymentForm.description" placeholder="如：还花呗、还信用卡" />
        </el-form-item>
        <el-form-item label="还款日期">
          <el-date-picker 
            v-model="repaymentForm.repaymentDate" 
            type="date" 
            format="YYYY-MM-DD" 
            value-format="YYYY-MM-DD"
            placeholder="选择还款日期"
            style="width: 100%" 
          />
        </el-form-item>
      </el-form>
      
      <el-button type="primary" @click="handleRepayment" :loading="repaymentLoading" style="margin-top: 16px;">
        确认还款
      </el-button>
      
      <!-- 还款日志 -->
      <el-divider />
      <div class="repayment-log">
        <h4>还款记录</h4>
        <el-table :data="repayments" size="small" max-height="250">
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span class="number-font">¥{{ row.amount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="200" />
          <el-table-column prop="repaymentDate" label="还款日期" width="120">
            <template #default="{ row }">
              {{ row.repaymentDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="handleDeleteRepayment(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="repayments.length === 0" description="暂无还款记录" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Wallet } from '@element-plus/icons-vue'
import { getLiabilities, createLiability, updateLiability, deleteLiability, createRepayment, getRepayments, deleteRepayment } from '../../api/liability'

const liabilities = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const activeTab = ref('asset')
const formRef = ref(null)

// 还款相关
const repaymentDialogVisible = ref(false)
const currentLiability = ref(null)
const repayments = ref([])
const repaymentLoading = ref(false)
const repaymentFormRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  type: 1,
  amount: null,
  dueDate: null,
  description: ''
})

const repaymentForm = reactive({
  amount: null,
  description: '',
  repaymentDate: null
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

const repaymentRules = {
  amount: [{ required: true, message: '请输入还款金额', trigger: 'blur' }]
}

const filteredList = computed(() => {
  return liabilities.value.filter(item => {
    if (activeTab.value === 'asset') return item.type === 1
    if (activeTab.value === 'liability') return item.type === 2
    return true
  })
})

// 计算资金账户剩余总额
const totalUsed = computed(() => {
  return liabilities.value.filter(item => item.type === 1).reduce((sum, item) => sum + (item.remainingAmount || 0), 0)
})

// 计算剩余负债总额（remainingAmount 表示还未还的金额）
const totalLiability = computed(() => {
  return liabilities.value.filter(item => item.type === 2).reduce((sum, item) => sum + (item.remainingAmount || 0), 0)
})

const handleTabChange = () => {
  // Tab change
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getLiabilities()
    liabilities.value = res.data || []
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    type: activeTab.value === 'asset' ? 1 : 2,
    amount: activeTab.value === 'liability' ? null : 0,
    dueDate: null,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    type: row.type,
    amount: row.amount,
    dueDate: row.dueDate,
    description: row.description
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const submitData = { ...form }
    // 资金账户不设置 usedAmount 和 remainingAmount
    if (form.type === 1) {
      submitData.usedAmount = 0
      submitData.remainingAmount = 0
    }
    
    if (isEdit.value) {
      await updateLiability(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createLiability(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    // Error handled in interceptor
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除"${row.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteLiability(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      // Error handled
    }
  }).catch(() => {})
}

// 还款相关方法
const openRepaymentDialog = async (row) => {
  currentLiability.value = row
  repaymentDialogVisible.value = true
  repaymentForm.amount = null
  repaymentForm.description = ''
  repaymentForm.repaymentDate = null  // 重置日期为当天
  await loadRepayments(row.id)
}

const loadRepayments = async (liabilityId) => {
  try {
    const res = await getRepayments(liabilityId)
    repayments.value = res.data || []
  } catch (error) {
    console.error('加载还款记录失败:', error)
  }
}

const handleRepayment = async () => {
  if (!repaymentForm.amount || repaymentForm.amount <= 0) {
    ElMessage.warning('请输入正确的还款金额')
    return
  }
  
  repaymentLoading.value = true
  try {
    await createRepayment({
      liabilityId: currentLiability.value.id,
      amount: repaymentForm.amount,
      description: repaymentForm.description,
      repaymentDate: repaymentForm.repaymentDate
    })
    ElMessage.success('还款成功')
    repaymentForm.amount = null
    repaymentForm.description = ''
    repaymentForm.repaymentDate = null
    await loadRepayments(currentLiability.value.id)
    loadData() // 刷新列表
  } catch (error) {
    // Error handled
  } finally {
    repaymentLoading.value = false
  }
}

const handleDeleteRepayment = (row) => {
  ElMessageBox.confirm('确定要删除这条还款记录吗？删除后将恢复负债金额', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRepayment(row.id)
      ElMessage.success('删除成功')
      await loadRepayments(currentLiability.value.id)
      loadData()
    } catch (error) {
      // Error handled
    }
  }).catch(() => {})
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.liability-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 600;
}

.overview-row {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
}

.list-card {
  border-radius: 16px;
}

.amount-cell {
  font-weight: 600;
}

.amount-cell.negative {
  color: #F56C6C;
}

.date-cell,
.desc-cell {
  color: #909399;
  font-size: 13px;
}

.repayment-log {
  margin-top: 16px;
}

.repayment-log h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}
</style>
