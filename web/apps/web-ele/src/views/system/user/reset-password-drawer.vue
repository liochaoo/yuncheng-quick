<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import { computed, nextTick, onBeforeUnmount, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus';

import { getUserDetailApi, resetUserPasswordApi } from '#/api/system/user';
import { useLatestRequest } from '#/hooks/use-latest-request';
import { useSecurityPolicyStore } from '#/store';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import {
  createConfirmPasswordValidator,
  createPasswordValidator,
} from '#/utils/form-validation';

interface DrawerOpenData {
  id: string;
}

interface PasswordFormModel {
  confirmPassword: string;
  password: string;
}

const emit = defineEmits<{
  success: [];
}>();

const securityPolicyStore = useSecurityPolicyStore();

const formRef = ref<FormInstance>();
const user = ref<DrawerOpenData>();
const userName = ref('');
const model = reactive<PasswordFormModel>({
  confirmPassword: '',
  password: '',
});
const detailRequest = useLatestRequest();
const initializing = detailRequest.loading;

const rules: FormRules<PasswordFormModel> = {
  confirmPassword: [
    { message: '请再次输入新密码', required: true, trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createConfirmPasswordValidator(() => model.password),
    },
  ],
  password: [
    { message: '请输入新密码', required: true, trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createPasswordValidator(
        () => securityPolicyStore.policy?.password,
        '新密码',
      ),
    },
  ],
};

const title = computed(() =>
  userName.value ? `重置密码：${userName.value}` : '重置密码',
);

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    if (initializing.value) return;
    const valid = await formRef.value?.validate().catch(() => false);
    if (!valid || !user.value) return;

    drawerApi.lock();
    try {
      await resetUserPasswordApi(user.value.id, model.password);
      ElMessage.success('密码重置成功');
      emit('success');
      drawerApi.close();
    } finally {
      drawerApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      detailRequest.invalidate();
      return;
    }
    try {
      await securityPolicyStore.load();
      const currentUser = drawerApi.getData<DrawerOpenData>();
      user.value = currentUser;
      userName.value = '';
      model.password = '';
      model.confirmPassword = '';
      const detail = await detailRequest.execute(() =>
        getUserDetailApi(currentUser.id),
      );
      if (!detail) return;
      userName.value = detail.realName;
      await nextTick();
      formRef.value?.clearValidate();
    } catch {
      drawerApi.close();
    }
  },
});

onBeforeUnmount(detailRequest.invalidate);
</script>

<template>
  <Drawer
    :loading="initializing"
    :title="title"
    :class="BUSINESS_FORM_DRAWER_WIDTH.small"
  >
    <ElForm
      ref="formRef"
      class="px-4"
      label-width="100px"
      :model="model"
      :rules="rules"
      :validate-on-rule-change="false"
    >
      <ElFormItem label="新密码" prop="password">
        <ElInput
          v-model="model.password"
          autocomplete="new-password"
          placeholder="请输入新密码"
          show-password
          type="password"
        />
      </ElFormItem>
      <ElFormItem label="确认密码" prop="confirmPassword">
        <ElInput
          v-model="model.confirmPassword"
          autocomplete="new-password"
          placeholder="请再次输入新密码"
          show-password
          type="password"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
