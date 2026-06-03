<template>
  <div class="part-time-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>兼职收入</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加记录
      </el-button>
    </div>

    <!-- 查询区域 -->
    <el-card class="query-card" shadow="never">
      <el-form :inline="true" :model="{}">
        <el-form-item label="查询范围">
          <el-radio-group v-model="queryType" @change="handleQueryTypeChange">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="thisMonth">本月</el-radio-button>
            <el-radio-button value="thisYear">本年</el-radio-button>
            <el-radio-button value="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item v-if="queryType === 'custom'" label="日期范围">
          <el-date-picker
            v-model="queryDateRange"
            type="daterange"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="queryLoading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <div class="stat-card monthly-card">
          <div class="stat-icon bg-gradient-info">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">本月兼职收入</p>
            <p class="stat-value number-font">¥{{ stats.monthlyAmount?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card total-card">
          <div class="stat-icon bg-gradient-success">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">兼职总收入</p>
            <p class="stat-value number-font">¥{{ stats.totalAmount?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card query-card-highlight">
          <div class="stat-icon bg-gradient-warning">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">{{ queryRangeLabel }}</p>
            <p class="stat-value number-font">¥{{ queryRangeAmount?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 记录列表 -->
    <el-card class="list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>兼职记录</span>
          <span class="current-month">{{ queryType === 'thisMonth' ? stats.currentMonth : (queryType === 'thisYear' ? getCurrentYear() + '年' : (queryDateRange.length ? queryDateRange.join(' 至 ') : '请查询')) }}</span>
        </div>
      </template>

      <el-table :data="currentTableData" v-loading="loading || queryLoading" stripe style="width: 100%">
        <el-table-column label="日期" min-width="120" prop="workDate" />
        
        <el-table-column label="金额" min-width="140" align="right">
          <template #default="{ row }">
            <span class="amount-cell number-font">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="工作描述" min-width="200">
          <template #default="{ row }">
            <span class="desc-cell">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" min-width="120" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <span class="pagination-info">共 {{ pagination.total }} 条记录</span>
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="sizes, prev, pager, next"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
      
      <el-empty v-if="currentTableData.length === 0 && !loading && !queryLoading" :description="queryResult.length > 0 ? '查询范围内暂无记录' : '本月暂无兼职记录'">
        <el-button type="primary" @click="handleAdd">添加第一条记录</el-button>
      </el-empty>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑记录' : '添加兼职记录'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="工作日期" prop="workDate">
          <el-date-picker 
            v-model="form.workDate" 
            type="date" 
            format="YYYY-MM-DD" 
            value-format="YYYY-MM-DD" 
            placeholder="选择工作日期"
            style="width: 100%" 
          />
        </el-form-item>
        
        <el-form-item label="收入金额" prop="amount">
          <el-input-number 
            v-model="form.amount" 
            :min="0.01" 
            :precision="2" 
            :controls="false" 
            style="width: 100%" 
            placeholder="请输入收入金额"
          />
        </el-form-item>
        
        <el-form-item label="工作描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="描述一下做了什么兼职..."
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Calendar, Wallet, Search, DataLine } from '@element-plus/icons-vue'
import { getMonthlyStats, getStats, createPartTimeJob, updatePartTimeJob, deletePartTimeJob, queryByDateRange } from '../../api/partTimeJob'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 查询相关
const queryLoading = ref(false)
const queryType = ref('all') // all: 全部, thisMonth: 本月, thisYear: 本年, custom: 自定义
const queryDateRange = ref([])
const queryResult = ref([])

// 分页相关
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

// 查询范围统计
const queryRangeAmount = ref(0)
const queryRangeLabel = ref('全部记录')

// 默认显示本月
const getDefaultMonthRange = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  return [`${year}-${String(month).padStart(2, '0')}-01`, `${year}-${String(month).padStart(2, '0')}-${new Date(year, month, 0).getDate()}`]
}

const stats = ref({
  totalAmount: 0,
  monthlyAmount: 0,
  currentMonth: ''
})

const monthlyJobs = ref([])

const form = reactive({
  id: null,
  workDate: '',
  amount: null,
  description: ''
})

const rules = {
  workDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入收入金额', trigger: 'blur' }]
}

// 计算当前年月
const getCurrentYearMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const getCurrentYear = () => {
  return new Date().getFullYear().toString()
}

// 计算当前显示的数据源
const currentTableData = computed(() => {
  return queryResult.value.length > 0 ? queryResult.value : monthlyJobs.value
})

// 分页处理
const handleSizeChange = (val) => {
  pagination.value.size = val
  pagination.value.page = 1
  handleQuery()
}

const handlePageChange = (val) => {
  pagination.value.page = val
  handleQuery()
}

const loadStats = async () => {
  try {
    const res = await getStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const loadMonthlyJobs = async () => {
  loading.value = true
  try {
    const res = await getMonthlyStats()
    monthlyJobs.value = res.data.jobs || []
  } catch (error) {
    console.error('加载兼职记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 查询兼职收入
const handleQuery = async () => {
  queryLoading.value = true
  try {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    
    if (queryType.value === 'all') {
      params.all = true
    } else if (queryType.value === 'thisMonth') {
      // 本月
      params.startMonth = `${year}-${String(month).padStart(2, '0')}`
      params.endMonth = `${year}-${String(month).padStart(2, '0')}`
    } else if (queryType.value === 'thisYear') {
      // 本年
      params.startMonth = `${year}-01`
      params.endMonth = `${year}-12`
    } else if (queryType.value === 'custom') {
      // 自定义范围
      if (queryDateRange.value && queryDateRange.value.length === 2) {
        params.startDate = queryDateRange.value[0]
        params.endDate = queryDateRange.value[1]
      } else {
        ElMessage.warning('请选择日期范围')
        queryLoading.value = false
        return
      }
    }
    
    const res = await queryByDateRange(params)
    queryResult.value = res.data.jobs || []
    pagination.value.total = res.data.total || 0
    // 只有查询范围合计变化，兼职总收入保持不变
    queryRangeAmount.value = res.data.totalAmount || 0
    
    // 更新查询范围标签
    if (res.data.rangeLabel) {
      queryRangeLabel.value = res.data.rangeLabel
    } else if (queryType.value === 'thisMonth') {
      queryRangeLabel.value = '本月合计'
    } else if (queryType.value === 'thisYear') {
      queryRangeLabel.value = '本年合计'
    } else if (queryType.value === 'custom') {
      queryRangeLabel.value = '查询范围合计'
    }
    
    // 显示查询结果的金额
    if (queryType.value === 'thisMonth') {
      stats.value.monthlyAmount = res.data.totalAmount || 0
    }
  } catch (error) {
    console.error('查询失败:', error)
  } finally {
    queryLoading.value = false
  }
}

const handleQueryTypeChange = () => {
  if (queryType.value === 'custom') {
    queryDateRange.value = getDefaultMonthRange()
  } else if (queryType.value === 'all') {
    // 全部记录不需要额外参数
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    workDate: new Date().toISOString().split('T')[0],
    amount: null,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    workDate: row.workDate,
    amount: row.amount,
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
    
    if (isEdit.value) {
      await updatePartTimeJob(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createPartTimeJob(submitData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadStats()
    loadMonthlyJobs()
    // 如果有查询结果，刷新查询
    if (queryResult.value.length > 0) {
      pagination.value.page = 1
      handleQuery()
    }
  } catch (error) {
    // Error handled in interceptor
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除 ${row.workDate} 的这条记录吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deletePartTimeJob(row.id)
      ElMessage.success('删除成功')
      loadStats()
      loadMonthlyJobs()
      // 如果有查询结果，刷新查询
      if (queryResult.value.length > 0) {
        handleQuery()
      }
    } catch (error) {
      // Error handled
    }
  }).catch(() => {})
}

onMounted(() => {
  loadStats()
  // 初始查询全部记录
  handleQuery()
})
</script>

<style scoped>
.part-time-page {
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

.query-card {
  margin-bottom: 20px;
  border-radius: 16px;
}

.query-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.stats-row {
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

.bg-gradient-info {
  background: linear-gradient(135deg, #409EFF 0%, #79BBFF 100%);
}

.bg-gradient-success {
  background: linear-gradient(135deg, #67C23A 0%, #95D475 100%);
}

.bg-gradient-warning {
  background: linear-gradient(135deg, #E6A23C 0%, #F3D19E 100%);
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.list-card {
  border-radius: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-month {
  color: #909399;
  font-size: 14px;
}

.amount-cell {
  font-weight: 600;
  color: #67C23A;
}

.desc-cell {
  color: #606266;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.pagination-info {
  color: #909399;
  font-size: 14px;
  margin-right: 16px;
}
</style>
