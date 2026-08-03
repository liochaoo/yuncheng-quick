<script lang="ts" setup>
import SectionHeader from '#/components/section/section-header.vue';

defineProps<{
  description?: string;
  title: string;
}>();

defineSlots<{
  actions?(): unknown;
  default(): unknown;
}>();
</script>

<template>
  <section class="mb-5 last:mb-0">
    <SectionHeader :description="description" :title="title">
      <template v-if="$slots.actions" #actions>
        <slot name="actions"></slot>
      </template>
    </SectionHeader>
    <slot></slot>
  </section>
</template>
