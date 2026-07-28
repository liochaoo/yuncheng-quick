<script lang="ts" setup>
import type { DictionaryDetail } from '#/api/system/dictionary';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getDictionaryDetailApi } from '#/api/system/dictionary';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

const { detail, Drawer, loading } = useBusinessDetailDrawer<DictionaryDetail>({
  load: getDictionaryDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  {
    key: 'dictionaryCode',
    label: '字典编码',
    value: detail.value?.dictionaryCode,
  },
  {
    key: 'dictionaryName',
    label: '字典名称',
    value: detail.value?.dictionaryName,
  },
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
    title="数据字典详情"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems" />
      </DetailSection>
      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
