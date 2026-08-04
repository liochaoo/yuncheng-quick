<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { RoleListItem } from '#/api/system/role';
import type { RoleType } from '#/api/system/types';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus, Trash2 } from '@vben/icons';

import { ElButton, ElMessage } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchDeleteRolesApi,
  deleteRoleApi,
  pageRolesApi,
} from '#/api/system/role';
import EnumTag from '#/components/display/enum-tag.vue';
import {
  ROLE_TYPE_SELECT_OPTIONS,
  ROLE_TYPE_TAG_OPTIONS,
} from '#/components/role/role-options';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { useIsSuperAdmin } from '#/hooks/use-super-admin';
import {
  actionColumn,
  centerColumn,
  checkboxColumn,
  dateColumn,
  textColumn,
} from '#/utils/table-columns';

import {
  NON_DELETABLE_ROLE_CODES,
  SUPER_ADMIN_ROLE_CODE,
} from '../_shared/constants';
import { roleIdentitySearchSchema } from '../_shared/search-schemas';
import { ROLE_PERMISSION_CODES } from './permission-codes';
import RoleDetailDrawer from './role-detail-drawer.vue';
import RoleFormDrawer from './role-form-drawer.vue';
import RoleUsersDrawer from './role-users-drawer.vue';

interface RoleSearchValues {
  roleCode?: string;
  roleName?: string;
  roleType?: RoleType;
}

const { hasAccessByCodes } = useAccess();
const isSuperAdmin = useIsSuperAdmin();
const canAccess = {
  add: hasAccessByCodes([ROLE_PERMISSION_CODES.ADD]),
  assignUser: hasAccessByCodes([ROLE_PERMISSION_CODES.ASSIGN_USER]),
  delete: hasAccessByCodes([ROLE_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([ROLE_PERMISSION_CODES.EDIT]),
};

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: RoleFormDrawer,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: RoleDetailDrawer,
  destroyOnClose: true,
});
const [UsersDrawer, usersDrawerApi] = useVbenDrawer({
  connectedComponent: RoleUsersDrawer,
  destroyOnClose: true,
});
const { runConfirmAction } = useConfirmAction();

const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    ...roleIdentitySearchSchema(),
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: ROLE_TYPE_SELECT_OPTIONS,
      },
      fieldName: 'roleType',
      label: '角色类型',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 xl:grid-cols-4',
};

const columns = [
  ...(canAccess.delete ? [checkboxColumn<RoleListItem>()] : []),
  textColumn<RoleListItem>({
    field: 'roleCode',
    minWidth: 160,
    title: '角色编码',
  }),
  textColumn<RoleListItem>({
    field: 'roleName',
    minWidth: 150,
    slots: { default: 'roleName' },
    title: '角色名称',
  }),
  centerColumn<RoleListItem>({
    field: 'roleType',
    slots: { default: 'roleType' },
    title: '角色类型',
    width: 110,
  }),
  centerColumn<RoleListItem>({
    field: 'userCount',
    title: '用户数量',
    width: 100,
  }),
  centerColumn<RoleListItem>({
    field: 'sortOrder',
    title: '排序号',
    width: 90,
  }),
  dateColumn<RoleListItem>({ field: 'createdAt', title: '创建时间' }),
  actionColumn<RoleListItem>({ width: 190 }),
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    columns,
    checkboxConfig: {
      checkMethod: ({ row }) =>
        canManage(row) && !NON_DELETABLE_ROLE_CODES.has(row.roleCode),
    },
    height: 'auto',
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: RoleSearchValues,
        ) =>
          pageRolesApi({
            page: page.currentPage,
            pageSize: page.pageSize,
            roleCode: values.roleCode?.trim() || undefined,
            roleName: values.roleName?.trim() || undefined,
            roleType: values.roleType,
          }),
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
  } as VxeTableGridOptions<RoleListItem>,
});

function refresh() {
  gridApi.query();
}

function openForm(mode: BusinessFormMode, row?: RoleListItem) {
  formDrawerApi.setData({ id: row?.id, mode }).open();
}

function openDetail(row: RoleListItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function canManage(row: RoleListItem) {
  return row.roleType === 'CUSTOM' || isSuperAdmin.value;
}

function canEdit(row: RoleListItem) {
  return canManage(row) && row.roleCode !== SUPER_ADMIN_ROLE_CODE;
}

function manageUsers(row: RoleListItem) {
  usersDrawerApi.setData({ roleId: row.id }).open();
}

function remove(row: RoleListItem) {
  void runConfirmAction({
    action: () => deleteRoleApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除角色【${row.roleName}】？删除前必须先移除该角色下的用户。`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, 1),
    successMessage: '删除成功',
    title: '删除角色',
  });
}

function roleRowActions(row: RoleListItem): RowAction[] {
  return [
    {
      label: '编辑',
      onClick: () => openForm('edit', row),
      visible: canAccess.edit && canEdit(row),
    },
    {
      label: '用户维护',
      onClick: () => manageUsers(row),
      visible: canAccess.assignUser && canManage(row),
    },
    {
      label: '删除',
      onClick: () => remove(row),
      type: 'danger',
      visible:
        canAccess.delete &&
        canManage(row) &&
        !NON_DELETABLE_ROLE_CODES.has(row.roleCode),
    },
  ];
}

function batchRemove() {
  const rows = gridApi.grid.getCheckboxRecords() as RoleListItem[];
  if (rows.length === 0) {
    ElMessage.warning('请选择需要删除的角色');
    return;
  }
  const protectedRole = rows.find((row) =>
    NON_DELETABLE_ROLE_CODES.has(row.roleCode),
  );
  if (protectedRole) {
    ElMessage.warning(`平台保留角色【${protectedRole.roleName}】不能删除`);
    return;
  }
  const unmanagedRole = rows.find((row) => !canManage(row));
  if (unmanagedRole) {
    ElMessage.warning(
      `系统角色【${unmanagedRole.roleName}】只能由超级管理员删除`,
    );
    return;
  }
  void runConfirmAction({
    action: () => batchDeleteRolesApi(rows.map((row) => row.id)),
    confirmButtonText: '删除',
    message: `确认删除选中的 ${rows.length} 个角色？`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, rows.length),
    successMessage: '删除成功',
    title: '批量删除角色',
  });
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <FormDrawer @success="refresh" />
    <UsersDrawer @success="refresh" />
    <Grid table-title="角色列表">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canAccess.add"
            type="primary"
            @click="openForm('create')"
          >
            <Plus class="mr-1 size-4" />
            新增角色
          </ElButton>
          <ElButton
            v-if="canAccess.delete"
            plain
            type="danger"
            @click="batchRemove"
          >
            <Trash2 class="mr-1 size-4" />
            批量删除
          </ElButton>
        </TableToolbarActions>
      </template>

      <template #roleName="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.roleName }}
        </ElButton>
      </template>

      <template #roleType="{ row }">
        <EnumTag :options="ROLE_TYPE_TAG_OPTIONS" :value="row.roleType" />
      </template>

      <template #action="{ row }">
        <RowActions :actions="roleRowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
