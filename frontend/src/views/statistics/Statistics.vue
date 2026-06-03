<template>
  <div class="statistics-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>统计报表</h1>
      <el-date-picker
        v-model="currentMonth"
        type="month"
        format="YYYY年MM月"
        value-format="YYYY-MM"
        :clearable="false"
        @change="loadStatistics"
      />
    </div>

    <!-- 收支概览 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="8">
        <div class="stat-card income-card card-hover">
          <div class="stat-icon bg-gradient-success">
            <el-icon><Top /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">本月收入</p>
            <p class="stat-value number-font">¥{{ statistics.totalIncome?.toFixed(2) || '0.00' }}</p>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card expense-card card-hover">
          <div class="stat-icon bg-gradient-danger">
            <el-icon><Bottom /></el-icon>
          </div>
          <div class="stat-content">
            <p class="stat-label">本月支出</p>
            <p class="stat-value number-font">¥{{ statistics.totalExpense?.toFixed(2) || '0.00' }}</p>
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
            <p 
              class="stat-value number-font"
              :class="(statistics.balance || 0) >= 0 ? 'amount-income' : 'amount-expense'"
            >
              ¥{{ statistics.balance?.toFixed(2) || '0.00' }}
            </p>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card class="chart-card card-hover" shadow="never">
          <template #header>
            <span>支出分类占比</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card card-hover" shadow="never">
          <template #header>
            <span>每日收支趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类支出详情 -->
    <el-card class="category-detail-card card-hover" shadow="never">
      <template #header>
        <span>分类支出详情</span>
      </template>
      <div v-if="statistics.categoryExpenses?.length > 0" class="category-list">
        <div 
          v-for="item in statistics.categoryExpenses" 
          :key="item.categoryId"
          class="category-item"
        >
          <div class="category-info">
            <span 
              class="category-dot"
              :style="{ background: item.color }"
            ></span>
            <span class="category-name">{{ item.categoryName }}</span>
          </div>
          <div class="category-progress">
            <el-progress 
              :percentage="item.percentage || 0" 
              :color="item.color"
              :stroke-width="10"
              :show-text="false"
            />
          </div>
          <div class="category-amount">
            <span class="amount number-font">¥{{ item.amount?.toFixed(2) }}</span>
            <span class="percentage">({{ item.percentage?.toFixed(1) }}%)</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="本月暂无支出记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { Top, Bottom, TrendCharts } from '@element-plus/icons-vue'
import { getStatistics } from '../../api/bill'
import * as echarts from 'echarts'

const currentMonth = ref(new Date().toISOString().slice(0, 7))
const statistics = ref({})

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const getCurrentMonth = () => {
  return currentMonth.value
}

const loadStatistics = async () => {
  try {
    const res = await getStatistics(getCurrentMonth())
    statistics.value = res.data
    updateCharts()
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const updateCharts = () => {
  updatePieChart()
  updateLineChart()
}

const updatePieChart = () => {
  if (!pieChartRef.value || !statistics.value.categoryExpenses?.length) return
  
  const data = statistics.value.categoryExpenses.map(item => ({
    name: item.categoryName,
    value: item.amount,
    itemStyle: { color: item.color }
  }))

  pieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 20,
      top: 'center',
      textStyle: { color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data
    }]
  })
}

const updateLineChart = () => {
  if (!lineChartRef.value || !statistics.value.dailyAmounts?.length) return
  
  const dates = statistics.value.dailyAmounts.map(item => item.date.slice(5))
  const incomes = statistics.value.dailyAmounts.map(item => item.income)
  const expenses = statistics.value.dailyAmounts.map(item => item.expense)

  lineChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = params[0].name + '<br/>'
        params.forEach(param => {
          result += `${param.marker} ${param.seriesName}: ¥${param.value}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['收入', '支出'],
      textStyle: { color: '#606266' },
      top: 0,
      right: 10
    },
    grid: {
      left: 50,
      right: 20,
      top: 40,
      bottom: 30,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#EBEEF5' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#909399' },
      splitLine: { lineStyle: { color: '#F5F7FA' } }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: incomes,
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: expenses,
        itemStyle: { color: '#F56C6C' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        }
      }
    ]
  })
}

const initCharts = () => {
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
  }
  window.addEventListener('resize', handleResize)
}

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

watch(currentMonth, loadStatistics)

onMounted(() => {
  initCharts()
  loadStatistics()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.statistics-page {
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

.charts-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 16px;
  height: 400px;
}

.chart-card :deep(.el-card__header) {
  font-weight: 600;
  border-bottom: none;
  padding-bottom: 0;
}

.chart-container {
  height: 320px;
}

.category-detail-card {
  border-radius: 16px;
}

.category-detail-card :deep(.el-card__header) {
  font-weight: 600;
  border-bottom: none;
  padding-bottom: 0;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.category-info {
  width: 120px;
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
  font-weight: 500;
}

.category-progress {
  flex: 1;
}

.category-amount {
  width: 150px;
  text-align: right;
}

.amount {
  font-weight: 600;
  margin-right: 4px;
}

.percentage {
  color: #909399;
  font-size: 12px;
}
</style>
