<script setup lang="ts">
import type { Order } from '../types';
import OrderCard from './OrderCard.vue';

defineProps<{
  title: string; icon: string; orders: Order[]; bg: string; titleColor: string;
}>();
defineEmits<{ advance: [id: string]; download: [id: string] }>();
</script>

<template>
  <div :class="['rounded-xl p-3', bg]">
    <div :class="['font-bold text-xs mb-3', titleColor]">{{ icon }} {{ title }} ({{ orders.length }})</div>
    <div class="space-y-2">
      <OrderCard v-for="o in orders" :key="o.id" :order="o"
                 @advance="id => $emit('advance', id)"
                 @download="id => $emit('download', id)" />
    </div>
  </div>
</template>
