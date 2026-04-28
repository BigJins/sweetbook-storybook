<script setup lang="ts">
import { ref, computed } from 'vue';
import type { PageData } from '../types';
import PageLayoutCover  from './pages/PageLayoutCover.vue';
import PageLayoutSplit  from './pages/PageLayoutSplit.vue';
import PageLayoutEnding from './pages/PageLayoutEnding.vue';

const props = defineProps<{ pages: PageData[]; title: string }>();
defineEmits<{
  'update:pageBody': [pageNumber: number, bodyText: string];
  regenerate: [pageNumber: number];
}>();

const idx = ref(0);
const current = computed(() => props.pages[idx.value]);
const canPrev = computed(() => idx.value > 0);
const canNext = computed(() => idx.value < props.pages.length - 1);
</script>

<template>
  <div class="flex items-center gap-3 justify-center">
    <button class="w-10 h-10 rounded-full bg-white border" :disabled="!canPrev" @click="idx--">‹</button>

    <div class="shadow-2xl rounded-lg overflow-hidden">
      <PageLayoutCover v-if="current.layout === 'COVER'"
        :illustration-url="current.illustrationUrl" :title="title" :page-number="current.pageNumber"
        editable @regenerate="$emit('regenerate', current.pageNumber)" />
      <PageLayoutSplit v-else-if="current.layout === 'SPLIT'"
        :illustration-url="current.illustrationUrl" :body-text="current.bodyText" :page-number="current.pageNumber"
        editable
        @update:body-text="v => $emit('update:pageBody', current.pageNumber, v)"
        @regenerate="$emit('regenerate', current.pageNumber)" />
      <PageLayoutEnding v-else
        :illustration-url="current.illustrationUrl" :body-text="current.bodyText" :page-number="current.pageNumber"
        editable
        @update:body-text="v => $emit('update:pageBody', current.pageNumber, v)"
        @regenerate="$emit('regenerate', current.pageNumber)" />
    </div>

    <button class="w-10 h-10 rounded-full bg-white border" :disabled="!canNext" @click="idx++">›</button>
  </div>

  <div class="mt-6 flex justify-center gap-2">
    <button v-for="(p, i) in pages" :key="p.pageNumber"
            class="w-10 h-14 rounded border-2 overflow-hidden"
            :class="i === idx ? 'border-gray-900' : 'border-transparent opacity-60'"
            @click="idx = i">
      <img v-if="p.illustrationUrl" :src="p.illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="w-full h-full bg-gray-200" />
    </button>
  </div>
</template>
