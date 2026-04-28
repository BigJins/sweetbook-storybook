<script setup lang="ts">
defineProps<{ drawingUrl: string | null; styleDescriptor: string | null }>();
</script>

<template>
  <div class="bg-white px-8 py-4 border-b border-gray-100 flex items-center gap-4 flex-wrap">
    <span class="text-xs uppercase tracking-wider text-gray-500 font-bold">원본 그림</span>
    <img v-if="drawingUrl" :src="drawingUrl" class="w-14 h-14 rounded-lg object-cover shadow-sm" />
    <div v-else class="w-14 h-14 bg-gray-100 rounded-lg" />
    <span class="text-gray-400 text-xl">→</span>
    <span class="text-xs uppercase tracking-wider text-gray-500 font-bold">일러스트 스타일</span>
    <span v-if="styleDescriptor" class="text-sm italic text-gray-700 bg-gray-50 px-3 py-2 rounded-md leading-relaxed">
      "{{ styleDescriptor }}"
    </span>
  </div>
</template>
