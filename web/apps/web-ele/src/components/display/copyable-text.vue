<script lang="ts" setup>
import { computed } from 'vue';

import { Copy } from '@vben/icons';

import { useClipboard } from '@vueuse/core';
import { ElMessage, ElTooltip } from 'element-plus';

const props = withDefaults(
  defineProps<{
    fallback?: string;
    value?: null | string;
  }>(),
  {
    fallback: '-',
    value: undefined,
  },
);

const { copy } = useClipboard({ legacy: true });
const normalizedValue = computed(() => props.value?.trim() || '');
const displayValue = computed(() => normalizedValue.value || props.fallback);

async function copyValue() {
  if (!normalizedValue.value) return;
  await copy(normalizedValue.value);
  ElMessage.success('复制成功');
}
</script>

<template>
  <span class="inline-flex max-w-full min-w-0 items-center gap-1 align-middle">
    <span class="min-w-0 truncate" :title="displayValue">
      {{ displayValue }}
    </span>
    <ElTooltip v-if="normalizedValue" content="复制" placement="top">
      <button
        aria-label="复制"
        class="text-muted-foreground hover:text-primary inline-flex shrink-0 cursor-pointer items-center border-0 bg-transparent p-0"
        type="button"
        @click.stop="copyValue"
      >
        <Copy class="size-3.5" />
      </button>
    </ElTooltip>
  </span>
</template>
