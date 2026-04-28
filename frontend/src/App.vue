<script setup lang="ts">
import Navbar from './components/Navbar.vue';
import { RouterView } from 'vue-router';
</script>
<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    <RouterView />
  </div>
</template>
