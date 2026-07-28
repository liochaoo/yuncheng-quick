<script lang="ts" setup>
import type {
  VxeGridListeners,
  VxeTableGridOptions,
} from '#/adapter/vxe-table';
import type { RoleOption } from '#/api/system/types';

import { computed, nextTick, onBeforeUnmount, ref } from 'vue';

import { ElButton, ElEmpty, ElInput, ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getRoleOptionsByIdsApi, pageRoleOptionsApi } from '#/api/system/role';
import EnumTag from '#/components/display/enum-tag.vue';
import PagedSelectionDialog from '#/components/select/paged-selection-dialog.vue';
import { useLatestRequest } from '#/hooks/use-latest-request';
import {
  centerColumn,
  checkboxColumn,
  textColumn,
} from '#/utils/table-columns';

import { ROLE_TYPE_TAG_OPTIONS } from '../_shared/display-options';

type RoleGridListeners = VxeGridListeners<RoleOption>;
type CheckboxAllEvent = Parameters<
  NonNullable<RoleGridListeners['checkboxAll']>
>[0];
type CheckboxChangeEvent = Parameters<
  NonNullable<RoleGridListeners['checkboxChange']>
>[0];
type CellClickEvent = Parameters<
  NonNullable<RoleGridListeners['cellClick']>
>[0];

const props = defineProps<{
  modelValue: string[];
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string[]];
}>();

const dialogVisible = ref(false);
const draftIds = ref<string[]>([]);
const knownRoles = ref(new Map<string, RoleOption>());
const roleCode = ref('');
const roleName = ref('');
const selectedRequest = useLatestRequest();

const displayedRoles = computed(() =>
  props.modelValue
    .map((id) => knownRoles.value.get(id))
    .filter((role): role is RoleOption => role !== undefined),
);

const selectedDraftRoles = computed(() =>
  draftIds.value
    .map((id) => knownRoles.value.get(id))
    .filter((role): role is RoleOption => role !== undefined),
);

function mergeRoles(roles: RoleOption[]) {
  const next = new Map(knownRoles.value);
  for (const role of roles) next.set(role.id, role);
  knownRoles.value = next;
}

async function loadSelected(ids: string[]) {
  selectedRequest.invalidate();
  if (ids.length === 0) return;
  const roles = await selectedRequest.execute(() =>
    getRoleOptionsByIdsApi(ids),
  );
  if (roles) mergeRoles(roles);
}

const columns = [
  checkboxColumn<RoleOption>(),
  textColumn<RoleOption>({
    field: 'roleName',
    minWidth: 150,
    title: '角色名称',
  }),
  textColumn<RoleOption>({
    field: 'roleCode',
    minWidth: 160,
    title: '角色编码',
  }),
  centerColumn<RoleOption>({
    field: 'roleType',
    slots: { default: 'roleType' },
    title: '角色类型',
    width: 110,
  }),
];

const [RoleGrid, roleGridApi] = useVbenVxeGrid({
  class: 'role-option-grid rounded-none',
  gridClass: 'role-option-grid__table',
  gridEvents: {
    checkboxAll: ({ checked }: CheckboxAllEvent) =>
      updateCurrentPageRoles(Boolean(checked)),
    checkboxChange: ({ checked, row }: CheckboxChangeEvent) =>
      updateRole(row, Boolean(checked)),
    cellClick: ({ column, row }: CellClickEvent) => {
      if (column.type !== 'checkbox') toggleRole(row);
    },
  },
  gridOptions: {
    checkboxConfig: {
      checkMethod: ({ row }) => !row.disabled,
    },
    columns,
    height: '100%',
    pagerConfig: { enabled: true, pageSize: 10 },
    proxyConfig: {
      ajax: {
        query: async ({ page }) => {
          const result = await pageRoleOptionsApi({
            page: page.currentPage,
            pageSize: page.pageSize,
            roleCode: roleCode.value.trim() || undefined,
            roleName: roleName.value.trim() || undefined,
          });
          mergeRoles(result.items);
          // VXE 提交远程数据后，再同步当前页的跨页勾选状态。
          setTimeout(() => void syncCurrentPageSelection(), 0);
          return result;
        },
      },
    },
    rowConfig: { keyField: 'id' },
  } as VxeTableGridOptions<RoleOption>,
  separator: false,
  showSearchForm: false,
});

function openDialog() {
  draftIds.value = [...props.modelValue];
  roleCode.value = '';
  roleName.value = '';
  dialogVisible.value = true;
  void loadSelected(props.modelValue).catch(() => undefined);
}

function search() {
  void roleGridApi.reload();
}

function resetSearch() {
  roleCode.value = '';
  roleName.value = '';
  search();
}

function selected(roleId: string) {
  return draftIds.value.includes(roleId);
}

function updateRole(role: RoleOption, value: boolean) {
  if (role.disabled) return;
  const ids = new Set(draftIds.value);
  if (value) ids.add(role.id);
  else ids.delete(role.id);
  draftIds.value = [...ids];
}

