<script lang="ts" setup>
import type { OrgDetail } from '#/api/system/organization';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getOrgDetailApi } from '#/api/system/organization';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { ORG_TYPE_OPTIONS } from '#/components/organization';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

const { detail, Drawer, loading } = useBusinessDetailDrawer<OrgDetail>({
  load: getOrgDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'orgType', label: '组织类型' },
  { key: 'orgName', label: '组织名称', value: detail.value?.orgName },
  { key: 'orgCode', label: '组织编码', value: detail.value?.orgCode },
  {
    key: 'parentName',
    label: '上级组织',
    value: detail.value?.parentName ?? '顶级组织',
  },
  {
    key: 'fullPath',
    label: '完整路径',
    span: 2,
    value: detail.value?.fullPath,
  },
  { key: 'depth', label: '所在层级', value: detail.value?.depth },
  { key: 'sortOrder', label: '排序号', value: detail.value?.sortOrder },
  {
    key: 'description',
    label: '说明',
    span: 2,
    value: detail.value?.description,
  },
]);

const recordItems = computed<DetailTableItem[]>(() => [
  {
    key: 'createdAt',
    label: '创建时间',
    value: formatDateTime(detail.value?.createdAt),
  },
  {
    key: 'updatedAt',
    label: '更新时间',
    value: formatDateTime(detail.value?.updatedAt),
  },
]);
</script>

<template>
  <Drawer
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
    :loading="loading"
    title="组织详情"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #orgType>
            <EnumTag :options="ORG_TYPE_OPTIONS" :value="detail.orgType" />
          </template>
        </DetailTable>
      </DetailSection>
      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
