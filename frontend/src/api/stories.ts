import { apiFetch } from './client';
import type { Story, StorySummary } from '../types';

export const listStories  = () => apiFetch<StorySummary[]>('/api/stories');
export const getStory     = (id: string) => apiFetch<Story>(`/api/stories/${id}`);
export const createStory  = (form: FormData) =>
  apiFetch<{ id: string; status: string }>('/api/stories', { method: 'POST', body: form });
export const updatePageBody = (id: string, n: number, bodyText: string) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/pages/${n}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ bodyText }),
  });
export const regeneratePage = (id: string, n: number) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/pages/${n}/regenerate`, { method: 'POST' });
export const retryStory = (id: string) =>
  apiFetch<{ ok: boolean }>(`/api/stories/${id}/retry`, { method: 'POST' });
