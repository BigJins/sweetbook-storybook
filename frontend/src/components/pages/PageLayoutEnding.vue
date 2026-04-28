<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  bodyText: string | null;
  pageNumber: number;
  editable?: boolean;
}>();
const emit = defineEmits<{ 'update:bodyText': [v: string]; regenerate: [] }>();
function onInput(e: Event) { emit('update:bodyText', (e.target as HTMLTextAreaElement).value); }
</script>

<template>
  <div class="relative w-[320px] sm:w-[380px] md:w-[420px] aspect-[3/4] bg-gradient-to-br from-indigo-200 to-emerald-200 overflow-hidden">
    <!-- full-bleed illustration -->
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-7xl opacity-40">🌙</div>

    <!-- gentle dark scrim top + bottom for chip + breathing -->
    <div class="absolute inset-x-0 top-0 h-20 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

    <!-- page badge -->
    <div class="absolute top-3 left-3 text-[11px] uppercase tracking-[0.18em] bg-black/45 text-white/95 rounded-full px-3 py-1 font-semibold backdrop-blur-sm">
      Page {{ pageNumber }}
    </div>

    <!-- regenerate chip -->
    <button v-if="editable"
            class="absolute top-3 right-3 bg-white/92 backdrop-blur-sm text-sm font-semibold px-3 py-1.5 rounded-full shadow-md hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- vellum closing card, vertically centered, narrower than full -->
    <div class="absolute left-6 right-6 top-1/2 -translate-y-1/2 bg-gradient-to-b from-white/96 to-amber-50/92 backdrop-blur-md rounded-2xl shadow-xl border border-white/70 max-h-[64%]">
      <div class="px-7 py-7 max-h-full overflow-y-auto">
        <!-- decorative top divider for "epilogue" feel -->
        <div class="flex items-center justify-center gap-2 text-amber-700/70 mb-4">
          <span class="h-px w-10 bg-amber-700/30" />
          <span class="text-xs">✦</span>
          <span class="h-px w-10 bg-amber-700/30" />
        </div>
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  rows="3"
                  class="w-full bg-transparent text-base md:text-lg leading-loose font-medium text-center italic text-gray-800 break-keep resize-none focus:outline-none focus:bg-white/40 rounded-md transition"
                  placeholder="마지막 페이지..." />
        <p v-else class="text-base md:text-lg leading-loose font-medium text-center italic text-gray-800 break-keep">{{ bodyText }}</p>
      </div>
    </div>
  </div>
</template>
