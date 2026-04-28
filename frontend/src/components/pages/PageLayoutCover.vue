<script setup lang="ts">
defineProps<{
  illustrationUrl: string | null;
  title: string;
  pageNumber: number;
  editable?: boolean;
}>();
defineEmits<{ regenerate: []; }>();
</script>

<template>
  <div class="relative w-[340px] md:w-[400px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 flex items-center justify-center overflow-hidden">
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-6xl opacity-50">📖</div>
    <div class="absolute top-4 left-4 right-4 bg-white/90 rounded-lg px-4 py-3 backdrop-blur-sm">
      <div class="text-xl md:text-2xl font-extrabold text-gray-900 leading-tight line-clamp-2 tracking-tight">{{ title }}</div>
    </div>
    <div class="absolute bottom-3 right-4 text-xs bg-black/60 text-white rounded-full px-3 py-1 font-medium">
      표지 · {{ pageNumber }}
    </div>
    <button v-if="editable && illustrationUrl" class="absolute bottom-4 left-4 bg-white/95 text-sm font-semibold px-3 py-1.5 rounded-lg shadow-sm hover:bg-white transition"
            @click="$emit('regenerate')">🔄 재생성</button>
  </div>
</template>
