<script setup lang="ts">
import { RouterLink } from 'vue-router';
</script>

<template>
  <nav class="bg-white border-b border-gray-200 px-8 py-4 flex items-center gap-7 text-base">
    <RouterLink to="/" class="font-extrabold text-lg tracking-tight">📚 동화공방</RouterLink>
    <RouterLink to="/" class="text-gray-600 hover:text-gray-900 font-medium" active-class="text-blue-600 font-semibold" exact>동화 목록</RouterLink>
    <RouterLink to="/orders" class="text-gray-600 hover:text-gray-900 font-medium" active-class="text-blue-600 font-semibold">주문</RouterLink>
  </nav>
</template>
