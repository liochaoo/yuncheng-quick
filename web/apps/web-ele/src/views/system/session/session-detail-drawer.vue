<script lang="ts" setup>
import type { OnlineSessionItem } from '#/api/system/session';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed } from 'vue';

import { formatDateTime } from '@vben/utils';

import { getOnlineSessionDetailApi } from '#/api/system/session';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnumTag from '#/components/display/enum-tag.vue';
import { useBusinessDetailDrawer } from '#/hooks/use-business-detail-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { CLIENT_TYPE_TAG_OPTIONS } from '../_shared/display-options';

const { detail, Drawer, loading } = useBusinessDetailDrawer<OnlineSessionItem>({
  load: getOnlineSessionDetailApi,
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'userId', label: '用户 ID', value: detail.value?.userId },
  { key: 'username', label: '登录名', value: detail.value?.username },
  { key: 'realName', label: '姓名', value: detail.value?.realName },
  { key: 'clientType', label: '客户端' },
  {
    key: 'current',
    label: '当前会话',
    value: detail.value?.current ? '是' : '否',
  },
  { key: 'loginIp', label: '登录 IP', value: detail.value?.loginIp },
  {
    key: 'loginTime',
    label: '登录时间',
    value: formatDateTime(detail.value?.loginTime),
  },
  {
    key: 'expiresAt',
    label: '过期时间',
    value: formatDateTime(detail.value?.expiresAt),
  },
  {
    key: 'sessionId',
    label: '会话 ID',
    span: 2,
    value: detail.value?.sessionId,
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
    title="在线会话详情"
    :class="BUSINESS_FORM_DRAWER_WIDTH.mediumWide"
  >
    <div v-if="detail" class="px-4">
      <DetailSection title="会话信息">
        <DetailTable :items="basicItems">
          <template #clientType>
            <EnumTag
              :options="CLIENT_TYPE_TAG_OPTIONS"
              :value="detail.clientType"
            />
          </template>
        </DetailTable>
      </DetailSection>
    </div>
  </Drawer>
</template>
