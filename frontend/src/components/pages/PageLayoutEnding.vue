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
  <div class="w-[340px] md:w-[400px] aspect-[3/4] flex flex-col">
    <div class="bg-amber-50 p-6">
      <textarea v-if="editable" :value="bodyText ?? ''" @input="onInput"
                class="w-full text-base md:text-lg text-center font-semibold leading-loose bg-transparent resize-none focus:outline-none focus:bg-white rounded-lg p-2"
                rows="3" />
      <p v-else class="text-base md:text-lg text-center font-semibold leading-loose text-gray-800">{{ bodyText }}</p>
    </div>
    <div class="flex-1 bg-gradient-to-br from-indigo-200 to-emerald-200 flex items-center justify-center relative">
      <img v-if="illustrationUrl" :src="illustrationUrl" class="w-full h-full object-cover" />
      <div v-else class="text-4xl opacity-60">🌙</div>
      <button v-if="editable" class="absolute bottom-3 right-3 text-sm font-semibold bg-white/95 px-3 py-1.5 rounded-lg shadow-sm hover:bg-white transition"
              @click="$emit('regenerate')">🔄 재생성</button>
    </div>
  </div>
</template>
