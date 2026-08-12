<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { OrgOption } from '#/api/common/organization';
import type {
  UserExportParams,
  UserListItem,
  UserOrgRelationType,
  UserOrgScope,
} from '#/api/system/user';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { ArrowUpToLine, Download, Plus, Trash2 } from '@vben/icons';
import { useUserStore } from '@vben/stores';
import { downloadFileFromBlob } from '@vben/utils';

import { ElButton, ElMessage, ElOption, ElSelect, ElTag } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchDeleteUsersApi,
  changeUserStatusApi,
  deleteUserApi,
  downloadUserImportTemplateApi,
  exportUsersApi,
  importUsersApi,
  pageUsersApi,
  unlockUserLoginApi,
} from '#/api/system/user';
import EnabledStatus from '#/components/display/enabled-status.vue';
import { ExcelImportDialog } from '#/components/excel';
import { AsyncOrgTree, OrgPath } from '#/components/organization';
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

function emptyUserPage(page: number, pageSize: number) {
  return { items: [], page, pageSize, total: 0 };
}

const { hasAccessByCodes } = useAccess();
const userStore = useUserStore();
const isSuperAdmin = useIsSuperAdmin();
const canAccess = {
  add: hasAccessByCodes([USER_PERMISSION_CODES.ADD]),
  changeStatus: hasAccessByCodes([USER_PERMISSION_CODES.CHANGE_STATUS]),
  delete: hasAccessByCodes([USER_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([USER_PERMISSION_CODES.EDIT]),
  export: hasAccessByCodes([USER_PERMISSION_CODES.EXPORT]),
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
const selectedOrg = ref<OrgOption>();
const selectedOrgId = ref<string>();
const orgScope = ref<UserOrgScope>('DIRECT');
const orgRelationType = ref<UserOrgRelationType>('ALL');
const importDialog = ref<InstanceType<typeof ExcelImportDialog>>();
const exporting = ref(false);

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
    field: 'primaryOrg',
    minWidth: 260,
    slots: { default: 'primaryOrg' },
    title: '主组织',
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
    height: '100%',
    pagerConfig: {
      enabled: true,
      pageSize: 20,
    },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: UserSearchValues,
        ) => {
          const requestOrgId = selectedOrgId.value;
          const requestOrgScope = requestOrgId ? orgScope.value : undefined;
          const requestOrgRelationType = requestOrgId
            ? orgRelationType.value
            : undefined;
          const requestEnabled = values.enabled;
          const requestRealName = values.realName?.trim() || undefined;
          const requestUsername = values.username?.trim() || undefined;
          const result = await pageUsersApi({
            enabled: requestEnabled,
            orgId: requestOrgId,
            orgRelationType: requestOrgRelationType,
            orgScope: requestOrgScope,
            page: page.currentPage,
            pageSize: page.pageSize,
            realName: requestRealName,
            username: requestUsername,
          });
          const latestValues = (gridApi.formApi.getLatestSubmissionValues() ??
            {}) as UserSearchValues;
          return requestOrgId === selectedOrgId.value &&
            requestOrgScope ===
              (selectedOrgId.value ? orgScope.value : undefined) &&
            requestOrgRelationType ===
              (selectedOrgId.value ? orgRelationType.value : undefined) &&
            requestEnabled === latestValues.enabled &&
            requestRealName === (latestValues.realName?.trim() || undefined) &&
            requestUsername === (latestValues.username?.trim() || undefined)
            ? result
            : emptyUserPage(page.currentPage, page.pageSize);
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
  } as VxeTableGridOptions<UserListItem>,
});

function refresh() {
  gridApi.query();
}

function currentExportParams(): UserExportParams {
  const values = (gridApi.formApi.getLatestSubmissionValues() ??
    {}) as UserSearchValues;
  return {
    enabled: values.enabled,
    orgId: selectedOrgId.value,
    orgRelationType: selectedOrgId.value ? orgRelationType.value : undefined,
    orgScope: selectedOrgId.value ? orgScope.value : undefined,
    realName: values.realName?.trim() || undefined,
    username: values.username?.trim() || undefined,
  };
}

async function exportUsers() {
  exporting.value = true;
  try {
    const source = await exportUsersApi(currentExportParams());
    const now = new Date();
    const date = [
      now.getFullYear(),
      String(now.getMonth() + 1).padStart(2, '0'),
      String(now.getDate()).padStart(2, '0'),
    ].join('');
    downloadFileFromBlob({ fileName: `用户数据_${date}.xlsx`, source });
  } finally {
    exporting.value = false;
  }
}

function selectOrg(org?: OrgOption) {
  selectedOrg.value = org;
  selectedOrgId.value = org?.id;
  gridApi.reload();
}

function changeOrgFilter() {
  gridApi.reload();
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
    <ExcelImportDialog
      ref="importDialog"
      :download-template="downloadUserImportTemplateApi"
      :import-file="importUsersApi"
      template-filename="用户导入模板.xlsx"
      title="导入用户"
      @success="refresh"
    />
    <div class="grid h-full min-h-0 grid-cols-[280px_minmax(0,1fr)] gap-4">
      <section class="flex min-h-0 flex-col rounded-lg border bg-card">
        <header class="border-b px-4 py-3">
          <div class="font-medium">组织机构</div>
          <div class="mt-1 truncate text-xs text-muted-foreground">
            {{ selectedOrg?.fullPath || '当前显示全部用户' }}
          </div>
        </header>
        <div class="min-h-0 flex-1 p-3">
          <AsyncOrgTree
            v-model="selectedOrgId"
            all-label="全部用户"
            @select="selectOrg"
          />
        </div>
        <footer
          v-if="selectedOrgId"
          class="grid grid-cols-2 gap-2 border-t p-3"
        >
          <ElSelect
            v-model="orgScope"
            aria-label="归属范围"
            @change="changeOrgFilter"
          >
            <ElOption label="直属" value="DIRECT" />
            <ElOption label="包含下级" value="INCLUDE_DESCENDANTS" />
          </ElSelect>
          <ElSelect
            v-model="orgRelationType"
            aria-label="归属类型"
            @change="changeOrgFilter"
          >
            <ElOption label="全部归属" value="ALL" />
            <ElOption label="仅主组织" value="PRIMARY" />
            <ElOption label="仅其他组织" value="OTHER" />
          </ElSelect>
        </footer>
      </section>

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
            <ElButton v-if="canAccess.add" @click="importDialog?.open()">
              <ArrowUpToLine class="mr-1 size-4" />
              导入用户
            </ElButton>
            <ElButton
              v-if="canAccess.export"
              :loading="exporting"
              @click="exportUsers"
            >
              <Download class="mr-1 size-4" />
              导出用户
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

        <template #primaryOrg="{ row }">
          <OrgPath :full-path="row.primaryOrg.fullPath" />
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
    </div>
  </Page>
</template>
