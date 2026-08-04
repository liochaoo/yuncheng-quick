<script lang="ts" setup>
import type { FileDetail } from '#/api/system/file';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { getFileDetailApi } from '#/api/system/file';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import { buildRecordDetailItems } from '#/components/detail/record-detail-items';
import EnumTag from '#/components/display/enum-tag.vue';
import { formatFileSize } from '#/components/file/file-display';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { FILE_ACCESS_TAG_OPTIONS, filePolicyLabel } from './file-options';

const { detail, Drawer, loading } = useBusinessDetailDrawer<FileDetail>({
  load: getFileDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'originalName', label: '文件名称', value: detail.value?.originalName },
  {
    key: 'fileSize',
    label: '文件大小',
    value: formatFileSize(detail.value?.fileSize ?? 0),
  },
  { key: 'contentType', label: '内容类型', value: detail.value?.contentType },
  {
    key: 'fileExtension',
    label: '文件扩展名',
    value: detail.value?.fileExtension,
  },
  { key: 'accessType', label: '访问类型' },
  {
    key: 'policyCode',
    label: '文件策略',
    value: filePolicyLabel(detail.value?.policyCode),
  },
]);

const storageItems = computed<DetailTableItem[]>(() => [
  {
    key: 'storagePlatform',
    label: '存储平台',
    value: detail.value?.storagePlatform,
  },
  {
    key: 'objectKey',
    label: '对象标识',
    span: 2,
    value: detail.value?.objectKey,
  },
  {
    key: 'sha256',
    label: 'SHA-256',
    span: 2,
    value: detail.value?.sha256,
  },
]);

const accessItems = computed<DetailTableItem[]>(() => [
  {
    key: 'previewUrl',
    label: '预览地址',
    span: 2,
    value: detail.value?.previewUrl,
  },
  {
    key: 'downloadUrl',
    label: '下载地址',
    span: 2,
    value: detail.value?.downloadUrl,
  },
]);

const businessItems = computed<DetailTableItem[]>(() => [
  { key: 'businessType', label: '业务类型', value: detail.value?.businessType },
  { key: 'businessId', label: '业务数据 ID', value: detail.value?.businessId },
  {
    key: 'businessPosition',
    label: '业务位置',
    value: detail.value?.businessPosition,
  },
  { key: 'sortOrder', label: '排序号', value: detail.value?.sortOrder },
]);

const recordItems = computed(() =>
  buildRecordDetailItems(detail.value, { showOperators: true }),
);
</script>

<template>
  <Drawer
    :loading="loading"
    title="文件详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="基础信息">
        <DetailTable :items="basicItems">
          <template #accessType>
            <EnumTag
              :options="FILE_ACCESS_TAG_OPTIONS"
              :value="detail.accessType"
            />
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="存储信息">
        <DetailTable :items="storageItems" />
      </DetailSection>

      <DetailSection title="访问地址">
        <DetailTable :items="accessItems" />
      </DetailSection>

      <DetailSection title="业务关联">
        <DetailTable :items="businessItems" />
      </DetailSection>

      <DetailSection title="记录信息">
        <DetailTable :items="recordItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
