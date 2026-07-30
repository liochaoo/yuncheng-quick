<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { OrganizationNodeOption } from '#/api/system/organization';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOrganizationNodeApi,
  listOrganizationNodeChildrenApi,
  pageOrganizationNodesApi,
} from '#/api/system/organization';
import EnumTag from '#/components/display/enum-tag.vue';
import { ORGANIZATION_NODE_TYPE_OPTIONS } from '#/components/organization';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { actionColumn, centerColumn, textColumn } from '#/utils/table-columns';

import OrganizationDetailDrawer from './organization-detail-drawer.vue';
import OrganizationFormDrawer from './organization-form-drawer.vue';
import OrganizationMoveDrawer from './organization-move-drawer.vue';
import { defaultChildType } from './organization-node-type-rules';
import { ORGANIZATION_PERMISSION_CODES } from './permission-codes';

interface OrganizationSearchValues {
  keyword?: string;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  add: hasAccessByCodes([ORGANIZATION_PERMISSION_CODES.ADD]),
  delete: hasAccessByCodes([ORGANIZATION_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([ORGANIZATION_PERMISSION_CODES.EDIT]),
  move: hasAccessByCodes([ORGANIZATION_PERMISSION_CODES.MOVE]),
};
const searching = ref(false);
const { runConfirmAction } = useConfirmAction();

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: OrganizationFormDrawer,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: OrganizationDetailDrawer,
  destroyOnClose: true,
});
const [MoveDrawer, moveDrawerApi] = useVbenDrawer({
  connectedComponent: OrganizationMoveDrawer,
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
        placeholder: '节点名称/编码/完整路径',
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
  textColumn<OrganizationNodeOption>({
    field: 'nodeName',
    minWidth: 220,
    slots: { default: 'nodeName' },
    title: '节点名称',
    treeNode: true,
  }),
  centerColumn<OrganizationNodeOption>({
    field: 'nodeType',
    slots: { default: 'nodeType' },
    title: '类型',
    width: 90,
  }),
  textColumn<OrganizationNodeOption>({
    field: 'nodeCode',
    minWidth: 150,
    title: '节点编码',
  }),
  textColumn<OrganizationNodeOption>({
    field: 'fullPath',
    minWidth: 320,
    title: '完整路径',
  }),
  centerColumn<OrganizationNodeOption>({
    field: 'sortOrder',
    title: '排序号',
    width: 90,
  }),
  actionColumn<OrganizationNodeOption>({ width: 230 }),
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
          values: OrganizationSearchValues,
        ) => {
          const keyword = values.keyword?.trim() || undefined;
          searching.value = Boolean(keyword);
          const result = await pageOrganizationNodesApi({
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
      loadMethod: async ({ row }: { row: OrganizationNodeOption }) =>
        listOrganizationNodeChildrenApi(row.id),
      parentField: 'parentId',
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions<OrganizationNodeOption>,
});

function refresh() {
  gridApi.query();
}

function openForm(
  mode: BusinessFormMode,
  options: {
    defaultParent?: OrganizationNodeOption;
    id?: string;
  } = {},
) {
  formDrawerApi
    .setData({
      ...options,
      defaultType: options.defaultParent
        ? defaultChildType(options.defaultParent.nodeType)
        : 'ORGANIZATION',
      mode,
    })
    .open();
}

function openDetail(row: OrganizationNodeOption) {
  detailDrawerApi.setData({ id: row.id }).open();
}

function openMove(row: OrganizationNodeOption) {
  moveDrawerApi.setData({ node: row }).open();
}

function remove(row: OrganizationNodeOption) {
  void runConfirmAction({
    action: () => deleteOrganizationNodeApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除组织节点【${row.nodeName}】？只有不存在下级和业务引用的节点才能删除。`,
    onSuccess: refresh,
    successMessage: '删除成功',
    title: '删除组织节点',
  });
}

function rowActions(row: OrganizationNodeOption): RowAction[] {
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
      disabled: row.protectedNode,
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

      <template #nodeName="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          {{ row.nodeName }}
        </ElButton>
      </template>

      <template #nodeType="{ row }">
        <EnumTag
          :options="ORGANIZATION_NODE_TYPE_OPTIONS"
          :value="row.nodeType"
        />
      </template>

      <template #action="{ row }">
        <RowActions :actions="rowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
