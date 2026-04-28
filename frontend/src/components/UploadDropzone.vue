<script setup lang="ts">
import { ref, computed } from 'vue';

const props = defineProps<{ modelValue: File | null }>();
const emit = defineEmits<{ 'update:modelValue': [file: File | null]; error: [message: string] }>();

const isDragging = ref(false);
const previewUrl = computed(() => props.modelValue ? URL.createObjectURL(props.modelValue) : null);

function validate(file: File): string | null {
  if (!['image/jpeg', 'image/png'].includes(file.type)) return '5MB 이하 JPG/PNG만 업로드 가능합니다';
  if (file.size > 5 * 1024 * 1024) return '5MB 이하 JPG/PNG만 업로드 가능합니다';
  return null;
}

function handle(file: File | null) {
  if (!file) { emit('update:modelValue', null); return; }
  const err = validate(file);
  if (err) { emit('error', err); return; }
  emit('update:modelValue', file);
}

function onDrop(e: DragEvent) {
  e.preventDefault();
  isDragging.value = false;
  handle(e.dataTransfer?.files?.[0] ?? null);
}

function onPick(e: Event) {
  handle((e.target as HTMLInputElement).files?.[0] ?? null);
}
</script>

<template>
  <label class="block aspect-square border-2 border-dashed rounded-xl bg-white cursor-pointer flex flex-col items-center justify-center transition relative overflow-hidden"
         :class="isDragging ? 'border-blue-500 bg-blue-50' : 'border-gray-300'"
         @dragover.prevent="isDragging = true"
         @dragleave="isDragging = false"
         @drop="onDrop">
    <img v-if="previewUrl" :src="previewUrl" class="absolute inset-0 w-full h-full object-cover" />
    <template v-else>
      <div class="text-5xl">🎨</div>
      <div class="mt-3 text-sm font-semibold">파일 끌어다 놓기</div>
      <div class="mt-1 text-xs text-gray-500">또는 클릭하여 선택</div>
      <div class="mt-4 text-[11px] text-gray-400">JPG, PNG · 최대 5MB</div>
    </template>
    <input type="file" accept="image/jpeg,image/png" capture="environment"
           class="hidden" @change="onPick" />
  </label>
</template>
