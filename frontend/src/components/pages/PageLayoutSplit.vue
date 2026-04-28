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
  <div class="flex w-[680px] aspect-[3/2]">
    <div class="flex-1 bg-gradient-to-br from-pink-200 to-violet-200 flex items-center justify-center">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-center text-gray-500">
        <div class="text-3xl">⚠️</div>
        <div class="text-xs mt-1">일러스트 실패</div>
      </div>
    </div>
    <div class="flex-1 bg-amber-50 p-8 flex flex-col justify-between">
      <div>
        <div class="text-[10px] uppercase font-bold tracking-wide text-gray-400">Page {{ pageNumber }}</div>
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  class="mt-3 w-full h-32 bg-transparent text-sm leading-relaxed resize-none focus:outline-none focus:bg-white rounded p-2"
                  placeholder="페이지 본문..." />
        <p v-else class="mt-3 text-sm leading-relaxed">{{ bodyText }}</p>
      </div>
      <div class="flex gap-2">
        <button class="text-xs px-3 py-1 border rounded bg-white" @click="$emit('regenerate')">🔄 재생성</button>
      </div>
    </div>
  </div>
</template>
