export type StoryStatus = 'DRAFT' | 'ANALYZING_DRAWING' | 'GENERATING_STORY'
  | 'GENERATING_IMAGES' | 'COMPLETED' | 'FAILED';

export type PageLayout = 'COVER' | 'SPLIT' | 'ENDING';

export type OrderStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED';

export interface StorySummary {
  id: string;
  title: string;
  childName: string;
  status: StoryStatus;
  coverUrl: string | null;
  createdAt: string;
  errorMessage: string | null;
}

export interface PageData {
  pageNumber: number;
  layout: PageLayout;
  bodyText: string | null;
  illustrationPrompt: string | null;
  illustrationUrl: string | null;
}

export interface Story {
  id: string;
  title: string;
  childName: string;
  status: StoryStatus;
  errorMessage: string | null;
  drawingUrl: string | null;
  styleDescriptor: string | null;
  imaginationPrompt: string;
  pages: PageData[];
  createdAt: string;
}

export interface OrderItem {
  bookSize: 'A5' | 'B5';
  coverType: 'SOFT' | 'HARD';
  copies: number;
}

export interface Order {
  id: string;
  story: { id: string; title: string; coverUrl: string | null };
  status: OrderStatus;
  recipientName: string;
  addressMemo: string | null;
  item: OrderItem;
  createdAt: string;
}

export interface ApiError { error: string; message: string; }
