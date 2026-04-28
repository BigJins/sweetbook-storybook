<script setup lang="ts">
defineProps<{ icon: string; title: string; subtitle: string; ctaText?: string; ctaTo?: string }>();
</script>

<template>
  <div class="text-center py-16 px-8 bg-white rounded-xl border border-gray-200">
    <div class="text-5xl">{{ icon }}</div>
    <h3 class="mt-3 text-lg font-bold text-gray-900">{{ title }}</h3>
    <p class="mt-1 text-sm text-gray-500">{{ subtitle }}</p>
    <RouterLink v-if="ctaText && ctaTo" :to="ctaTo"
      class="inline-block mt-4 bg-gray-900 text-white px-5 py-2 rounded-lg text-sm font-bold">
      {{ ctaText }}
    </RouterLink>
  </div>
</template>
