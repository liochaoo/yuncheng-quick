<script lang="ts" setup>
import type { ReferenceProps } from '@scalar/api-reference';

import { computed, onMounted, ref, shallowRef } from 'vue';
import { useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { usePreferences } from '@vben/preferences';
import { useAccessStore } from '@vben/stores';

import { ApiReference } from '@scalar/api-reference';
import { ElButton, ElResult } from 'element-plus';

import { getOpenApiDocumentApi, getOpenApiStatusApi } from '#/api/core/openapi';

import '@scalar/api-reference/style.css';

defineOptions({ name: 'DevelopmentOpenApi' });

const router = useRouter();
const accessStore = useAccessStore();
const { isDark } = usePreferences();

const loading = ref(true);
const loadFailed = ref(false);
const document = shallowRef<Record<string, unknown>>();

const colorModeClass = computed(() =>
  isDark.value ? 'dark-mode' : 'light-mode',
);

const scalarConfiguration = computed<
  NonNullable<ReferenceProps['configuration']>
>(() => ({
  agent: { disabled: true, hideAddApi: true },
  authentication: {
    preferredSecurityScheme: 'bearerAuth',
  },
  content: document.value,
  customFetch: authenticatedSameOriginFetch,
  defaultOpenAllTags: false,
  defaultOpenFirstTag: true,
  hideClientButton: false,
  layout: 'modern',
  locale: 'zh-CN',
  mcp: { disabled: true },
  operationTitleSource: 'summary',
  persistAuth: false,
  showDeveloperTools: 'never',
  telemetry: false,
  theme: 'default',
  withDefaultFonts: false,
}));

async function authenticatedSameOriginFetch(
  input: RequestInfo | URL,
  init?: RequestInit,
) {
  const request = new Request(input, init);
  const target = new URL(request.url, window.location.origin);
  if (target.origin !== window.location.origin) {
    return fetch(request);
  }

  const headers = new Headers(request.headers);
  const token = accessStore.accessToken;
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return fetch(new Request(request, { headers }));
}

async function load() {
  loading.value = true;
  loadFailed.value = false;
  document.value = undefined;
  try {
    const status = await getOpenApiStatusApi();
    if (!status.enabled || !status.documentUrl) {
      return;
    }
    document.value = await getOpenApiDocumentApi(status.documentUrl);
  } catch {
    loadFailed.value = true;
  } finally {
    loading.value = false;
  }
}

function backToWorkspace() {
  void router.push('/workspace');
}

onMounted(() => void load());
</script>

<template>
  <Page auto-content-height>
    <section class="h-full min-h-0 overflow-hidden rounded-lg border bg-card">
      <div
        v-if="loading"
        v-loading="true"
        element-loading-background="transparent"
        class="h-full min-h-[420px]"
      ></div>

      <div
        v-else-if="!document"
        class="flex h-full min-h-[420px] items-center justify-center"
      >
        <ElResult
          :sub-title="
            loadFailed
              ? '接口文档加载失败，请稍后重试。'
              : '为避免生产环境暴露接口结构，OpenAPI 接口文档当前已关闭。如需使用，请联系部署人员开启。'
          "
          :title="loadFailed ? '接口文档加载失败' : '当前环境未启用接口文档'"
          :icon="loadFailed ? 'error' : 'info'"
        >
          <template #extra>
            <ElButton v-if="loadFailed" type="primary" @click="load">
              重新加载
            </ElButton>
            <ElButton @click="backToWorkspace">返回工作台</ElButton>
          </template>
        </ElResult>
      </div>

      <div
        v-else-if="document"
        :class="colorModeClass"
        class="openapi-reference h-full overflow-auto"
      >
        <ApiReference :configuration="scalarConfiguration">
          <template #sidebar-end>
            <span aria-hidden="true" class="hidden"></span>
          </template>
        </ApiReference>
      </div>
    </section>
  </Page>
</template>

<style scoped>
.openapi-reference {
  container-type: size;
}

.openapi-reference :deep(.scalar-app) {
  min-height: 100%;
}

.openapi-reference :deep(.scalar-api-reference) {
  --full-height: 100cqh;
}

/* Scalar 独立页面的关闭按钮默认相对浏览器窗口定位；嵌入后台后应落在客户端弹框内。 */
.openapi-reference :deep(.scalar-app-layout .app-exit-button) {
  position: absolute;
  color: var(--scalar-color-1);
  background: var(--scalar-background-2);
}

.openapi-reference :deep(.scalar-app-layout .app-exit-button:hover) {
  background: var(--scalar-background-3);
}
</style>
