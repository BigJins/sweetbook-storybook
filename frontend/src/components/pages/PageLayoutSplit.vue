<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();

function onInput(e: Event) {
  emit('update:bodyText', (e.target as HTMLTextAreaElement).value);
}

// Adaptive type scale: prevent clipping by stepping size + leading down with content length.
// Order per spec: 1) constrain line length (card width), 2) reduce font size,
// 3) reduce line-height. Step 4 (overflow-y-auto inside card) is the last-resort safety net.
const bodyClass = computed(() => {
  const len = (props.bodyText ?? '').length;
  if (len <= 60)  return 'text-3xl md:text-4xl leading-[1.7]';      // very short, hero feel
  if (len <= 100) return 'text-2xl md:text-3xl leading-[1.65]';     // typical 2-sentence page
  if (len <= 160) return 'text-xl md:text-2xl leading-[1.6]';
  if (len <= 240) return 'text-lg md:text-xl leading-[1.55]';
  return 'text-base md:text-lg leading-relaxed';                    // long edits, last-step fallback
});

// Off-center placement, picture-book convention: even pages text-left, odd pages text-right.
// Page 2 left, Page 3 right, Page 4 left — gives visual rhythm across a SPLIT spread sequence.
const cardSide = computed(() => (props.pageNumber % 2 === 0 ? 'left' : 'right'));
</script>

<template>
  <div class="relative w-[320px] sm:w-[400px] md:w-[460px] aspect-[3/4] bg-gradient-to-br from-pink-200 to-violet-200 overflow-hidden">
    <!-- full-bleed illustration -->
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-x-0 top-0 h-[60%] flex flex-col items-center justify-center text-gray-500">
      <div class="text-5xl">⚠️</div>
      <div class="text-sm mt-2">일러스트 실패</div>
    </div>

    <!-- top scrim — keeps chips legible regardless of art -->
    <div class="absolute inset-x-0 top-0 h-20 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

    <!-- page badge -->
    <div class="absolute top-3 left-3 text-[11px] uppercase tracking-[0.18em] bg-black/45 text-white/95 rounded-full px-3 py-1 font-semibold backdrop-blur-sm">
      Page {{ pageNumber }}
    </div>

    <!-- regenerate chip -->
    <button v-if="editable"
            class="absolute top-3 right-3 bg-white/92 backdrop-blur-sm text-sm font-semibold px-3 py-1.5 rounded-full shadow-md hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- vellum body card — bottom-anchored, off-center (alternates by page parity),
         narrower than full-width so the central focal area of the illustration stays visible.
         Card grows from min to max with content; final clip is overflow-y-auto. -->
    <div class="absolute bottom-5 w-[78%] min-h-[22%] max-h-[60%] bg-gradient-to-b from-white/96 to-amber-50/92 backdrop-blur-md rounded-2xl shadow-xl border border-white/70"
         :class="cardSide === 'left' ? 'left-4' : 'right-4'">
      <div class="px-6 py-5 max-h-full overflow-y-auto">
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  rows="2"
                  :class="['w-full bg-transparent resize-none focus:outline-none focus:bg-white/40 rounded-md transition story-body', bodyClass]"
                  placeholder="페이지 본문..." />
        <p v-else :class="['story-body', bodyClass]">{{ bodyText }}</p>
      </div>
    </div>
  </div>
</template>
