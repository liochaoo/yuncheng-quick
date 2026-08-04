<script lang="ts" setup>
import type { DictionaryOptionDetail } from '#/api/system/dictionary';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { getDictionaryOptionDetailApi } from '#/api/system/dictionary';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import { buildRecordDetailItems } from '#/components/detail/record-detail-items';
import EnabledStatus from '#/components/display/enabled-status.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

interface DictionaryOptionDetailData {
  dictionaryId: string;
  id: string;
}

const { detail, Drawer, loading } = useBusinessDetailDrawer<
  DictionaryOptionDetail,
  DictionaryOptionDetailData
>({
  load: (id, data) => getDictionaryOptionDetailApi(data.dictionaryId, id),
});

const basicItems = computed<DetailTableItem[]>(() => [
  {
    key: 'optionValue',
    label: '选项值',
    value: detail.value?.optionValue,
  },
  {
    key: 'optionLabel',
    label: '选项标签',
    value: detail.value?.optionLabel,
  },
  { key: 'enabled', label: '状态' },
  { key: 'sortOrder', label: '排序号', value: detail.value?.sortOrder },
  {
    key: 'description',
    label: '说明',
    span: 2,
    value: detail.value?.description,
  },
]);

const recordItems = computed(() => buildRecordDetailItems(detail.value));
</script>

<template>
  <Drawer
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
    :loading="loading"
    title="字典选项详情"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #enabled>
            <EnabledStatus :model-value="detail.enabled" />
          </template>
        </DetailTable>
      </DetailSection>
      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
