<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, onMounted, ref } from 'vue';

import { AuthenticationLogin, VbenButton, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { ElMessage } from 'element-plus';

import { useTianaiCaptcha } from '#/components/tianai-captcha';
import { useAuthStore, useSecurityPolicyStore } from '#/store';
import { getPasswordValidationMessage } from '#/utils/form-validation';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();
const securityPolicyStore = useSecurityPolicyStore();
const { verify: verifyCaptcha } = useTianaiCaptcha();
const passwordChangeToken = ref('');

async function handleSubmit(values: Recordable<any>) {
  const policy = await securityPolicyStore.load();
  const captchaVerification = policy.captcha.loginEnabled
    ? await verifyCaptcha('LOGIN')
    : undefined;
  if (policy.captcha.loginEnabled && !captchaVerification) return;

  const result = await authStore.authLogin({
    captchaVerification: captchaVerification ?? undefined,
    password: String(values.password ?? ''),
    username: String(values.username ?? ''),
  });
  if (result.passwordChangeRequired) {
    passwordChangeToken.value = result.passwordChangeToken;
  }
}

async function handleRequiredPasswordChange(values: Recordable<any>) {
  try {
    await authStore.changeRequiredPassword({
      newPassword: String(values.newPassword ?? ''),
      passwordChangeToken: passwordChangeToken.value,
    });
  } catch (error) {
    if (
      (error as { response?: { status?: number } })?.response?.status === 401
    ) {
      passwordChangeToken.value = '';
    }
    return;
  }
  passwordChangeToken.value = '';
  ElMessage.success('密码修改成功，请使用新密码重新登录');
}

function returnToLogin() {
  passwordChangeToken.value = '';
}

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      component: 'VbenInput',
      componentProps: {
        placeholder: $t('authentication.usernameTip'),
      },
      fieldName: 'username',
      label: $t('authentication.username'),
      rules: z.string().min(1, { message: $t('authentication.usernameTip') }),
    },
    {
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: $t('authentication.password'),
      },
      fieldName: 'password',
      label: $t('authentication.password'),
      rules: z.string().min(1, { message: $t('authentication.passwordTip') }),
    },
  ];
});

const passwordChangeFormSchema = computed((): VbenFormSchema[] => [
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

onMounted(() => {
  void securityPolicyStore.load().catch(() => undefined);
});
</script>

<template>
  <AuthenticationLogin
    v-if="!passwordChangeToken"
    key="login"
    :form-schema="formSchema"
    :loading="authStore.loginLoading"
    :show-code-login="false"
    :show-forget-password="
      securityPolicyStore.policy?.feature.passwordRecoveryEnabled ?? false
    "
    :show-qrcode-login="false"
    :show-register="
      securityPolicyStore.policy?.feature.registrationEnabled ?? false
    "
    :show-third-party-login="false"
    @submit="handleSubmit"
  />
  <AuthenticationLogin
    v-else
    key="password-change"
    :form-schema="passwordChangeFormSchema"
    :loading="authStore.loginLoading"
    :show-code-login="false"
    :show-forget-password="false"
    :show-qrcode-login="false"
    :show-register="false"
    :show-remember-me="false"
    :show-third-party-login="false"
    sub-title="首次使用系统默认密码登录，请先设置自己的密码"
    submit-button-text="修改密码"
    title="修改初始密码"
    @submit="handleRequiredPasswordChange"
  >
    <template #to-register>
      <VbenButton class="mt-4 w-full" variant="outline" @click="returnToLogin">
        返回登录
      </VbenButton>
    </template>
  </AuthenticationLogin>
</template>
