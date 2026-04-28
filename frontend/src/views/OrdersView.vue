<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { Order, OrderStatus } from '../types';
import { listOrders, updateOrderStatus } from '../api/orders';
import KanbanColumn from '../components/KanbanColumn.vue';

const orders = ref<Order[]>([]);

async function refresh() { orders.value = await listOrders(); }

const byStatus = (s: OrderStatus) => computed(() => orders.value.filter(o => o.status === s));
const pending    = byStatus('PENDING');
const processing = byStatus('PROCESSING');
const completed  = byStatus('COMPLETED');

async function onAdvance(id: string) {
  const cur = orders.value.find(o => o.id === id);
  if (!cur) return;
  const next = cur.status === 'PENDING' ? 'PROCESSING' : 'COMPLETED';
  await updateOrderStatus(id, next);
  await refresh();
}

function onDownload(id: string) {
  window.location.href = `/api/orders/${id}/export`;
}

onMounted(refresh);
</script>

<template>
  <main class="max-w-7xl mx-auto px-8 py-8">
    <div class="flex justify-between items-baseline mb-6">
      <h1 class="text-xl font-extrabold">주문 관리</h1>
      <span class="text-xs text-gray-500">총 {{ orders.length }}건</span>
    </div>
    <div class="grid grid-cols-3 gap-4">
      <KanbanColumn title="PENDING" icon="⏳" :orders="pending" bg="bg-amber-100" title-color="text-amber-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="PROCESSING" icon="🛠️" :orders="processing" bg="bg-blue-100" title-color="text-blue-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="COMPLETED" icon="✅" :orders="completed" bg="bg-emerald-100" title-color="text-emerald-900"
                    @advance="onAdvance" @download="onDownload" />
    </div>
  </main>
</template>
