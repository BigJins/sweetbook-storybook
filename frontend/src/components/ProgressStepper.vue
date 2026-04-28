<script setup lang="ts">
import type { StoryStatus } from '../types';

const props = defineProps<{ status: StoryStatus; completedPages: number }>();

const steps = [
  { key: 'ANALYZING_DRAWING', label: '그림 분석' },
  { key: 'GENERATING_STORY',  label: '스토리 작성' },
  { key: 'GENERATING_IMAGES', label: '일러스트' },
  { key: 'COMPLETED',         label: '완성' },
];

function stateOf(stepKey: string): 'done' | 'active' | 'pending' {
  const order = ['DRAFT','ANALYZING_DRAWING','GENERATING_STORY','GENERATING_IMAGES','COMPLETED'];
  const cur = order.indexOf(props.status);
  const ix  = order.indexOf(stepKey);
  if (cur > ix) return 'done';
  if (cur === ix) return 'active';
  return 'pending';
}
</script>

<template>
  <div class="flex justify-between items-center max-w-2xl mx-auto text-sm">
    <template v-for="(step, i) in steps" :key="step.key">
      <div class="flex-1 text-center">
        <div :class="[
          'w-10 h-10 rounded-full mx-auto flex items-center justify-center font-bold text-base',
          stateOf(step.key) === 'done'   ? 'bg-emerald-500 text-white' :
          stateOf(step.key) === 'active' ? 'bg-blue-500 text-white animate-pulse' :
                                            'bg-gray-200 text-gray-400']">
          <span v-if="stateOf(step.key) === 'done'">✓</span>
          <span v-else>{{ i + 1 }}</span>
        </div>
        <div :class="['mt-3', stateOf(step.key) !== 'pending' ? 'text-gray-900 font-semibold' : 'text-gray-400']">
          <span v-if="step.key === 'GENERATING_IMAGES' && stateOf(step.key) === 'active'">
            {{ completedPages }}/5 일러스트
          </span>
          <span v-else>{{ step.label }}</span>
        </div>
      </div>
      <div v-if="i < steps.length - 1" class="flex-1 h-0.5 bg-gray-200 mx-2" />
    </template>
  </div>
</template>
