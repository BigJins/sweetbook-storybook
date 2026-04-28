<script setup lang="ts">
defineProps<{ label?: string }>();
</script>

<template>
  <div class="text-center py-20 text-gray-500">
    <div class="inline-block w-9 h-9 border-[3px] border-gray-200 border-t-gray-900 rounded-full animate-spin"></div>
    <div class="mt-3 text-sm">{{ label ?? '불러오는 중...' }}</div>
  </div>
</template>
