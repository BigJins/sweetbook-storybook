<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();

function onInput(e: Event) {
  emit('update:bodyText', (e.target as HTMLTextAreaElement).value);
}
</script>

<template>
  <div class="relative w-[320px] sm:w-[380px] md:w-[420px] aspect-[3/4] bg-gradient-to-br from-pink-200 to-violet-200 overflow-hidden">
    <!-- full-bleed illustration -->
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-x-0 top-0 h-3/5 flex flex-col items-center justify-center text-gray-500">
      <div class="text-5xl">⚠️</div>
      <div class="text-sm mt-2">일러스트 실패</div>
    </div>

    <!-- subtle top scrim -->
    <div class="absolute inset-x-0 top-0 h-20 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

    <!-- page badge -->
    <div class="absolute top-3 left-3 text-[11px] uppercase tracking-[0.18em] bg-black/45 text-white/95 rounded-full px-3 py-1 font-semibold backdrop-blur-sm">
      Page {{ pageNumber }}
    </div>

    <!-- regenerate chip -->
    <button v-if="editable"
            class="absolute top-3 right-3 bg-white/92 backdrop-blur-sm text-sm font-semibold px-3 py-1.5 rounded-full shadow-md hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- vellum body card, bottom-anchored -->
    <div class="absolute inset-x-4 bottom-4 bg-gradient-to-b from-white/96 to-amber-50/92 backdrop-blur-md rounded-2xl shadow-xl border border-white/70 max-h-[58%]">
      <div class="px-6 py-5 max-h-full overflow-y-auto">
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  rows="3"
                  class="w-full bg-transparent text-base md:text-lg leading-loose text-gray-800 break-keep resize-none focus:outline-none focus:bg-white/40 rounded-md transition"
                  placeholder="페이지 본문..." />
        <p v-else class="text-base md:text-lg leading-loose text-gray-800 break-keep">{{ bodyText }}</p>
      </div>
    </div>
  </div>
</template>
