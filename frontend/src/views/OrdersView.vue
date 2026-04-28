<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { Order, OrderStatus } from '../types';
import { listOrders, updateOrderStatus } from '../api/orders';
import { ApiException } from '../api/client';
import KanbanColumn from '../components/KanbanColumn.vue';
import EmptyState from '../components/EmptyState.vue';
import Spinner from '../components/Spinner.vue';

const orders = ref<Order[]>([]);
const loading = ref(true);
const loadError = ref<string | null>(null);

async function refresh() {
  try {
    orders.value = await listOrders();
    loadError.value = null;
  } catch (e) {
    loadError.value = e instanceof ApiException ? e.message : '서버에 연결할 수 없어요';
  } finally {
    loading.value = false;
  }
}

const byStatus = (s: OrderStatus) => computed(() => orders.value.filter(o => o.status === s));
const pending    = byStatus('PENDING');
const processing = byStatus('PROCESSING');
const completed  = byStatus('COMPLETED');

async function onAdvance(id: string) {
  const cur = orders.value.find(o => o.id === id);
  if (!cur) return;
  const next = cur.status === 'PENDING' ? 'PROCESSING' : 'COMPLETED';
  try {
    await updateOrderStatus(id, next);
    await refresh();
  } catch (e) {
    loadError.value = e instanceof ApiException ? e.message : '상태 변경에 실패했어요';
  }
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

    <div v-if="loadError"
         class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg p-3 mb-4">
      ⚠️ {{ loadError }}
    </div>

    <Spinner v-if="loading" />

    <EmptyState v-else-if="orders.length === 0 && !loadError"
      icon="📦" title="아직 주문이 없어요"
      subtitle="동화 미리보기에서 '책 만들기' 버튼을 눌러보세요" />

    <div v-else-if="orders.length > 0" class="grid grid-cols-3 gap-4">
      <KanbanColumn title="PENDING" icon="⏳" :orders="pending" bg="bg-amber-100" title-color="text-amber-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="PROCESSING" icon="🛠️" :orders="processing" bg="bg-blue-100" title-color="text-blue-900"
                    @advance="onAdvance" @download="onDownload" />
      <KanbanColumn title="COMPLETED" icon="✅" :orders="completed" bg="bg-emerald-100" title-color="text-emerald-900"
                    @advance="onAdvance" @download="onDownload" />
    </div>
  </main>
</template>
