<script lang="ts" setup>
import { computed } from 'vue';

import { ElOption, ElSelect } from 'element-plus';

import { useDictionaryOptions } from '#/hooks/use-dictionary-options';

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    clearable?: boolean;
    dictionaryCode: string;
    disabled?: boolean;
    modelValue?: string | string[];
    multiple?: boolean;
    placeholder?: string;
  }>(),
  {
    clearable: true,
    disabled: false,
    modelValue: undefined,
    multiple: false,
    placeholder: '请选择',
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string | string[] | undefined];
}>();

const { loaded, loading, options, reload } = useDictionaryOptions(
  () => props.dictionaryCode,
);

const selectedValues = computed(() => {
  const value = props.modelValue;
  if (Array.isArray(value)) return new Set(value);
  return new Set(value === undefined || value === '' ? [] : [value]);
});

const visibleOptions = computed(() => [
  ...options.value
    .filter(
      (option) => option.enabled || selectedValues.value.has(option.value),
    )
    .map((option) => ({
      ...option,
      displayLabel: option.enabled ? option.label : `${option.label}（已停用）`,
    })),
  ...(loaded.value
    ? [...selectedValues.value]
        .filter(
          (value) => !options.value.some((option) => option.value === value),
        )
        .map((value) => ({
          displayLabel: `${value}（已删除）`,
          enabled: false,
          label: value,
          value,
        }))
    : []),
]);

defineExpose({ reload });
</script>

<template>
  <ElSelect
    :clearable="clearable"
    :disabled="disabled"
    :loading="loading"
    :model-value="modelValue"
    :multiple="multiple"
    :placeholder="placeholder"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <ElOption
      v-for="option in visibleOptions"
      :key="option.value"
      :label="option.displayLabel"
      :value="option.value"
    />
  </ElSelect>
</template>
