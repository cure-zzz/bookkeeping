<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑账单' : '添加账单'"
    size="450px"
    :before-close="handleClose"
  >
    <el-form 
      ref="formRef" 
      :model="form" 
      :rules="rules" 
      label-position="top"
      class="bill-form"
    >
      <el-form-item label="账单类型" prop="type">
        <el-segmented v-model="form.type" :options="typeOptions" @change="handleTypeChange" />
      </el-form-item>
      
      <el-form-item label="金额" prop="amount">
        <el-input-number
          v-model="form.amount"
          :precision="2"
          :step="0.01"
          :min="0.01"
          :controls="false"
          placeholder="请输入金额"
          class="amount-input"
        >
          <template #prefix>¥</template>
        </el-input-number>
      </el-form-item>
      
      <el-form-item label="分类" prop="categoryId">
        <el-select 
          v-model="form.categoryId" 
          placeholder="请选择分类"
          class="category-select"
          filterable
        >
          <el-option
            v-for="cat in filteredCategories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          >
            <div class="category-option">
              <span 
                class="category-dot" 
                :style="{ background: cat.color }"
              ></span>
              {{ cat.name }}
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="支付方式" prop="paymentAccountId">
        <el-select 
          v-model="form.paymentAccountId" 
          :placeholder="accounts.length > 0 ? '请选择支付方式（可选）' : '暂无可用账户，请在账户管理中添加'"
          class="category-select"
          clearable
          :disabled="accounts.length === 0"
        >
          <el-option
            v-for="acc in accounts"
            :key="acc.id"
            :label="acc.name + ' (剩余: ¥' + (acc.remainingAmount || 0).toFixed(2) + ')'"
            :value="acc.id"
          >
            <div class="category-option">
              <span class="account-dot" :style="{ background: '#409EFF' }"></span>
              {{ acc.name }}
              <span class="account-balance">¥{{ (acc.remainingAmount || 0).toFixed(2) }}</span>
            </div>
          </el-option>
        </el-select>
      </el-form-item>
      
      <el-form-item label="日期" prop="billDate">
        <el-date-picker
          v-model="form.billDate"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :disabled-date="disabledDate"
          class="date-picker"
        />
      </el-form-item>
      
      <el-form-item label="备注">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="添加备注（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="drawer-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          {{ isEdit ? '保存' : '添加' }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createBill, updateBill } from '../../api/bill'
import { useCategoryStore } from '../../stores/category'
import { useAccountStore } from '../../stores/account'

const emit = defineEmits(['success'])

const categoryStore = useCategoryStore()
const accountStore = useAccountStore()

const visible = ref(false)
const loading = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const form = reactive({
  type: 2,
  amount: null,
  categoryId: null,
  paymentAccountId: null,
  billDate: new Date().toISOString().split('T')[0],
  description: ''
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  billDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const typeOptions = [
  { label: '支出', value: 2 },
  { label: '收入', value: 1 }
]

const filteredCategories = computed(() => {
  return categoryStore.getCategoriesByType(form.type)
})

const accounts = computed(() => accountStore.accounts)

const handleTypeChange = () => {
  form.categoryId = null
}

const disabledDate = (date) => {
  return date > new Date()
}

const open = (data) => {
  visible.value = true
  // 打开时刷新账户列表
  accountStore.fetchAccounts()
  
  if (data?.id) {
    isEdit.value = true
    editingId.value = data.id
    form.type = data.type
    form.amount = parseFloat(data.amount)
    form.categoryId = data.categoryId
    form.paymentAccountId = data.paymentAccountId
    form.billDate = data.billDate
    form.description = data.description || ''
  } else {
    isEdit.value = false
    editingId.value = null
    form.type = data?.type || 2
    form.amount = null
    form.categoryId = null
    form.paymentAccountId = null
    form.billDate = new Date().toISOString().split('T')[0]
    form.description = ''
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    if (isEdit.value) {
      await updateBill(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createBill(form)
      ElMessage.success('添加成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    // 验证失败或请求失败
  } finally {
    loading.value = false
  }
}

defineExpose({ open })
</script>

<style scoped>
.bill-form {
  padding: 0 20px;
}

.amount-input {
  width: 100%;
}

.amount-input :deep(.el-input__wrapper) {
  padding-left: 32px;
  font-size: 24px;
  font-weight: 600;
  font-family: 'DIN Alternate', 'Helvetica Neue', monospace;
}

.category-select,
.date-picker {
  width: 100%;
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

.account-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.account-balance {
  float: right;
  color: #67C23A;
  font-size: 12px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #EBEEF5;
}
</style>
