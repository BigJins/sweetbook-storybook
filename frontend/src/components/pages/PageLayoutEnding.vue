<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue';

const props = defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();

// Same adaptive doctrine as SPLIT — italic centered for the closing line.
const bodyClass = computed(() => {
  const len = (props.bodyText ?? '').length;
  if (len <= 50)  return 'text-3xl md:text-4xl leading-[1.65]';
  if (len <= 90)  return 'text-2xl md:text-3xl leading-[1.6]';
  if (len <= 150) return 'text-xl md:text-2xl leading-[1.55]';
  if (len <= 220) return 'text-lg md:text-xl leading-[1.5]';
  return 'text-base md:text-lg leading-[1.5]';
});

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
  <!-- Closing spread: text on left, illustration on right.
       The final image is what the reader is left looking at — a small
       reading-experience touch borrowed from real picture books. -->
  <div class="flex flex-col md:flex-row w-[320px] sm:w-[400px] md:w-[720px] lg:w-[820px] md:aspect-[3/2]">

    <!-- TEXT HALF (left) -->
    <div class="relative md:w-1/2 aspect-[3/4] md:aspect-auto story-paper overflow-hidden md:spread-spine-left">
      <div class="absolute inset-0 flex flex-col items-center justify-center px-[10%] py-[8%]">
        <!-- decorative top rule -->
        <div class="flex items-center justify-center gap-3 text-amber-900/30 mb-5">
          <span class="h-px w-10 bg-amber-900/25" />
          <span class="text-xs">✦</span>
          <span class="h-px w-10 bg-amber-900/25" />
        </div>

        <div class="w-full max-w-[32ch]">
          <textarea v-if="editable" ref="textareaRef"
                    :value="bodyText ?? ''"
                    rows="2"
                    @input="onInput"
                    :class="['w-full bg-transparent text-center italic resize-none focus:outline-none focus:bg-amber-50/60 rounded-md transition story-body overflow-hidden', bodyClass]"
                    placeholder="마지막 페이지..." />
          <p v-else :class="['text-center italic story-body', bodyClass]">{{ bodyText }}</p>
        </div>
      </div>

      <div class="absolute bottom-3 inset-x-0 text-center text-[11px] text-amber-900/40 italic font-serif tracking-wide">
        — {{ pageNumber }} —
      </div>
    </div>

    <!-- ILLUSTRATION HALF (right) -->
    <div class="relative md:w-1/2 aspect-[3/4] md:aspect-auto bg-gradient-to-br from-indigo-200 to-emerald-200 overflow-hidden md:spread-spine-right">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
      <div v-else class="absolute inset-0 flex items-center justify-center text-7xl opacity-40">🌙</div>

      <div class="absolute inset-x-0 top-0 h-16 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

      <button v-if="editable"
              class="absolute top-3 right-3 bg-black/30 hover:bg-black/55 backdrop-blur-sm text-white/85 hover:text-white text-xs font-medium px-3 py-1.5 rounded-full transition border border-white/15"
              @click="$emit('regenerate')">🔄 재생성</button>
    </div>
  </div>
</template>
