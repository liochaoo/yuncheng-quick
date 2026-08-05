<script lang="ts" setup>
import type { DetailTableItem } from './detail-table.types';

import { computed } from 'vue';

import { useMediaQuery } from '@vueuse/core';

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

const rows = computed(() => {
  const result: Array<Array<{ item: DetailTableItem; span: number }>> = [];
  let row: Array<{ item: DetailTableItem; span: number }> = [];
  let remaining = columns.value;

  props.items.forEach((item) => {
    const requestedSpan = Math.min(item.span ?? 1, columns.value);

    if (requestedSpan > remaining && row.length > 0) {
      const lastCell = row[row.length - 1];
      if (lastCell) lastCell.span += remaining;
      result.push(row);
      row = [];
      remaining = columns.value;
    }

    const span = Math.min(requestedSpan, remaining);
    row.push({ item, span });
    remaining -= span;

    if (remaining === 0) {
      result.push(row);
      row = [];
      remaining = columns.value;
    }
  });

  if (row.length > 0) {
    const lastCell = row[row.length - 1];
    if (lastCell) lastCell.span += remaining;
    result.push(row);
  }

  return result;
});
</script>

<template>
  <table class="detail-table">
    <colgroup>
      <template v-for="column in columns" :key="column">
        <col class="detail-table__label-column" />
        <col />
      </template>
    </colgroup>
    <tbody>
      <tr v-for="(row, rowIndex) in rows" :key="rowIndex">
        <template v-for="cell in row" :key="cell.item.key">
          <th class="detail-table__label" scope="row">
            {{ cell.item.label }}
          </th>
          <td class="detail-table__content" :colspan="cell.span * 2 - 1">
            <slot :name="cell.item.key" :item="cell.item">
              {{ formatEmptyValue(cell.item.value) }}
            </slot>
          </td>
        </template>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.detail-table {
  width: 100%;
  font-size: var(--el-font-size-base);
  color: var(--el-text-color-regular);
  table-layout: fixed;
  border-collapse: collapse;
}

.detail-table__label,
.detail-table__content {
  padding: 10px 14px;
  line-height: 22px;
  vertical-align: middle;
  text-align: left;
  overflow-wrap: anywhere;
  border: 1px solid var(--el-border-color);
}

.detail-table__label-column {
  width: 132px;
}

.detail-table__label {
  min-width: 132px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  background: var(--el-fill-color-light) !important;
}

.detail-table__content {
  min-width: 0;
  background: var(--el-bg-color);
}
</style>
