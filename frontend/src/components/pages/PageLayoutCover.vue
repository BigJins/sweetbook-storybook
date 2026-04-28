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
  <div class="relative w-[340px] aspect-[3/4] bg-gradient-to-br from-emerald-200 to-cyan-200 flex items-center justify-center overflow-hidden">
    <img v-if="illustrationUrl" :src="illustrationUrl" class="absolute inset-0 w-full h-full object-cover" />
    <div v-else class="absolute inset-0 flex items-center justify-center text-5xl opacity-50">📖</div>
    <div class="absolute top-3 left-3 right-3 bg-white/85 rounded px-3 py-2">
      <div class="text-base font-extrabold text-gray-900 line-clamp-2">{{ title }}</div>
    </div>
    <div class="absolute bottom-2 right-3 text-[11px] bg-black/50 text-white rounded-full px-2 py-0.5">
      표지 · {{ pageNumber }}
    </div>
    <button v-if="editable && illustrationUrl" class="absolute bottom-3 left-3 bg-white/90 text-xs px-2 py-1 rounded"
            @click="$emit('regenerate')">🔄 재생성</button>
  </div>
</template>
