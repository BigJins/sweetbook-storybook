<script setup lang="ts">
import type { Story } from '../types';
defineProps<{ story: Story }>();
defineEmits<{ close: []; created: [] }>();
</script>

<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 text-sm text-gray-500">
    OrderModal stub (Task 27)
  </div>
</template>
