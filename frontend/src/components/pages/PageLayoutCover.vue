<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  illustrationUrl: string | null;
  title: string;
  childName?: string;
  pageNumber: number;
  editable?: boolean;
}>();
defineEmits<{ regenerate: []; }>();

// Adaptive cover-title scale: shrink before clipping
const titleClass = computed(() => {
  const len = (props.title ?? '').length;
  if (len <= 10) return 'text-4xl md:text-5xl leading-[1.15]';
  if (len <= 18) return 'text-3xl md:text-4xl leading-[1.18]';
  if (len <= 28) return 'text-2xl md:text-3xl leading-[1.22]';
  return 'text-xl md:text-2xl leading-[1.3]';
});
</script>

<template>
  <!-- Single-page cover: 3:4 portrait. No overlay card — title sits directly
       on the illustration with text-shadow for legibility. Decorative rules
       above and below give a book-cover frame without a UI box. -->
  <div class="relative w-[320px] sm:w-[400px] md:w-[460px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 overflow-hidden">
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-7xl opacity-40">📖</div>

    <!-- soft top + stronger bottom scrims for chip and title legibility -->
    <div class="absolute inset-x-0 top-0 h-24 bg-gradient-to-b from-black/30 via-black/8 to-transparent pointer-events-none" />
    <div class="absolute inset-x-0 bottom-0 h-[55%] bg-gradient-to-t from-black/65 via-black/30 to-transparent pointer-events-none" />

    <!-- page badge (visually secondary) -->
    <div class="absolute top-3 left-4 text-[11px] uppercase tracking-[0.2em] text-white/75 font-semibold">
      표지
    </div>

    <!-- regenerate chip (visually secondary, low contrast on a glassy chip) -->
    <button v-if="editable && illustrationUrl"
            class="absolute top-3 right-3 bg-white/12 hover:bg-white/22 backdrop-blur-sm text-white/85 hover:text-white text-xs font-medium px-3 py-1.5 rounded-full transition border border-white/20"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- title block — book-cover composition: rule · title · rule · author -->
    <div class="absolute inset-x-7 bottom-10 text-center">
      <div class="flex items-center justify-center gap-3 text-white/55 mb-4">
        <span class="h-px flex-1 max-w-[60px] bg-white/35" />
        <span class="text-xs tracking-widest">✦</span>
        <span class="h-px flex-1 max-w-[60px] bg-white/35" />
      </div>

      <h2 :class="['story-cover-title text-center line-clamp-3', titleClass]">
        {{ title }}
      </h2>

      <div v-if="childName" class="story-cover-author mt-4 text-[11px] md:text-xs uppercase">
        작가 · {{ childName }}
      </div>
    </div>
  </div>
</template>
