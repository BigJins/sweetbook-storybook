<script setup lang="ts">
import { ref } from 'vue';
import type { Story } from '../types';
import { createOrder } from '../api/orders';
import { ApiException } from '../api/client';

const props = defineProps<{ story: Story }>();
const emit = defineEmits<{ close: []; created: [] }>();

const bookSize = ref<'A5' | 'B5'>('A5');
const coverType = ref<'SOFT' | 'HARD'>('HARD');
const copies = ref(1);
const recipientName = ref('');
const addressMemo = ref('');
const error = ref<string | null>(null);
const submitting = ref(false);

const cover = props.story.pages.find(p => p.pageNumber === 1)?.illustrationUrl;

async function submit() {
  if (recipientName.value.trim().length < 1 || recipientName.value.trim().length > 30) {
    error.value = '받는 분 이름은 1~30자로 적어주세요';
    return;
  }
  submitting.value = true;
  error.value = null;
  try {
    await createOrder({
      storyId: props.story.id,
      bookSize: bookSize.value, coverType: coverType.value,
      copies: copies.value,
      recipientName: recipientName.value.trim(),
      addressMemo: addressMemo.value,
    });
    emit('created');
  } catch (e) {
    error.value = e instanceof ApiException ? e.message : '오류가 발생했어요';
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl w-[480px] shadow-2xl">
      <div class="px-6 py-5 border-b border-gray-100 flex justify-between items-center">
        <h2 class="text-base font-bold">📦 종이책 주문</h2>
        <button class="text-gray-400 text-xl" @click="$emit('close')">×</button>
      </div>

      <div class="px-6 py-5 space-y-5">
        <div class="bg-gray-50 rounded-lg p-3 flex gap-3 items-center">
          <img v-if="cover" :src="cover" class="w-12 h-16 rounded object-cover" />
          <div>
            <div class="font-bold text-sm">{{ story.title }}</div>
            <div class="text-xs text-gray-500 mt-0.5">작가: {{ story.childName }} · 5페이지</div>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">책 사이즈</div>
          <div class="flex gap-2">
            <button v-for="s in ['A5','B5']" :key="s"
                    :class="['flex-1 py-2 rounded-lg border-2 text-sm font-bold',
                            bookSize === s ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300 text-gray-700']"
                    @click="bookSize = s as 'A5' | 'B5'">
              {{ s }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">표지</div>
          <div class="flex gap-2">
            <button v-for="c in [{v:'SOFT',label:'소프트커버'},{v:'HARD',label:'하드커버'}]" :key="c.v"
                    :class="['flex-1 py-2 rounded-lg border-2 text-sm font-bold',
                             coverType === c.v ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300']"
                    @click="coverType = c.v as 'SOFT' | 'HARD'">
              {{ c.label }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-xs font-bold mb-2">부수</div>
          <div class="flex items-center gap-3">
            <button class="w-9 h-9 border rounded text-lg" @click="copies = Math.max(1, copies-1)">−</button>
            <div class="text-lg font-bold w-10 text-center">{{ copies }}</div>
            <button class="w-9 h-9 border rounded text-lg" @click="copies = Math.min(10, copies+1)">+</button>
            <span class="text-xs text-gray-400 ml-2">최대 10부</span>
          </div>
        </div>

        <div>
          <label class="block text-xs font-bold mb-2">받는 분 이름 <span class="text-red-500">*</span></label>
          <input v-model="recipientName" class="w-full px-3 py-2 border border-gray-300 rounded text-sm"
                 maxlength="30" />
        </div>

        <div>
          <label class="block text-xs font-bold mb-2">주소 메모</label>
          <textarea v-model="addressMemo" class="w-full px-3 py-2 border border-gray-300 rounded text-sm h-16"
                    maxlength="500" placeholder="실제 발송은 하지 않습니다 (시연용)"></textarea>
        </div>

        <div v-if="error" class="bg-red-50 text-red-700 text-xs rounded p-2">{{ error }}</div>
      </div>

      <div class="px-6 py-4 border-t border-gray-100 flex justify-end gap-2">
        <button class="border px-4 py-2 rounded-lg text-sm" @click="$emit('close')">취소</button>
        <button class="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-bold disabled:opacity-50"
                :disabled="submitting" @click="submit">
          {{ submitting ? '생성중...' : '주문 생성' }}
        </button>
      </div>
    </div>
  </div>
</template>
