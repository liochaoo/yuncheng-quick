<script lang="ts" setup>
import type { VbenFormSchema } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationRegister, z } from '@vben/common-ui';
import { LOGIN_PATH } from '@vben/constants';

import { ElMessage } from 'element-plus';

import { registerApi, sendRegisterEmailCodeApi } from '#/api';
import { useTianaiCaptcha } from '#/components/tianai-captcha';
import { useSecurityPolicyStore } from '#/store';
import {
  getPasswordValidationMessage,
  USERNAME_INPUT_GUIDE,
  USERNAME_PATTERN,
  USERNAME_VALIDATION_MESSAGE,
} from '#/utils/form-validation';

defineOptions({ name: 'Register' });

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
    description: USERNAME_INPUT_GUIDE,
    fieldName: 'username',
    label: '登录名',
    rules: z
      .string()
      .min(1, { message: '请输入登录名' })
      .regex(USERNAME_PATTERN, USERNAME_VALIDATION_MESSAGE),
  },
  {
    component: 'VbenInput',
    componentProps: {
      placeholder: '请输入姓名',
    },
    fieldName: 'realName',
    label: '姓名',
    rules: z
      .string()
      .min(1, { message: '请输入姓名' })
      .max(64, { message: '姓名不能超过 64 个字符' }),
  },
  {
    component: 'VbenInput',
    componentProps: {
      placeholder: 'example@example.com',
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
      placeholder: '请输入密码',
    },
    fieldName: 'password',
    label: '密码',
    rules: z.string().superRefine((value, context) => {
      const message = getPasswordValidationMessage(
        value,
        securityPolicyStore.policy?.password,
      );
      if (message) {
        context.addIssue({ code: z.ZodIssueCode.custom, message });
      }
    }),
  },
  {
    component: 'VbenInputPassword',
    componentProps: {
      placeholder: '请再次输入密码',
    },
    dependencies: {
      rules(values) {
        return z
          .string()
          .min(1, { message: '请再次输入密码' })
          .refine((value) => value === values.password, {
            message: '两次输入的密码不一致',
          });
      },
      triggerFields: ['password'],
    },
    fieldName: 'confirmPassword',
    label: '确认密码',
  },
]);

/** 校验注册信息并在图形验证通过后发送邮箱验证码。 */
async function handleSendCode() {
  const policy = await securityPolicyStore.load();
  if (!policy.feature.registrationEnabled) {
    ElMessage.warning('用户注册功能未开启');
    throw new Error('用户注册功能未开启');
  }
  const values = await formComponentRef.value?.getFormApi().getValues();
  const username = String(values?.username ?? '').trim();
  const email = String(values?.email ?? '')
    .trim()
    .toLowerCase();

  if (!USERNAME_PATTERN.test(username)) {
    ElMessage.warning('请先输入正确的登录名');
    throw new Error('登录名格式不正确');
  }
  if (!z.string().email().safeParse(email).success) {
    ElMessage.warning('请先输入正确的电子邮箱');
    throw new Error('电子邮箱格式不正确');
  }

  const captchaVerification = await verifyCaptcha('REGISTER_EMAIL');
  if (!captchaVerification) {
    return;
  }

  await sendRegisterEmailCodeApi({
    captchaVerification,
    email,
    username,
  });
  ElMessage.success('验证码已发送');
}

async function handleSubmit(value: Recordable<any>) {
  const policy = await securityPolicyStore.load();
  if (!policy.feature.registrationEnabled) {
    ElMessage.warning('用户注册功能未开启');
    await router.replace(LOGIN_PATH);
    return;
  }
  loading.value = true;
  try {
    await registerApi({
      code: String(value.code ?? '').trim(),
      email: String(value.email ?? '')
        .trim()
        .toLowerCase(),
      password: String(value.password ?? ''),
      realName: String(value.realName ?? '').trim(),
      username: String(value.username ?? '').trim(),
    });
    ElMessage.success('注册成功，请登录');
    await router.replace(LOGIN_PATH);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    const policy = await securityPolicyStore.load();
    if (!policy.feature.registrationEnabled) {
      ElMessage.warning('用户注册功能未开启');
      await router.replace(LOGIN_PATH);
    }
  } catch {
    await router.replace(LOGIN_PATH);
  }
});
</script>

<template>
  <AuthenticationRegister
    ref="formComponentRef"
    :form-schema="formSchema"
    :loading="loading"
    sub-title="填写基本信息并验证邮箱后创建账号"
    submit-button-text="注册"
    title="创建账号"
    @submit="handleSubmit"
  />
</template>
