import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useStoryStatus } from '../useStoryStatus';

describe('useStoryStatus', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    globalThis.fetch = vi.fn();
  });
  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('polls until COMPLETED then stops', async () => {
    const responses = [
      { status: 'GENERATING_STORY' },
      { status: 'GENERATING_IMAGES' },
      { status: 'COMPLETED' },
    ];
    let i = 0;
    (globalThis.fetch as any).mockImplementation(() => Promise.resolve({
      ok: true, status: 200, json: () => Promise.resolve(responses[i++])
    }));

    const { story, isPolling, start, stop } = useStoryStatus('s1');
    start();
    await vi.advanceTimersByTimeAsync(2100);
    await vi.advanceTimersByTimeAsync(2100);
    await vi.advanceTimersByTimeAsync(2100);

    expect(story.value?.status).toBe('COMPLETED');
    expect(isPolling.value).toBe(false);
    stop();
  });

  it('stops on FAILED', async () => {
    (globalThis.fetch as any).mockResolvedValue({
      ok: true, status: 200, json: () => Promise.resolve({ status: 'FAILED' })
    });
    const { story, isPolling, start } = useStoryStatus('s1');
    start();
    await vi.advanceTimersByTimeAsync(2100);
    expect(story.value?.status).toBe('FAILED');
    expect(isPolling.value).toBe(false);
  });
});
