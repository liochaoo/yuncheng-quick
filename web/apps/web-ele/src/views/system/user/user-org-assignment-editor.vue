<script lang="ts" setup>
import type { OrgOption } from '#/api/common/organization';

import { computed, onBeforeUnmount, ref } from 'vue';

import { ElButton, ElTag } from 'element-plus';

import { getOrgOptionsByIdsApi } from '#/api/common/organization';
import { OrgPath, OrgSelectionDialog } from '#/components/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';

const props = defineProps<{
  modelValue: string[];
  primaryOrgId: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string[]];
  'update:primaryOrgId': [value: string];
}>();

const knownOptions = ref(new Map<string, OrgOption>());
const dialogVisible = ref(false);
const selectedRequest = useLatestRequest();

const displayedOptions = computed(() =>
  props.modelValue
    .map((id) => knownOptions.value.get(id))
    .filter((item): item is OrgOption => item !== undefined)
    .toSorted((left, right) => {
      if (left.id === props.primaryOrgId) return -1;
      if (right.id === props.primaryOrgId) return 1;
      return left.fullPath.localeCompare(right.fullPath);
    }),
);

function mergeOptions(options: OrgOption[]) {
  const next = new Map(knownOptions.value);
  for (const option of options) next.set(option.id, option);
  knownOptions.value = next;
}

async function loadSelected(ids: string[]) {
  selectedRequest.invalidate();
  if (ids.length === 0) return;
  const options = await selectedRequest.execute(() =>
    getOrgOptionsByIdsApi(ids),
  );
  if (options) mergeOptions(options);
}

function openManage() {
  dialogVisible.value = true;
}

function confirm(options: OrgOption[]) {
  mergeOptions(options);
  const ids = [
    ...props.modelValue,
    ...options.map((option) => option.id),
  ].filter((id, index, values) => values.indexOf(id) === index);
  emit('update:modelValue', ids);
  const firstOrgId = ids[0];
  if (!props.primaryOrgId && firstOrgId) {
    emit('update:primaryOrgId', firstOrgId);
  }
}

function setPrimary(orgId: string) {
  emit('update:primaryOrgId', orgId);
}

function remove(orgId: string) {
  if (orgId === props.primaryOrgId) return;
  emit(
    'update:modelValue',
    props.modelValue.filter((id) => id !== orgId),
  );
}

function clearOptions() {
  selectedRequest.invalidate();
  dialogVisible.value = false;
  knownOptions.value = new Map();
}

onBeforeUnmount(() => selectedRequest.invalidate());

defineExpose({ clearOptions, loadSelected, openManage });
</script>

<template>
  <div v-loading="selectedRequest.loading.value" class="w-full">
    <div
      v-if="displayedOptions.length > 0"
      class="overflow-hidden rounded border"
    >
      <div
        v-for="item in displayedOptions"
        :key="item.id"
        class="flex items-center gap-3 border-b px-3 py-2.5 last:border-b-0"
      >
        <OrgPath class="min-w-0 flex-1 text-sm" :full-path="item.fullPath" />

        <div class="flex shrink-0 items-center gap-2">
          <ElTag v-if="item.id === primaryOrgId" effect="dark" type="primary">
            主组织
          </ElTag>
          <template v-else>
            <ElButton link type="primary" @click="setPrimary(item.id)">
              设置为主组织
            </ElButton>
            <ElButton link type="danger" @click="remove(item.id)">
              移除
            </ElButton>
          </template>
        </div>
      </div>
    </div>
    <div
      v-else
      class="rounded border border-dashed px-4 py-2.5 text-center text-sm text-muted-foreground"
    >
      请至少选择一个归属组织
    </div>

    <OrgSelectionDialog
      v-model="dialogVisible"
      :exclude-ids="modelValue"
      multiple
      :selected-ids="[]"
      title="添加归属组织"
      @confirm="confirm"
    />
  </div>
</template>
