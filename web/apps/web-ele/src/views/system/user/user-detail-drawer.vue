<script lang="ts" setup>
import type { OrgOption } from '#/api/common/organization';
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
import { OrgAssignmentList } from '#/components/organization';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

interface UserDetailView {
  orgs: OrgOption[];
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

      <DetailSection title="归属组织">
        <OrgAssignmentList
          :items="detail.orgs"
          :primary-org-id="detail.user.primaryOrgId"
        />
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
