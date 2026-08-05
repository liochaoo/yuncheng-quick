<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type {
  OperationLogItem,
  OperationLogPageParams,
} from '#/api/system/log';
import type { RowAction } from '#/components/table/row-actions.types';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { useVbenDrawer } from '@vben/common-ui';
import { Trash2 } from '@vben/icons';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { pageOperationLogsApi } from '#/api/system/log';
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

import LogCleanDialog from './log-clean-dialog.vue';
import {
  LOG_RESULT_SELECT_OPTIONS,
  LOG_RESULT_TAG_OPTIONS,
} from './log-options';
import { formatOccurredAtBoundary } from './log-query-utils';
import OperationLogDetailDrawer from './operation-log-detail-drawer.vue';
import { LOG_PERMISSION_CODES } from './permission-codes';

interface LogCleanDialogExpose {
  open: () => Promise<void>;
}

const { hasAccessByCodes } = useAccess();
const canClean = hasAccessByCodes([LOG_PERMISSION_CODES.CLEAN]);
const cleanDialogRef = ref<LogCleanDialogExpose>();

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: OperationLogDetailDrawer,
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
      fieldName: 'action',
      label: '操作名称',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'username',
      label: '操作人',
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
        options: LOG_RESULT_SELECT_OPTIONS,
      },
      fieldName: 'success',
      label: '执行结果',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'requestPath',
      label: '请求路径',
    },
    {
      component: 'InputNumber',
      componentProps: {
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入毫秒数',
        precision: 0,
        step: 1,
        stepStrictly: true,
      },
      fieldName: 'minDurationMillis',
      label: '最低耗时',
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
  dateColumn<OperationLogItem>({ field: 'occurredAt', title: '发生时间' }),
  textColumn<OperationLogItem>({
    field: 'action',
    minWidth: 160,
    title: '操作名称',
  }),
  textColumn<OperationLogItem>({
    field: 'username',
    minWidth: 130,
    title: '操作人',
  }),
  centerColumn<OperationLogItem>({
    field: 'httpMethod',
    title: '方法',
    width: 90,
  }),
  textColumn<OperationLogItem>({
    field: 'requestPath',
    minWidth: 220,
    title: '请求路径',
  }),
  centerColumn<OperationLogItem>({
    field: 'success',
    slots: { default: 'success' },
    title: '执行结果',
    width: 90,
  }),
  centerColumn<OperationLogItem>({
    field: 'durationMillis',
    formatter: ({ cellValue }) => `${cellValue} ms`,
    title: '执行耗时',
    width: 100,
  }),
  textColumn<OperationLogItem>({
    field: 'traceId',
    minWidth: 240,
    slots: { default: 'traceId' },
    title: '链路 ID',
  }),
  actionColumn<OperationLogItem>({ width: 80 }),
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
          values: Partial<OperationLogPageParams>,
        ) =>
          pageOperationLogsApi({
            action: values.action?.trim() || undefined,
            minDurationMillis: values.minDurationMillis,
            occurredAtEnd: values.occurredAtEnd,
            occurredAtStart: values.occurredAtStart,
            page: page.currentPage,
            pageSize: page.pageSize,
            requestPath: values.requestPath?.trim() || undefined,
            success: values.success,
            traceId: values.traceId?.trim() || undefined,
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
  } as VxeTableGridOptions<OperationLogItem>,
});

function openDetail(row: OperationLogItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function rowActions(row: OperationLogItem): RowAction[] {
  return [{ label: '查看', onClick: () => openDetail(row) }];
}
</script>

<template>
  <div class="h-full min-h-0">
    <DetailDrawer />
    <LogCleanDialog
      ref="cleanDialogRef"
      type="OPERATION"
      @success="gridApi.query()"
    />
    <Grid table-title="操作日志">
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

      <template #success="{ row }">
        <EnumTag :options="LOG_RESULT_TAG_OPTIONS" :value="row.success" />
      </template>
      <template #traceId="{ row }">
        <CopyableText :value="row.traceId" />
      </template>
      <template #action="{ row }">
        <RowActions :actions="rowActions(row)" />
      </template>
    </Grid>
  </div>
</template>
