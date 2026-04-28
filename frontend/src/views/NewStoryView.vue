<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import UploadDropzone from '../components/UploadDropzone.vue';
import { createStory } from '../api/stories';
import { ApiException } from '../api/client';

const router = useRouter();
const drawing = ref<File | null>(null);
const childName = ref('');
const imagination = ref('');
const errors = ref<string[]>([]);
const submitting = ref(false);

const charCount = computed(() => imagination.value.length);

function validate(): string[] {
  const list: string[] = [];
  if (!drawing.value) list.push('그림을 업로드해주세요');
  const name = childName.value.trim();
  if (name.length < 1 || name.length > 20) list.push('아이 이름은 1~20자로 적어주세요');
  if (imagination.value.length < 10 || imagination.value.length > 500) list.push('상상은 10자 이상 500자 이하로 적어주세요');
  return list;
}

async function submit() {
  errors.value = validate();
  if (errors.value.length > 0) return;
  submitting.value = true;
  try {
    const fd = new FormData();
    fd.append('drawing', drawing.value!);
    fd.append('childName', childName.value.trim());
    fd.append('imaginationPrompt', imagination.value);
    const resp = await createStory(fd);
    router.push(`/stories/${resp.id}`);
  } catch (e) {
    errors.value = [e instanceof ApiException ? e.message : '알 수 없는 오류'];
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <main class="max-w-5xl mx-auto px-8 py-8">
    <div class="text-sm mb-6"><RouterLink to="/" class="text-gray-500">← 동화 목록</RouterLink></div>
    <h1 class="text-xl font-bold mb-6">새 동화 만들기</h1>

    <div class="grid md:grid-cols-2 gap-8">
      <div>
        <div class="text-sm font-bold mb-2">① 아이의 그림 <span class="text-red-500">*</span></div>
        <p class="text-xs text-gray-500 mb-3">크레용·색연필·디지털 다 OK. 일러스트의 스타일 레퍼런스로 사용됩니다.</p>
        <UploadDropzone v-model="drawing" @error="m => errors = [m]" />
      </div>

      <div>
        <div class="mb-6">
          <label class="block text-sm font-bold mb-2">② 아이 이름 <span class="text-red-500">*</span></label>
          <input v-model="childName" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                 placeholder="예: 서아" maxlength="20" />
          <div class="mt-1 text-[11px] text-gray-400">최대 20자</div>
        </div>

        <div class="mb-6">
          <label class="block text-sm font-bold mb-2">③ 아이의 상상 <span class="text-red-500">*</span></label>
          <textarea v-model="imagination" class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm h-40"
                    placeholder="예: 곰돌이가 우주에 가서 별을 따왔어!" maxlength="500"></textarea>
          <div class="mt-1 flex justify-between text-[11px] text-gray-400">
            <span>10자 이상 500자 이하</span><span>{{ charCount }} / 500</span>
          </div>
        </div>

        <div v-if="errors.length > 0" class="bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg p-3 mb-4">
          <div v-for="e in errors" :key="e">⚠️ {{ e }}</div>
        </div>

        <button class="w-full bg-gray-900 text-white py-3 rounded-lg font-bold disabled:opacity-50"
                :disabled="submitting" @click="submit">
          {{ submitting ? '생성 중...' : '동화 만들기 ✨' }}
        </button>
      </div>
    </div>
  </main>
</template>
