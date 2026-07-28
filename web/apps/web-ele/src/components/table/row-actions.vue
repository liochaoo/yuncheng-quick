<script lang="ts" setup>
import type { RowAction } from './row-actions.types';

import { computed } from 'vue';

import { ElButton } from 'element-plus';

const props = defineProps<{
  actions: RowAction[];
}>();

const visibleActions = computed(() =>
  props.actions.filter((action) => action.visible !== false),
);
</script>

<template>
  <div class="row-actions">
    <ElButton
      v-for="action in visibleActions"
      :key="action.label"
      class="row-action-button"
      :disabled="action.disabled"
      link
      :type="action.type ?? 'primary'"
      @click="action.onClick"
    >
      {{ action.label }}
    </ElButton>
  </div>
</template>

<style scoped>
.row-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
}

.row-action-button + .row-action-button {
  margin-left: 0;
}
</style>
