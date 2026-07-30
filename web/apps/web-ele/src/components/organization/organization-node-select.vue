<script lang="ts" setup>
import type { LoadFunction } from 'element-plus';

import type {
  OrganizationNodeOption,
  OrganizationNodeType,
} from '#/api/common/organization';

import { computed, onBeforeUnmount, ref, watch } from 'vue';

import {
  ElEmpty,
  ElInput,
  ElPagination,
  ElPopover,
  ElScrollbar,
  ElTag,
  ElTree,
} from 'element-plus';

import {
  getOrganizationNodeOptionApi,
  listOrganizationNodeOptionsApi,
  searchOrganizationNodeOptionsApi,
} from '#/api/common/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';

import { organizationNodeTypeLabel } from './organization-node-options';

interface TreeOption extends OrganizationNodeOption {
  disabled: boolean;
  label: string;
  leaf: boolean;
}

const props = withDefaults(
  defineProps<{
    clearable?: boolean;
    disabled?: boolean;
    excludeSubtreeRootId?: string;
    modelValue?: string;
    placeholder?: string;
    selectableTypes?: OrganizationNodeType[];
  }>(),
  {
    clearable: true,
    disabled: false,
    excludeSubtreeRootId: undefined,
    modelValue: undefined,
    placeholder: '请选择组织节点',
    selectableTypes: () => [],
  },
);

const emit = defineEmits<{
  change: [value?: OrganizationNodeOption];
  'update:modelValue': [value?: string];
}>();

const current = ref<OrganizationNodeOption>();
const keyword = ref('');
const page = ref(1);
const pageSize = 10;
const popoverVisible = ref(false);
const searchItems = ref<OrganizationNodeOption[]>([]);
const searchTotal = ref(0);
const currentRequest = useLatestRequest();
const searchRequest = useLatestRequest();
const searchLoading = searchRequest.loading;
const displayValue = computed(() => current.value?.fullPath ?? '');
const treeKey = computed(
  () =>
    `${props.selectableTypes.toSorted().join(',')}|${props.excludeSubtreeRootId ?? ''}`,
);
let searchTimer: ReturnType<typeof setTimeout> | undefined;

function disabledNode(node: OrganizationNodeOption) {
  if (
    props.selectableTypes.length > 0 &&
    !props.selectableTypes.includes(node.nodeType)
  ) {
    return true;
  }
  const excluded = props.excludeSubtreeRootId;
  return Boolean(
    excluded && (node.id === excluded || node.ancestorIds.includes(excluded)),
  );
}

function toTreeOption(node: OrganizationNodeOption): TreeOption {
  return {
    ...node,
    disabled: disabledNode(node),
    label: node.nodeName,
    leaf: !node.hasChildren,
  };
}

const loadTreeNode: LoadFunction = async (treeNode, resolve) => {
  try {
    const data = treeNode.data as OrganizationNodeOption | undefined;
    const parentId = treeNode.level === 0 ? undefined : data?.id;
    const items = await listOrganizationNodeOptionsApi(parentId);
    resolve(items.map((item) => toTreeOption(item)));
  } catch {
    resolve([]);
  }
};

async function loadCurrent(id?: string, force = false) {
  if (!force && id && current.value?.id === id) return;
  currentRequest.invalidate();
  current.value = undefined;
  if (!id) {
    return;
  }
  const result = await currentRequest.execute(() =>
    getOrganizationNodeOptionApi(id),
  );
  if (result) current.value = result;
}

async function search() {
  const value = keyword.value.trim();
  searchRequest.invalidate();
  if (!value) {
    searchItems.value = [];
    searchTotal.value = 0;
    return;
  }
  const result = await searchRequest.execute(() =>
    searchOrganizationNodeOptionsApi({
      keyword: value,
      page: page.value,
      pageSize,
    }),
  );
  if (!result) return;
  searchItems.value = result.items;
  searchTotal.value = result.total;
}

function choose(node: OrganizationNodeOption) {
  if (disabledNode(node)) return;
  current.value = node;
  emit('update:modelValue', node.id);
  emit('change', node);
  popoverVisible.value = false;
}

function clear() {
  current.value = undefined;
  emit('update:modelValue', undefined);
  emit('change', undefined);
}

function changePage(value: number) {
  page.value = value;
  void search();
}

function openChanged(visible: boolean) {
  if (searchTimer) clearTimeout(searchTimer);
  searchTimer = undefined;
  searchRequest.invalidate();
  if (!visible) return;
  keyword.value = '';
  page.value = 1;
  searchItems.value = [];
  searchTotal.value = 0;
}

watch(
  () => props.modelValue,
  (value) => void loadCurrent(value),
  { immediate: true },
);

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchRequest.invalidate();
  searchItems.value = [];
  searchTotal.value = 0;
  page.value = 1;
  if (!keyword.value.trim()) return;
  searchTimer = setTimeout(() => {
    searchTimer = undefined;
    void search();
  }, 250);
});

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer);
  currentRequest.invalidate();
  searchRequest.invalidate();
});

defineExpose({
  reload: () => loadCurrent(props.modelValue, true),
});
</script>

<template>
  <ElPopover
    v-model:visible="popoverVisible"
    placement="bottom-start"
    trigger="click"
    :width="680"
    @hide="openChanged(false)"
    @show="openChanged(true)"
  >
    <template #reference>
      <ElInput
        class="organization-node-select"
        :clearable="clearable"
        :disabled="disabled"
        :model-value="displayValue"
        :placeholder="placeholder"
        readonly
        @clear="clear"
      />
    </template>

    <div class="flex h-[420px] min-h-0 flex-col gap-3">
      <ElInput
        v-model="keyword"
        clearable
        placeholder="按名称、编码或完整路径搜索"
      />

      <div v-if="keyword.trim()" class="min-h-0 flex-1">
        <ElScrollbar v-loading="searchLoading" class="h-full">
          <ElEmpty v-if="searchItems.length === 0" description="暂无匹配节点" />
          <button
            v-for="item in searchItems"
            v-else
            :key="item.id"
            class="mb-1 flex w-full items-start gap-2 rounded px-3 py-2 text-left hover:bg-accent"
            :class="{ 'cursor-not-allowed opacity-50': disabledNode(item) }"
            :disabled="disabledNode(item)"
            type="button"
            @click="choose(item)"
          >
            <ElTag effect="plain" size="small">
              {{ organizationNodeTypeLabel(item.nodeType) }}
            </ElTag>
            <span class="min-w-0 flex-1">
              <span class="block font-medium">{{ item.nodeName }}</span>
              <span class="text-muted-foreground block truncate text-xs">
                {{ item.fullPath }}
              </span>
            </span>
          </button>
        </ElScrollbar>
      </div>

      <ElTree
        v-else
        :key="treeKey"
        class="min-h-0 flex-1 overflow-auto"
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
            <span>{{ data.nodeName }}</span>
            <span class="text-muted-foreground text-xs">
              {{ organizationNodeTypeLabel(data.nodeType) }}
            </span>
          </span>
        </template>
      </ElTree>

      <div
        v-if="keyword.trim() && searchTotal > pageSize"
        class="flex justify-end border-t pt-2"
      >
        <ElPagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :current-page="page"
          :total="searchTotal"
          @current-change="changePage"
        />
      </div>
    </div>
  </ElPopover>
</template>

<style scoped>
.organization-node-select {
  width: 100%;
}
</style>
