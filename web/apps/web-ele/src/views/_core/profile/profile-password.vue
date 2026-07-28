<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import { onMounted, reactive, ref } from 'vue';

import { ElButton, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus';

import { changeProfilePasswordApi } from '#/api';
import DetailSection from '#/components/detail/detail-section.vue';
import { useAuthStore, useSecurityPolicyStore } from '#/store';
import {
  createConfirmPasswordValidator,
  createPasswordValidator,
} from '#/utils/form-validation';

interface PasswordFormModel {
  confirmPassword: string;
  currentPassword: string;
  newPassword: string;
}

const authStore = useAuthStore();
const securityPolicyStore = useSecurityPolicyStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const model = reactive<PasswordFormModel>({
  confirmPassword: '',
  currentPassword: '',
  newPassword: '',
});

const rules: FormRules<PasswordFormModel> = {
  confirmPassword: [
    { message: '请再次输入新密码', required: true, trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createConfirmPasswordValidator(() => model.newPassword),
    },
  ],
  currentPassword: [
    { message: '请输入当前密码', required: true, trigger: 'blur' },
  ],
  newPassword: [
    { message: '请输入新密码', required: true, trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createPasswordValidator(
        () => securityPolicyStore.policy?.password,
        '新密码',
      ),
    },
    {
      trigger: 'blur',
      validator: (_rule, value, callback) => {
        callback(
          String(value ?? '') === model.currentPassword
            ? new Error('新密码不能与当前密码相同')
            : undefined,
        );
      },
    },
  ],
};

onMounted(() => {
  void securityPolicyStore.load().catch(() => undefined);
});

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await changeProfilePasswordApi({
      currentPassword: model.currentPassword,
      newPassword: model.newPassword,
    });
    ElMessage.success('密码修改成功，请重新登录');
    await authStore.logout(false);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <DetailSection
    description="修改成功后将注销当前账号的全部登录会话。"
    title="修改密码"
  >
    <ElForm
      ref="formRef"
      class="max-w-2xl"
      label-position="top"
      :model="model"
      :rules="rules"
    >
      <ElFormItem label="当前密码" prop="currentPassword">
        <ElInput
          v-model="model.currentPassword"
          autocomplete="current-password"
          placeholder="请输入当前密码"
          show-password
          type="password"
        />
      </ElFormItem>
      <ElFormItem label="新密码" prop="newPassword">
        <ElInput
          v-model="model.newPassword"
          autocomplete="new-password"
          placeholder="请输入新密码"
          show-password
          type="password"
        />
      </ElFormItem>
      <ElFormItem label="确认新密码" prop="confirmPassword">
        <ElInput
          v-model="model.confirmPassword"
          autocomplete="new-password"
          placeholder="请再次输入新密码"
          show-password
          type="password"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton type="primary" :loading="loading" @click="handleSubmit">
          修改密码
        </ElButton>
      </ElFormItem>
    </ElForm>
  </DetailSection>
</template>
