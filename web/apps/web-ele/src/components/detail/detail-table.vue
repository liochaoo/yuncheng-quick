<script lang="ts" setup>
import type { DetailTableItem } from './detail-table.types';

import { computed } from 'vue';

import { useMediaQuery } from '@vueuse/core';
import { ElDescriptions, ElDescriptionsItem } from 'element-plus';

import { formatEmptyValue } from '#/utils/display';

const props = withDefaults(
  defineProps<{
    columns?: 1 | 2;
    items: DetailTableItem[];
  }>(),
  {
    columns: 2,
  },
);

const compact = useMediaQuery('(max-width: 767px)');
const columns = computed(() => (compact.value ? 1 : props.columns));
</script>

<template>
  <ElDescriptions
    border
    class="detail-table"
    :column="columns"
    direction="horizontal"
  >
    <ElDescriptionsItem
      v-for="item in items"
      :key="item.key"
      label-align="left"
      label-class-name="detail-table__label"
      :label="item.label"
      :span="compact ? 1 : (item.span ?? 1)"
    >
      <slot :name="item.key" :item="item">
        {{ formatEmptyValue(item.value) }}
      </slot>
    </ElDescriptionsItem>
  </ElDescriptions>
</template>

<style scoped>
.detail-table {
  --el-descriptions-table-border: 1px solid var(--el-border-color);
}

.detail-table :deep(.el-descriptions__cell) {
  padding: 10px 14px !important;
  line-height: 22px;
  overflow-wrap: anywhere;
}

.detail-table :deep(.detail-table__label) {
  width: 132px;
  min-width: 132px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  background: var(--el-fill-color-light) !important;
}
</style>
