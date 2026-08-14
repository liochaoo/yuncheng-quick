<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { OrgOption } from '#/api/system/organization';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { ArrowUpToLine, Download, Plus } from '@vben/icons';
import { downloadFileFromBlob } from '@vben/utils';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOrgApi,
  downloadOrgImportTemplateApi,
  exportOrgsApi,
  importOrgsApi,
  listOrgChildrenApi,
  listOrgsApi,
} from '#/api/system/organization';
import EnumTag from '#/components/display/enum-tag.vue';
import { ExcelImportDialog } from '#/components/excel';
import {
  ORG_TYPE_OPTIONS,
  OrgPath,
  OrgTypeIcon,
} from '#/components/organization';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { actionColumn, centerColumn, textColumn } from '#/utils/table-columns';

import OrgDetailDrawer from './org-detail-drawer.vue';
import OrgFormDrawer from './org-form-drawer.vue';
import OrgMoveDrawer from './org-move-drawer.vue';
import { defaultChildType } from './org-type-rules';
import { ORG_PERMISSION_CODES } from './permission-codes';

interface OrgSearchValues {
  orgCode?: string;
  orgName?: string;
}

interface OrgFormSuccess {
  id: string;
  mode: 'create' | 'edit';
  nameChanged: boolean;
  orgCode: string;
  orgName: string;
  parentId?: string;
  sortChanged: boolean;
  sortOrder: number;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  add: hasAccessByCodes([ORG_PERMISSION_CODES.ADD]),
  delete: hasAccessByCodes([ORG_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([ORG_PERMISSION_CODES.EDIT]),
  export: hasAccessByCodes([ORG_PERMISSION_CODES.EXPORT]),
  move: hasAccessByCodes([ORG_PERMISSION_CODES.MOVE]),
};
const { runConfirmAction } = useConfirmAction();
const importDialog = ref<InstanceType<typeof ExcelImportDialog>>();
const exporting = ref(false);

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: OrgFormDrawer,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: OrgDetailDrawer,
  destroyOnClose: true,
});
const [MoveDrawer, moveDrawerApi] = useVbenDrawer({
  connectedComponent: OrgMoveDrawer,
  destroyOnClose: true,
});

const formOptions: VbenFormProps = {
  actionLayout: 'inline',
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入组织名称',
      },
      fieldName: 'orgName',
      label: '组织名称',
    },
    {
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入组织编码',
      },
      fieldName: 'orgCode',
      label: '组织编码',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-[minmax(0,360px)_minmax(0,360px)_auto]',
};

const columns = [
  textColumn<OrgOption>({
    field: 'orgName',
    minWidth: 220,
    slots: { default: 'orgName' },
    title: '组织名称',
    treeNode: true,
  }),
  centerColumn<OrgOption>({
    field: 'orgType',
    slots: { default: 'orgType' },
    title: '类型',
    width: 90,
  }),
  textColumn<OrgOption>({
    field: 'orgCode',
    minWidth: 150,
    title: '组织编码',
  }),
  textColumn<OrgOption>({
    field: 'fullPath',
    minWidth: 320,
    slots: { default: 'fullPath' },
    title: '完整路径',
  }),
  centerColumn<OrgOption>({
    field: 'sortOrder',
    title: '排序号',
    width: 90,
  }),
  actionColumn<OrgOption>({ width: 230 }),
];

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    columns,
    height: 'auto',
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async (_params: unknown, values: OrgSearchValues) => {
          const orgCode = values.orgCode?.trim() || undefined;
          const orgName = values.orgName?.trim() || undefined;
          const items = await listOrgsApi({
            orgCode,
            orgName,
          });
          const latestValues = (gridApi.formApi.getLatestSubmissionValues() ??
            {}) as OrgSearchValues;
          const latestOrgCode = latestValues.orgCode?.trim() || undefined;
          const latestOrgName = latestValues.orgName?.trim() || undefined;
          if (orgCode !== latestOrgCode || orgName !== latestOrgName) return [];
          return orgCode || orgName
            ? items.map((item) => ({ ...item, hasChildren: false }))
            : items;
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
    treeConfig: {
      childrenField: 'children',
      hasChildField: 'hasChildren',
      lazy: true,
      loadMethod: async ({ row }: { row: OrgOption }) =>
        listOrgChildrenApi(row.id),
      parentField: 'parentId',
      reserve: true,
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions<OrgOption>,
});

function expandedRows() {
  return gridApi.grid.getTreeExpandRecords() as OrgOption[];
}

async function refreshRootsPreservingExpansion() {
  const expanded = expandedRows()
    .map((row) => ({ depth: row.depth, id: row.id }))
    .toSorted((left, right) => left.depth - right.depth);
  await gridApi.grid.clearTreeExpandReserve();
  await gridApi.query();
  for (const item of expanded) {
    const row = gridApi.grid.getRowById(item.id) as null | OrgOption;
    if (row?.hasChildren) {
      await gridApi.grid.reloadTreeExpand(row);
      await gridApi.grid.setTreeExpand(row, true);
    }
  }
}

