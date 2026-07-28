<script lang="ts" setup>
import type { RoleOption } from '#/api/system/types';

import { onBeforeUnmount, onMounted, reactive, ref } from 'vue';

import {
  ElButton,
  ElEmpty,
  ElInput,
  ElPagination,
  ElScrollbar,
} from 'element-plus';

import { pageRoleOptionsApi } from '#/api/system/role';
import { useLatestRequest } from '#/hooks/use-latest-request';

const props = defineProps<{
  activeRoleId?: string;
}>();

const emit = defineEmits<{
  select: [role?: RoleOption];
}>();

const keyword = ref('');
const roles = ref<RoleOption[]>([]);
const pageState = reactive({ page: 1, pageSize: 10, total: 0 });
const roleRequest = useLatestRequest();
const loading = roleRequest.loading;

async function loadRoles(preserveSelection = true) {
  try {
    const result = await roleRequest.execute(() =>
      pageRoleOptionsApi({
        keyword: keyword.value.trim() || undefined,
        page: pageState.page,
        pageSize: pageState.pageSize,
      }),
    );
    if (!result) return;
    roles.value = result.items;
    pageState.total = result.total;

    const currentRole = preserveSelection
      ? result.items.find((role) => role.id === props.activeRoleId)
      : undefined;
    emit('select', currentRole ?? result.items[0]);
  } catch {
    roles.value = [];
    pageState.total = 0;
    emit('select');
  }
}

function search() {
  pageState.page = 1;
  void loadRoles(false);
}

function resetSearch() {
  keyword.value = '';
  search();
}

function changePage(page: number) {
  pageState.page = page;
  void loadRoles(false);
}

onMounted(() => void loadRoles(false));
onBeforeUnmount(roleRequest.invalidate);
</script>

<template>
  <section class="flex h-full min-h-0 flex-col rounded-lg border bg-card">
    <header class="border-b p-4">
      <div class="mb-3 text-sm font-medium">角色列表</div>
      <ElInput
        v-model="keyword"
        clearable
        placeholder="搜索角色名称或编码"
        @clear="resetSearch"
        @keyup.enter="search"
      >
        <template #append>
          <ElButton @click="search">查询</ElButton>
        </template>
      </ElInput>
    </header>

    <div v-loading="loading" class="min-h-0 flex-1 p-2">
      <ElScrollbar class="h-full">
        <button
          v-for="role in roles"
          :key="role.id"
          class="mb-1 flex w-full items-center rounded-md border border-transparent px-3 py-2 text-left text-sm transition-colors hover:bg-accent"
          :class="{
            'border-primary/40 bg-primary/15 text-accent-foreground shadow-sm':
              role.id === activeRoleId,
          }"
          type="button"
          @click="emit('select', role)"
        >
          <span class="min-w-0">
            <span class="block truncate font-medium">{{ role.roleName }}</span>
            <span class="block truncate text-xs text-muted-foreground">
              {{ role.roleCode }}
            </span>
          </span>
        </button>
        <ElEmpty
          v-if="!loading && roles.length === 0"
          description="暂无角色"
          :image-size="72"
        />
      </ElScrollbar>
    </div>

    <footer class="border-t px-2 py-3">
      <ElPagination
        v-model:current-page="pageState.page"
        class="justify-center"
        layout="prev, pager, next"
        :pager-count="5"
        :page-size="pageState.pageSize"
        size="small"
        :total="pageState.total"
        @current-change="changePage"
      />
      <div class="mt-1 text-center text-xs text-muted-foreground">
        共 {{ pageState.total }} 个角色
      </div>
    </footer>
  </section>
</template>
