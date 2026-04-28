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
  <div class="w-[340px] aspect-[3/4] flex flex-col">
    <div class="bg-amber-50 p-4">
      <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                class="w-full text-sm text-center font-semibold leading-relaxed bg-transparent resize-none focus:outline-none focus:bg-white rounded p-2"
                rows="2" />
      <p v-else class="text-sm text-center font-semibold">{{ bodyText }}</p>
    </div>
    <div class="flex-1 bg-gradient-to-br from-indigo-200 to-emerald-200 flex items-center justify-center relative">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-3xl opacity-60">🌙</div>
      <button v-if="editable" class="absolute bottom-2 right-2 text-xs bg-white/90 px-2 py-1 rounded"
              @click="$emit('regenerate')">🔄 재생성</button>
    </div>
  </div>
</template>
