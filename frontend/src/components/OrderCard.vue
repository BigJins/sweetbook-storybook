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
  return status === 'PROCESSING' ? 'bg-emerald-500 hover:bg-emerald-600' : 'bg-gray-900 hover:bg-gray-800';
}
</script>

<template>
  <div class="bg-white rounded-xl p-4 shadow-sm">
    <div class="flex gap-3 items-center mb-2.5">
      <img v-if="order.story.coverUrl" :src="order.story.coverUrl"
           class="w-9 h-11 rounded-md object-cover shadow-sm" />
      <div v-else class="w-9 h-11 rounded-md bg-gray-200" />
      <div class="text-sm font-bold flex-1 truncate leading-snug">{{ order.story.title }}</div>
    </div>
    <div class="text-xs text-gray-600 leading-relaxed">
      {{ order.item.bookSize }} · {{ order.item.coverType === 'HARD' ? '하드' : '소프트' }} · {{ order.item.copies }}부
    </div>
    <div class="text-xs text-gray-600 mt-0.5">받는분: {{ order.recipientName }}</div>
    <div class="mt-3 flex gap-1.5">
      <button v-if="order.status !== 'COMPLETED'"
              :class="['flex-1 text-white rounded-lg text-xs font-bold py-2 transition', nextClass(order.status)]"
              @click="$emit('advance', order.id)">
        {{ nextLabel(order.status) }}
      </button>
      <button :class="['rounded-lg text-xs font-semibold py-2 px-3 transition',
                       order.status === 'COMPLETED'
                         ? 'bg-gray-900 hover:bg-gray-800 text-white font-bold flex-1'
                         : 'border hover:bg-gray-50']"
              @click="$emit('download', order.id)">
        📦 {{ order.status === 'COMPLETED' ? 'ZIP 다운로드' : 'ZIP' }}
      </button>
    </div>
  </div>
</template>
