<script lang="ts" setup>
import type { RoleDetail, RoleUserListItem } from '#/api/system/role';
import type { RowAction } from '#/components/table/row-actions.types';

import { computed, nextTick, onBeforeUnmount, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { Plus, Trash2 } from '@vben/icons';

import { ElButton, ElMessage, ElTabPane, ElTabs } from 'element-plus';

import {
  addRoleUsersApi,
  getRoleDetailApi,
  removeRoleUsersApi,
} from '#/api/system/role';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { useLatestRequest } from '#/hooks/use-latest-request';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import RoleUserGrid from './role-user-grid.vue';

type RoleUserGridInstance = InstanceType<typeof RoleUserGrid>;

interface DrawerOpenData {
  roleId: string;
}

const emit = defineEmits<{
  success: [];
}>();

const activeTab = ref<'candidates' | 'users'>('users');
const candidatesGridRef = ref<RoleUserGridInstance>();
// 只记录当前一次抽屉打开期间是否访问过候选页签，关闭后即失效。
const candidatesLoaded = ref(false);
const role = ref<RoleDetail>();
const roleId = ref<string>();
const usersGridRef = ref<RoleUserGridInstance>();
const title = computed(() =>
  role.value ? `角色用户：${role.value.roleName}` : '角色用户',
);
const { runConfirmAction } = useConfirmAction();
const detailRequest = useLatestRequest();
const roleLoading = detailRequest.loading;

const [Drawer, drawerApi] = useVbenDrawer({
  footer: false,
  onClosed() {
    detailRequest.invalidate();
    role.value = undefined;
    roleId.value = undefined;
    candidatesLoaded.value = false;
  },
  async onOpenChange(isOpen) {
    if (!isOpen) return;
    const data = drawerApi.getData<DrawerOpenData>();
    role.value = undefined;
    roleId.value = data.roleId;
    activeTab.value = 'users';
    candidatesLoaded.value = false;
    try {
      const detail = await detailRequest.execute(() =>
        getRoleDetailApi(data.roleId),
      );
      if (detail) role.value = detail;
    } catch {
      drawerApi.close();
    }
  },
  async onOpened() {
    await nextTick();
    await usersGridRef.value?.query();
  },
});

onBeforeUnmount(detailRequest.invalidate);

async function loadActiveTab(name: number | string) {
  if (name !== 'candidates' || candidatesLoaded.value) return;
  const alreadyMounted = Boolean(candidatesGridRef.value);
  await nextTick();
  // 首次挂载由表格自身加载；已经挂载时说明数据被标记为需要刷新。
  if (alreadyMounted) await candidatesGridRef.value?.query();
  candidatesLoaded.value = true;
}

async function refreshAfterAdd(addedCount: number) {
  await candidatesGridRef.value?.refreshAfterRemove(addedCount);
  await usersGridRef.value?.query();
  emit('success');
}

async function refreshAfterRemove(removedCount: number) {
  await usersGridRef.value?.refreshAfterRemove(removedCount);
  candidatesLoaded.value = false;
  emit('success');
}

function addSelected() {
  const rows = candidatesGridRef.value?.selectedRows() ?? [];
  if (!roleId.value || rows.length === 0) {
    ElMessage.warning('请选择需要添加的用户');
    return;
  }
  const currentRoleId = roleId.value;
  void runConfirmAction({
    action: () =>
      addRoleUsersApi(
        currentRoleId,
        rows.map((row) => row.id),
      ),
    message: `确认向当前角色添加选中的 ${rows.length} 个用户？`,
    onSuccess: () => refreshAfterAdd(rows.length),
    successMessage: '添加成功',
    title: '添加角色用户',
  });
}

function removeSelected(rows = usersGridRef.value?.selectedRows() ?? []) {
  if (!roleId.value || rows.length === 0) {
    ElMessage.warning('请选择需要移除的用户');
    return;
  }
  const currentRoleId = roleId.value;
  void runConfirmAction({
    action: () =>
      removeRoleUsersApi(
        currentRoleId,
        rows.map((row) => row.id),
      ),
    confirmButtonText: '移除',
    message: `确认从当前角色移除选中的 ${rows.length} 个用户？`,
    onSuccess: () => refreshAfterRemove(rows.length),
    successMessage: '移除成功',
    title: '移除角色用户',
  });
}

function assignedUserActions(row: RoleUserListItem): RowAction[] {
  return [
    {
      label: '移除',
      onClick: () => removeSelected([row]),
      type: 'danger',
    },
  ];
}
</script>

<template>
  <Drawer
    :loading="roleLoading"
    :title="title"
    :class="BUSINESS_FORM_DRAWER_WIDTH.xlarge"
    content-class="overflow-hidden"
  >
    <ElTabs
      v-model="activeTab"
      class="role-users-tabs h-full px-4"
      @tab-change="loadActiveTab"
    >
      <ElTabPane label="已分配用户" name="users">
        <RoleUserGrid ref="usersGridRef" mode="users" :role-id="roleId">
          <template #toolbar-tools>
            <TableToolbarActions>
              <ElButton plain type="danger" @click="removeSelected()">
                <Trash2 class="mr-1 size-4" />
                批量移除
              </ElButton>
            </TableToolbarActions>
          </template>
          <template #action="{ row }">
            <RowActions :actions="assignedUserActions(row)" />
          </template>
        </RoleUserGrid>
      </ElTabPane>
      <ElTabPane label="添加用户" lazy name="candidates">
        <RoleUserGrid
          ref="candidatesGridRef"
          mode="candidates"
          :role-id="roleId"
        >
          <template #toolbar-tools>
            <TableToolbarActions>
              <ElButton type="primary" @click="addSelected">
                <Plus class="mr-1 size-4" />
                添加选中用户
              </ElButton>
            </TableToolbarActions>
          </template>
        </RoleUserGrid>
      </ElTabPane>
    </ElTabs>
  </Drawer>
</template>

<style scoped>
.role-users-tabs :deep(.el-tabs__content) {
  min-height: 0;
  overflow: hidden;
}

.role-users-tabs :deep(.el-tab-pane) {
  height: 100%;
}
</style>
