<script lang="ts" setup>
import type { SecurityPolicy } from '#/api/system/security';

import { onMounted, ref } from 'vue';

import { useAccess } from '@vben/access';
import { Page } from '@vben/common-ui';

import {
  ElButton,
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElMessage,
  ElSwitch,
} from 'element-plus';

import {
  getSecurityPolicyManagementApi,
  updateSecurityPolicyApi,
} from '#/api/system/security';
import FormSection from '#/components/form/form-section.vue';
import { useSecurityPolicyStore } from '#/store';

import { SECURITY_PERMISSION_CODES } from './permission-codes';

defineOptions({ name: 'SystemSecurity' });

const { hasAccessByCodes } = useAccess();
const canEdit = hasAccessByCodes([SECURITY_PERMISSION_CODES.EDIT]);
const securityPolicyStore = useSecurityPolicyStore();
const loading = ref(false);
const model = ref<SecurityPolicy>();
const saving = ref(false);

async function load() {
  loading.value = true;
  try {
    model.value = await getSecurityPolicyManagementApi();
  } finally {
    loading.value = false;
  }
}

async function save() {
  if (!model.value || !canEdit) return;
  if (model.value.password.minLength > model.value.password.maxLength) {
    ElMessage.warning('密码最大长度不能小于最小长度');
    return;
  }
  saving.value = true;
  try {
    model.value = await updateSecurityPolicyApi(model.value);
    securityPolicyStore.$reset();
    ElMessage.success('安全策略保存成功');
  } finally {
    saving.value = false;
  }
}

onMounted(() => void load());
</script>

<template>
  <Page auto-content-height>
    <section
      v-loading="loading"
      class="flex h-full min-h-0 flex-col overflow-hidden rounded-lg border bg-card"
    >
      <header class="flex items-center justify-between gap-4 border-b p-4">
        <div class="text-base font-medium">安全管理</div>
        <ElButton
          v-if="canEdit"
          :disabled="!model"
          :loading="saving"
          type="primary"
          @click="save"
        >
          保存设置
        </ElButton>
      </header>

      <div v-if="model" class="min-h-0 flex-1 overflow-auto p-6">
        <ElForm
          class="security-form w-full"
          label-position="left"
          :model="model"
        >
          <FormSection title="功能开关">
            <div class="security-option-grid rounded-lg border px-5 py-2">
              <ElFormItem class="mb-0 py-3" label="开放用户注册">
                <ElSwitch
                  v-model="model.feature.registrationEnabled"
                  :disabled="!canEdit"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="开放邮箱找回密码">
                <ElSwitch
                  v-model="model.feature.passwordRecoveryEnabled"
                  :disabled="!canEdit"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="允许绑定或修改邮箱">
                <ElSwitch
                  v-model="model.feature.profileEmailEnabled"
                  :disabled="!canEdit"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </ElFormItem>
            </div>
          </FormSection>

          <FormSection title="登录验证">
            <div class="security-option-grid rounded-lg border px-5 py-2">
              <ElFormItem class="mb-0 py-3" label="登录图形验证码">
                <ElSwitch
                  v-model="model.captcha.loginEnabled"
                  :disabled="!canEdit"
                  active-text="开启"
                  inactive-text="关闭"
                />
              </ElFormItem>
            </div>
          </FormSection>

          <FormSection title="登录失败控制">
            <div class="security-option-grid rounded-lg border px-5 py-2">
              <ElFormItem class="mb-0 py-3" label="最大失败次数">
                <ElInputNumber
                  v-model="model.loginFailure.maxFailedAttempts"
                  :disabled="!canEdit"
                  :max="20"
                  :min="3"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="观察窗口（分钟）">
                <ElInputNumber
                  v-model="model.loginFailure.windowMinutes"
                  :disabled="!canEdit"
                  :max="1440"
                  :min="1"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="锁定时长（分钟）">
                <ElInputNumber
                  v-model="model.loginFailure.lockMinutes"
                  :disabled="!canEdit"
                  :max="1440"
                  :min="1"
                />
              </ElFormItem>
            </div>
          </FormSection>

          <FormSection title="密码规则">
            <div class="security-option-grid rounded-lg border px-5 py-2">
              <ElFormItem class="mb-0 py-3" label="密码最小长度">
                <ElInputNumber
                  v-model="model.password.minLength"
                  :disabled="!canEdit"
                  :max="64"
                  :min="1"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="密码最大长度">
                <ElInputNumber
                  v-model="model.password.maxLength"
                  :disabled="!canEdit"
                  :max="64"
                  :min="1"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="禁止重复最近密码">
                <ElInputNumber
                  v-model="model.password.historyCount"
                  :disabled="!canEdit"
                  :max="10"
                  :min="1"
                />
                <span class="ml-2 text-muted-foreground">次</span>
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="必须包含小写字母">
                <ElSwitch
                  v-model="model.password.requireLowercase"
                  :disabled="!canEdit"
                  active-text="是"
                  inactive-text="否"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="必须包含大写字母">
                <ElSwitch
                  v-model="model.password.requireUppercase"
                  :disabled="!canEdit"
                  active-text="是"
                  inactive-text="否"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="必须包含数字">
                <ElSwitch
                  v-model="model.password.requireDigit"
                  :disabled="!canEdit"
                  active-text="是"
                  inactive-text="否"
                />
              </ElFormItem>
              <ElFormItem class="mb-0 py-3" label="必须包含特殊字符">
                <ElSwitch
                  v-model="model.password.requireSpecial"
                  :disabled="!canEdit"
                  active-text="是"
                  inactive-text="否"
                />
              </ElFormItem>
            </div>
          </FormSection>
        </ElForm>
      </div>
    </section>
  </Page>
</template>

<style scoped>
.security-option-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  column-gap: 32px;
}

:deep(.security-form .el-form-item__label) {
  width: auto !important;
  margin-right: 20px;
}

:deep(.security-form .el-form-item__content) {
  flex: none;
}

@media (width <= 640px) {
  .security-option-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
