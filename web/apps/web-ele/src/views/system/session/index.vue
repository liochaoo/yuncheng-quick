<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type {
  OnlineSessionItem,
  OnlineSessionPageParams,
} from '#/api/system/session';
import type { RowAction } from '#/components/table/row-actions.types';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { LogOut } from '@vben/icons';

import { ElButton, ElMessage } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchKickoutOnlineSessionsApi,
  kickoutOnlineSessionApi,
  pageOnlineSessionsApi,
} from '#/api/system/session';
import EnumTag from '#/components/display/enum-tag.vue';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import {
  actionColumn,
  centerColumn,
  checkboxColumn,
  dateColumn,
  textColumn,
} from '#/utils/table-columns';

import { CLIENT_TYPE_TAG_OPTIONS } from '../_shared/display-options';
import { SESSION_PERMISSION_CODES } from './permission-codes';
import SessionDetailDrawer from './session-detail-drawer.vue';
import { SESSION_STATUS_TAG_OPTIONS } from './session-options';

defineOptions({ name: 'SystemSession' });

const { hasAccessByCodes } = useAccess();
const canKickout = hasAccessByCodes([SESSION_PERMISSION_CODES.KICKOUT]);
const { runConfirmAction } = useConfirmAction();

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: SessionDetailDrawer,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'username',
      label: '登录名',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 xl:grid-cols-4',
};

const columns = [
  ...(canKickout ? [checkboxColumn<OnlineSessionItem>()] : []),
  textColumn<OnlineSessionItem>({
    field: 'username',
    minWidth: 140,
    slots: { default: 'username' },
    title: '登录名',
  }),
  textColumn<OnlineSessionItem>({
    field: 'realName',
    minWidth: 130,
    title: '姓名',
  }),
  centerColumn<OnlineSessionItem>({
    field: 'clientType',
    slots: { default: 'clientType' },
    title: '客户端',
    width: 120,
  }),
  textColumn<OnlineSessionItem>({
    field: 'loginIp',
    minWidth: 140,
    title: '登录 IP',
  }),
  dateColumn<OnlineSessionItem>({
    field: 'loginTime',
    title: '登录时间',
  }),
  dateColumn<OnlineSessionItem>({
    field: 'expiresAt',
    title: '过期时间',
  }),
  centerColumn<OnlineSessionItem>({
    field: 'current',
    slots: { default: 'current' },
    title: '会话状态',
    width: 100,
  }),
  ...(canKickout ? [actionColumn<OnlineSessionItem>({ width: 100 })] : []),
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    checkboxConfig: {
      checkMethod: ({ row }) => !row.current,
    },
    columns,
    height: 'auto',
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: Partial<OnlineSessionPageParams>,
        ) =>
          pageOnlineSessionsApi({
            page: page.currentPage,
            pageSize: page.pageSize,
            username: values.username?.trim() || undefined,
          }),
      },
    },
    rowConfig: { keyField: 'sessionId' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<OnlineSessionItem>,
});

function openDetail(row: OnlineSessionItem) {
  detailDrawerApi.setData({ id: row.sessionId }).open();
}

function kickout(row: OnlineSessionItem) {
  void runConfirmAction({
    action: () => kickoutOnlineSessionApi(row.sessionId),
    confirmButtonText: '下线',
    message: `确认强制下线用户【${row.username}】的当前会话？`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, 1),
    successMessage: '会话已下线',
    title: '强制下线',
  });
}

function rowActions(row: OnlineSessionItem): RowAction[] {
  return [
    {
      disabled: row.current,
      label: '强制下线',
      onClick: () => kickout(row),
      type: 'danger',
    },
  ];
}

function batchKickout() {
  const rows = gridApi.grid.getCheckboxRecords() as OnlineSessionItem[];
  if (rows.length === 0) {
    ElMessage.warning('请选择需要下线的会话');
    return;
  }
  if (rows.length > 100) {
    ElMessage.warning('单次最多下线 100 个会话');
    return;
  }
  void runConfirmAction({
    action: () =>
      batchKickoutOnlineSessionsApi(rows.map((row) => row.sessionId)),
    confirmButtonText: '下线',
    message: `确认强制下线选中的 ${rows.length} 个会话？`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, rows.length),
    successMessage: '所选会话已下线',
    title: '批量强制下线',
  });
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="在线会话">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton v-if="canKickout" plain type="danger" @click="batchKickout">
            <LogOut class="mr-1 size-4" />
            批量下线
          </ElButton>
        </TableToolbarActions>
      </template>

      <template #username="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.username }}
        </ElButton>
      </template>
      <template #clientType="{ row }">
        <EnumTag :options="CLIENT_TYPE_TAG_OPTIONS" :value="row.clientType" />
      </template>
      <template #current="{ row }">
        <EnumTag :options="SESSION_STATUS_TAG_OPTIONS" :value="row.current" />
      </template>
      <template #action="{ row }">
        <RowActions :actions="rowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
