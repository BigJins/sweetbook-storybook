<script setup lang="ts">
defineProps<{ icon: string; title: string; subtitle: string; ctaText?: string; ctaTo?: string }>();
</script>

<template>
  <div class="text-center py-20 px-8 bg-white rounded-2xl border border-gray-200">
    <div class="text-6xl">{{ icon }}</div>
    <h3 class="mt-4 text-xl font-bold text-gray-900">{{ title }}</h3>
    <p class="mt-2 text-base text-gray-500 leading-relaxed">{{ subtitle }}</p>
    <RouterLink v-if="ctaText && ctaTo" :to="ctaTo"
      class="inline-block mt-6 bg-gray-900 text-white px-6 py-3 rounded-xl text-base font-bold shadow-md hover:shadow-lg transition">
      {{ ctaText }}
    </RouterLink>
  </div>
</template>
