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
  <div class="flex w-[680px] md:w-[800px] aspect-[3/2]">
    <div class="flex-1 bg-gradient-to-br from-pink-200 to-violet-200 flex items-center justify-center">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-center text-gray-500">
        <div class="text-4xl">⚠️</div>
        <div class="text-sm mt-2">일러스트 실패</div>
      </div>
    </div>
    <div class="flex-1 bg-amber-50 p-8 md:p-10 flex flex-col justify-between">
      <div>
        <div class="text-xs uppercase font-bold tracking-wide text-gray-400">Page {{ pageNumber }}</div>
        <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                  class="mt-4 w-full h-40 bg-transparent text-base md:text-lg leading-loose resize-none focus:outline-none focus:bg-white rounded-lg p-2"
                  placeholder="페이지 본문..." />
        <p v-else class="mt-4 text-base md:text-lg leading-loose text-gray-800">{{ bodyText }}</p>
      </div>
      <div class="flex gap-2 mt-4">
        <button class="text-sm font-semibold px-4 py-2 border rounded-lg bg-white hover:bg-gray-50 transition" @click="$emit('regenerate')">🔄 재생성</button>
      </div>
    </div>
  </div>
</template>
