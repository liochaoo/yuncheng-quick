<script lang="ts" setup>
import { computed } from 'vue';

import { WorkbenchHeader } from '@vben/common-ui';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';

defineOptions({ name: 'Workspace' });

const userStore = useUserStore();

const avatar = computed(
  () => userStore.userInfo?.avatar || preferences.app.defaultAvatar,
);
const displayName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || '用户',
);
</script>

<template>
  <div class="p-5">
    <WorkbenchHeader :avatar>
      <template #title>您好，{{ displayName }}</template>
      <template #description> 欢迎使用 {{ preferences.app.name }} </template>
    </WorkbenchHeader>
  </div>
</template>
