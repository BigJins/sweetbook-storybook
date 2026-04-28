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
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4" @click.self="$emit('close')">
    <div class="bg-white rounded-2xl w-full max-w-[520px] shadow-2xl max-h-[90vh] flex flex-col">
      <div class="px-7 py-5 border-b border-gray-100 flex justify-between items-center shrink-0">
        <h2 class="text-lg font-extrabold tracking-tight">📦 종이책 주문</h2>
        <button class="text-gray-400 text-2xl hover:text-gray-700 leading-none" @click="$emit('close')">×</button>
      </div>

      <div class="px-7 py-6 space-y-6 overflow-y-auto">
        <div class="bg-gray-50 rounded-xl p-4 flex gap-4 items-center">
          <img v-if="cover" :src="cover" class="w-14 h-18 rounded-lg object-cover shadow-sm" />
          <div>
            <div class="font-bold text-base leading-snug">{{ story.title }}</div>
            <div class="text-sm text-gray-500 mt-1">작가: {{ story.childName }} · 5페이지</div>
          </div>
        </div>

        <div>
          <div class="text-sm font-bold mb-2.5">책 사이즈</div>
          <div class="flex gap-2">
            <button v-for="s in ['A5','B5']" :key="s"
                    :class="['flex-1 py-3 rounded-xl border-2 text-base font-bold transition',
                            bookSize === s ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300 text-gray-700 hover:border-gray-500']"
                    @click="bookSize = s as 'A5' | 'B5'">
              {{ s }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-sm font-bold mb-2.5">표지</div>
          <div class="flex gap-2">
            <button v-for="c in [{v:'SOFT',label:'소프트커버'},{v:'HARD',label:'하드커버'}]" :key="c.v"
                    :class="['flex-1 py-3 rounded-xl border-2 text-base font-bold transition',
                             coverType === c.v ? 'bg-gray-900 text-white border-gray-900' : 'border-gray-300 hover:border-gray-500']"
                    @click="coverType = c.v as 'SOFT' | 'HARD'">
              {{ c.label }}
            </button>
          </div>
        </div>

        <div>
          <div class="text-sm font-bold mb-2.5">부수</div>
          <div class="flex items-center gap-3">
            <button class="w-10 h-10 border rounded-lg text-xl hover:bg-gray-50 transition" @click="copies = Math.max(1, copies-1)">−</button>
            <div class="text-xl font-bold w-12 text-center">{{ copies }}</div>
            <button class="w-10 h-10 border rounded-lg text-xl hover:bg-gray-50 transition" @click="copies = Math.min(10, copies+1)">+</button>
            <span class="text-sm text-gray-400 ml-2">최대 10부</span>
          </div>
        </div>

        <div>
          <label class="block text-sm font-bold mb-2.5">받는 분 이름 <span class="text-red-500">*</span></label>
          <input v-model="recipientName" class="w-full px-4 py-3 border border-gray-300 rounded-xl text-base focus:outline-none focus:border-gray-900 transition"
                 maxlength="30" />
        </div>

        <div>
          <label class="block text-sm font-bold mb-2.5">주소 메모</label>
          <textarea v-model="addressMemo" class="w-full px-4 py-3 border border-gray-300 rounded-xl text-base h-20 leading-relaxed focus:outline-none focus:border-gray-900 transition"
                    maxlength="500" placeholder="실제 발송은 하지 않습니다 (시연용)"></textarea>
        </div>

        <div v-if="error" class="bg-red-50 text-red-700 text-sm rounded-lg p-3 leading-relaxed">⚠️ {{ error }}</div>
      </div>

      <div class="px-7 py-4 border-t border-gray-100 flex justify-end gap-2 shrink-0">
        <button class="border px-5 py-2.5 rounded-xl text-base hover:bg-gray-50 transition" @click="$emit('close')">취소</button>
        <button class="bg-gray-900 text-white px-5 py-2.5 rounded-xl text-base font-bold shadow-md hover:shadow-lg transition disabled:opacity-50 disabled:shadow-none"
                :disabled="submitting" @click="submit">
          {{ submitting ? '생성중...' : '주문 생성' }}
        </button>
      </div>
    </div>
  </div>
</template>
