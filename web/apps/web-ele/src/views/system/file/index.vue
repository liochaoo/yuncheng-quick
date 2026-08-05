<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { FileApi } from '#/api/core';
import type { FileListItem } from '#/api/system/file';
import type { RowAction } from '#/components/table/row-actions.types';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus, Trash2 } from '@vben/icons';

import { ElButton, ElMessage } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  batchDeleteSystemFilesApi,
  deleteSystemFileApi,
  pageFilesApi,
} from '#/api/system/file';
import EnumTag from '#/components/display/enum-tag.vue';
import {
  downloadFileRecord,
  formatFileSize,
  isPreviewableFile,
  previewFileRecord,
} from '#/components/file/file-display';
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

import FileDetailDrawer from './file-detail-drawer.vue';
import {
  FILE_ACCESS_SELECT_OPTIONS,
  FILE_ACCESS_TAG_OPTIONS,
  FILE_POLICY_OPTIONS,
  filePolicyLabel,
} from './file-options';
import FileUploadDrawer from './file-upload-drawer.vue';
import { FILE_PERMISSION_CODES } from './permission-codes';

interface FileSearchValues {
  accessType?: FileApi.AccessType;
  originalName?: string;
  policyCode?: string;
  storagePlatform?: string;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  delete: hasAccessByCodes([FILE_PERMISSION_CODES.DELETE]),
  download: hasAccessByCodes([FILE_PERMISSION_CODES.DOWNLOAD]),
  preview: hasAccessByCodes([FILE_PERMISSION_CODES.PREVIEW]),
  upload: hasAccessByCodes([FILE_PERMISSION_CODES.UPLOAD]),
};
const hasRowActions =
  canAccess.delete || canAccess.download || canAccess.preview;
const { runConfirmAction } = useConfirmAction();

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: FileDetailDrawer,
  destroyOnClose: true,
});
const [UploadDrawer, uploadDrawerApi] = useVbenDrawer({
  connectedComponent: FileUploadDrawer,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  collapsed: true,
  collapsedRows: 1,
  schema: [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'originalName',
      label: '文件名称',
    },
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: FILE_POLICY_OPTIONS,
      },
      fieldName: 'policyCode',
      label: '文件策略',
    },
    {
      component: 'Select',
      componentProps: {
        clearable: true,
        options: FILE_ACCESS_SELECT_OPTIONS,
      },
      fieldName: 'accessType',
      label: '访问类型',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'storagePlatform',
      label: '存储平台',
    },
  ],
  showCollapseButton: true,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-2 xl:grid-cols-4',
};

const columns = [
  ...(canAccess.delete ? [checkboxColumn<FileListItem>()] : []),
  textColumn<FileListItem>({
    field: 'originalName',
    minWidth: 220,
    slots: { default: 'originalName' },
    title: '文件名称',
  }),
  textColumn<FileListItem>({
    field: 'contentType',
    minWidth: 180,
    title: '内容类型',
  }),
  centerColumn<FileListItem>({
    field: 'fileSize',
    formatter: ({ cellValue }) => formatFileSize(Number(cellValue)),
    title: '文件大小',
    width: 110,
  }),
  textColumn<FileListItem>({
    field: 'storagePlatform',
    minWidth: 130,
    title: '存储平台',
  }),
  centerColumn<FileListItem>({
    field: 'policyCode',
    formatter: ({ cellValue }) => filePolicyLabel(String(cellValue)),
    title: '文件策略',
    width: 110,
  }),
  centerColumn<FileListItem>({
    field: 'accessType',
    slots: { default: 'accessType' },
    title: '访问类型',
    width: 90,
  }),
  textColumn<FileListItem>({
    field: 'businessType',
    minWidth: 130,
    title: '业务类型',
  }),
  textColumn<FileListItem>({
    field: 'businessPosition',
    minWidth: 120,
    title: '业务位置',
  }),
  dateColumn<FileListItem>({ field: 'createdAt', title: '上传时间' }),
  ...(hasRowActions ? [actionColumn<FileListItem>({ width: 170 })] : []),
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
          values: FileSearchValues,
        ) =>
          pageFilesApi({
            accessType: values.accessType,
            originalName: values.originalName?.trim() || undefined,
            page: page.currentPage,
            pageSize: page.pageSize,
            policyCode: values.policyCode,
            storagePlatform: values.storagePlatform?.trim() || undefined,
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
  } as VxeTableGridOptions<FileListItem>,
});

function refresh() {
  gridApi.query();
}

function openDetail(row: FileListItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

async function preview(row: FileListItem) {
  try {
    await previewFileRecord(row);
  } catch {
    // 请求客户端已统一提示错误。
  }
}

async function download(row: FileListItem) {
  try {
    await downloadFileRecord(row);
  } catch {
    // 请求客户端已统一提示错误。
  }
}

function remove(row: FileListItem) {
  void runConfirmAction({
    action: () => deleteSystemFileApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除文件【${row.originalName}】？文件记录和物理文件将同时删除。`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, 1),
    successMessage: '删除成功',
    title: '删除文件',
  });
}

function fileRowActions(row: FileListItem): RowAction[] {
  return [
    {
      label: '预览',
      onClick: () => void preview(row),
      visible: canAccess.preview && isPreviewableFile(row),
    },
    {
      label: '下载',
      onClick: () => void download(row),
      visible: canAccess.download,
    },
    {
      label: '删除',
      onClick: () => remove(row),
      type: 'danger',
      visible: canAccess.delete,
    },
  ];
}

function batchRemove() {
  const rows = gridApi.grid.getCheckboxRecords() as FileListItem[];
  if (rows.length === 0) {
    ElMessage.warning('请选择需要删除的文件');
    return;
  }
  void runConfirmAction({
    action: () => batchDeleteSystemFilesApi(rows.map((row) => row.id)),
    confirmButtonText: '删除',
    message: `确认删除选中的 ${rows.length} 个文件？文件记录和物理文件将同时删除。`,
    onSuccess: () => refreshAfterRowsRemoved(gridApi, rows.length),
    successMessage: '删除成功',
    title: '批量删除文件',
  });
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <UploadDrawer @success="refresh" />
    <Grid table-title="文件列表">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canAccess.upload"
            type="primary"
            @click="uploadDrawerApi.open()"
          >
            <Plus class="mr-1 size-4" />
            上传文件
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

      <template #originalName="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.originalName }}
        </ElButton>
      </template>

      <template #accessType="{ row }">
        <EnumTag :options="FILE_ACCESS_TAG_OPTIONS" :value="row.accessType" />
      </template>

      <template #action="{ row }">
        <RowActions :actions="fileRowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
