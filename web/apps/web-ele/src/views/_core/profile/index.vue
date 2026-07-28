<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';

import { Profile } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';

import { useSecurityPolicyStore } from '#/store';

import ProfileBasic from './profile-basic.vue';
import ProfileEmail from './profile-email.vue';
import ProfilePassword from './profile-password.vue';

defineOptions({ name: 'ProfileCenter' });

const userStore = useUserStore();
const securityPolicyStore = useSecurityPolicyStore();
const activeTab = ref('basic');
const tabs = computed(() => {
  const items = [{ label: '基本信息', value: 'basic' }];
  if (securityPolicyStore.policy?.feature.profileEmailEnabled) {
    items.push({ label: '电子邮箱', value: 'email' });
  }
  items.push({ label: '修改密码', value: 'password' });
  return items;
});

onMounted(() => {
  void securityPolicyStore.load().catch(() => undefined);
});
</script>

<template>
  <Profile
    v-model:model-value="activeTab"
    :tabs="tabs"
    title="个人中心"
    :user-info="userStore.userInfo"
  >
    <template #content>
      <ProfileBasic v-if="activeTab === 'basic'" />
      <ProfileEmail v-if="activeTab === 'email'" />
      <ProfilePassword v-if="activeTab === 'password'" />
    </template>
  </Profile>
</template>
