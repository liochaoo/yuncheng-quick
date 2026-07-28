<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import { onMounted, onUnmounted, reactive, ref } from 'vue';

import { ElButton, ElForm, ElFormItem, ElInput, ElMessage } from 'element-plus';

import {
  changeProfileEmailApi,
  getProfileApi,
  sendProfileEmailCodeApi,
} from '#/api';
import DetailSection from '#/components/detail/detail-section.vue';
import { useTianaiCaptcha } from '#/components/tianai-captcha';

interface EmailFormModel {
  code: string;
  currentPassword: string;
  email: string;
}

const CODE_LENGTH = 6;
const formRef = ref<FormInstance>();
const { verify: verifyCaptcha } = useTianaiCaptcha();
const currentEmail = ref<string>();
const loading = ref(false);
const sending = ref(false);
const countdown = ref(0);
let countdownTimer: number | undefined;

const model = reactive<EmailFormModel>({
  code: '',
  currentPassword: '',
  email: '',
});

const rules: FormRules<EmailFormModel> = {
  code: [
    { message: '请输入邮箱验证码', required: true, trigger: 'blur' },
    {
      len: CODE_LENGTH,
      message: `验证码必须为 ${CODE_LENGTH} 位`,
      trigger: 'blur',
    },
  ],
  currentPassword: [
    { message: '请输入当前密码', required: true, trigger: 'blur' },
  ],
  email: [
    { message: '请输入新邮箱', required: true, trigger: 'blur' },
    { message: '新邮箱格式不正确', trigger: 'blur', type: 'email' },
    { max: 254, message: '新邮箱不能超过 254 个字符', trigger: 'blur' },
  ],
};

async function loadProfile() {
  const profile = await getProfileApi();
  currentEmail.value = profile.email ?? undefined;
}

function startCountdown() {
  window.clearInterval(countdownTimer);
  countdown.value = 60;
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      window.clearInterval(countdownTimer);
      countdownTimer = undefined;
    }
  }, 1000);
}

/** 校验邮箱和当前密码，并在图形验证通过后发送邮箱验证码。 */
async function handleSendCode() {
  if (countdown.value > 0 || sending.value) return;
  const valid = await formRef.value
    ?.validateField(['email', 'currentPassword'])
    .then(() => true)
    .catch(() => false);
  if (!valid) return;
  sending.value = true;
  try {
    const captchaVerification = await verifyCaptcha('CHANGE_EMAIL');
    if (!captchaVerification) return;

    await sendProfileEmailCodeApi({
      captchaVerification,
      currentPassword: model.currentPassword,
      email: model.email.trim().toLowerCase(),
    });
    startCountdown();
    ElMessage.success('验证码已发送');
  } finally {
    sending.value = false;
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await changeProfileEmailApi({
      code: model.code.trim(),
      currentPassword: model.currentPassword,
      email: model.email.trim().toLowerCase(),
    });
    ElMessage.success('电子邮箱修改成功');
    formRef.value?.resetFields();
    window.clearInterval(countdownTimer);
    countdown.value = 0;
    await loadProfile();
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadProfile();
});
onUnmounted(() => window.clearInterval(countdownTimer));
</script>

<template>
  <DetailSection
    description="验证码将发送到新邮箱；为确认本人操作，发送和保存时均需校验当前密码。"
    title="修改电子邮箱"
  >
    <div class="mb-5 rounded-md border border-border bg-muted/30 px-4 py-3">
      <span class="text-sm text-muted-foreground">当前邮箱：</span>
      <span class="text-sm text-foreground">{{
        currentEmail || '未绑定'
      }}</span>
    </div>
    <ElForm
      ref="formRef"
      class="max-w-2xl"
      label-position="top"
      :model="model"
      :rules="rules"
    >
      <ElFormItem label="新邮箱" prop="email">
        <ElInput
          v-model="model.email"
          autocomplete="email"
          maxlength="254"
          placeholder="请输入新邮箱"
        />
      </ElFormItem>
      <ElFormItem label="当前密码" prop="currentPassword">
        <ElInput
          v-model="model.currentPassword"
          autocomplete="current-password"
          placeholder="请输入当前密码"
          show-password
          type="password"
        />
      </ElFormItem>
      <ElFormItem label="邮箱验证码" prop="code">
        <div class="flex w-full gap-3">
          <ElInput
            v-model="model.code"
            class="flex-1"
            :maxlength="CODE_LENGTH"
            placeholder="请输入验证码"
          />
          <ElButton
            :disabled="countdown > 0"
            :loading="sending"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? `${countdown} 秒后重发` : '发送验证码' }}
          </ElButton>
        </div>
      </ElFormItem>
      <ElFormItem>
        <ElButton type="primary" :loading="loading" @click="handleSubmit">
          保存邮箱
        </ElButton>
      </ElFormItem>
    </ElForm>
  </DetailSection>
</template>
