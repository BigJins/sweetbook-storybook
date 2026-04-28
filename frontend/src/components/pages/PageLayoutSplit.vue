<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue';

const props = defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();

// Adaptive type scale — picks size + leading from body length so text stays
// inside the paper-page bounds without ever needing a scrollbar.
//   1) line length is constrained by the paper page width
//   2) font size steps down with content length
//   3) line-height tightens slightly at smaller sizes
//   4) (last resort) text overflows the page bounds visibly — only triggers
//      on extreme paste-in cases, well past anything the AI produces.
const bodyClass = computed(() => {
  const len = (props.bodyText ?? '').length;
  if (len <= 50)  return 'text-3xl md:text-4xl leading-[1.65]';
  if (len <= 90)  return 'text-2xl md:text-3xl leading-[1.6]';
  if (len <= 150) return 'text-xl md:text-2xl leading-[1.55]';
  if (len <= 220) return 'text-lg md:text-xl leading-[1.5]';
  return 'text-base md:text-lg leading-[1.5]';
});

// Image side alternates by page parity → left/right rhythm across SPLIT pages.
// Page 2 image-left, 3 image-right, 4 image-left.
const imageOnLeft = computed(() => props.pageNumber % 2 === 0);
const flexDir = computed(() =>
  imageOnLeft.value ? 'flex-col md:flex-row' : 'flex-col md:flex-row-reverse'
);

// Auto-resize textarea so the editor stops growing past the paper page area
// instead of producing an internal scrollbar.
const textareaRef = ref<HTMLTextAreaElement | null>(null);
function autosize() {
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = 'auto';
  el.style.height = el.scrollHeight + 'px';
}
function onInput(e: Event) {
  emit('update:bodyText', (e.target as HTMLTextAreaElement).value);
  autosize();
}
watch(() => props.bodyText, () => nextTick(autosize), { immediate: true });
</script>

<template>
  <!-- 2-page spread: at md+, two 3:4 portrait pages side by side (3:2 landscape).
       On mobile, stacks vertically (image on top, text below) so it remains
       usable without horizontal scrolling. -->
  <div :class="['flex w-[320px] sm:w-[400px] md:w-[720px] lg:w-[820px] md:aspect-[3/2]', flexDir]">

    <!-- ILLUSTRATION HALF -->
    <div class="relative md:w-1/2 aspect-[3/4] md:aspect-auto bg-gradient-to-br from-pink-200 to-violet-200 overflow-hidden"
         :class="imageOnLeft ? 'md:spread-spine-left' : 'md:spread-spine-right'">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
      <div v-else class="absolute inset-0 flex flex-col items-center justify-center text-gray-500">
        <div class="text-5xl">⚠️</div>
        <div class="text-sm mt-2">일러스트 실패</div>
      </div>

      <!-- top scrim for chip legibility -->
      <div class="absolute inset-x-0 top-0 h-16 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

      <!-- regenerate chip — visually secondary, glassy on the illustration -->
      <button v-if="editable"
              class="absolute top-3 right-3 bg-black/30 hover:bg-black/55 backdrop-blur-sm text-white/85 hover:text-white text-xs font-medium px-3 py-1.5 rounded-full transition border border-white/15"
              @click="$emit('regenerate')">🔄 재생성</button>
    </div>

    <!-- TEXT HALF — printed paper page -->
    <div class="relative md:w-1/2 aspect-[3/4] md:aspect-auto story-paper overflow-hidden"
         :class="imageOnLeft ? 'md:spread-spine-right' : 'md:spread-spine-left'">

      <!-- centered text body with picture-book book-margin proportions
           (10–12% horizontal margin, vertical centering) -->
      <div class="absolute inset-0 flex items-center justify-center px-[10%] py-[8%]">
        <div class="w-full max-w-[36ch]">
          <textarea v-if="editable" ref="textareaRef"
                    :value="bodyText ?? ''"
                    rows="2"
                    @input="onInput"
                    :class="['w-full bg-transparent resize-none focus:outline-none focus:bg-amber-50/60 rounded-md transition story-body overflow-hidden', bodyClass]"
                    placeholder="페이지 본문..." />
          <p v-else :class="['story-body', bodyClass]">{{ bodyText }}</p>
        </div>
      </div>

      <!-- page number bottom-center, like a printed book -->
      <div class="absolute bottom-3 inset-x-0 text-center text-[11px] text-amber-900/40 italic font-serif tracking-wide">
        — {{ pageNumber }} —
      </div>
    </div>
  </div>
</template>
