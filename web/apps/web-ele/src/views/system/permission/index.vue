<script lang="ts" setup>
import type { PermissionMenuNode } from '#/api/system/permission';
import type { RoleOption } from '#/api/system/types';

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';

import { ElButton, ElMessage } from 'element-plus';

import {
  clearRebuildableCacheApi,
  getPermissionMenuTreeApi,
  getRolePermissionApi,
  saveRolePermissionApi,
} from '#/api/system/permission';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { useLatestRequest } from '#/hooks/use-latest-request';

import { PERMISSION_PERMISSION_CODES } from './permission-codes';
import PermissionMenuTree from './permission-menu-tree.vue';
import PermissionRolePanel from './permission-role-panel.vue';

const { hasAccessByCodes } = useAccess();
const canAssign = hasAccessByCodes([PERMISSION_PERMISSION_CODES.ASSIGN]);
const canClearCache = hasAccessByCodes([
  PERMISSION_PERMISSION_CODES.CLEAR_CACHE,
]);
const { confirming: clearingCache, runConfirmAction } = useConfirmAction();

const activeRole = ref<RoleOption>();
const activeRoleId = computed(() => activeRole.value?.id);
const checkedMenuIds = ref<string[]>([]);
const initialMenuIds = ref<string[]>([]);
const menuLoading = ref(false);
const menuTree = ref<PermissionMenuNode[]>([]);
const readOnly = ref(true);
const saving = ref(false);
const permissionRequest = useLatestRequest();
const permissionLoading = permissionRequest.loading;

const loading = computed(() => menuLoading.value || permissionLoading.value);

const catalogMenuIds = computed(() => {
  const result = new Set<string>();
  walkMenus(menuTree.value, (menu) => {
    if (menu.menuType === 'CATALOG') result.add(menu.id);
  });
  return result;
});

const currentMenuIds = computed(() =>
  checkedMenuIds.value.filter((id) => !catalogMenuIds.value.has(id)).toSorted(),
);

const dirty = computed(
  () => currentMenuIds.value.join(',') !== initialMenuIds.value.join(','),
);

function walkMenus(
  menus: PermissionMenuNode[],
  visitor: (menu: PermissionMenuNode) => void,
) {
  for (const menu of menus) {
    visitor(menu);
    if (menu.children?.length) walkMenus(menu.children, visitor);
  }
}

function normalizedActualIds(ids: string[]) {
  return ids.filter((id) => !catalogMenuIds.value.has(id)).toSorted();
}

function resetPermissionState() {
  checkedMenuIds.value = [];
  initialMenuIds.value = [];
  readOnly.value = true;
}

async function loadRolePermission(roleId: string) {
  try {
    const permission = await permissionRequest.execute(() =>
      getRolePermissionApi(roleId),
    );
    if (!permission || roleId !== activeRoleId.value) return;
    readOnly.value = !canAssign || permission.readOnly;
    checkedMenuIds.value = [...permission.menuIds];
    initialMenuIds.value = normalizedActualIds(permission.menuIds);
  } catch {
    if (roleId === activeRoleId.value) resetPermissionState();
  }
}

async function selectRole(role?: RoleOption) {
  if (role?.id === activeRoleId.value) return;
  permissionRequest.invalidate();
  resetPermissionState();
  if (!role) {
    activeRole.value = undefined;
    return;
  }
  activeRole.value = role;
  await loadRolePermission(role.id);
}

async function save() {
  if (!activeRoleId.value || readOnly.value || !dirty.value) return;
  saving.value = true;
  try {
    await saveRolePermissionApi(activeRoleId.value, currentMenuIds.value);
    ElMessage.success('权限保存成功');
    await loadRolePermission(activeRoleId.value);
  } finally {
    saving.value = false;
  }
}

function clearCache() {
  void runConfirmAction({
    action: clearRebuildableCacheApi,
    confirmButtonText: '清空缓存',
    message: '确认清空全部可重建缓存？运行时认证和会话数据不会被清理。',
    successMessage: '可重建缓存已清空',
    title: '清空缓存',
  });
}

async function loadMenuTree() {
  menuLoading.value = true;
  menuTree.value = [];
  try {
    menuTree.value = await getPermissionMenuTreeApi();
    initialMenuIds.value = normalizedActualIds(initialMenuIds.value);
  } catch {
    menuTree.value = [];
    initialMenuIds.value = [];
  } finally {
    menuLoading.value = false;
  }
}

onMounted(() => void loadMenuTree());
onBeforeUnmount(permissionRequest.invalidate);
</script>

<template>
  <Page auto-content-height>
    <div class="grid h-full min-h-0 grid-cols-[260px_minmax(0,1fr)] gap-4">
      <PermissionRolePanel
        :active-role-id="activeRoleId"
        @select="selectRole"
      />

      <section class="flex min-h-0 flex-col rounded-lg border bg-card">
        <header class="flex items-center justify-between gap-4 border-b p-4">
          <div class="min-w-0">
            <div class="truncate text-sm font-medium">
              {{ activeRole?.roleName || '请选择角色' }}
            </div>
            <div class="mt-1 text-xs text-muted-foreground">
              {{
                readOnly
                  ? '当前角色权限只读'
                  : activeRole?.roleType === 'CUSTOM'
                    ? '配置普通菜单和按钮权限，敏感权限仅限系统角色'
                    : '配置菜单和按钮权限'
              }}
            </div>
          </div>
          <div class="flex shrink-0 items-center gap-2">
            <ElButton
              v-if="canAssign && activeRoleId && !readOnly"
              :disabled="loading || !dirty"
              :loading="saving"
              type="primary"
              @click="save"
            >
              保存权限
            </ElButton>
            <ElButton
              v-if="canClearCache"
              :loading="clearingCache"
              type="warning"
              plain
              @click="clearCache"
            >
              清空缓存
            </ElButton>
          </div>
        </header>

        <PermissionMenuTree
          v-model:checked-ids="checkedMenuIds"
          :active-role-id="activeRoleId"
          :active-role-type="activeRole?.roleType"
          :loading="loading"
          :menu-tree="menuTree"
          :read-only="readOnly"
        />
      </section>
    </div>
  </Page>
</template>
