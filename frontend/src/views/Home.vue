<template>
  <div class="home">
    <!-- 欢迎卡片 -->
    <div class="welcome-card card-hover">
      <div class="welcome-content">
        <h2>你好，{{ userStore.user.nickname || userStore.user.username }} 👋</h2>
        <p>今天是 {{ today }}，继续加油！</p>
      </div>
      <div class="welcome-illustration">
        <el-icon :size="80" color="#fff"><Wallet /></el-icon>
      </div>
    </div>

    <!-- 今日概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="8">
        <div class="stat-card income-card card-hover">
          <div class="stat-icon bg-gradient-success">
            <el-icon><Top /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">今日收入</p>
            <p class="stat-value number-font">¥{{ todayIncome.toFixed(2) }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card expense-card card-hover">
          <div class="stat-icon bg-gradient-danger">
            <el-icon><Bottom /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">今日支出</p>
            <p class="stat-value number-font">¥{{ todayExpense.toFixed(2) }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card balance-card card-hover">
          <div class="stat-icon bg-gradient-primary">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">本月结余</p>
            <p class="stat-value number-font" :class="monthBalance >= 0 ? 'amount-income' : 'amount-expense'">
              ¥{{ monthBalance.toFixed(2) }}
            </p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 最近账单和年度图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="recent-bills-card card-hover">
          <template #header>
            <div class="card-header">
              <span>最近账单</span>
              <el-button type="primary" link @click="$router.push('/bills')">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          
          <div v-if="recentBills.length === 0" class="empty-state">
            <el-empty description="暂无账单记录" />
          </div>
          
          <div v-else class="bill-list">
            <div 
              v-for="bill in recentBills" 
              :key="bill.id" 
              class="bill-item"
            >
              <div class="bill-left">
                <div 
                  class="category-icon" 
                  :style="{ background: getCategoryColor(bill.categoryId) + '20', color: getCategoryColor(bill.categoryId) }"
                >
                  <el-icon><Coin /></el-icon>
                </div>
                <div class="bill-info">
                  <p class="category-name">{{ getCategoryName(bill.categoryId) }}</p>
                  <p class="bill-desc">{{ bill.description || '无备注' }}</p>
                </div>
              </div>
              <div class="bill-right">
                <p class="bill-amount" :class="bill.type === 1 ? 'amount-income' : 'amount-expense'">
                  {{ bill.type === 1 ? '+' : '-' }}¥{{ bill.amount }}
                </p>
                <p class="bill-date">{{ formatDate(bill.billDate) }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="yearly-chart-card card-hover">
          <template #header>
            <div class="card-header">
              <span>本年收入支出</span>
              <el-select v-model="selectedYear" size="small" style="width: 100px" @change="loadYearlyData">
                <el-option 
                  v-for="year in availableYears" 
                  :key="year" 
                  :label="year + '年'" 
                  :value="year" 
                />
              </el-select>
            </div>
          </template>
          
          <div ref="chartRef" class="chart-container" :style="{ height: chartHeight + 'px' }"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card card-hover">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="quick-actions">
        <el-button type="primary" circle size="large" @click="showAddBill('income')">
          <el-icon><Top /></el-icon>
          记收入
        </el-button>
        <el-button type="danger" circle size="large" @click="showAddBill('expense')">
          <el-icon><Bottom /></el-icon>
          记支出
        </el-button>
      </div>
    </el-card>

    <!-- 添加账单抽屉 -->
    <BillDrawer ref="billDrawerRef" @success="loadData" />

    <!-- 备案号 -->
    <div class="icp-footer">
      <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">
        黔ICP备2026008738号-1
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Top, Bottom, TrendCharts, Coin, ArrowRight } from '@element-plus/icons-vue'
import { getRecentBills, getStatistics, getYearlyStatistics } from '../api/bill'
import { useUserStore } from '../stores/user'
import { useCategoryStore } from '../stores/category'
import BillDrawer from './bill/BillDrawer.vue'
import * as echarts from 'echarts'

const userStore = useUserStore()
const categoryStore = useCategoryStore()

const recentBills = ref([])
const todayIncome = ref(0)
const todayExpense = ref(0)
const monthBalance = ref(0)
const billDrawerRef = ref(null)

// 图表相关
const chartRef = ref(null)
const selectedYear = ref(new Date().getFullYear())
const availableYears = ref([])
const chartInstance = ref(null)
const chartHeight = ref(380)
let resizeObserver = null
let chartReady = false

const today = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

const getCategoryName = (id) => {
  return categoryStore.getCategoryById(id)?.name || '未知'
}

const getCategoryColor = (id) => {
  return categoryStore.getCategoryById(id)?.color || '#409EFF'
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

const showAddBill = (type) => {
  billDrawerRef.value?.open({ type: type === 'income' ? 1 : 2 })
}

const initChart = () => {
  if (!chartRef.value) return

  chartInstance.value = echarts.init(chartRef.value, null, { renderer: 'canvas' })

  // 默认零数据
  const zeroData = new Array(12).fill(null)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: '#fff',
      borderColor: '#e4e7ed',
      borderWidth: 1,
      padding: [10, 14],
      textStyle: { color: '#303133', fontSize: 13 },
      formatter: function (params) {
        if (!params || !params.length) return ''
        var idx = params[0].dataIndex
        var month = String(idx + 1).padStart(2, '0')
        var title = selectedYear.value + '年' + month + '月'
        var income = 0, expense = 0
        for (var i = 0; i < params.length; i++) {
          if (params[i].seriesName === '收入') income = params[i].value || 0
          if (params[i].seriesName === '支出') expense = params[i].value || 0
        }
        return '<div style="min-width:140px;">' +
          '<div style="font-weight:600;margin-bottom:10px;padding-bottom:8px;' +
          'border-bottom:1px solid #f0f0f0;color:#303133;">' + title + '</div>' +
          '<div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px;">' +
          '<span><span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#22c55e;margin-right:6px;vertical-align:middle;"></span><span style="color:#909399;">收入</span></span>' +
          '<span style="font-weight:600;color:#22c55e;">¥' + income.toFixed(2) + '</span></div>' +
          '<div style="display:flex;align-items:center;justify-content:space-between;">' +
          '<span><span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:#ef4444;margin-right:6px;vertical-align:middle;"></span><span style="color:#909399;">支出</span></span>' +
          '<span style="font-weight:600;color:#ef4444;">¥' + expense.toFixed(2) + '</span></div>' +
          '</div>'
      }
    },
    legend: {
      data: ['收入', '支出'],
      bottom: 0,
      itemGap: 24,
      textStyle: { color: '#606266', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '18%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
      axisLabel: { color: '#909399', fontSize: 12 },
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#909399',
        fontSize: 12,
        formatter: function (val) {
          if (val >= 10000) return (val / 10000) + '万'
          return val
        }
      },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        barWidth: '40%',
        barCategoryGap: '5%',
        itemStyle: {
          color: '#22c55e',
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          color: '#22c55e',
          fontSize: 11,
          formatter: function (params) {
            if (params.value === null || params.value === 0) return ''
            return params.value.toLocaleString()
          }
        },
        data: zeroData
      },
      {
        name: '支出',
        type: 'bar',
        barWidth: '40%',
        barCategoryGap: '5%',
        itemStyle: {
          color: '#ef4444',
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          color: '#ef4444',
          fontSize: 11,
          formatter: function (params) {
            if (params.value === null || params.value === 0) return ''
            return params.value.toLocaleString()
          }
        },
        data: zeroData
      }
    ]
  }

  chartInstance.value.setOption(option)
  chartReady = true

  // 使用 window resize 代替 ResizeObserver，避免频繁触发
  const handleResize = () => {
    if (chartReady && chartInstance.value && chartRef.value) {
      chartInstance.value.resize()
    }
  }
  window.addEventListener('resize', handleResize)
  // 保存引用以便卸载时移除
  resizeObserver = { disconnect: () => window.removeEventListener('resize', handleResize) }
}

