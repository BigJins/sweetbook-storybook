<script setup lang="ts">
import type { StorySummary } from '../types';
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps<{ story: StorySummary }>();
const router = useRouter();

const isCompleted = computed(() => props.story.status === 'COMPLETED');
const isFailed    = computed(() => props.story.status === 'FAILED');
const isGenerating = computed(() =>
  ['ANALYZING_DRAWING', 'GENERATING_STORY', 'GENERATING_IMAGES', 'DRAFT'].includes(props.story.status));

const stepLabel = computed(() => {
  switch (props.story.status) {
    case 'DRAFT': return '시작 중...';
    case 'ANALYZING_DRAWING': return '그림 분석 중...';
    case 'GENERATING_STORY': return '스토리 작성 중...';
    case 'GENERATING_IMAGES': return '일러스트 생성 중...';
    default: return '';
  }
});

function open() {
  if (isFailed.value || isGenerating.value || isCompleted.value) {
    router.push(`/stories/${props.story.id}`);
  }
}

defineEmits<{ retry: [id: string] }>();
</script>

<template>
  <div class="bg-white border rounded-xl overflow-hidden cursor-pointer transition hover:shadow-lg"
       :class="isFailed ? 'border-red-200' : 'border-gray-200'"
       @click="open">
    <div class="aspect-[3/4] relative flex items-center justify-center">
      <template v-if="isCompleted && story.coverUrl">
        <img :src="story.coverUrl" class="w-full h-full object-cover" :alt="story.title" />
      </template>
      <template v-else-if="isGenerating">
        <div class="w-full h-full bg-gradient-to-br from-amber-100 to-orange-200 flex flex-col items-center justify-center">
          <div class="text-3xl">⏳</div>
          <div class="mt-2 text-xs text-amber-900 font-semibold">{{ stepLabel }}</div>
        </div>
      </template>
      <template v-else-if="isFailed">
        <div class="w-full h-full bg-red-50 flex flex-col items-center justify-center">
          <div class="text-3xl">⚠️</div>
          <div class="mt-2 text-xs text-red-900 font-semibold">생성 실패</div>
          <button class="mt-2 bg-red-500 text-white text-xs px-3 py-1 rounded"
                  @click.stop="$emit('retry', story.id)">다시 시도</button>
        </div>
      </template>
    </div>
    <div class="p-3">
      <div class="font-bold text-sm">{{ story.title || '제목 생성중...' }}</div>
      <div class="mt-1 text-xs text-gray-500">{{ isCompleted ? '5페이지' : story.childName }}</div>
    </div>
  </div>
</template>
