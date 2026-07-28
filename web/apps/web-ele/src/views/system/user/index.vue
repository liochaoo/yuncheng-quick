<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { UserListItem } from '#/api/system/user';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus, Trash2 } from '@vben/icons';
import { useUserStore } from '@vben/stores';

import { ElButton, ElMessage, ElTag } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchDeleteUsersApi,
  changeUserStatusApi,
  deleteUserApi,
  pageUsersApi,
  unlockUserLoginApi,
} from '#/api/system/user';
import EnabledStatus from '#/components/display/enabled-status.vue';
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

import { userIdentitySearchSchema } from '../_shared/search-schemas';
import { USER_PERMISSION_CODES } from './permission-codes';
import ResetPasswordDrawer from './reset-password-drawer.vue';
import UserDetailDrawer from './user-detail-drawer.vue';
import UserFormDrawer from './user-form-drawer.vue';

interface UserSearchValues {
  enabled?: boolean;
  realName?: string;
  username?: string;
}

const { hasAccessByCodes } = useAccess();
const userStore = useUserStore();
const isSuperAdmin = useIsSuperAdmin();
const canAccess = {
  add: hasAccessByCodes([USER_PERMISSION_CODES.ADD]),
  changeStatus: hasAccessByCodes([USER_PERMISSION_CODES.CHANGE_STATUS]),
  delete: hasAccessByCodes([USER_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([USER_PERMISSION_CODES.EDIT]),
  resetPassword: hasAccessByCodes([USER_PERMISSION_CODES.RESET_PASSWORD]),
  unlock: hasAccessByCodes([USER_PERMISSION_CODES.UNLOCK]),
};

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: UserFormDrawer,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: UserDetailDrawer,
  destroyOnClose: true,
});
const [PasswordDrawer, passwordDrawerApi] = useVbenDrawer({
  connectedComponent: ResetPasswordDrawer,
  destroyOnClose: true,
});
const { runConfirmAction } = useConfirmAction();

const formOptions: VbenFormProps = {
  collapsed: false,
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  schema: [
    ...userIdentitySearchSchema(),
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [
          { label: '启用', value: true },
          { label: '停用', value: false },
        ],
      },
      fieldName: 'enabled',
      label: '启用状态',
    },
  ],
};

const columns = [
  ...(canAccess.delete ? [checkboxColumn<UserListItem>()] : []),
  textColumn<UserListItem>({
    field: 'username',
    minWidth: 140,
    slots: { default: 'username' },
    title: '登录名',
  }),
  textColumn<UserListItem>({
    field: 'realName',
    minWidth: 120,
    title: '姓名',
  }),
  textColumn<UserListItem>({
    field: 'phone',
    minWidth: 140,
    title: '手机号码',
  }),
  textColumn<UserListItem>({
    field: 'email',
    minWidth: 200,
    title: '电子邮箱',
  }),
  textColumn<UserListItem>({
    field: 'roles',
    formatter: ({ row }) =>
      row.roles.map((role) => role.roleName).join('、') || '-',
    minWidth: 200,
    title: '角色',
  }),
  centerColumn<UserListItem>({
    field: 'enabled',
    slots: { default: 'enabled' },
    title: '启用状态',
    width: 100,
  }),
  centerColumn<UserListItem>({
    field: 'loginLocked',
    slots: { default: 'loginLocked' },
    title: '锁定状态',
    width: 110,
  }),
  centerColumn<UserListItem>({
    field: 'sortOrder',
    title: '排序号',
    width: 90,
  }),
  dateColumn<UserListItem>({ field: 'createdAt', title: '创建时间' }),
  actionColumn<UserListItem>(),
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    columns,
    height: 'auto',
    pagerConfig: {
      enabled: true,
      pageSize: 20,
    },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: UserSearchValues,
        ) =>
          pageUsersApi({
            enabled: values.enabled,
            page: page.currentPage,
            pageSize: page.pageSize,
            realName: values.realName?.trim() || undefined,
            username: values.username?.trim() || undefined,
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
  } as VxeTableGridOptions<UserListItem>,
});

