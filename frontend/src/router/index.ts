import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: HomeView },
    { path: '/stories/new', component: () => import('../views/NewStoryView.vue') },
    { path: '/stories/:id', component: () => import('../views/StoryDetailView.vue') },
    { path: '/orders', component: () => import('../views/OrdersView.vue') },
  ],
});
