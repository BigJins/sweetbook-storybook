<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStoryStatus } from '../composables/useStoryStatus';
import { updatePageBody, regeneratePage } from '../api/stories';
import ProgressStepper from '../components/ProgressStepper.vue';
import BeforeAfterStrip from '../components/BeforeAfterStrip.vue';
import BookViewer from '../components/BookViewer.vue';
import OrderModal from '../components/OrderModal.vue';
import Spinner from '../components/Spinner.vue';

const route = useRoute();
const router = useRouter();
const id = route.params.id as string;

const { story, start } = useStoryStatus(id);
const showOrder = ref(false);

const isCompleted = computed(() => story.value?.status === 'COMPLETED');
const isFailed    = computed(() => story.value?.status === 'FAILED');
const isGenerating = computed(() =>
  story.value && ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES'].includes(story.value.status));

const completedPages = computed(() =>
  story.value?.pages?.filter(p => p.illustrationUrl).length ?? 0);

const styleKeywords = computed(() => {
  if (!story.value?.styleDescriptor) return null;
  try {
    const parsed = JSON.parse(story.value.styleDescriptor);
    return parsed.keywords?.join(', ');
  } catch { return null; }
});

async function onPageBodyUpdate(n: number, body: string) {
  await updatePageBody(id, n, body);
}

async function onRegenerate(n: number) {
  await regeneratePage(id, n);
  setTimeout(() => start(), 500);
}

onMounted(start);
</script>

<template>
  <main class="max-w-6xl mx-auto">
    <div class="bg-white border-b border-gray-200 px-8 py-3 flex items-center justify-between">
      <div class="flex items-center gap-4 text-sm">
        <RouterLink to="/" class="text-gray-500">← 동화 목록</RouterLink>
        <h1 class="font-bold text-base">{{ story?.title || '동화 생성중...' }}</h1>
        <span v-if="story" class="text-xs text-gray-500">작가: {{ story.childName }}</span>
      </div>
      <button v-if="isCompleted" class="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-bold"
              @click="showOrder = true">📦 이 동화로 책 만들기</button>
    </div>

    <Spinner v-if="!story" />

    <div v-else-if="isGenerating" class="py-12 px-8 text-center">
      <div class="text-sm text-gray-500 mb-6">{{ story?.childName }}의 동화를 만들고 있어요</div>
      <ProgressStepper :status="story!.status" :completed-pages="completedPages" />
      <div class="mt-8 grid grid-cols-5 gap-3 max-w-2xl mx-auto">
        <div v-for="n in 5" :key="n"
             class="aspect-[3/4] rounded-lg flex items-center justify-center text-xs"
             :class="story!.pages?.find(p => p.pageNumber === n)?.illustrationUrl
                     ? 'bg-emerald-100 text-emerald-700'
                     : 'bg-gray-100 text-gray-400'">
          {{ story!.pages?.find(p => p.pageNumber === n)?.illustrationUrl ? '✓' : '대기' }}
        </div>
      </div>
    </div>

    <div v-else-if="isFailed" class="py-12 px-8 text-center text-red-700">
      <div class="text-3xl">⚠️</div>
      <p class="mt-3 font-semibold">{{ story?.errorMessage || '동화 생성에 실패했어요' }}</p>
      <p class="mt-1 text-xs text-red-600">목록 화면에서 다시 시도 버튼을 눌러주세요</p>
    </div>

    <template v-else-if="isCompleted && story">
      <BeforeAfterStrip :drawing-url="story.drawingUrl" :style-descriptor="styleKeywords" />
      <div class="p-8">
        <BookViewer :pages="story.pages" :title="story.title"
                    @update:page-body="onPageBodyUpdate" @regenerate="onRegenerate" />
      </div>
    </template>

    <OrderModal v-if="showOrder && story" :story="story" @close="showOrder = false"
                @created="() => router.push('/orders')" />
  </main>
</template>
