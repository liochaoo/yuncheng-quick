<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { RoleUserListItem } from '#/api/system/role';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import { pageRoleCandidateUsersApi, pageRoleUsersApi } from '#/api/system/role';
import EnabledStatus from '#/components/display/enabled-status.vue';
import {
  actionColumn,
  centerColumn,
  checkboxColumn,
  textColumn,
} from '#/utils/table-columns';

import { userIdentitySearchSchema } from '../_shared/search-schemas';

interface SearchValues {
  realName?: string;
  username?: string;
}

const props = defineProps<{
  mode: 'candidates' | 'users';
  roleId?: string;
}>();

const formOptions: VbenFormProps = {
  collapsed: false,
  schema: userIdentitySearchSchema(),
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 xl:grid-cols-3',
};

const columns = [
  checkboxColumn<RoleUserListItem>(),
  textColumn<RoleUserListItem>({
    field: 'username',
    minWidth: 130,
    title: '登录名',
  }),
  textColumn<RoleUserListItem>({
    field: 'realName',
    minWidth: 120,
    title: '姓名',
  }),
  textColumn<RoleUserListItem>({
    field: 'roles',
    formatter: ({ row }) =>
      row.roles.map((item) => item.roleName).join('、') || '-',
    minWidth: 180,
    title: '当前角色',
  }),
  centerColumn<RoleUserListItem>({
    field: 'enabled',
    slots: { default: 'enabled' },
    title: '状态',
    width: 90,
  }),
  ...(props.mode === 'users'
    ? [actionColumn<RoleUserListItem>({ width: 90 })]
    : []),
];

const [Grid, gridApi] = useVbenVxeGrid({
  class: 'role-user-grid',
  formOptions,
  gridClass: 'role-user-grid__table',
  gridOptions: {
    columns,
    height: '100%',
    pagerConfig: { enabled: true, pageSize: 10 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: SearchValues,
        ) => {
          if (!props.roleId) {
            return { items: [], page: 1, pageSize: page.pageSize, total: 0 };
          }
          const params = {
            page: page.currentPage,
            pageSize: page.pageSize,
            realName: values.realName?.trim() || undefined,
            username: values.username?.trim() || undefined,
          };
          return props.mode === 'users'
            ? pageRoleUsersApi(props.roleId, params)
            : pageRoleCandidateUsersApi(props.roleId, params);
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<RoleUserListItem>,
  separator: false,
});

function query() {
  return gridApi.query();
}

function selectedRows() {
  return gridApi.grid.getCheckboxRecords() as RoleUserListItem[];
}

function refreshAfterRemove(removedCount: number) {
  return refreshAfterRowsRemoved(gridApi, removedCount);
}

defineExpose({ query, refreshAfterRemove, selectedRows });
</script>

<template>
  <Grid>
    <template #toolbar-tools>
      <slot name="toolbar-tools"></slot>
    </template>
    <template #enabled="{ row }">
      <EnabledStatus :model-value="row.enabled" />
    </template>
    <template #action="{ row }">
      <slot name="action" :row="row"></slot>
    </template>
  </Grid>
</template>

<style scoped>
.role-user-grid {
  height: 100%;
}

:deep(.role-user-grid__table) {
  height: 100% !important;
}
</style>
