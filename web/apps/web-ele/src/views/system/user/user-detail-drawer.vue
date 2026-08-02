<script lang="ts" setup>
import type { OrgContextOption } from '#/api/common/organization';
import type { RoleOption } from '#/api/system/types';
import type { UserDetail } from '#/api/system/user';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { ElTag } from 'element-plus';

import { getOrgOptionsByIdsApi } from '#/api/common/organization';
import { getRoleOptionsByIdsApi } from '#/api/system/role';
import { getUserDetailApi } from '#/api/system/user';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnabledStatus from '#/components/display/enabled-status.vue';
import { orgTypeLabel } from '#/components/organization';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

interface UserDetailView {
  orgs: OrgContextOption[];
  roles: RoleOption[];
  user: UserDetail;
}

const { detail, Drawer, loading } = useBusinessDetailDrawer<UserDetailView>({
  async load(id) {
    const user = await getUserDetailApi(id);
    const [roles, orgs] = await Promise.all([
      getRoleOptionsByIdsApi(user.roleIds),
      getOrgOptionsByIdsApi(user.orgIds),
    ]);
    return { orgs, roles, user };
  },
});

const primaryOrg = computed(() =>
  detail.value?.orgs.find((org) => org.id === detail.value?.user.primaryOrgId),
);

const basicItems = computed<DetailTableItem[]>(() => {
  const user = detail.value?.user;
  return [
    { key: 'username', label: '登录名', value: user?.username },
    { key: 'realName', label: '姓名', value: user?.realName },
    { key: 'phone', label: '手机号码', value: user?.phone },
    { key: 'email', label: '电子邮箱', value: user?.email },
    { key: 'enabled', label: '启用状态' },
    { key: 'sortOrder', label: '排序号', value: user?.sortOrder },
    { key: 'roles', label: '角色', span: 2 },
  ];
});

const recordItems = computed<DetailTableItem[]>(() => {
  const user = detail.value?.user;
  return [
    {
      key: 'createdAt',
      label: '创建时间',
      value: formatDateTime(user?.createdAt),
    },
    {
      key: 'updatedAt',
      label: '更新时间',
      value: formatDateTime(user?.updatedAt),
    },
  ];
});

const primaryOrgItems = computed<DetailTableItem[]>(() => {
  const org = primaryOrg.value;
  return [
    {
      key: 'directOrg',
      label: '直接归属节点',
      value: org?.orgName,
    },
    {
      key: 'orgType',
      label: '节点类型',
      value: org ? orgTypeLabel(org.orgType) : undefined,
    },
    {
      key: 'fullPath',
      label: '完整路径',
      span: 2,
      value: org?.fullPath,
    },
    {
      key: 'topOrganization',
      label: '顶级组织',
      value: org?.topOrganization?.orgName ?? '-',
    },
    {
      key: 'organization',
      label: '所在组织',
      value: org?.organization?.orgName ?? '-',
    },
    {
      key: 'topDepartment',
      label: '顶级部门',
      value: org?.topDepartment?.orgName ?? '-',
    },
    {
      key: 'department',
      label: '所在部门',
      value: org?.department?.orgName ?? '-',
    },
    {
      key: 'topGroup',
      label: '顶级小组',
      value: org?.topGroup?.orgName ?? '-',
    },
    {
      key: 'group',
      label: '所在小组',
      value: org?.group?.orgName ?? '-',
    },
  ];
});

const securityItems = computed<DetailTableItem[]>(() => {
  const user = detail.value?.user;
  return [
    {
      key: 'passwordChangedAt',
      label: '密码修改时间',
      span: 2,
      value: formatDateTime(user?.passwordChangedAt),
    },
    {
      key: 'loginFailedCount',
      label: '登录失败次数',
      value: user?.loginFailedCount,
    },
    { key: 'loginLocked', label: '登录锁定状态' },
    {
      key: 'loginLockedUntil',
      label: '锁定截止时间',
      span: 2,
      value: user?.loginLocked
        ? formatDateTime(user.loginLockedUntil ?? undefined)
        : '-',
    },
  ];
});

function identityText(
  top?: null | { id: string; orgName: string },
  current?: null | { id: string; orgName: string },
) {
  if (!current) return '-';
  return top && top.id !== current.id
    ? `${top.orgName} / ${current.orgName}`
    : current.orgName;
}
</script>

<template>
  <Drawer
    :loading="loading"
    title="用户详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #enabled>
            <EnabledStatus :model-value="detail.user.enabled" />
          </template>
          <template #roles>
            <div v-if="detail.roles.length > 0" class="flex flex-wrap gap-2">
              <ElTag v-for="role in detail.roles" :key="role.id" effect="plain">
                {{ role.roleName }}
              </ElTag>
            </div>
            <span v-else>-</span>
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="主归属身份">
        <DetailTable :items="primaryOrgItems" />
      </DetailSection>

      <DetailSection title="全部组织归属">
        <div class="space-y-3">
          <div
            v-for="org in detail.orgs"
            :key="org.id"
            class="rounded-md border px-4 py-3"
          >
            <div class="flex items-center gap-2">
              <span class="font-medium">{{ org.orgName }}</span>
              <ElTag
                v-if="org.id === detail.user.primaryOrgId"
                effect="dark"
                type="primary"
              >
                主归属
              </ElTag>
              <ElTag v-else effect="plain">其他归属</ElTag>
              <ElTag effect="plain" type="info">
                {{ orgTypeLabel(org.orgType) }}
              </ElTag>
            </div>
            <div class="mt-1 text-sm text-muted-foreground">
              {{ org.fullPath }}
            </div>
            <div
              class="mt-3 grid grid-cols-1 gap-x-4 gap-y-1 text-xs text-muted-foreground sm:grid-cols-3"
            >
              <div>
                组织：
                {{ identityText(org.topOrganization, org.organization) }}
              </div>
              <div>
                部门：
                {{ identityText(org.topDepartment, org.department) }}
              </div>
              <div>
                小组：
                {{ identityText(org.topGroup, org.group) }}
              </div>
            </div>
          </div>
        </div>
      </DetailSection>

      <DetailSection title="账号安全">
        <DetailTable :items="securityItems">
          <template #loginLocked>
            <ElTag :type="detail.user.loginLocked ? 'warning' : 'success'">
              {{ detail.user.loginLocked ? '临时锁定' : '正常' }}
            </ElTag>
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
