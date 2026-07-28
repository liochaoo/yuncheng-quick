<script lang="ts" setup>
import { computed } from 'vue';

import { ElInputNumber } from 'element-plus';

const props = withDefaults(
  defineProps<{
    disabled?: boolean | null;
    max?: number;
    min?: number;
    modelValue: number;
  }>(),
  {
    disabled: null,
    max: undefined,
    min: 0,
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: number];
}>();

// 未显式设置时不向内部控件传 disabled，使其可以继承 ElForm 状态。
const disabledAttrs = computed(() =>
  props.disabled === null ? {} : { disabled: props.disabled },
);
</script>

<template>
  <ElInputNumber
    v-bind="disabledAttrs"
    class="!w-full"
    :max="max"
    :min="min"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event ?? min)"
  />
</template>
