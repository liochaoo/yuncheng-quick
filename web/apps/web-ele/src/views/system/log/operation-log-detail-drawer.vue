<script lang="ts" setup>
import type { OperationLogItem } from '#/api/system/log';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getOperationLogDetailApi } from '#/api/system/log';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import CopyableText from '#/components/display/copyable-text.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { LOG_RESULT_TAG_OPTIONS } from './log-options';

const { detail, Drawer, loading } = useBusinessDetailDrawer<OperationLogItem>({
  load: getOperationLogDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'action', label: '操作名称', value: detail.value?.action },
  { key: 'success', label: '执行结果' },
  {
    key: 'username',
    label: '操作人',
    value: detail.value?.username,
  },
  { key: 'realName', label: '操作人姓名', value: detail.value?.realName },
  { key: 'ip', label: 'IP 地址', value: detail.value?.ip },
  {
    key: 'occurredAt',
    label: '发生时间',
    value: formatDateTime(detail.value?.occurredAt),
  },
  {
    key: 'errorMessage',
    label: '失败原因',
    span: 2,
    value: detail.value?.errorMessage,
  },
]);

const requestItems = computed<DetailTableItem[]>(() => [
  { key: 'httpMethod', label: '请求方法', value: detail.value?.httpMethod },
  {
    key: 'durationMillis',
    label: '执行耗时',
    value: `${detail.value?.durationMillis ?? 0} ms`,
  },
  {
    key: 'requestPath',
    label: '请求路径',
    span: 2,
    value: detail.value?.requestPath,
  },
  {
    key: 'requestParams',
    label: '请求参数',
    span: 2,
    value: detail.value?.requestParams,
  },
]);

const traceItems = computed<DetailTableItem[]>(() => [
  {
    key: 'method',
    label: '执行方法',
    span: 2,
    value:
      detail.value && `${detail.value.className}.${detail.value.methodName}`,
  },
  {
    key: 'userId',
    label: '用户 ID',
    value: detail.value?.userId,
  },
  {
    key: 'traceId',
    label: '链路 ID',
    value: detail.value?.traceId,
  },
  {
    key: 'userAgent',
    label: 'User-Agent',
    span: 2,
    value: detail.value?.userAgent,
  },
]);
</script>

<template>
  <Drawer
    :loading="loading"
    title="操作日志详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="日志信息">
        <DetailTable :items="basicItems">
          <template #success>
            <EnumTag
              :options="LOG_RESULT_TAG_OPTIONS"
              :value="detail.success"
            />
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="请求信息">
        <DetailTable :items="requestItems">
          <template #requestParams>
            <pre class="m-0 whitespace-pre-wrap break-all font-sans">{{
              detail.requestParams || '-'
            }}</pre>
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="链路信息">
        <DetailTable :items="traceItems">
          <template #userId>
            <span class="whitespace-nowrap">{{ detail.userId || '-' }}</span>
          </template>
          <template #traceId>
            <CopyableText :value="detail.traceId" />
          </template>
        </DetailTable>
      </DetailSection>
    </div>
  </Drawer>
</template>
