<script setup lang="ts">
defineProps<{ label?: string }>();
</script>

<template>
  <div class="text-center py-16 text-gray-500">
    <div class="inline-block w-6 h-6 border-2 border-gray-300 border-t-gray-900 rounded-full animate-spin"></div>
    <div class="mt-2 text-xs">{{ label ?? '불러오는 중...' }}</div>
  </div>
</template>
