<script lang="ts" setup>
import type { LoginLogItem } from '#/api/system/log';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getLoginLogDetailApi } from '#/api/system/log';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { CLIENT_TYPE_TAG_OPTIONS } from '../_shared/display-options';
import { LOG_RESULT_TAG_OPTIONS, LOGIN_EVENT_TAG_OPTIONS } from './log-options';

const { detail, Drawer, loading } = useBusinessDetailDrawer<LoginLogItem>({
  load: getLoginLogDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'eventType', label: '事件类型' },
  { key: 'success', label: '执行结果' },
  { key: 'loginName', label: '登录名', value: detail.value?.loginName },
  { key: 'realName', label: '姓名', value: detail.value?.realName },
  { key: 'clientType', label: '客户端' },
  { key: 'ip', label: 'IP 地址', value: detail.value?.ip },
  {
    key: 'occurredAt',
    label: '发生时间',
    value: formatDateTime(detail.value?.occurredAt),
  },
  {
    key: 'failureReason',
    label: '失败原因',
    span: 2,
    value: detail.value?.failureReason,
  },
]);

const traceItems = computed<DetailTableItem[]>(() => [
  { key: 'userId', label: '用户 ID', value: detail.value?.userId },
  { key: 'sessionId', label: '会话 ID', value: detail.value?.sessionId },
  {
    key: 'traceId',
    label: '链路 ID',
    span: 2,
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
    title="登录日志详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="日志信息">
        <DetailTable :items="basicItems">
          <template #eventType>
            <EnumTag
              :options="LOGIN_EVENT_TAG_OPTIONS"
              :value="detail.eventType"
            />
          </template>
          <template #success>
            <EnumTag
              :options="LOG_RESULT_TAG_OPTIONS"
              :value="detail.success"
            />
          </template>
          <template #clientType>
            <EnumTag
              :options="CLIENT_TYPE_TAG_OPTIONS"
              :value="detail.clientType"
            />
          </template>
        </DetailTable>
      </DetailSection>

      <DetailSection title="请求信息">
        <DetailTable :items="traceItems" />
      </DetailSection>
    </div>
  </Drawer>
</template>
