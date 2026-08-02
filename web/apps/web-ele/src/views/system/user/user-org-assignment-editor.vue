<script lang="ts" setup>
import type { OrgOption } from '#/api/common/organization';

import { computed, onBeforeUnmount, ref } from 'vue';

import { ElButton, ElEmpty, ElTag } from 'element-plus';

import { getOrgOptionsByIdsApi } from '#/api/common/organization';
import { OrgSelectionDialog, orgTypeLabel } from '#/components/organization';
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
const replacingOrgId = ref<string>();
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

const dialogMultiple = computed(() => !replacingOrgId.value);
const dialogSelectedIds = computed(() =>
  replacingOrgId.value ? [replacingOrgId.value] : props.modelValue,
);
const dialogDisabledIds = computed(() =>
  replacingOrgId.value || !props.primaryOrgId ? [] : [props.primaryOrgId],
);
const dialogExcludeIds = computed(() =>
  replacingOrgId.value
    ? props.modelValue.filter((id) => id !== replacingOrgId.value)
    : [],
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
  replacingOrgId.value = undefined;
  dialogVisible.value = true;
}

function openReplace(orgId: string) {
  replacingOrgId.value = orgId;
  dialogVisible.value = true;
}

function confirm(options: OrgOption[]) {
  mergeOptions(options);
  if (replacingOrgId.value) {
    const replacement = options[0];
    if (!replacement) return;
    const oldId = replacingOrgId.value;
    const ids = props.modelValue.map((id) =>
      id === oldId ? replacement.id : id,
    );
    emit('update:modelValue', ids);
    if (props.primaryOrgId === oldId) {
      emit('update:primaryOrgId', replacement.id);
    }
    replacingOrgId.value = undefined;
    return;
  }
  const ids = options.map((option) => option.id);
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
  replacingOrgId.value = undefined;
  knownOptions.value = new Map();
}

onBeforeUnmount(() => selectedRequest.invalidate());

defineExpose({ clearOptions, loadSelected });
</script>

<template>
  <div v-loading="selectedRequest.loading.value" class="w-full space-y-3">
    <div class="flex justify-end">
      <ElButton type="primary" plain @click="openManage">
        添加或调整归属
      </ElButton>
    </div>

    <div
      v-if="displayedOptions.length > 0"
      class="overflow-hidden rounded border"
    >
      <div
        v-for="item in displayedOptions"
        :key="item.id"
        class="grid grid-cols-[90px_minmax(130px,0.8fr)_minmax(220px,1.8fr)_auto] items-center gap-3 border-b px-3 py-2.5 last:border-b-0"
      >
        <ElTag v-if="item.id === primaryOrgId" effect="dark" type="primary">
          主归属
        </ElTag>
        <span v-else class="text-sm text-muted-foreground">其他归属</span>

        <div class="min-w-0">
          <div class="truncate">{{ item.orgName }}</div>
          <div class="text-xs text-muted-foreground">
            {{ orgTypeLabel(item.orgType) }}
          </div>
        </div>

        <div class="truncate text-sm text-muted-foreground">
          {{ item.fullPath }}
        </div>

        <div class="flex shrink-0 gap-2">
          <ElButton link type="primary" @click="openReplace(item.id)">
            更换
          </ElButton>
          <ElButton
            v-if="item.id !== primaryOrgId"
            link
            type="primary"
            @click="setPrimary(item.id)"
          >
            设为主归属
          </ElButton>
          <ElButton
            link
            type="danger"
            :disabled="item.id === primaryOrgId"
            @click="remove(item.id)"
          >
            删除
          </ElButton>
        </div>
      </div>
    </div>
    <ElEmpty v-else description="请至少选择一个组织归属" :image-size="72" />

    <OrgSelectionDialog
      v-model="dialogVisible"
      :disabled-ids="dialogDisabledIds"
      :exclude-ids="dialogExcludeIds"
      :multiple="dialogMultiple"
      :selected-ids="dialogSelectedIds"
      title="选择组织归属"
      @confirm="confirm"
    />
  </div>
</template>
