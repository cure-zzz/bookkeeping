<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useCategoryStore } from './stores/category'

const categoryStore = useCategoryStore()

onMounted(async () => {
  if (localStorage.getItem('token')) {
    await categoryStore.fetchCategories()
  }
})
</script>