async function reloadParent(parentId?: string) {
  if (!parentId) {
    await refreshRootsPreservingExpansion();
    return;
  }
  const parent = gridApi.grid.getRowById(parentId) as null | OrgOption;
  if (!parent) {
    await refreshRootsPreservingExpansion();
    return;
  }
  const children = await listOrgChildrenApi(parentId);
  parent.hasChildren = children.length > 0;
  if (parent.hasChildren) {
    await gridApi.grid.reloadTreeExpand(parent);
    await gridApi.grid.setTreeExpand(parent, true);
  } else {
    await gridApi.grid.clearTreeExpandLoaded(parent);
    await gridApi.grid.setTreeExpand(parent, false);
  }
}

async function formSaved(result: OrgFormSuccess) {
  if (result.mode === 'create') {
    await reloadParent(result.parentId);
    return;
  }
  if (result.nameChanged) {
    await refreshRootsPreservingExpansion();
    return;
  }
  if (result.sortChanged) {
    await reloadParent(result.parentId);
    return;
  }
  const row = gridApi.grid.getRowById(result.id) as null | OrgOption;
  if (row) {
    row.orgCode = result.orgCode;
    row.orgName = result.orgName;
  }
}

async function moveSaved() {
  await refreshRootsPreservingExpansion();
}

async function importSaved() {
  await refreshRootsPreservingExpansion();
}

async function exportOrgs() {
  exporting.value = true;
  try {
    const source = await exportOrgsApi();
    const now = new Date();
    const date = [
      now.getFullYear(),
      String(now.getMonth() + 1).padStart(2, '0'),
      String(now.getDate()).padStart(2, '0'),
    ].join('');
    downloadFileFromBlob({ fileName: `组织数据_${date}.xlsx`, source });
  } finally {
    exporting.value = false;
  }
}

function openForm(
  mode: BusinessFormMode,
  options: {
    defaultParent?: OrgOption;
    id?: string;
  } = {},
) {
  formDrawerApi
    .setData({
      ...options,
      defaultType: options.defaultParent
        ? defaultChildType(options.defaultParent.orgType)
        : 'ORGANIZATION',
      mode,
    })
    .open();
}

function openDetail(row: OrgOption) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function openMove(row: OrgOption) {
  moveDrawerApi.setData({ org: row }).open();
}

function remove(row: OrgOption) {
  void runConfirmAction({
    action: () => deleteOrgApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除组织【${row.orgName}】？只有不存在下级和业务引用的组织才能删除。`,
    onSuccess: () => reloadParent(row.parentId ?? undefined),
    successMessage: '删除成功',
    title: '删除组织',
  });
}

function rowActions(row: OrgOption): RowAction[] {
  return [
    {
      label: '添加下级',
      onClick: () => openForm('create', { defaultParent: row }),
      visible: canAccess.add,
    },
    {
      label: '编辑',
      onClick: () => openForm('edit', { id: row.id }),
      visible: canAccess.edit,
    },
    {
      label: '移动',
      onClick: () => openMove(row),
      visible: canAccess.move,
    },
    {
      disabled: row.protectedOrg,
      label: '删除',
      onClick: () => remove(row),
      type: 'danger',
      visible: canAccess.delete,
    },
  ];
}
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <FormDrawer @success="formSaved" />
    <MoveDrawer @success="moveSaved" />
    <ExcelImportDialog
      ref="importDialog"
      data-name="组织数据"
      :download-template="downloadOrgImportTemplateApi"
      :import-file="importOrgsApi"
      template-description="模板包含填写说明及当前系统可作为上级的组织编码参考。"
      template-filename="组织导入模板.xlsx"
      title="导入组织"
      usage-text="请使用系统模板或本系统导出的组织文件。导入只新增组织，全部校验通过后才会写入。"
      @success="importSaved"
    />
    <Grid table-title="组织列表">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canAccess.add"
            type="primary"
            @click="openForm('create')"
          >
            <Plus class="mr-1 size-4" />
            新增组织
          </ElButton>
          <ElButton v-if="canAccess.add" @click="importDialog?.open()">
            <ArrowUpToLine class="mr-1 size-4" />
            导入组织
          </ElButton>
          <ElButton
            v-if="canAccess.export"
            :loading="exporting"
            @click="exportOrgs"
          >
            <Download class="mr-1 size-4" />
            导出组织
          </ElButton>
        </TableToolbarActions>
      </template>

      <template #orgName="{ row }">
        <span class="flex min-w-0 items-center gap-2">
          <OrgTypeIcon :type="row.orgType" />
          <ElButton link type="primary" @click="openDetail(row)">
            {{ row.orgName }}
          </ElButton>
        </span>
      </template>

      <template #fullPath="{ row }">
        <OrgPath :full-path="row.fullPath" />
      </template>

      <template #orgType="{ row }">
        <EnumTag :options="ORG_TYPE_OPTIONS" :value="row.orgType" />
      </template>

      <template #action="{ row }">
        <RowActions :actions="rowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
