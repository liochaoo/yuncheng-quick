<script lang="ts" setup>
import type { LoadFunction } from 'element-plus';

import type { OrgOption, OrgType } from '#/api/common/organization';

import { computed, ref, watch } from 'vue';

import {
  ElButton,
  ElCheckbox,
  ElEmpty,
  ElInput,
  ElMessage,
  ElScrollbar,
  ElTag,
  ElTree,
} from 'element-plus';

import {
  getOrgOptionsByIdsApi,
  listOrgOptionsApi,
  searchOrgOptionsApi,
} from '#/api/common/organization';
import {
  getOrgDetailApi,
  listOrgChildrenApi,
  listOrgsApi,
} from '#/api/system/organization';
import PagedSelectionDialog from '#/components/select/paged-selection-dialog.vue';
import { useLatestRequest } from '#/hooks/use-latest-request';

import { orgTypeLabel } from './org-options';

interface OrgTreeOption extends OrgOption {
  disabled: boolean;
  label: string;
  leaf: boolean;
}

const props = withDefaults(
  defineProps<{
    clearable?: boolean;
    disabledIds?: string[];
    excludeIds?: string[];
    excludeSubtreeRootId?: string;
    multiple?: boolean;
    permissionScope?: 'organization-management' | 'user-management';
    selectableTypes?: OrgType[];
    selectedIds?: string[];
    title?: string;
  }>(),
  {
    clearable: true,
    disabledIds: () => [],
    excludeIds: () => [],
    excludeSubtreeRootId: undefined,
    multiple: false,
    permissionScope: 'user-management',
    selectableTypes: () => [],
    selectedIds: () => [],
    title: '选择组织',
  },
);

const emit = defineEmits<{
  confirm: [items: OrgOption[]];
}>();

const visible = defineModel<boolean>({ required: true });
const draftIds = ref<string[]>([]);
const knownOptions = ref(new Map<string, OrgOption>());
const keyword = ref('');
const searchItems = ref<OrgOption[]>([]);
const selectedRequest = useLatestRequest();
const searchRequest = useLatestRequest();
const treeKey = computed(() =>
  [
    props.selectableTypes.toSorted().join(','),
    props.excludeSubtreeRootId ?? '',
    props.excludeIds.toSorted().join(','),
    props.multiple ? 'multiple' : 'single',
  ].join('|'),
);

const selectedOptions = computed(() =>
  draftIds.value
    .map((id) => knownOptions.value.get(id))
    .filter((item): item is OrgOption => item !== undefined),
);

function mergeOptions(options: OrgOption[]) {
  const next = new Map(knownOptions.value);
  for (const option of options) next.set(option.id, option);
  knownOptions.value = next;
}

function disabledOrg(org: OrgOption) {
  if (props.disabledIds.includes(org.id)) return true;
  if (props.excludeIds.includes(org.id)) return true;
  if (
    props.selectableTypes.length > 0 &&
    !props.selectableTypes.includes(org.orgType)
  ) {
    return true;
  }
  const excludedRoot = props.excludeSubtreeRootId;
  return Boolean(
    excludedRoot &&
    (org.id === excludedRoot || org.ancestorIds.includes(excludedRoot)),
  );
}

function toTreeOption(org: OrgOption): OrgTreeOption {
  return {
    ...org,
    disabled: disabledOrg(org),
    label: org.orgName,
    leaf: !org.hasChildren,
  };
}

function detailToOption(
  detail: Awaited<ReturnType<typeof getOrgDetailApi>>,
): OrgOption {
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
  };
}

const loadTreeNode: LoadFunction = async (treeNode, resolve, reject) => {
  try {
    const data = treeNode.data as OrgOption | undefined;
    const parentId = treeNode.level === 0 ? undefined : data?.id;
    const items =
      props.permissionScope === 'organization-management'
        ? await listOrgChildrenApi(parentId)
        : await listOrgOptionsApi(parentId);
    mergeOptions(items);
    resolve(items.map((item) => toTreeOption(item)));
  } catch {
    reject();
  }
};

async function loadSelected() {
  selectedRequest.invalidate();
  if (props.selectedIds.length === 0) return;
  const items = await selectedRequest.execute(async () => {
    if (props.permissionScope === 'organization-management') {
      const details = await Promise.all(
        props.selectedIds.map((id) => getOrgDetailApi(id)),
      );
      return details.map((detail) => detailToOption(detail));
    }
    return getOrgOptionsByIdsApi(props.selectedIds);
  });
  if (items) mergeOptions(items);
}

async function search() {
  const value = keyword.value.trim();
  searchRequest.invalidate();
  if (!value) {
    searchItems.value = [];
    return;
  }
  const result = await searchRequest.execute(async () => {
    if (props.permissionScope === 'organization-management') {
      return listOrgsApi({ keyword: value });
    }
    return searchOrgOptionsApi({
      keyword: value,
    });
  });
  if (!result) return;
  mergeOptions(result);
  searchItems.value = result;
}

function resetSearch() {
  keyword.value = '';
  searchItems.value = [];
}

function selected(id: string) {
  return draftIds.value.includes(id);
}

