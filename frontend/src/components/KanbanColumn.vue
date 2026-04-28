<script setup lang="ts">
import type { Order } from '../types';
import OrderCard from './OrderCard.vue';

defineProps<{
  title: string; icon: string; orders: Order[]; bg: string; titleColor: string;
}>();
defineEmits<{ advance: [id: string]; download: [id: string] }>();
</script>

<template>
  <div :class="['rounded-2xl p-4', bg]">
    <div :class="['font-bold text-sm mb-4 flex items-center gap-1.5', titleColor]">
      <span class="text-base">{{ icon }}</span>
      <span>{{ title }}</span>
      <span class="opacity-60">({{ orders.length }})</span>
    </div>
    <div class="space-y-3">
      <OrderCard v-for="o in orders" :key="o.id" :order="o"
                 @advance="id => $emit('advance', id)"
                 @download="id => $emit('download', id)" />
    </div>
  </div>
</template>
