import { apiFetch } from './client';
import type { Order } from '../types';

export interface OrderCreatePayload {
  storyId: string; bookSize: 'A5' | 'B5'; coverType: 'SOFT' | 'HARD';
  copies: number; recipientName: string; addressMemo: string;
}

export const listOrders   = () => apiFetch<Order[]>('/api/orders');
export const createOrder  = (p: OrderCreatePayload) =>
  apiFetch<{ id: string; status: string }>('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(p),
  });
export const updateOrderStatus = (id: string, status: 'PROCESSING' | 'COMPLETED') =>
  apiFetch<{ ok: boolean }>(`/api/orders/${id}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status }),
  });
