<script lang="ts" setup>
import type { LoadFunction } from 'element-plus';

import type { OrgOption } from '#/api/common/organization';

import { onBeforeUnmount, ref, watch } from 'vue';

import { ElEmpty, ElInput, ElScrollbar, ElTree } from 'element-plus';

import {
  listOrgOptionsApi,
  searchOrgOptionsApi,
} from '#/api/common/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';

import OrgPath from './org-path.vue';
import OrgTypeIcon from './org-type-icon.vue';

interface OrgTreeOption extends OrgOption {
  label: string;
  leaf: boolean;
}

withDefaults(
  defineProps<{
    allLabel?: string;
    modelValue?: string;
    showAll?: boolean;
  }>(),
  {
    allLabel: '全部用户',
    modelValue: undefined,
    showAll: true,
  },
);

const emit = defineEmits<{
  select: [value?: OrgOption];
  'update:modelValue': [value?: string];
}>();

const keyword = ref('');
const searchItems = ref<OrgOption[]>([]);
const searchRequest = useLatestRequest();
let searchTimer: ReturnType<typeof setTimeout> | undefined;

function toTreeOption(org: OrgOption): OrgTreeOption {
  return {
    ...org,
    label: org.orgName,
    leaf: !org.hasChildren,
  };
}

const loadTreeNode: LoadFunction = async (treeNode, resolve, reject) => {
  try {
    const data = treeNode.data as OrgOption | undefined;
    const parentId = treeNode.level === 0 ? undefined : data?.id;
    const items = await listOrgOptionsApi(parentId);
    resolve(items.map((item) => toTreeOption(item)));
  } catch {
    reject();
  }
};

function select(org?: OrgOption) {
  emit('update:modelValue', org?.id);
  emit('select', org);
}

async function search() {
  const value = keyword.value.trim();
  searchRequest.invalidate();
  if (!value) {
    searchItems.value = [];
    return;
  }
  const result = await searchRequest.execute(() =>
    searchOrgOptionsApi({
      keyword: value,
    }),
  );
  if (!result) return;
  searchItems.value = result;
}

watch(keyword, () => {
  if (searchTimer) clearTimeout(searchTimer);
  searchRequest.invalidate();
  searchItems.value = [];
  if (!keyword.value.trim()) return;
  searchTimer = setTimeout(() => {
    searchTimer = undefined;
    void search();
  }, 250);
});

onBeforeUnmount(() => {
  if (searchTimer) clearTimeout(searchTimer);
  searchRequest.invalidate();
});
</script>

<template>
  <div class="flex h-full min-h-0 flex-col gap-3">
    <ElInput v-model="keyword" clearable placeholder="搜索组织名称或编码" />

    <button
      v-if="showAll"
      class="rounded px-3 py-2 text-left text-sm hover:bg-accent"
      :class="{ 'bg-accent font-medium text-primary': !modelValue }"
      type="button"
      @click="select()"
    >
      {{ allLabel }}
    </button>

    <div class="min-h-0 flex-1">
      <ElScrollbar
        v-if="keyword.trim()"
        v-loading="searchRequest.loading.value"
        class="h-full"
      >
        <ElEmpty
          v-if="searchItems.length === 0"
          description="暂无匹配组织"
          :image-size="72"
        />
        <button
          v-for="item in searchItems"
          v-else
          :key="item.id"
          class="mb-1 w-full rounded px-3 py-2 text-left hover:bg-accent"
          :class="{
            'bg-accent text-primary': modelValue === item.id,
          }"
          type="button"
          @click="select(item)"
        >
          <span class="flex items-center gap-2 text-sm">
            <OrgTypeIcon :type="item.orgType" />
            <span class="truncate">{{ item.orgName }}</span>
          </span>
          <OrgPath
            class="pl-6 text-xs text-muted-foreground"
            :full-path="item.fullPath"
          />
        </button>
      </ElScrollbar>

      <ElTree
        v-else
        class="h-full overflow-auto"
        :expand-on-click-node="false"
        highlight-current
        lazy
        node-key="id"
        :current-node-key="modelValue"
        :load="loadTreeNode"
        :props="{ isLeaf: 'leaf', label: 'label' }"
        @node-click="select"
      >
        <template #default="{ data }">
          <span class="flex min-w-0 items-center gap-2">
            <OrgTypeIcon :type="data.orgType" />
            <span class="truncate">{{ data.orgName }}</span>
          </span>
        </template>
      </ElTree>
    </div>
  </div>
</template>
