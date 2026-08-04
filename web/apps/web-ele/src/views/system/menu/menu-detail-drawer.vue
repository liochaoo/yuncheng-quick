<script lang="ts" setup>
import type { MenuDetail } from '#/api/system/menu';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElTag } from 'element-plus';

import { getMenuDetailApi } from '#/api/system/menu';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import { buildRecordDetailItems } from '#/components/detail/record-detail-items';
import EnabledStatus from '#/components/display/enabled-status.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { MENU_TYPE_TAG_OPTIONS } from '../_shared/display-options';

const { detail, Drawer, loading } = useBusinessDetailDrawer<MenuDetail>({
  load: getMenuDetailApi,
});

const isRoute = computed(() => detail.value?.menu.menuType !== 'BUTTON');

const basicItems = computed<DetailTableItem[]>(() => {
  const menu = detail.value?.menu;
  return [
    { key: 'menuType', label: '菜单类型' },
    { key: 'menuName', label: '菜单名称', value: menu?.menuName },
    {
      key: 'parentName',
      label: '上级菜单',
      value: detail.value?.parentName ?? '一级菜单',
    },
    { key: 'sortOrder', label: '排序号', value: menu?.sortOrder },
  ];
});

const routeItems = computed<DetailTableItem[]>(() => {
  const menu = detail.value?.menu;
  if (!menu) return [];
  if (menu.menuType === 'BUTTON') {
    return [
      {
        key: 'permissionCode',
        label: '权限码',
        span: 2,
        value: menu.permissionCode,
      },
    ];
  }
  const items: DetailTableItem[] = [
    { key: 'routeName', label: '路由名称', value: menu.routeName },
    { key: 'routePath', label: '路由路径', value: menu.routePath },
  ];
  if (menu.menuType === 'CATALOG') {
    items.push({ key: 'redirect', label: '重定向路径', value: menu.redirect });
  }
  if (menu.menuType === 'MENU') {
    items.push(
      { key: 'componentPath', label: '页面组件', value: menu.componentPath },
      { key: 'permissionCode', label: '权限码', value: menu.permissionCode },
      { key: 'redirect', label: '重定向路径', value: menu.redirect },
      { key: 'activePath', label: '激活菜单路径', value: menu.activePath },
    );
  }
  if (menu.menuType === 'EMBEDDED') {
    items.push(
      {
        key: 'iframeSrc',
        label: '内嵌地址',
        span: 2,
        value: menu.iframeSrc,
      },
      { key: 'activePath', label: '激活菜单路径', value: menu.activePath },
    );
  }
  if (menu.menuType === 'LINK') {
    items.push({
      key: 'link',
      label: '链接地址',
      span: 2,
      value: menu.link,
    });
  }
  return items;
});

const displayItems = computed<DetailTableItem[]>(() => {
  const menu = detail.value?.menu;
  return [
    { key: 'icon', label: '图标' },
    { key: 'activeIcon', label: '激活图标' },
    { key: 'badgeType', label: '徽标类型', value: menu?.badgeType },
    { key: 'badge', label: '徽标内容', value: menu?.badge },
    { key: 'badgeVariants', label: '徽标样式', value: menu?.badgeVariants },
    { key: 'menuVisibility', label: '菜单显示' },
  ];
});

function yesNo(value?: boolean) {
  return value ? '是' : '否';
}

const advancedItems = computed<DetailTableItem[]>(() => {
  const menu = detail.value?.menu;
  if (!menu) return [];
  const items: DetailTableItem[] = [];
  if (menu.menuType === 'CATALOG' || menu.menuType === 'MENU') {
    items.push({
      key: 'hideChildrenInMenu',
      label: '隐藏子菜单',
      value: yesNo(menu.hideChildrenInMenu),
    });
  }
  if (menu.menuType !== 'LINK') {
    items.push(
      {
        key: 'hideInBreadcrumb',
        label: '面包屑隐藏',
        value: yesNo(menu.hideInBreadcrumb),
      },
      { key: 'hideInTab', label: '标签页隐藏', value: yesNo(menu.hideInTab) },
    );
  }
  if (menu.menuType === 'MENU') {
    items.push({
      key: 'keepAlive',
      label: '缓存页面',
      value: yesNo(menu.keepAlive),
    });
  }
  if (menu.menuType === 'EMBEDDED' || menu.menuType === 'MENU') {
    items.push(
      { key: 'affixTab', label: '固定标签页', value: yesNo(menu.affixTab) },
      {
        key: 'affixTabOrder',
        label: '固定标签顺序',
        value: menu.affixTab ? menu.affixTabOrder : undefined,
      },
      {
        key: 'noBasicLayout',
        label: '不使用基础布局',
        value: yesNo(menu.noBasicLayout),
      },
      {
        key: 'maxNumOfOpenTab',
        label: '最大标签页数量',
        value: menu.maxNumOfOpenTab,
      },
    );
  }
  items.push({
    key: 'fullPathKey',
    label: '完整路径标识',
    value: yesNo(menu.fullPathKey),
  });
  if (menu.menuType === 'LINK') {
    items.push({
      key: 'openInNewWindow',
      label: '新窗口打开',
      value: yesNo(menu.openInNewWindow),
    });
  }
  items.push({
    key: 'query',
    label: '路由查询参数',
    span: 2,
  });
  return items;
});

const recordItems = computed(() => buildRecordDetailItems(detail.value?.menu));
</script>

<template>
  <Drawer
    :loading="loading"
    title="菜单详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.large"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #menuType>
            <EnumTag
              :options="MENU_TYPE_TAG_OPTIONS"
              :value="detail.menu.menuType"
            />
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="路由与权限">
        <DetailTable :items="routeItems" />
      </DetailSection>

      <template v-if="isRoute">
        <DetailSection title="展示与图标">
          <DetailTable :items="displayItems">
            <template #icon>
              <div v-if="detail.menu.icon" class="flex items-center gap-2">
                <IconifyIcon class="size-5" :icon="detail.menu.icon" />
                <code>{{ detail.menu.icon }}</code>
              </div>
              <span v-else>-</span>
            </template>
            <template #activeIcon>
              <div
                v-if="detail.menu.activeIcon"
                class="flex items-center gap-2"
              >
                <IconifyIcon class="size-5" :icon="detail.menu.activeIcon" />
                <code>{{ detail.menu.activeIcon }}</code>
              </div>
              <span v-else>-</span>
            </template>
            <template #menuVisibility>
              <EnabledStatus
                active-text="显示"
                inactive-text="隐藏"
                :model-value="!detail.menu.hideInMenu"
              />
            </template>
          </DetailTable>
        </DetailSection>

        <DetailSection title="高级设置">
          <DetailTable :items="advancedItems">
            <template #query>
              <div
                v-if="Object.keys(detail.menu.query ?? {}).length > 0"
                class="flex flex-wrap gap-2"
              >
                <ElTag
                  v-for="(value, key) in detail.menu.query"
                  :key="key"
                  effect="plain"
                >
                  {{ key }}={{ value }}
                </ElTag>
              </div>
              <span v-else>-</span>
            </template>
          </DetailTable>
        </DetailSection>
      </template>

      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
