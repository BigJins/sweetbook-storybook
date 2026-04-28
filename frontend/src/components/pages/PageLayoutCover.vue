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

// Adaptive title scale — short titles get hero-sized, longer titles step down.
// Same overflow doctrine as body: shrink before clipping.
const titleClass = computed(() => {
  const len = (props.title ?? '').length;
  if (len <= 10) return 'text-4xl md:text-5xl leading-[1.15]';
  if (len <= 18) return 'text-3xl md:text-4xl leading-[1.18]';
  if (len <= 28) return 'text-2xl md:text-3xl leading-[1.22]';
  return 'text-xl md:text-2xl leading-[1.3]';
});
</script>

<template>
  <div class="relative w-[320px] sm:w-[400px] md:w-[460px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 overflow-hidden">
    <!-- full-bleed illustration -->
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-7xl opacity-40">📖</div>

    <!-- top scrim for chip legibility -->
    <div class="absolute inset-x-0 top-0 h-20 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

    <!-- page badge -->
    <div class="absolute top-3 left-3 text-[11px] uppercase tracking-[0.18em] bg-black/45 text-white/95 rounded-full px-3 py-1 font-semibold backdrop-blur-sm">
      표지
    </div>

    <!-- regenerate chip -->
    <button v-if="editable && illustrationUrl"
            class="absolute top-3 right-3 bg-white/92 backdrop-blur-sm text-sm font-semibold px-3 py-1.5 rounded-full shadow-md hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- vellum hero card — narrower than the page (78%) so it doesn't crowd the
         illustration; positioned in the lower-third where ground/sky usually
         sits in a children's book illustration, leaving the upper character
         area visible. -->
    <div class="absolute left-1/2 -translate-x-1/2 w-[78%] bottom-[12%] max-h-[58%] bg-gradient-to-b from-white/96 to-amber-50/92 backdrop-blur-md rounded-2xl shadow-xl border border-white/70">
      <div class="px-6 py-6 max-h-full overflow-y-auto">
        <!-- decorative top divider -->
        <div class="flex items-center justify-center gap-2 text-amber-700/70 mb-3">
          <span class="h-px w-10 bg-amber-700/30" />
          <span class="text-xs">✦</span>
          <span class="h-px w-10 bg-amber-700/30" />
        </div>
        <h2 :class="['story-title text-center line-clamp-3', titleClass]">
          {{ title }}
        </h2>
        <div v-if="childName" class="story-author mt-4 text-sm md:text-base text-center">
          작가 · {{ childName }}
        </div>
      </div>
    </div>
  </div>
</template>
