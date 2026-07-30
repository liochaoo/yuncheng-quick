<script lang="ts" setup>
import type { OrganizationNodeDetail } from '#/api/system/organization';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getOrganizationNodeDetailApi } from '#/api/system/organization';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { ORGANIZATION_NODE_TYPE_OPTIONS } from '#/components/organization';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

const { detail, Drawer, loading } =
  useBusinessDetailDrawer<OrganizationNodeDetail>({
    load: getOrganizationNodeDetailApi,
  });

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'nodeType', label: '节点类型' },
  { key: 'nodeName', label: '节点名称', value: detail.value?.nodeName },
  { key: 'nodeCode', label: '节点编码', value: detail.value?.nodeCode },
  {
    key: 'parentName',
    label: '上级节点',
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
    :class="BUSINESS_FORM_DRAWER_WIDTH.medium"
    :loading="loading"
    title="组织节点详情"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #nodeType>
            <EnumTag
              :options="ORGANIZATION_NODE_TYPE_OPTIONS"
              :value="detail.nodeType"
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
