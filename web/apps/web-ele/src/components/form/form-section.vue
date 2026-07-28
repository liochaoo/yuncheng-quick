<script lang="ts" setup>
import SectionHeader from '#/components/section/section-header.vue';

defineProps<{
  description?: string;
  title: string;
}>();
</script>

<template>
  <section class="mb-5 last:mb-0">
    <SectionHeader :description="description" :title="title" />
    <slot></slot>
  </section>
</template>