async function syncCurrentPageSelection() {
  await nextTick();
  if (!dialogVisible.value) return;
  if (typeof roleGridApi.grid.getTableData !== 'function') return;
  const roles = roleGridApi.grid.getTableData().tableData as RoleOption[];
  await roleGridApi.grid.setCheckboxRow(roles, false);
  await roleGridApi.grid.setCheckboxRow(
    roles.filter((role) => selected(role.id)),
    true,
  );
}

function updateCurrentPageRoles(value: boolean) {
  const roles = roleGridApi.grid.getTableData().tableData as RoleOption[];
  const ids = new Set(draftIds.value);
  for (const role of roles) {
    if (role.disabled) continue;
    if (value) ids.add(role.id);
    else ids.delete(role.id);
  }
  draftIds.value = [...ids];
}

function toggleRole(role: RoleOption) {
  if (role.disabled) return;
  const value = !selected(role.id);
  updateRole(role, value);
  void roleGridApi.grid.setCheckboxRow(role, value);
}

function clearSelected() {
  draftIds.value = draftIds.value.filter(
    (id) => knownRoles.value.get(id)?.disabled,
  );
  void syncCurrentPageSelection();
}

function removeSelectedRole(role: RoleOption) {
  updateRole(role, false);
  void syncCurrentPageSelection();
}

function confirm() {
  emit('update:modelValue', [...draftIds.value]);
  dialogVisible.value = false;
}

function clearOptions() {
  selectedRequest.invalidate();
  dialogVisible.value = false;
  draftIds.value = [];
  knownRoles.value = new Map();
}

onBeforeUnmount(() => {
  selectedRequest.invalidate();
});

defineExpose({ clearOptions, loadSelected });
</script>

<template>
  <div
    class="user-role-select"
    role="button"
    tabindex="0"
    @click="openDialog"
    @keydown.enter.prevent="openDialog"
    @keydown.space.prevent="openDialog"
  >
    <div v-if="displayedRoles.length > 0" class="flex flex-wrap gap-1.5">
      <ElTag
        v-for="role in displayedRoles"
        :key="role.id"
        effect="plain"
        size="small"
      >
        {{ role.roleName }}
      </ElTag>
    </div>
    <span v-else class="text-muted-foreground">请选择角色</span>
  </div>

  <PagedSelectionDialog
    v-model="dialogVisible"
    :loading="selectedRequest.loading.value"
    :selected-count="draftIds.length"
    title="选择角色"
    @clear="clearSelected"
    @confirm="confirm"
  >
    <template #search>
      <div class="grid grid-cols-[1fr_1fr_auto_auto] gap-2">
        <ElInput
          v-model="roleCode"
          clearable
          placeholder="角色编码"
          @keyup.enter="search"
        />
        <ElInput
          v-model="roleName"
          clearable
          placeholder="角色名称"
          @keyup.enter="search"
        />
        <ElButton @click="resetSearch">重置</ElButton>
        <ElButton type="primary" @click="search">查询</ElButton>
      </div>
    </template>

    <template #list>
      <RoleGrid>
        <template #roleType="{ row }">
          <EnumTag
            effect="plain"
            :options="ROLE_TYPE_TAG_OPTIONS"
            size="small"
            :value="row.roleType"
          />
        </template>
        <template #empty>
          <ElEmpty description="暂无可选角色" :image-size="72" />
        </template>
      </RoleGrid>
    </template>

    <template #selected>
      <div v-if="selectedDraftRoles.length > 0" class="space-y-2">
        <div
          v-for="role in selectedDraftRoles"
          :key="role.id"
          class="flex items-center justify-between gap-3 rounded-md border px-3 py-2"
        >
          <div class="min-w-0">
            <div class="truncate text-sm">{{ role.roleName }}</div>
            <div class="truncate text-xs text-muted-foreground">
              {{ role.roleCode }}
            </div>
          </div>
          <ElButton
            v-if="!role.disabled"
            link
            type="danger"
            @click="removeSelectedRole(role)"
          >
            移除
          </ElButton>
          <span v-else class="text-xs text-muted-foreground">不可调整</span>
        </div>
      </div>
      <ElEmpty v-else description="暂未选择角色" :image-size="72" />
    </template>
  </PagedSelectionDialog>
</template>

<style scoped>
.user-role-select {
  width: 100%;
  min-height: 32px;
  max-height: 96px;
  padding: 5px 11px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 20px;
  cursor: pointer;
  background-color: var(--el-input-bg-color, var(--el-fill-color-blank));
  border: 1px solid var(--el-input-border-color, var(--el-border-color));
  border-radius: var(--el-input-border-radius, var(--el-border-radius-base));
  transition: var(--el-transition-box-shadow);
}

.user-role-select:hover {
  border-color: var(--el-border-color-hover);
}

.user-role-select:focus-visible {
  outline: 0;
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

:deep(.role-option-grid),
:deep(.role-option-grid__table) {
  height: 100% !important;
}
</style>
