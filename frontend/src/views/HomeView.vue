<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { RouterLink } from 'vue-router';
import { listStories, retryStory } from '../api/stories';
import type { StorySummary } from '../types';
import StoryCard from '../components/StoryCard.vue';
import EmptyState from '../components/EmptyState.vue';

const stories = ref<StorySummary[]>([]);
const loading = ref(true);
let pollTimer: number | null = null;

async function refresh() {
  try {
    stories.value = await listStories();
  } finally {
    loading.value = false;
  }
}

function hasGenerating() {
  return stories.value.some(s =>
    ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES'].includes(s.status));
}

async function startPolling() {
  if (pollTimer) return;
  pollTimer = window.setInterval(async () => {
    if (hasGenerating()) await refresh();
  }, 2000);
}

async function onRetry(id: string) {
  await retryStory(id);
  await refresh();
  startPolling();
}

onMounted(async () => {
  await refresh();
  startPolling();
});
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer); });
</script>

<template>
  <main class="max-w-6xl mx-auto">
    <section class="px-8 py-8 bg-gradient-to-br from-amber-100 to-pink-100">
      <div class="max-w-2xl">
        <h1 class="text-2xl font-extrabold text-gray-900">아이의 그림과 상상이 동화책이 됩니다</h1>
        <p class="mt-2 text-sm text-gray-600">그림 1장과 상상 한 줄을 올리면 30초 안에 5페이지 동화책이 완성됩니다.</p>
        <RouterLink to="/stories/new"
          class="inline-block mt-4 bg-gray-900 text-white px-6 py-3 rounded-lg font-bold text-sm">
          + 새 동화 만들기
        </RouterLink>
      </div>
    </section>

    <section class="px-8 py-8">
      <div class="flex justify-between items-baseline mb-4">
        <h2 class="text-base font-bold">내 동화</h2>
        <span class="text-xs text-gray-500">총 {{ stories.length }}편</span>
      </div>

      <div v-if="loading" class="text-center py-16 text-gray-500">불러오는 중...</div>

      <EmptyState v-else-if="stories.length === 0"
        icon="📖" title="아직 동화가 없어요" subtitle="첫 동화를 만들어 시작해보세요"
        cta-text="+ 첫 동화 만들기" cta-to="/stories/new" />

      <div v-else class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <StoryCard v-for="s in stories" :key="s.id" :story="s" @retry="onRetry" />
      </div>
    </section>
  </main>
</template>
