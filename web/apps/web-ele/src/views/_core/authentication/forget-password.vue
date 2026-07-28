<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationForgetPassword, z } from '@vben/common-ui';
import { LOGIN_PATH } from '@vben/constants';

import { ElMessage } from 'element-plus';

import { resetPasswordApi, sendPasswordEmailCodeApi } from '#/api';
import { useTianaiCaptcha } from '#/components/tianai-captcha';
import { useSecurityPolicyStore } from '#/store';
import { getPasswordValidationMessage } from '#/utils/form-validation';

defineOptions({ name: 'ForgetPassword' });

const CODE_LENGTH = 6;

const formComponentRef = ref<{
  getFormApi: () => {
    getValues: () => Promise<Recordable<any>>;
  };
}>();
const loading = ref(false);
const router = useRouter();
const securityPolicyStore = useSecurityPolicyStore();
const { verify: verifyCaptcha } = useTianaiCaptcha();

const formSchema = computed((): VbenFormSchema[] => [
  {
    component: 'VbenInput',
    componentProps: {
      placeholder: '请输入登录名',
    },
    fieldName: 'username',
    label: '登录名',
    rules: z.string().min(1, { message: '请输入登录名' }),
  },
  {
    component: 'VbenInput',
    componentProps: {
      placeholder: '请输入账号绑定的电子邮箱',
    },
    fieldName: 'email',
    label: '电子邮箱',
    rules: z
      .string()
      .min(1, { message: '请输入电子邮箱' })
      .max(254, { message: '电子邮箱不能超过 254 个字符' })
      .email('电子邮箱格式不正确'),
  },
  {
    component: 'VbenPinInput',
    componentProps: {
      codeLength: CODE_LENGTH,
      createText: (countdown: number) =>
        countdown > 0 ? `${countdown} 秒后重发` : '发送验证码',
      handleSendCode,
      placeholder: '请输入验证码',
    },
    fieldName: 'code',
    label: '邮箱验证码',
    rules: z.string().length(CODE_LENGTH, {
      message: `请输入 ${CODE_LENGTH} 位验证码`,
    }),
  },
  {
    component: 'VbenInputPassword',
    componentProps: {
      passwordStrength: true,
      placeholder: '请输入新密码',
    },
    fieldName: 'newPassword',
    label: '新密码',
    rules: z.string().superRefine((value, context) => {
      const message = getPasswordValidationMessage(
        value,
        securityPolicyStore.policy?.password,
        '新密码',
      );
      if (message) {
        context.addIssue({ code: z.ZodIssueCode.custom, message });
      }
    }),
  },
  {
    component: 'VbenInputPassword',
    componentProps: {
      placeholder: '请再次输入新密码',
    },
    dependencies: {
      rules(values) {
        return z
          .string()
          .min(1, { message: '请再次输入新密码' })
          .refine((value) => value === values.newPassword, {
            message: '两次输入的密码不一致',
          });
      },
      triggerFields: ['newPassword'],
    },
    fieldName: 'confirmPassword',
    label: '确认新密码',
  },
]);

/** 校验账号信息并在图形验证通过后发送邮箱验证码。 */
async function handleSendCode() {
  const policy = await securityPolicyStore.load();
  if (!policy.feature.passwordRecoveryEnabled) {
    ElMessage.warning('找回密码功能未开启');
    throw new Error('找回密码功能未开启');
  }
  const values = await formComponentRef.value?.getFormApi().getValues();
  const username = String(values?.username ?? '').trim();
  const email = String(values?.email ?? '')
    .trim()
    .toLowerCase();

  if (!username) {
    ElMessage.warning('请先输入登录名');
    throw new Error('登录名不能为空');
  }
  if (!z.string().email().safeParse(email).success) {
    ElMessage.warning('请先输入正确的电子邮箱');
    throw new Error('电子邮箱格式不正确');
  }

  const captchaVerification = await verifyCaptcha('RESET_PASSWORD_EMAIL');
  if (!captchaVerification) {
    return;
  }

  await sendPasswordEmailCodeApi({
    captchaVerification,
    email,
    username,
  });
  ElMessage.success('如果账号与邮箱匹配，验证码将发送到该邮箱');
}

async function handleSubmit(value: Recordable<any>) {
  const policy = await securityPolicyStore.load();
  if (!policy.feature.passwordRecoveryEnabled) {
    ElMessage.warning('找回密码功能未开启');
    await router.replace(LOGIN_PATH);
    return;
  }
  loading.value = true;
  try {
    await resetPasswordApi({
      code: String(value.code ?? '').trim(),
      email: String(value.email ?? '')
        .trim()
        .toLowerCase(),
      newPassword: String(value.newPassword ?? ''),
      username: String(value.username ?? '').trim(),
    });
    ElMessage.success('密码已重置，请使用新密码登录');
    await router.replace(LOGIN_PATH);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    const policy = await securityPolicyStore.load();
    if (!policy.feature.passwordRecoveryEnabled) {
      ElMessage.warning('找回密码功能未开启');
      await router.replace(LOGIN_PATH);
    }
  } catch {
    await router.replace(LOGIN_PATH);
  }
});
</script>

<template>
  <AuthenticationForgetPassword
    ref="formComponentRef"
    :form-schema="formSchema"
    :loading="loading"
    sub-title="验证账号绑定的邮箱后重置密码"
    submit-button-text="重置密码"
    title="找回密码"
    @submit="handleSubmit"
  />
</template>
