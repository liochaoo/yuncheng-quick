<script lang="ts" setup>
import type { ProfileApi } from '#/api';
import type { DetailTableItem } from '#/components/detail/detail-table.types';

import { computed, onMounted, ref } from 'vue';

import { formatDateTime } from '@vben/utils';

import { ElTag } from 'element-plus';

import {
  deleteProfileAvatarApi,
  getProfileApi,
  uploadProfileAvatarApi,
} from '#/api';
import DetailSection from '#/components/detail/detail-section.vue';
import DetailTable from '#/components/detail/detail-table.vue';
import EnabledStatus from '#/components/display/enabled-status.vue';
import { AvatarUpload } from '#/components/file';
import { useAuthStore } from '#/store';

const loading = ref(false);
const profile = ref<ProfileApi.Info>();
const authStore = useAuthStore();

const avatarFile = computed({
  get: () => profile.value?.avatarFile ?? null,
  set: (value) => {
    if (!profile.value) return;
    profile.value.avatarFile = value;
    profile.value.avatar = value?.previewUrl ?? null;
  },
});

const basicItems = computed<DetailTableItem[]>(() => [
  { key: 'username', label: '登录名', value: profile.value?.username },
  { key: 'realName', label: '姓名', value: profile.value?.realName },
  { key: 'phone', label: '手机号码', value: profile.value?.phone },
  { key: 'email', label: '电子邮箱', value: profile.value?.email },
  { key: 'enabled', label: '状态' },
  {
    key: 'createdAt',
    label: '创建时间',
    value: formatDateTime(profile.value?.createdAt),
  },
  {
    key: 'passwordChangedAt',
    label: '密码最后修改时间',
    value: formatDateTime(profile.value?.passwordChangedAt),
  },
  { key: 'roles', label: '角色', span: 2 },
]);

async function loadProfile() {
  loading.value = true;
  try {
    profile.value = await getProfileApi();
  } finally {
    loading.value = false;
  }
}

async function uploadAvatar(file: File) {
  const uploaded = await uploadProfileAvatarApi(file);
  if (profile.value) {
    profile.value.avatar = uploaded.previewUrl;
    profile.value.avatarFile = uploaded;
  }
  try {
    await authStore.fetchUserInfo();
  } catch {
    // 头像已经保存成功，刷新顶部用户信息失败不应把上传结果标记为失败。
  }
  return uploaded;
}

async function deleteAvatar() {
  await deleteProfileAvatarApi();
  if (profile.value) {
    profile.value.avatar = null;
    profile.value.avatarFile = null;
  }
  try {
    await authStore.fetchUserInfo();
  } catch {
    // 头像已经删除成功，刷新顶部用户信息失败不应恢复已经删除的头像。
  }
}

onMounted(loadProfile);
</script>

<template>
  <div v-loading="loading">
    <DetailSection
      description="支持常用图片格式，上传前可以裁剪头像。"
      title="头像设置"
    >
      <AvatarUpload
        v-model="avatarFile"
        :delete-handler="deleteAvatar"
        server-managed-replacement
        :size="120"
        :upload-handler="uploadAvatar"
      />
    </DetailSection>

    <DetailSection title="基本信息">
      <DetailTable :items="basicItems">
        <template #enabled>
          <EnabledStatus v-if="profile" :model-value="profile.enabled" />
        </template>
        <template #roles>
          <div v-if="profile?.roleNames.length" class="flex flex-wrap gap-2">
            <ElTag
              v-for="roleName in profile.roleNames"
              :key="roleName"
              effect="plain"
            >
              {{ roleName }}
            </ElTag>
          </div>
          <span v-else>-</span>
        </template>
      </DetailTable>
    </DetailSection>
  </div>
</template>
