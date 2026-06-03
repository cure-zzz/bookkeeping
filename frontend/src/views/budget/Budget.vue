<template>
  <div class="budget-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>预算管理</h1>
      <div class="header-right">
        <el-date-picker
          v-model="currentMonth"
          type="month"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          :clearable="false"
          @change="loadBudgets"
        />
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          设置预算
        </el-button>
      </div>
    </div>

    <!-- 预算概览 -->
    <el-card class="overview-card card-hover" shadow="never">
      <div class="overview-content">
        <div class="overview-item">
          <p class="label">总预算</p>
          <p class="value number-font">¥{{ totalBudget.toFixed(2) }}</p>
        </div>
        <div class="overview-divider"></div>
        <div class="overview-item">
          <p class="label">已花费</p>
          <p class="value number-font amount-expense">¥{{ totalSpent.toFixed(2) }}</p>
        </div>
        <div class="overview-divider"></div>
        <div class="overview-item">
          <p class="label">剩余可用</p>
          <p class="value number-font" :class="remaining >= 0 ? 'amount-income' : 'amount-expense'">
            ¥{{ remaining.toFixed(2) }}
          </p>
        </div>
      </div>
      <div class="overview-progress">
        <el-progress 
          :percentage="usagePercentage" 
          :color="progressColor"
          :stroke-width="12"
          :show-text="false"
        />
        <p class="progress-text">已使用 {{ usagePercentage.toFixed(1) }}%</p>
      </div>
    </el-card>

    <!-- 预算列表 -->
    <el-card class="budget-list-card card-hover" shadow="never">
      <template #header>
        <span>分类预算</span>
      </template>
      
      <div v-if="budgets.length > 0" class="budget-grid">
        <div 
          v-for="budget in budgets" 
          :key="budget.id"
          class="budget-item"
        >
          <div class="budget-header">
            <div class="category-info">
              <span 
                class="category-dot"
                :style="{ background: getCategoryColor(budget.categoryId) }"
              ></span>
              <span class="category-name">{{ getCategoryName(budget.categoryId) }}</span>
            </div>
            <el-dropdown @command="(cmd) => handleCommand(cmd, budget)">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">
                    <el-icon><Edit /></el-icon>编辑
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <el-icon><Delete /></el-icon>删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div class="budget-amount">
            <span class="spent number-font">¥{{ getSpentAmount(budget.categoryId).toFixed(2) }}</span>
            <span class="separator">/</span>
            <span class="total number-font">¥{{ budget.amount }}</span>
          </div>
          
          <div class="budget-progress">
            <el-progress 
              :percentage="getBudgetPercentage(budget)" 
              :color="getCategoryColor(budget.categoryId)"
              :stroke-width="8"
              :show-text="false"
            />
          </div>
          
          <div class="budget-footer">
            <span class="remain" :class="getRemaining(budget) >= 0 ? 'amount-income' : 'amount-expense'">
              {{ getRemaining(budget) >= 0 ? '剩余' : '超支' }} ¥{{ Math.abs(getRemaining(budget)).toFixed(2) }}
            </span>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无预算设置">
        <el-button type="primary" @click="handleAdd">设置第一个预算</el-button>
      </el-empty>
    </el-card>

    <!-- 添加/编辑预算弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑预算' : '设置预算'"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-position="top"
      >
        <el-form-item label="支出分类" prop="categoryId">
          <el-select 
            v-model="form.categoryId" 
            placeholder="请选择分类"
            filterable
            :disabled="isEdit"
          >
            <el-option
              v-for="cat in expenseCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            >
              <div class="category-option">
                <span class="category-dot" :style="{ background: cat.color }"></span>
                {{ cat.name }}
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="预算金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :precision="2"
            :step="100"
            :min="0.01"
            :controls="false"
            placeholder="请输入预算金额"
            class="amount-input"
          >
            <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? '保存' : '确定' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled, Edit, Delete } from '@element-plus/icons-vue'
import { getBudgets, createBudget, updateBudget, deleteBudget } from '../../api/budget'
import { getStatistics } from '../../api/bill'
import { useCategoryStore } from '../../stores/category'

const categoryStore = useCategoryStore()

const currentMonth = ref(new Date().toISOString().slice(0, 7))
const budgets = ref([])
const statistics = ref({})
const dialogVisible = ref(false)
const loading = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  categoryId: null,
  amount: null
})

const rules = {
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
}

const expenseCategories = computed(() => categoryStore.getCategoriesByType(2))

const totalBudget = computed(() => {
  return budgets.value.reduce((sum, b) => sum + parseFloat(b.amount), 0)
})

