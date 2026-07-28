<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, onMounted } from 'vue';

import { AuthenticationLogin, z } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { useTianaiCaptcha } from '#/components/tianai-captcha';
import { useAuthStore, useSecurityPolicyStore } from '#/store';

defineOptions({ name: 'Login' });

const authStore = useAuthStore();
const securityPolicyStore = useSecurityPolicyStore();
const { verify: verifyCaptcha } = useTianaiCaptcha();

async function handleSubmit(values: Recordable<any>) {
  const policy = await securityPolicyStore.load();
  const captchaVerification = policy.captcha.loginEnabled
    ? await verifyCaptcha('LOGIN')
    : undefined;
  if (policy.captcha.loginEnabled && !captchaVerification) return;

  return authStore.authLogin({
    captchaVerification: captchaVerification ?? undefined,
    password: String(values.password ?? ''),
    username: String(values.username ?? ''),
  });
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

onMounted(() => {
  void securityPolicyStore.load().catch(() => undefined);
});
</script>

<template>
  <AuthenticationLogin
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
</template>
