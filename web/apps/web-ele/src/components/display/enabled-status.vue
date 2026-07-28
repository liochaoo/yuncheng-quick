<script lang="ts" setup>
import type { TagProps } from 'element-plus';

import { ElSwitch, ElTag } from 'element-plus';

withDefaults(
  defineProps<{
    activeText?: string;
    activeType?: TagProps['type'];
    disabled?: boolean;
    editable?: boolean;
    inactiveText?: string;
    inactiveType?: TagProps['type'];
    modelValue: boolean;
  }>(),
  {
    activeText: '启用',
    activeType: 'success',
    disabled: false,
    editable: false,
    inactiveText: '停用',
    inactiveType: 'info',
  },
);

const emit = defineEmits<{
  change: [value: boolean];
}>();
</script>

<template>
  <ElSwitch
    v-if="editable"
    :disabled="disabled"
    :model-value="modelValue"
    @change="emit('change', Boolean($event))"
  />
  <ElTag v-else :type="modelValue ? activeType : inactiveType">
    {{ modelValue ? activeText : inactiveText }}
  </ElTag>
</template>
