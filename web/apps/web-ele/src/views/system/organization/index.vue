<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { OrgOption } from '#/api/system/organization';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOrgApi,
  listOrgChildrenApi,
  pageOrgsApi,
} from '#/api/system/organization';
import EnumTag from '#/components/display/enum-tag.vue';
import { ORG_TYPE_OPTIONS } from '#/components/organization';
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
  keyword?: string;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  add: hasAccessByCodes([ORG_PERMISSION_CODES.ADD]),
  delete: hasAccessByCodes([ORG_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([ORG_PERMISSION_CODES.EDIT]),
  move: hasAccessByCodes([ORG_PERMISSION_CODES.MOVE]),
};
const searching = ref(false);
const { runConfirmAction } = useConfirmAction();

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
        placeholder: '组织名称/编码/完整路径',
      },
      fieldName: 'keyword',
      label: '关键字',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-[minmax(0,480px)_auto]',
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
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: OrgSearchValues,
        ) => {
          const keyword = values.keyword?.trim() || undefined;
          searching.value = Boolean(keyword);
          const result = await pageOrgsApi({
            keyword,
            page: page.currentPage,
            pageSize: page.pageSize,
          });
          return searching.value
            ? {
                ...result,
                items: result.items.map((item) => ({
                  ...item,
                  hasChildren: false,
                })),
              }
            : result;
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
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions<OrgOption>,
});

function refresh() {
  gridApi.query();
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
    onSuccess: refresh,
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
    <FormDrawer @success="refresh" />
    <MoveDrawer @success="refresh" />
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
        </TableToolbarActions>
      </template>

      <template #orgName="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.orgName }}
        </ElButton>
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
