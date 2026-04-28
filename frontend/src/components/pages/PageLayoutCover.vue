<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  title: string;
  childName?: string;
  pageNumber: number;
  editable?: boolean;
}>();
defineEmits<{ regenerate: []; }>();
</script>

<template>
  <div class="relative w-[320px] sm:w-[380px] md:w-[420px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 overflow-hidden">
    <!-- full-bleed illustration -->
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-7xl opacity-40">📖</div>

    <!-- subtle top scrim so chips stay legible on bright art -->
    <div class="absolute inset-x-0 top-0 h-20 bg-gradient-to-b from-black/15 to-transparent pointer-events-none" />

    <!-- page badge (top-left) -->
    <div class="absolute top-3 left-3 text-[11px] uppercase tracking-[0.18em] bg-black/45 text-white/95 rounded-full px-3 py-1 font-semibold backdrop-blur-sm">
      표지
    </div>

    <!-- regenerate chip (top-right, floating) -->
    <button v-if="editable && illustrationUrl"
            class="absolute top-3 right-3 bg-white/92 backdrop-blur-sm text-sm font-semibold px-3 py-1.5 rounded-full shadow-md hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>

    <!-- vellum hero title card, lower third -->
    <div class="absolute left-5 right-5 bottom-7 bg-gradient-to-b from-white/96 to-amber-50/92 backdrop-blur-md rounded-2xl px-6 py-6 shadow-xl border border-white/70">
      <!-- decorative top divider -->
      <div class="flex items-center justify-center gap-2 text-amber-700/70 mb-3">
        <span class="h-px w-8 bg-amber-700/30" />
        <span class="text-xs">✦</span>
        <span class="h-px w-8 bg-amber-700/30" />
      </div>
      <h2 class="text-2xl md:text-3xl font-extrabold text-gray-900 text-center leading-[1.2] tracking-tight line-clamp-3 break-keep">
        {{ title }}
      </h2>
      <div v-if="childName" class="mt-3 text-sm text-gray-500 text-center">
        작가 · {{ childName }}
      </div>
    </div>
  </div>
</template>
