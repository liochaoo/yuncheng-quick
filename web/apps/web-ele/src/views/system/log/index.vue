<script lang="ts" setup>
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { ElTabPane, ElTabs } from 'element-plus';

import LoginLogPanel from './login-log-panel.vue';
import OperationLogPanel from './operation-log-panel.vue';

defineOptions({ name: 'SystemLog' });

const activeTab = ref('login');
</script>

<template>
  <Page auto-content-height>
    <section
      class="flex h-full min-h-0 flex-col overflow-hidden rounded-lg border bg-card"
    >
      <ElTabs v-model="activeTab" class="system-log-tabs min-h-0 flex-1">
        <ElTabPane label="登录日志" name="login" lazy>
          <LoginLogPanel />
        </ElTabPane>
        <ElTabPane label="操作日志" name="operation" lazy>
          <OperationLogPanel />
        </ElTabPane>
      </ElTabs>
    </section>
  </Page>
</template>

<style scoped>
.system-log-tabs {
  display: flex;
  flex-direction: column;
}

.system-log-tabs :deep(.el-tabs__header) {
  flex: none;
  padding: 0 16px;
  margin: 0;
}

.system-log-tabs :deep(.el-tabs__content),
.system-log-tabs :deep(.el-tab-pane) {
  height: 100%;
  min-height: 0;
}

.system-log-tabs :deep(.el-tabs__content) {
  flex: 1;
  padding-top: 12px;
}
</style>
