<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import UploadDropzone from '../components/UploadDropzone.vue';
import { createStory } from '../api/stories';
import { ApiException, apiFetch } from '../api/client';

interface AppConfigResponse {
  mockMode: boolean;
  demoStory: {
    childName: string;
    imaginationPrompt: string;
    drawingUrl: string;
  } | null;
}

const router = useRouter();
const drawing = ref<File | null>(null);
const childName = ref('');
const imagination = ref('');
const errors = ref<string[]>([]);
const submitting = ref(false);
const mockMode = ref(false);
const demoDrawingUrl = ref('');

const charCount = computed(() => imagination.value.length);

onMounted(async () => {
  const config = await apiFetch<AppConfigResponse>('/api/app-config');
  mockMode.value = config.mockMode;
  if (config.mockMode && config.demoStory) {
    childName.value = config.demoStory.childName;
    imagination.value = config.demoStory.imaginationPrompt;
    demoDrawingUrl.value = config.demoStory.drawingUrl;
  }
});

function validate(): string[] {
  const list: string[] = [];
  if (!mockMode.value && !drawing.value) list.push('그림을 업로드해 주세요.');
  const name = childName.value.trim();
  if (name.length < 1 || name.length > 20) list.push('아이 이름은 1~20자로 적어주세요.');
  if (imagination.value.length < 10 || imagination.value.length > 500) list.push('상상은 10자 이상 500자 이하로 적어주세요.');
  return list;
}

async function submit() {
  errors.value = validate();
  if (errors.value.length > 0) return;
  submitting.value = true;
  try {
    const fd = new FormData();
    if (drawing.value) fd.append('drawing', drawing.value);
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

function handleDropzoneError(message: string) {
  errors.value = [message];
}
</script>

<template>
  <main class="max-w-5xl mx-auto px-8 py-10">
    <div class="text-base mb-6">
      <RouterLink to="/" class="text-gray-500 hover:text-gray-900">← 동화 목록</RouterLink>
    </div>
    <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight mb-8">새 동화 만들기</h1>

    <div
      v-if="mockMode"
      class="mb-8 rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm leading-relaxed text-amber-900"
    >
      <div class="font-bold mb-1">데모 mock 모드</div>
      <div>
        평가용 데모에서는 입력값이 고정됩니다. 업로드 그림, 아이 이름, 상상 내용은 선택할 수 없고 정해진 예시로
        생성 과정을 보여줍니다.
      </div>
    </div>

    <div class="grid md:grid-cols-2 gap-10">
      <div>
        <div class="text-base font-bold mb-2">① 아이의 그림 <span class="text-red-500">*</span></div>
        <p class="text-sm text-gray-500 mb-4 leading-relaxed">
          크레용·색연필·디지털 드로잉 모두 가능합니다. 일러스트 스타일의 레퍼런스로 사용됩니다.
        </p>
        <div v-if="mockMode" class="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm">
          <img :src="demoDrawingUrl" alt="데모용 고정 그림" class="w-full object-cover" />
        </div>
        <UploadDropzone v-else v-model="drawing" @error="handleDropzoneError" />
      </div>

      <div>
        <div class="mb-7">
          <label class="block text-base font-bold mb-2">② 아이 이름 <span class="text-red-500">*</span></label>
          <input
            v-model="childName"
            :disabled="mockMode"
            class="w-full px-4 py-3 border border-gray-300 rounded-xl text-base focus:outline-none focus:border-gray-900 transition disabled:bg-gray-50 disabled:text-gray-500"
            placeholder="예: 서아"
            maxlength="20"
          />
          <div class="mt-1.5 text-xs text-gray-400">최대 20자</div>
        </div>

        <div class="mb-7">
          <label class="block text-base font-bold mb-2">③ 아이의 상상 <span class="text-red-500">*</span></label>
          <textarea
            v-model="imagination"
            :disabled="mockMode"
            class="w-full px-4 py-3 border border-gray-300 rounded-xl text-base h-44 leading-relaxed focus:outline-none focus:border-gray-900 transition disabled:bg-gray-50 disabled:text-gray-500"
            placeholder="예: 곰돌이가 우주를 여행하며 별을 만나는 이야기"
            maxlength="500"
          ></textarea>
          <div class="mt-1.5 flex justify-between text-xs text-gray-400">
            <span>10자 이상 500자 이하</span><span>{{ charCount }} / 500</span>
          </div>
        </div>

        <div
          v-if="errors.length > 0"
          class="bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl p-4 mb-5 leading-relaxed"
        >
          <div v-for="e in errors" :key="e">• {{ e }}</div>
        </div>

        <button
          class="w-full bg-gray-900 text-white py-4 rounded-xl font-bold text-base shadow-md hover:shadow-lg transition disabled:opacity-50 disabled:shadow-none"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '생성 중...' : '동화 만들기 ✨' }}
        </button>
      </div>
    </div>
  </main>
</template>
