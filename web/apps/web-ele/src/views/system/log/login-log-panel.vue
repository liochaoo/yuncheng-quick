<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { LoginLogItem, LoginLogPageParams } from '#/api/system/log';
import type { RowAction } from '#/components/table/row-actions.types';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { useVbenDrawer } from '@vben/common-ui';
import { Trash2 } from '@vben/icons';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { pageLoginLogsApi } from '#/api/system/log';
import CopyableText from '#/components/display/copyable-text.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import {
  actionColumn,
  centerColumn,
  dateColumn,
  textColumn,
} from '#/utils/table-columns';

import {
  CLIENT_TYPE_SELECT_OPTIONS,
  CLIENT_TYPE_TAG_OPTIONS,
} from '../_shared/display-options';
import LogCleanDialog from './log-clean-dialog.vue';
import {
  LOG_RESULT_SELECT_OPTIONS,
  LOG_RESULT_TAG_OPTIONS,
  LOGIN_EVENT_SELECT_OPTIONS,
  LOGIN_EVENT_TAG_OPTIONS,
} from './log-options';
import { formatOccurredAtBoundary } from './log-query-utils';
import LoginLogDetailDrawer from './login-log-detail-drawer.vue';
import { LOG_PERMISSION_CODES } from './permission-codes';

interface LogCleanDialogExpose {
  open: () => Promise<void>;
}

const { hasAccessByCodes } = useAccess();
const canClean = hasAccessByCodes([LOG_PERMISSION_CODES.CLEAN]);
const cleanDialogRef = ref<LogCleanDialogExpose>();

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: LoginLogDetailDrawer,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  collapsed: true,
  collapsedRows: 1,
  fieldMappingTime: [
    [
      'occurredAtRange',
      ['occurredAtStart', 'occurredAtEnd'],
      formatOccurredAtBoundary,
    ],
  ],
  schema: [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'loginName',
      label: '登录名',
    },
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: LOG_RESULT_SELECT_OPTIONS,
      },
      fieldName: 'success',
      label: '执行结果',
    },
    {
      component: 'DatePicker',
      componentProps: {
        clearable: true,
        endPlaceholder: '结束时间',
        format: 'YYYY-MM-DD HH:mm:ss',
        rangeSeparator: '至',
        startPlaceholder: '开始时间',
        type: 'datetimerange',
      },
      fieldName: 'occurredAtRange',
      label: '发生时间',
    },
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: LOGIN_EVENT_SELECT_OPTIONS,
      },
      fieldName: 'eventType',
      label: '事件类型',
    },
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: CLIENT_TYPE_SELECT_OPTIONS,
      },
      fieldName: 'clientType',
      label: '客户端',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'traceId',
      label: '链路 ID',
    },
  ],
  showCollapseButton: true,
  submitOnEnter: true,
  wrapperClass:
    'grid-cols-1 md:grid-cols-2 xl:grid-cols-[minmax(0,0.9fr)_minmax(0,0.9fr)_minmax(0,1.35fr)_minmax(0,0.85fr)]',
};

const columns = [
  dateColumn<LoginLogItem>({ field: 'occurredAt', title: '发生时间' }),
  centerColumn<LoginLogItem>({
    field: 'eventType',
    slots: { default: 'eventType' },
    title: '事件类型',
    width: 100,
  }),
  centerColumn<LoginLogItem>({
    field: 'success',
    slots: { default: 'success' },
    title: '执行结果',
    width: 90,
  }),
  textColumn<LoginLogItem>({
    field: 'loginName',
    minWidth: 140,
    title: '登录名',
  }),
  textColumn<LoginLogItem>({
    field: 'realName',
    minWidth: 120,
    title: '姓名',
  }),
  centerColumn<LoginLogItem>({
    field: 'clientType',
    slots: { default: 'clientType' },
    title: '客户端',
    width: 120,
  }),
  textColumn<LoginLogItem>({
    field: 'ip',
    minWidth: 130,
    title: 'IP 地址',
  }),
  textColumn<LoginLogItem>({
    field: 'traceId',
    minWidth: 240,
    slots: { default: 'traceId' },
    title: '链路 ID',
  }),
  actionColumn<LoginLogItem>({ width: 80 }),
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    columns,
    height: 'auto',
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: Partial<LoginLogPageParams>,
        ) =>
          pageLoginLogsApi({
            clientType: values.clientType,
            eventType: values.eventType,
            loginName: values.loginName?.trim() || undefined,
            occurredAtEnd: values.occurredAtEnd,
            occurredAtStart: values.occurredAtStart,
            page: page.currentPage,
            pageSize: page.pageSize,
            success: values.success,
            traceId: values.traceId?.trim() || undefined,
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
  } as VxeTableGridOptions<LoginLogItem>,
});

function openDetail(row: LoginLogItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function rowActions(row: LoginLogItem): RowAction[] {
  return [{ label: '查看', onClick: () => openDetail(row) }];
}
</script>

<template>
  <div class="h-full min-h-0">
    <DetailDrawer />
    <LogCleanDialog
      ref="cleanDialogRef"
      type="LOGIN"
      @success="gridApi.query()"
    />
    <Grid table-title="登录日志">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canClean"
            plain
            type="danger"
            @click="cleanDialogRef?.open()"
          >
            <Trash2 class="mr-1 size-4" />
            清理日志
          </ElButton>
        </TableToolbarActions>
      </template>

      <template #eventType="{ row }">
        <EnumTag :options="LOGIN_EVENT_TAG_OPTIONS" :value="row.eventType" />
      </template>
      <template #success="{ row }">
        <EnumTag :options="LOG_RESULT_TAG_OPTIONS" :value="row.success" />
      </template>
      <template #traceId="{ row }">
        <CopyableText :value="row.traceId" />
      </template>
      <template #clientType="{ row }">
        <EnumTag :options="CLIENT_TYPE_TAG_OPTIONS" :value="row.clientType" />
      </template>
      <template #action="{ row }">
        <RowActions :actions="rowActions(row)" />
      </template>
    </Grid>
  </div>
</template>
