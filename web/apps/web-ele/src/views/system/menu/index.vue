<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MenuItem, MenuType } from '#/api/system/menu';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { useAccess } from '@vben/access';
import { Page, useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon, Plus } from '@vben/icons';

import { ElButton } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteMenuApi,
  getMenuDeleteImpactApi,
  getMenuTreeApi,
} from '#/api/system/menu';
import EnabledStatus from '#/components/display/enabled-status.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { actionColumn, centerColumn, textColumn } from '#/utils/table-columns';

import { MENU_TYPE_TAG_OPTIONS } from '../_shared/display-options';
import MenuDetailDrawer from './menu-detail-drawer.vue';
import MenuFormDrawer from './menu-form-drawer.vue';
import {
  defaultChildMenuType,
  menuTypeAllowsChildren,
} from './menu-type-rules';
import { MENU_PERMISSION_CODES } from './permission-codes';

interface MenuSearchValues {
  keyword?: string;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  add: hasAccessByCodes([MENU_PERMISSION_CODES.ADD]),
  delete: hasAccessByCodes([MENU_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([MENU_PERMISSION_CODES.EDIT]),
};
const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: MenuFormDrawer,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: MenuDetailDrawer,
  destroyOnClose: true,
});
const { runConfirmAction } = useConfirmAction();

const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'keyword',
      label: '关键字',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-1 md:grid-cols-3',
};

function routeTarget(row: MenuItem) {
  return row.componentPath || row.iframeSrc || row.link || '-';
}

const columns = [
  textColumn<MenuItem>({
    field: 'menuName',
    minWidth: 240,
    slots: { default: 'menuName' },
    treeNode: true,
    title: '菜单名称',
  }),
  centerColumn<MenuItem>({
    field: 'menuType',
    slots: { default: 'menuType' },
    title: '类型',
    width: 90,
  }),
  textColumn<MenuItem>({
    field: 'routePath',
    minWidth: 180,
    title: '路由路径',
  }),
  textColumn<MenuItem>({
    field: 'componentPath',
    formatter: ({ row }) => routeTarget(row),
    minWidth: 220,
    title: '组件或链接',
  }),
  textColumn<MenuItem>({
    field: 'permissionCode',
    minWidth: 210,
    title: '权限码',
  }),
  centerColumn<MenuItem>({
    field: 'hideInMenu',
    slots: { default: 'displayState' },
    title: '菜单显示',
    width: 100,
  }),
  centerColumn<MenuItem>({
    field: 'sortOrder',
    title: '排序号',
    width: 90,
  }),
  actionColumn<MenuItem>({ width: 180 }),
];

function matches(menu: MenuItem, keyword: string) {
  const value = keyword.toLowerCase();
  return (
    menu.menuName.toLowerCase().includes(value) ||
    menu.routePath?.toLowerCase().includes(value) ||
    menu.permissionCode?.toLowerCase().includes(value)
  );
}

function filterTree(menus: MenuItem[], keyword?: string): MenuItem[] {
  const value = keyword?.trim();
  if (!value) return menus;
  return menus.flatMap((menu) => {
    if (matches(menu, value)) return [menu];
    const children = filterTree(menu.children ?? [], value);
    return children.length > 0 ? [{ ...menu, children }] : [];
  });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions,
  gridOptions: {
    columns,
    height: 'auto',
    pagerConfig: { enabled: false },
    proxyConfig: {
      ajax: {
        query: async (_params: unknown, values: MenuSearchValues) => {
          const menus = await getMenuTreeApi();
          return filterTree(menus, values.keyword);
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
      expandAll: true,
      parentField: 'parentId',
      rowField: 'id',
      transform: false,
    },
  } as VxeTableGridOptions<MenuItem>,
});

function refresh() {
  gridApi.query();
}

function openForm(
  mode: BusinessFormMode,
  options: {
    defaultParentId?: string;
    defaultType?: MenuType;
    id?: string;
  } = {},
) {
  formDrawerApi.setData({ ...options, mode }).open();
}

function openDetail(row: MenuItem) {
  detailDrawerApi.setData({ id: row.id }).open();
}

async function remove(row: MenuItem) {
  let impact;
  try {
    impact = await getMenuDeleteImpactApi(row.id);
  } catch {
    return;
  }
  void runConfirmAction({
    action: () => deleteMenuApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除菜单【${row.menuName}】及其下级？将删除 ${impact.menuCount} 个菜单节点和 ${impact.roleRelationCount} 条角色授权关系。`,
    onSuccess: refresh,
    successMessage: '删除成功',
    title: '删除菜单',
  });
}

function menuRowActions(row: MenuItem): RowAction[] {
  return [
    {
      label: '添加下级',
      onClick: () =>
        openForm('create', {
          defaultParentId: row.id,
          defaultType: defaultChildMenuType(row.menuType),
        }),
      visible: canAccess.add && menuTypeAllowsChildren(row.menuType),
    },
    {
      label: '编辑',
      onClick: () => openForm('edit', { id: row.id }),
      visible: canAccess.edit,
    },
    {
      label: '删除',
      onClick: () => void remove(row),
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
    <Grid table-title="菜单列表">
      <template #toolbar-tools>
        <TableToolbarActions>
          <ElButton
            v-if="canAccess.add"
            type="primary"
            @click="openForm('create', { defaultType: 'CATALOG' })"
          >
            <Plus class="mr-1 size-4" />
            新增菜单
          </ElButton>
        </TableToolbarActions>
      </template>

      <template #menuName="{ row }">
        <ElButton link type="primary" @click="openDetail(row)">
          <IconifyIcon v-if="row.icon" class="mr-1 size-4" :icon="row.icon" />
          {{ row.menuName }}
        </ElButton>
      </template>

      <template #menuType="{ row }">
        <EnumTag :options="MENU_TYPE_TAG_OPTIONS" :value="row.menuType" />
      </template>

      <template #displayState="{ row }">
        <EnabledStatus
          active-text="显示"
          inactive-text="隐藏"
          :model-value="!row.hideInMenu"
        />
      </template>

      <template #action="{ row }">
        <RowActions :actions="menuRowActions(row)" />
      </template>
    </Grid>
  </Page>
</template>