function refresh() {
  gridApi.query();
}

function openForm(mode: BusinessFormMode, row?: UserListItem) {
  formDrawerApi.setData({ id: row?.id, mode }).open();
}

function openDetail(row: UserListItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function openResetPassword(row: UserListItem) {
  passwordDrawerApi.setData({ id: row.id }).open();
}

function selectedRows() {
  return gridApi.grid.getCheckboxRecords() as UserListItem[];
}

function canManageUser(row: UserListItem) {
  return (
    isSuperAdmin.value || row.roles.every((role) => role.roleType === 'CUSTOM')
  );
}

async function changeStatus(row: UserListItem, enabled: boolean) {
  await runConfirmAction({
    action: () => changeUserStatusApi(row.id, enabled),
    message: `确认${enabled ? '启用' : '停用'}用户【${row.realName}】？`,
    onSuccess: refresh,
    successMessage: `${enabled ? '启用' : '停用'}成功`,
    title: `${enabled ? '启用' : '停用'}用户`,
  });
}

function remove(row: UserListItem) {
  void runConfirmAction({
    action: () => deleteUserApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除用户【${row.realName}】？`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, 1),
    successMessage: '删除成功',
    title: '删除用户',
  });
}

function unlock(row: UserListItem) {
  void runConfirmAction({
    action: () => unlockUserLoginApi(row.id),
    message: `确认解除用户【${row.realName}】的登录锁定？`,
    onSuccess: refresh,
    successMessage: '登录锁定已解除',
    title: '解除登录锁定',
  });
}

function userRowActions(row: UserListItem): RowAction[] {
  return [
    {
      label: '编辑',
      onClick: () => openForm('edit', row),
      visible: canAccess.edit && canManageUser(row),
    },
    {
      label: '重置密码',
      onClick: () => openResetPassword(row),
      visible: canAccess.resetPassword && canManageUser(row),
    },
    {
      label: '解除锁定',
      onClick: () => unlock(row),
      visible: canAccess.unlock && row.loginLocked && canManageUser(row),
    },
    {
      disabled: row.id === userStore.userInfo?.userId,
      label: '删除',
      onClick: () => remove(row),
      type: 'danger',
      visible: canAccess.delete && canManageUser(row),
    },
  ];
}

function batchRemove() {
  const rows = selectedRows();
  if (rows.length === 0) {
    ElMessage.warning('请选择需要删除的用户');
    return;
  }
  const currentUser = rows.find((row) => row.id === userStore.userInfo?.userId);
  if (currentUser) {
    ElMessage.warning('不能删除当前登录用户');
    return;
  }
  const protectedUser = rows.find((row) => !canManageUser(row));
  if (protectedUser) {
    ElMessage.warning(
      `用户【${protectedUser.realName}】包含系统角色，不能删除`,
    );
    return;
  }
  void runConfirmAction({
    action: () => batchDeleteUsersApi(rows.map((row) => row.id)),
    confirmButtonText: '删除',
    message: `确认删除选中的 ${rows.length} 个用户？`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, rows.length),
    successMessage: '删除成功',
    title: '批量删除用户',
  });
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <FormDrawer @success="refresh" />
    <PasswordDrawer @success="refresh" />
    <Grid table-title="用户列表">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canAccess.add"
            type="primary"
            @click="openForm('create')"
          >
            <Plus class="mr-1 size-4" />
            新增用户
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

      <template #username="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.username }}
        </ElButton>
      </template>

      <template #enabled="{ row }">
        <EnabledStatus
          :editable="canAccess.changeStatus"
          :disabled="
            row.id === userStore.userInfo?.userId || !canManageUser(row)
          "
          :model-value="row.enabled"
          @change="(value) => changeStatus(row, value)"
        />
      </template>

      <template #loginLocked="{ row }">
        <ElTag :type="row.loginLocked ? 'warning' : 'success'">
          {{ row.loginLocked ? '临时锁定' : '正常' }}
        </ElTag>
      </template>

      <template #action="{ row }">
        <RowActions :actions="userRowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
