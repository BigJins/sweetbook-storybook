import { ref, onUnmounted } from 'vue';
import type { Story } from '../types';
import { getStory } from '../api/stories';

const TERMINAL = new Set(['COMPLETED', 'FAILED']);

export function useStoryStatus(id: string, intervalMs = 2000) {
  const story = ref<Story | null>(null);
  const isPolling = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  async function tick() {
    try {
      story.value = await getStory(id);
      if (TERMINAL.has(story.value.status)) stop();
    } catch (e) {
      console.error('poll failed', e);
    }
  }

  function start() {
    if (isPolling.value) return;
    isPolling.value = true;
    tick();
    timer = setInterval(tick, intervalMs);
  }

  function stop() {
    isPolling.value = false;
    if (timer) { clearInterval(timer); timer = null; }
  }

  onUnmounted(stop);

  return { story, isPolling, start, stop, refresh: tick };
}
