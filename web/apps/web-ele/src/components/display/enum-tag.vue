<script lang="ts" setup>
import type { TagProps } from 'element-plus';

import type { EnumTagOptions } from './enum-tag.types';

import { computed } from 'vue';

import { ElTag } from 'element-plus';

const props = withDefaults(
  defineProps<{
    effect?: TagProps['effect'];
    fallback?: string;
    options: EnumTagOptions;
    size?: TagProps['size'];
    value?: boolean | number | string;
  }>(),
  {
    effect: 'light',
    fallback: '-',
    size: 'default',
    value: undefined,
  },
);

const option = computed(() => props.options[String(props.value)]);
</script>

<template>
  <ElTag :effect="effect" :size="size" :type="option?.type">
    {{ option?.label ?? fallback }}
  </ElTag>
</template>
