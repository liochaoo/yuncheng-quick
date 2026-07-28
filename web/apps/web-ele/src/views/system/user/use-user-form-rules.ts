import type { FormRules } from 'element-plus';

import type { Ref } from 'vue';

import type { UserFormModel } from './user-form-model';

import { computed } from 'vue';

import { checkUserUniquenessApi } from '#/api/system/user';
import { useSecurityPolicyStore } from '#/store';
import {
  createConfirmPasswordValidator,
  createPasswordValidator,
  createUniqueValidator,
  PHONE_PATTERN,
  PHONE_VALIDATION_MESSAGE,
  USERNAME_PATTERN,
  USERNAME_VALIDATION_MESSAGE,
} from '#/utils/form-validation';

export function useUserFormRules(
  model: UserFormModel,
  isCreate: Readonly<Ref<boolean>>,
  recordId: Readonly<Ref<string | undefined>>,
) {
  const securityPolicyStore = useSecurityPolicyStore();
  void securityPolicyStore.load().catch(() => undefined);

  return computed<FormRules<UserFormModel>>(() => ({
    confirmPassword: isCreate.value
      ? [
          { message: '请再次输入初始密码', required: true, trigger: 'blur' },
          {
            trigger: 'blur',
            validator: createConfirmPasswordValidator(() => model.password),
          },
        ]
      : [],
    email: [
      { max: 254, message: '邮箱不能超过 254 个字符', trigger: 'blur' },
      { message: '邮箱格式不正确', trigger: 'blur', type: 'email' },
      {
        trigger: 'blur',
        validator: createUniqueValidator({
          check: async (value) => {
            const result = await checkUserUniquenessApi({
              field: 'EMAIL',
              id: recordId.value,
              value,
            });
            return result.available;
          },
          message: '邮箱已存在',
          normalize: (value) => value.trim().toLowerCase(),
        }),
      },
    ],
    password: isCreate.value
      ? [
          { message: '请输入初始密码', required: true, trigger: 'blur' },
          {
            trigger: 'blur',
            validator: createPasswordValidator(
              () => securityPolicyStore.policy?.password,
              '初始密码',
            ),
          },
        ]
      : [],
    phone: [
      {
        message: PHONE_VALIDATION_MESSAGE,
        pattern: PHONE_PATTERN,
        trigger: 'blur',
      },
      {
        trigger: 'blur',
        validator: createUniqueValidator({
          check: async (value) => {
            const result = await checkUserUniquenessApi({
              field: 'PHONE',
              id: recordId.value,
              value,
            });
            return result.available;
          },
          message: '手机号码已存在',
        }),
      },
    ],
    realName: [
      { message: '请输入姓名', required: true, trigger: 'blur' },
      { max: 64, message: '姓名不能超过 64 个字符', trigger: 'blur' },
    ],
    roleIds: [
      {
        message: '请至少选择一个角色',
        required: true,
        trigger: 'change',
        type: 'array',
      },
    ],
    username: isCreate.value
      ? [
          { message: '请输入登录名', required: true, trigger: 'blur' },
          {
            message: USERNAME_VALIDATION_MESSAGE,
            pattern: USERNAME_PATTERN,
            trigger: 'blur',
          },
          {
            trigger: 'blur',
            validator: createUniqueValidator({
              check: async (value) => {
                const result = await checkUserUniquenessApi({
                  field: 'USERNAME',
                  value,
                });
                return result.available;
              },
              message: '登录名已存在',
            }),
          },
        ]
      : [],
  }));
}