function choose(org: OrgOption) {
  if (disabledOrg(org)) return;
  if (!props.multiple) {
    draftIds.value = [org.id];
    return;
  }
  const ids = new Set(draftIds.value);
  if (ids.has(org.id)) ids.delete(org.id);
  else ids.add(org.id);
  draftIds.value = [...ids];
}

function remove(org: OrgOption) {
  if (props.disabledIds.includes(org.id)) return;
  draftIds.value = draftIds.value.filter((id) => id !== org.id);
}

function clear() {
  draftIds.value = draftIds.value.filter((id) =>
    props.disabledIds.includes(id),
  );
}

function confirm() {
  if (selectedOptions.value.length !== draftIds.value.length) {
    ElMessage.warning('已选组织尚未完整加载，请稍后重试');
    void loadSelected();
    return;
  }
  emit('confirm', selectedOptions.value);
  visible.value = false;
}

watch(visible, (value) => {
  if (!value) {
    selectedRequest.invalidate();
    searchRequest.invalidate();
    return;
  }
  draftIds.value = props.multiple
    ? [...new Set(props.selectedIds)]
    : props.selectedIds.slice(0, 1);
  resetSearch();
  void loadSelected();
});
</script>

<template>
  <PagedSelectionDialog
    v-model="visible"
    :clearable="clearable"
    :loading="selectedRequest.loading.value"
    :selected-count="draftIds.length"
    :title="title"
    @clear="clear"
    @confirm="confirm"
  >
    <template #search>
      <div class="grid grid-cols-[minmax(0,1fr)_auto_auto] gap-2">
        <ElInput
          v-model="keyword"
          clearable
          placeholder="组织名称、编码或完整路径"
          @keyup.enter="search"
        />
        <ElButton @click="resetSearch">重置</ElButton>
        <ElButton type="primary" @click="search">查询</ElButton>
      </div>
    </template>

    <template #list>
      <div class="flex h-full min-h-0 flex-col">
        <ElScrollbar
          v-if="keyword.trim()"
          v-loading="searchRequest.loading.value"
          class="min-h-0 flex-1 p-2"
        >
          <ElEmpty v-if="searchItems.length === 0" description="暂无匹配组织" />
          <button
            v-for="item in searchItems"
            v-else
            :key="item.id"
            class="mb-1 flex w-full items-start gap-2 rounded px-3 py-2 text-left hover:bg-accent"
            :class="{ 'cursor-not-allowed opacity-50': disabledOrg(item) }"
            :disabled="disabledOrg(item)"
            type="button"
            @click="choose(item)"
          >
            <ElCheckbox
              v-if="multiple"
              :disabled="disabledOrg(item)"
              :model-value="selected(item.id)"
              @click.stop
              @change="choose(item)"
            />
            <span
              v-else
              class="mt-1 size-3 rounded-full border"
              :class="{ 'border-primary bg-primary': selected(item.id) }"
            ></span>
            <ElTag effect="plain" size="small">
              {{ orgTypeLabel(item.orgType) }}
            </ElTag>
            <span class="min-w-0 flex-1">
              <span class="block font-medium">{{ item.orgName }}</span>
              <span class="block truncate text-xs text-muted-foreground">
                {{ item.fullPath }}
              </span>
            </span>
          </button>
        </ElScrollbar>

        <ElTree
          v-else
          :key="treeKey"
          class="min-h-0 flex-1 overflow-auto p-2"
          lazy
          node-key="id"
          :load="loadTreeNode"
          :props="{
            disabled: 'disabled',
            isLeaf: 'leaf',
            label: 'label',
          }"
          @node-click="choose"
        >
          <template #default="{ data }">
            <span class="flex min-w-0 items-center gap-2">
              <ElCheckbox
                v-if="multiple"
                :disabled="disabledOrg(data)"
                :model-value="selected(data.id)"
                @click.stop
                @change="choose(data)"
              />
              <span>{{ data.orgName }}</span>
              <span class="text-xs text-muted-foreground">
                {{ orgTypeLabel(data.orgType) }}
              </span>
            </span>
          </template>
        </ElTree>
      </div>
    </template>

    <template #selected>
      <div v-if="selectedOptions.length > 0" class="space-y-2">
        <div
          v-for="item in selectedOptions"
          :key="item.id"
          class="flex items-start justify-between gap-3 rounded-md border px-3 py-2"
        >
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <span class="truncate text-sm">{{ item.orgName }}</span>
              <ElTag effect="plain" size="small">
                {{ orgTypeLabel(item.orgType) }}
              </ElTag>
            </div>
            <div class="truncate text-xs text-muted-foreground">
              {{ item.fullPath }}
            </div>
          </div>
          <ElButton
            v-if="!disabledIds.includes(item.id)"
            link
            type="danger"
            @click="remove(item)"
          >
            移除
          </ElButton>
          <span v-else class="text-xs text-muted-foreground">不可移除</span>
        </div>
      </div>
      <ElEmpty v-else description="暂未选择组织" :image-size="72" />
    </template>
  </PagedSelectionDialog>
</template>
