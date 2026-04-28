<script setup lang="ts">
defineProps<{ drawingUrl: string | null; styleDescriptor: string | null }>();
</script>

<template>
  <div class="bg-white px-8 py-3 border-b border-gray-100 flex items-center gap-4">
    <span class="text-[10px] uppercase tracking-wider text-gray-500 font-bold">원본 그림</span>
    <img v-if="drawingUrl" :src="drawingUrl" class="w-12 h-12 rounded-lg object-cover" />
    <div v-else class="w-12 h-12 bg-gray-100 rounded-lg" />
    <span class="text-gray-400 text-lg">→</span>
    <span class="text-[10px] uppercase tracking-wider text-gray-500 font-bold">일러스트 스타일</span>
    <span v-if="styleDescriptor" class="text-xs italic text-gray-700 bg-gray-50 px-3 py-1.5 rounded-md">
      "{{ styleDescriptor }}"
    </span>
  </div>
</template>
