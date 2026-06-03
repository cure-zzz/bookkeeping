<template>
  <div class="bill-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>账单管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加账单
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="monthrange"
            range-separator="至"
            start-placeholder="开始月份"
            end-placeholder="结束月份"
            format="YYYY-MM"
            value-format="YYYY-MM"
            :shortcuts="monthShortcuts"
            :unlink-panels="true"
            :disabled-date="disabledDate"
            @change="handleMonthFilter"
            style="width: 260px"
          />
          <el-button 
            :type="dateRangeMode === 'day' ? 'primary' : 'default'" 
            size="small" 
            style="margin-left: 8px"
            @click="switchToDayMode"
          >
            日
          </el-button>
        </el-form-item>
        
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable @change="handleFilter">
            <el-option label="全部" :value="null" />
            <el-option label="收入" :value="1" />
            <el-option label="支出" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="全部" clearable @change="handleFilter" filterable>
            <el-option label="全部" :value="null" />
            <el-option
              v-for="cat in categories"
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
        
        <el-form-item>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账单列表 -->
    <el-card class="bill-card" shadow="never">
      <el-table 
        :data="bills" 
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column label="日期" width="120" prop="billDate">
          <template #default="{ row }">
            <span class="date-cell">{{ formatDate(row.billDate) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="分类" min-width="150">
          <template #default="{ row }">
            <div class="category-cell">
              <span 
                class="category-badge"
                :style="{ 
                  background: getCategoryColor(row.categoryId) + '20',
                  color: getCategoryColor(row.categoryId)
                }"
              >
                {{ getCategoryName(row.categoryId) }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="备注" min-width="200">
          <template #default="{ row }">
            <span class="desc-cell">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            <span 
              class="amount-cell number-font"
              :class="row.type === 1 ? 'amount-income' : 'amount-expense'"
            >
              {{ row.type === 1 ? '+' : '-' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center">
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
      
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
      
      <div v-if="bills.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无账单记录">
          <el-button type="primary" @click="handleAdd">添加第一笔账单</el-button>
        </el-empty>
      </div>
    </el-card>

    <!-- 添加/编辑抽屉 -->
    <BillDrawer ref="drawerRef" @success="loadBills" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getBills, deleteBill } from '../../api/bill'
import { useCategoryStore } from '../../stores/category'
import BillDrawer from './BillDrawer.vue'

const categoryStore = useCategoryStore()

const bills = ref([])
const loading = ref(false)
const dateRange = ref([])
const drawerRef = ref(null)
const dateRangeMode = ref('month') // 默认使用月份范围

const filterForm = reactive({
  type: null,
  categoryId: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  page: 1,
  pageSize: 20
})

const total = ref(0)

const categories = computed(() => categoryStore.categories)

const monthShortcuts = [
  {
    text: '本月',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth(), 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return [start, end]
    }
  },
  {
    text: '近3个月',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth() - 2, 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return [start, end]
    }
  },
  {
    text: '近6个月',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), now.getMonth() - 5, 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return [start, end]
    }
  },
  {
    text: '本年',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear(), 0, 1)
      const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
      return [start, end]
    }
  },
  {
    text: '去年',
    value: () => {
      const now = new Date()
      const start = new Date(now.getFullYear() - 1, 0, 1)
      const end = new Date(now.getFullYear() - 1, 11, 31)
      return [start, end]
    }
  }
]

const getCategoryName = (id) => categoryStore.getCategoryById(id)?.name || '未知'
const getCategoryColor = (id) => categoryStore.getCategoryById(id)?.color || '#409EFF'

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric',
    weekday: 'short'
  })
}

// 限制结束日期不能早于开始日期
const disabledDate = (time) => {
  if (dateRange.value && dateRange.value[0]) {
    const startDate = new Date(dateRange.value[0] + '-01')
    return time.getTime() < startDate.getTime()
  }
  return false
}

const handleFilter = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    filterForm.startDate = dateRange.value[0]
    filterForm.endDate = dateRange.value[1]
  } else {
    filterForm.startDate = null
    filterForm.endDate = null
  }
  pagination.page = 1
  loadBills()
}

const handleMonthFilter = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    // 月份范围需要转换为完整的日期范围
    filterForm.startDate = dateRange.value[0] + '-01'
    // 获取该月最后一天
    const endDate = new Date(dateRange.value[1] + '-01')
    const lastDay = new Date(endDate.getFullYear(), endDate.getMonth() + 1, 0)
    filterForm.endDate = dateRange.value[1] + '-' + String(lastDay.getDate()).padStart(2, '0')
  } else {
    filterForm.startDate = null
    filterForm.endDate = null
  }
  pagination.page = 1
  loadBills()
}

const switchToDayMode = () => {
  dateRangeMode.value = 'day'
  dateRange.value = []
  filterForm.startDate = null
  filterForm.endDate = null
}

const resetFilter = () => {
  dateRange.value = []
  dateRangeMode.value = 'month'
  filterForm.type = null
  filterForm.categoryId = null
  filterForm.startDate = null
  filterForm.endDate = null
  pagination.page = 1
  loadBills()
}

const loadBills = async () => {
  loading.value = true
  try {
    const res = await getBills({
      ...filterForm,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    // res.data 包含 PageResult: {data: [...], total: 100}
    bills.value = Array.isArray(res.data?.data) ? res.data.data : []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('加载账单失败:', error)
    bills.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  drawerRef.value?.open({})
}

const handleEdit = (row) => {
  drawerRef.value?.open(row)
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除这笔 ${row.type === 1 ? '收入' : '支出'} ¥${row.amount} 吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteBill(row.id)
      ElMessage.success('删除成功')
      loadBills()
    } catch (error) {
      // 删除失败
    }
  }).catch(() => {})
}

const handleSizeChange = () => {
  pagination.page = 1
  loadBills()
}

const handlePageChange = () => {
  loadBills()
}

onMounted(() => {
  loadBills()
})
</script>

<style scoped>
.bill-list-page {
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

.filter-card,
.bill-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.date-cell {
  color: #606266;
}

.category-cell {
  display: flex;
  align-items: center;
}

.category-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.desc-cell {
  color: #909399;
  font-size: 13px;
}

.amount-cell {
  font-size: 16px;
  font-weight: 600;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  padding: 60px 0;
}
</style>