const totalSpent = computed(() => {
  return budgets.value.reduce((sum, b) => {
    return sum + getSpentAmount(b.categoryId)
  }, 0)
})

const remaining = computed(() => totalBudget.value - totalSpent.value)

const usagePercentage = computed(() => {
  if (totalBudget.value === 0) return 0
  return Math.min((totalSpent.value / totalBudget.value) * 100, 100)
})

const progressColor = computed(() => {
  const pct = usagePercentage.value
  if (pct < 50) return '#67C23A'
  if (pct < 80) return '#E6A23C'
  return '#F56C6C'
})

const getCategoryName = (id) => categoryStore.getCategoryById(id)?.name || '未知'
const getCategoryColor = (id) => categoryStore.getCategoryById(id)?.color || '#409EFF'

const getSpentAmount = (categoryId) => {
  const categoryExpenses = statistics.value.categoryExpenses || []
  const found = categoryExpenses.find(e => e.categoryId === categoryId)
  return found ? parseFloat(found.amount) : 0
}

const getRemaining = (budget) => {
  const spent = getSpentAmount(budget.categoryId)
  return parseFloat(budget.amount) - spent
}

const getBudgetPercentage = (budget) => {
  const spent = getSpentAmount(budget.categoryId)
  const total = parseFloat(budget.amount)
  if (total === 0) return 0
  return Math.min((spent / total) * 100, 100)
}

const loadBudgets = async () => {
  try {
    const [budgetRes, statRes] = await Promise.all([
      getBudgets(currentMonth.value),
      getStatistics(currentMonth.value)
    ])
    budgets.value = budgetRes.data
    statistics.value = statRes.data
  } catch (error) {
    console.error('加载预算失败:', error)
  }
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  form.categoryId = null
  form.amount = null
  dialogVisible.value = true
}

const handleEdit = (budget) => {
  isEdit.value = true
  editingId.value = budget.id
  form.categoryId = budget.categoryId
  form.amount = parseFloat(budget.amount)
  dialogVisible.value = true
}

const handleDelete = (budget) => {
  ElMessageBox.confirm(
    `确定要删除「${getCategoryName(budget.categoryId)}」的预算吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBudget(budget.id)
      ElMessage.success('删除成功')
      loadBudgets()
    } catch (error) {
      // 删除失败
    }
  }).catch(() => {})
}

const handleCommand = (cmd, budget) => {
  if (cmd === 'edit') {
    handleEdit(budget)
  } else if (cmd === 'delete') {
    handleDelete(budget)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const data = {
      categoryId: form.categoryId,
      amount: form.amount,
      month: currentMonth.value
    }
    
    if (isEdit.value) {
      await updateBudget(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createBudget(data)
      ElMessage.success('设置成功')
    }
    
    dialogVisible.value = false
    loadBudgets()
  } catch (error) {
    // 验证失败或请求失败
  } finally {
    loading.value = false
  }
}

watch(currentMonth, loadBudgets)

onMounted(() => {
  loadBudgets()
})
</script>

<style scoped>
.budget-page {
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

.header-right {
  display: flex;
  gap: 12px;
}

.overview-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.overview-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 20px 0;
}

.overview-item {
  text-align: center;
}

.overview-item .label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.overview-item .value {
  font-size: 28px;
  font-weight: 600;
}

.overview-divider {
  width: 1px;
  height: 50px;
  background: #EBEEF5;
}

.overview-progress {
  padding: 0 40px 10px;
}

.progress-text {
  text-align: right;
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

.budget-list-card {
  border-radius: 16px;
}

.budget-list-card :deep(.el-card__header) {
  font-weight: 600;
  border-bottom: none;
  padding-bottom: 0;
}

.budget-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.budget-item {
  background: #F5F7FA;
  border-radius: 12px;
  padding: 20px;
}

.budget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.category-name {
  font-weight: 600;
}

.more-icon {
  cursor: pointer;
  color: #909399;
  font-size: 18px;
}

.more-icon:hover {
  color: #409EFF;
}

.budget-amount {
  margin-bottom: 12px;
}

.budget-amount .spent {
  font-size: 20px;
  font-weight: 600;
  color: #F56C6C;
}

.budget-amount .separator {
  color: #909399;
  margin: 0 4px;
}

.budget-amount .total {
  color: #909399;
}

.budget-progress {
  margin-bottom: 12px;
}

.budget-footer {
  text-align: right;
}

.remain {
  font-size: 13px;
  font-weight: 500;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.amount-input {
  width: 100%;
}

.amount-input :deep(.el-input__wrapper) {
  font-size: 18px;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
}
</style>