const loadYearlyData = async () => {
  try {
    const res = await getYearlyStatistics(selectedYear.value)
    const monthlyData = res.data

    const incomeData = []
    const expenseData = []

    for (var i = 1; i <= 12; i++) {
      var monthData = monthlyData[i] || { income: 0, expense: 0 }
      incomeData.push(parseFloat(monthData.income) || 0)
      expenseData.push(parseFloat(monthData.expense) || 0)
    }

    if (chartInstance.value) {
      // 直接更新 series 数据，避免 setOption 配置冲突问题
      chartInstance.value.setOption({
        series: [
          { name: '收入', data: incomeData },
          { name: '支出', data: expenseData }
        ]
      })
    }
  } catch (error) {
    console.error('加载年度数据失败:', error)
  }
}

const updateAvailableYears = () => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let y = currentYear; y >= currentYear - 5; y--) {
    years.push(y)
  }
  availableYears.value = years
}

const loadData = async () => {
  try {
    const [recentRes, statRes] = await Promise.all([
      getRecentBills(5),
      getStatistics(getCurrentMonth())
    ])
    
    recentBills.value = recentRes.data
    monthBalance.value = statRes.data.balance || 0
    
    // 计算今日收支
    const today = new Date().toISOString().split('T')[0]
    recentRes.data.forEach(bill => {
      if (bill.billDate === today) {
        if (bill.type === 1) {
          todayIncome.value += parseFloat(bill.amount)
        } else {
          todayExpense.value += parseFloat(bill.amount)
        }
      }
    })
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const getCurrentMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

onMounted(() => {
  updateAvailableYears()
  loadData()
  nextTick(() => {
    initChart()
    loadYearlyData()
  })
})

onUnmounted(() => {
  chartReady = false
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
  color: white;
  padding: 32px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-content h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.welcome-content p {
  opacity: 0.9;
  font-size: 14px;
}

.welcome-illustration {
  opacity: 0.3;
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

.recent-bills-card,
.quick-actions-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-weight: 600;
  font-size: 16px;
}

.bill-list {
  display: flex;
  flex-direction: column;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #EBEEF5;
}

.bill-item:last-child {
  border-bottom: none;
}

.bill-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.bill-info .category-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.bill-desc {
  color: #909399;
  font-size: 12px;
}

.bill-right {
  text-align: right;
}

.bill-amount {
  font-weight: 600;
  font-size: 16px;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
}

.bill-date {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.empty-state {
  padding: 40px 0;
}

.quick-actions {
  display: flex;
  gap: 16px;
}

.quick-actions .el-button {
  width: 120px;
  height: 120px;
  font-size: 16px;
  border-radius: 16px;
  flex-direction: column;
  gap: 8px;
}

.recent-bills-card,
.yearly-chart-card {
  border-radius: 16px;
  margin-bottom: 24px;
}

.chart-row {
  display: flex;
  align-items: stretch;
}

.chart-row .el-col {
  display: flex;
  flex-direction: column;
}

.chart-row .el-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chart-container {
  width: 100%;
  height: 360px;
}

.icp-footer {
  text-align: center;
  padding: 16px 0;
  margin-top: 8px;
}

.icp-footer a {
  color: #909399;
  font-size: 12px;
  text-decoration: none;
}

.icp-footer a:hover {
  color: #409EFF;
}
</style>
