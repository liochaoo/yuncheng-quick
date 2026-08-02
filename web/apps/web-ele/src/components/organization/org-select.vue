<script lang="ts" setup>
import type { OrgOption, OrgType } from '#/api/common/organization';

import { computed, onBeforeUnmount, ref, watch } from 'vue';

import { ElInput } from 'element-plus';

import { getOrgOptionApi } from '#/api/common/organization';
import { getOrgDetailApi } from '#/api/system/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';

import OrgSelectionDialog from './org-selection-dialog.vue';

const props = withDefaults(
  defineProps<{
    clearable?: boolean;
    disabled?: boolean;
    excludeSubtreeRootId?: string;
    modelValue?: string;
    permissionScope?: 'organization-management' | 'user-management';
    placeholder?: string;
    selectableTypes?: OrgType[];
  }>(),
  {
    clearable: true,
    disabled: false,
    excludeSubtreeRootId: undefined,
    modelValue: undefined,
    permissionScope: 'user-management',
    placeholder: '请选择组织',
    selectableTypes: () => [],
  },
);

const emit = defineEmits<{
  change: [value?: OrgOption];
  'update:modelValue': [value?: string];
}>();

const currentOrg = ref<OrgOption>();
const dialogVisible = ref(false);
const currentRequest = useLatestRequest();
const displayValue = computed(() => currentOrg.value?.fullPath ?? '');

async function loadCurrent(id?: string, force = false) {
  if (!force && id && currentOrg.value?.id === id) return;
  currentRequest.invalidate();
  currentOrg.value = undefined;
  if (!id) return;
  const result = await currentRequest.execute(async () => {
    if (props.permissionScope === 'organization-management') {
      const detail = await getOrgDetailApi(id);
      return {
        ancestorIds: [],
        depth: detail.depth,
        fullPath: detail.fullPath,
        hasChildren: false,
        id: detail.id,
        orgCode: detail.orgCode,
        orgName: detail.orgName,
        orgType: detail.orgType,
        parentId: detail.parentId,
        protectedOrg: false,
        sortOrder: detail.sortOrder,
      } satisfies OrgOption;
    }
    return getOrgOptionApi(id);
  });
  if (result) currentOrg.value = result;
}

function open() {
  if (!props.disabled) dialogVisible.value = true;
}

function confirm(items: OrgOption[]) {
  const item = items[0];
  currentOrg.value = item;
  emit('update:modelValue', item?.id);
  emit('change', item);
}

function clear() {
  currentOrg.value = undefined;
  emit('update:modelValue', undefined);
  emit('change', undefined);
}

watch(
  () => props.modelValue,
  (value) => void loadCurrent(value),
  { immediate: true },
);

onBeforeUnmount(() => currentRequest.invalidate());

defineExpose({
  reload: () => loadCurrent(props.modelValue, true),
});
</script>

<template>
  <ElInput
    class="org-select"
    :clearable="clearable"
    :disabled="disabled"
    :model-value="displayValue"
    :placeholder="placeholder"
    readonly
    @clear="clear"
    @click="open"
  />

  <OrgSelectionDialog
    v-model="dialogVisible"
    :clearable="clearable"
    :exclude-subtree-root-id="excludeSubtreeRootId"
    :permission-scope="permissionScope"
    :selectable-types="selectableTypes"
    :selected-ids="modelValue ? [modelValue] : []"
    @confirm="confirm"
  />
</template>

<style scoped>
.org-select {
  width: 100%;
}
</style>
