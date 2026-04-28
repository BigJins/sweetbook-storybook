import type { ApiError } from '../types';

export class ApiException extends Error {
  constructor(public code: string, message: string, public status: number) { super(message); }
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const r = await fetch(path, init);
  if (!r.ok) {
    let body: ApiError;
    try { body = await r.json(); } catch { body = { error: 'NETWORK', message: r.statusText }; }
    throw new ApiException(body.error, body.message, r.status);
  }
  if (r.status === 204) return undefined as T;
  return r.json();
}
