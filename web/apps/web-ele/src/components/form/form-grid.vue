<script lang="ts" setup>
withDefaults(
  defineProps<{
    columns?: 1 | 2;
  }>(),
  {
    columns: 2,
  },
);
</script>

<template>
  <div
    class="grid grid-cols-1 gap-x-5"
    :class="{ 'md:grid-cols-2': columns === 2 }"
  >
    <slot></slot>
  </div>
</template>
