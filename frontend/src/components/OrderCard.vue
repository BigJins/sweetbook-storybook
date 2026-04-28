<script setup lang="ts">
import type { Order } from '../types';

defineProps<{ order: Order }>();
defineEmits<{ advance: [id: string]; download: [id: string] }>();

function nextLabel(status: string): string {
  if (status === 'PENDING') return '→ 제작 시작';
  if (status === 'PROCESSING') return '→ 완료';
  return '';
}
function nextClass(status: string): string {
  return status === 'PROCESSING' ? 'bg-emerald-500' : 'bg-gray-900';
}
</script>

<template>
  <div class="bg-white rounded-lg p-3 shadow-sm">
    <div class="flex gap-2 items-center mb-2">
      <img v-if="order.story.coverUrl" :src="order.story.coverUrl"
           class="w-7 h-9 rounded object-cover" />
      <div v-else class="w-7 h-9 rounded bg-gray-200" />
      <div class="text-xs font-bold flex-1 truncate">{{ order.story.title }}</div>
    </div>
    <div class="text-[10px] text-gray-600 leading-relaxed">
      {{ order.item.bookSize }} · {{ order.item.coverType === 'HARD' ? '하드' : '소프트' }} · {{ order.item.copies }}부
    </div>
    <div class="text-[10px] text-gray-600">받는분: {{ order.recipientName }}</div>
    <div class="mt-2 flex gap-1">
      <button v-if="order.status !== 'COMPLETED'"
              :class="['flex-1 text-white rounded text-[10px] font-bold py-1.5', nextClass(order.status)]"
              @click="$emit('advance', order.id)">
        {{ nextLabel(order.status) }}
      </button>
      <button :class="['rounded text-[10px] py-1.5 px-2',
                       order.status === 'COMPLETED' ? 'bg-gray-900 text-white font-bold flex-1' : 'border']"
              @click="$emit('download', order.id)">
        📦 {{ order.status === 'COMPLETED' ? 'ZIP 다운로드' : 'ZIP' }}
      </button>
    </div>
  </div>
</template>
