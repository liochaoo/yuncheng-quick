<script lang="ts" setup>
import { computed } from 'vue';

const props = defineProps<{ fullPath?: null | string }>();

const displayPath = computed(() => {
  const path = props.fullPath?.trim();
  if (!path) return '-';
  const parts = path.split(' / ');
  return parts.length <= 4
    ? path
    : `${parts[0]} / … / ${parts.at(-2)} / ${parts.at(-1)}`;
});
</script>

<template>
  <span class="block min-w-0 truncate" :title="fullPath || undefined">
    {{ displayPath }}
  </span>
</template>
