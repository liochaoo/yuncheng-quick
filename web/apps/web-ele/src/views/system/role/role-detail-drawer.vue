<script lang="ts" setup>
import type { RoleDetail } from '#/api/system/role';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { getRoleDetailApi } from '#/api/system/role';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import { buildRecordDetailItems } from '#/components/detail/record-detail-items';
import EnumTag from '#/components/display/enum-tag.vue';
import { ROLE_TYPE_TAG_OPTIONS } from '#/components/role/role-options';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

const { detail, Drawer, loading } = useBusinessDetailDrawer<RoleDetail>({
  load: getRoleDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'roleCode', label: '角色编码', value: detail.value?.roleCode },
  { key: 'roleName', label: '角色名称', value: detail.value?.roleName },
  { key: 'roleType', label: '角色类型' },
  { key: 'sortOrder', label: '排序号', value: detail.value?.sortOrder },
  { key: 'userCount', label: '用户数量', value: detail.value?.userCount },
]);

const recordItems = computed(() => buildRecordDetailItems(detail.value));
</script>

<template>
  <Drawer
    :loading="loading"
    title="角色详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #roleType>
            <EnumTag
              :options="ROLE_TYPE_TAG_OPTIONS"
              :value="detail.roleType"
            />
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
