<script lang="ts" setup>
import { computed } from 'vue';

import { AuthPageLayout } from '@vben/layouts';
import { preferences } from '@vben/preferences';

import { runtimeBrandConfig } from '#/config/brand';
import { $t } from '#/locales';

const appName = computed(() => preferences.app.name);
const logo = computed(() => preferences.logo.source);
const logoDark = computed(() => preferences.logo.sourceDark);
const pageTitle = computed(
  () => runtimeBrandConfig.loginTitle || $t('authentication.pageTitle'),
);
const pageDescription = computed(
  () => runtimeBrandConfig.loginDescription || $t('authentication.pageDesc'),
);
</script>

<template>
  <AuthPageLayout
    :app-name="appName"
    :logo="logo"
    :logo-dark="logoDark"
    :page-description="pageDescription"
    :page-title="pageTitle"
  >
    <!-- 自定义工具栏 -->
    <!-- <template #toolbar></template> -->
  </AuthPageLayout>
</template>
