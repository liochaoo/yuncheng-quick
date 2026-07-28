<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PermissionMenuNode } from '#/api/system/permission';
import type { RoleType } from '#/api/system/types';

import { computed, nextTick, ref, watch } from 'vue';

import { ElButton, ElCheckbox, ElEmpty, ElInput, ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import EnumTag from '#/components/display/enum-tag.vue';
import { centerColumn, textColumn } from '#/utils/table-columns';

import { MENU_TYPE_TAG_OPTIONS } from '../_shared/display-options';

const props = defineProps<{
  activeRoleId?: string;
  activeRoleType?: RoleType;
  loading: boolean;
  menuTree: PermissionMenuNode[];
  readOnly: boolean;
}>();

const checkedIds = defineModel<string[]>('checkedIds', { required: true });
const keyword = ref('');

const columns = [
  textColumn<PermissionMenuNode>({
    field: 'menuName',
    minWidth: 300,
    slots: { default: 'menuName' },
    title: '菜单／权限名称',
    treeNode: true,
  }),
  centerColumn<PermissionMenuNode>({
    field: 'menuType',
    slots: { default: 'menuType' },
    title: '类型',
    width: 90,
  }),
  textColumn<PermissionMenuNode>({
    field: 'permissionCode',
    minWidth: 220,
    slots: { default: 'permissionCode' },
    title: '权限码',
  }),
  centerColumn<PermissionMenuNode>({
    field: 'grantDescription',
    slots: { default: 'grantDescription' },
    title: '授权说明',
    width: 120,
  }),
];

const [Grid, gridApi] = useVbenVxeGrid<PermissionMenuNode>({
  gridOptions: {
    columns,
    cellConfig: { height: 48 },
    height: '100%',
    pagerConfig: { enabled: false },
    rowConfig: { keyField: 'id' },
    toolbarConfig: { enabled: false },
    treeConfig: {
      childrenField: 'children',
      expandAll: true,
      parentField: 'parentId',
      rowField: 'id',
      showLine: true,
      transform: false,
    },
  } as VxeTableGridOptions<PermissionMenuNode>,
});

const menuById = computed(() => {
  const result = new Map<string, PermissionMenuNode>();
  walkMenus(props.menuTree, (menu) => result.set(menu.id, menu));
  return result;
});

const visibleCheckedIds = computed(() => visualCheckedIdSet(checkedIds.value));

const filteredTree = computed(() => {
  const value = keyword.value.trim().toLowerCase();
  return value ? filterMenus(props.menuTree, value) : props.menuTree;
});

function walkMenus(
  menus: PermissionMenuNode[],
  visitor: (menu: PermissionMenuNode) => void,
) {
  for (const menu of menus) {
    visitor(menu);
    if (menu.children?.length) walkMenus(menu.children, visitor);
  }
}

function filterMenus(
  menus: PermissionMenuNode[],
  value: string,
): PermissionMenuNode[] {
  return menus.flatMap((menu) => {
    const children = filterMenus(menu.children ?? [], value);
    const matched =
      menu.menuName.toLowerCase().includes(value) ||
      Boolean(menu.permissionCode?.toLowerCase().includes(value));
    return matched || children.length > 0 ? [{ ...menu, children }] : [];
  });
}

function parentIds(menu: PermissionMenuNode) {
  const result: string[] = [];
  let parentId = menu.parentId;
  while (parentId) {
    result.push(parentId);
    parentId = menuById.value.get(parentId)?.parentId;
  }
  return result;
}

function descendantIds(menu: PermissionMenuNode) {
  const result: string[] = [];
  walkMenus(menu.children ?? [], (child) => result.push(child.id));
  return result;
}

function visualCheckedIdSet(ids: Iterable<string>) {
  const result = new Set<string>();
  for (const id of ids) {
    const menu = menuById.value.get(id);
    if (menu && menu.menuType !== 'CATALOG') result.add(id);
  }
  const explicitlyCheckedIds = [...result];
  for (const id of explicitlyCheckedIds) {
    const menu = menuById.value.get(id);
    if (!menu) continue;
    for (const parentId of parentIds(menu)) result.add(parentId);
  }
  return result;
}

function applyCheckedIds(ids: Iterable<string>) {
  checkedIds.value = [...visualCheckedIdSet(ids)];
}

function nodeDisabled(value: unknown) {
  const menu = value as PermissionMenuNode;
  return props.readOnly || !canGrant(menu);
}

function canGrant(menu: PermissionMenuNode) {
  return (
    menu.grantable &&
    (!menu.systemRoleOnly || props.activeRoleType === 'SYSTEM')
  );
}

function toggleMenu(row: unknown, value: boolean) {
  const menu = row as PermissionMenuNode;
  if (nodeDisabled(menu)) return;
  const ids = new Set(checkedIds.value);
  const affectedIds = [menu.id, ...descendantIds(menu)];
  if (value) {
    for (const id of affectedIds) {
      const affected = menuById.value.get(id);
      if (affected && canGrant(affected)) ids.add(id);
    }
    for (const parentId of parentIds(menu)) {
      const parent = menuById.value.get(parentId);
      if (parent && canGrant(parent)) ids.add(parentId);
    }
  } else {
    for (const id of affectedIds) {
      const affected = menuById.value.get(id);
      if (affected && canGrant(affected)) ids.delete(id);
    }
  }
  applyCheckedIds(ids);
}

function setExpanded(expanded: boolean) {
  void gridApi.grid.setAllTreeExpand(expanded);
}

function checkAll() {
  const ids = new Set(checkedIds.value);
  walkMenus(props.menuTree, (menu) => {
    if (canGrant(menu) && menu.menuType !== 'CATALOG') ids.add(menu.id);
  });
  applyCheckedIds(ids);
}

function clearGrantable() {
  const ids = new Set(checkedIds.value);
  walkMenus(props.menuTree, (menu) => {
    if (canGrant(menu)) ids.delete(menu.id);
  });
  applyCheckedIds(ids);
}

watch(
  filteredTree,
  (menus) => {
    gridApi.setGridOptions({ data: menus });
    void nextTick(() => gridApi.grid.setAllTreeExpand?.(true));
  },
  { immediate: true },
);

watch(
  () => props.loading,
  (loading) => gridApi.setLoading(loading),
  { immediate: true },
);
</script>

<template>
  <div class="flex min-h-0 flex-1 flex-col">
    <div class="flex items-center justify-between gap-3 border-b p-3">
      <ElInput
        v-model="keyword"
        class="max-w-80"
        clearable
        placeholder="搜索菜单名称或权限码"
      />
      <div class="flex gap-2">
        <ElButton @click="setExpanded(true)">全部展开</ElButton>
        <ElButton @click="setExpanded(false)">全部收起</ElButton>
        <ElButton v-if="!readOnly" @click="checkAll">全选</ElButton>
        <ElButton v-if="!readOnly" @click="clearGrantable">清空</ElButton>
      </div>
    </div>

    <div class="min-h-0 flex-1 p-1">
      <Grid v-if="activeRoleId" class="permission-menu-grid">
        <template #menuName="{ row }">
          <div class="flex min-w-0 items-center gap-2">
            <ElCheckbox
              :disabled="nodeDisabled(row)"
              :model-value="visibleCheckedIds.has(row.id)"
              @change="(value) => toggleMenu(row, Boolean(value))"
              @click.stop
            />
            <span class="truncate">{{ row.menuName }}</span>
          </div>
        </template>
        <template #menuType="{ row }">
          <EnumTag
            effect="plain"
            :options="MENU_TYPE_TAG_OPTIONS"
            size="small"
            :value="row.menuType"
          />
        </template>
        <template #permissionCode="{ row }">
          <code class="text-xs text-muted-foreground">
            {{ row.permissionCode || '-' }}
          </code>
        </template>
        <template #grantDescription="{ row }">
          <ElTag v-if="!row.grantable" effect="plain" size="small">
            固定首页
          </ElTag>
          <ElTag
            v-else-if="row.systemRoleOnly"
            effect="plain"
            size="small"
            type="warning"
          >
            仅系统角色
          </ElTag>
          <span v-else class="text-muted-foreground">-</span>
        </template>
        <template #empty>
          <ElEmpty description="没有匹配的菜单或权限" :image-size="72" />
        </template>
      </Grid>
      <ElEmpty v-else description="请选择角色后配置权限" />
    </div>
  </div>
</template>

<style scoped>
.permission-menu-grid {
  background: transparent;
}

.permission-menu-grid :deep(.vxe-grid) {
  height: 100% !important;
}
</style>
