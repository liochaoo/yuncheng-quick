import type { FormItemRule } from 'element-plus';

import type { AuthApi } from '#/api/core/auth';

interface UniqueValidatorOptions {
  check: (value: string) => Promise<boolean>;
  delay?: number;
  message: string;
  normalize?: (value: string) => string;
}

export const USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9._-]{2,49}$/;
export const USERNAME_INPUT_GUIDE =
  '3～50 个字符，以字母开头，可使用字母、数字和常用连接符号';
export const USERNAME_VALIDATION_MESSAGE = '登录名格式不正确';
export const PHONE_PATTERN = /^1[3-9]\d{9}$/;
export const PHONE_VALIDATION_MESSAGE = '手机号码格式不正确';

const SPECIAL_CHARACTERS = new Set('~!@#$%^&*()_+-={}[]|:;"\'<>,.?/`\\');

/** 返回密码校验错误；没有错误时返回 undefined。 */
export function getPasswordValidationMessage(
  value: unknown,
  policy: AuthApi.PasswordPolicy | undefined,
  fieldName = '密码',
): string | undefined {
  if (!policy) return '安全策略尚未加载，请稍后重试';

  const password = String(value ?? '');
  if (!password) return `请输入${fieldName}`;

  const length = [...password].length;
  if (length < policy.minLength || length > policy.maxLength) {
    return `${fieldName}长度应为 ${policy.minLength}～${policy.maxLength} 个字符`;
  }

  const missingRequirements: string[] = [];
  if (policy.requireLowercase && !/\p{Ll}/u.test(password)) {
    missingRequirements.push('小写字母');
  }
  if (policy.requireUppercase && !/\p{Lu}/u.test(password)) {
    missingRequirements.push('大写字母');
  }
  if (policy.requireDigit && !/\p{Nd}/u.test(password)) {
    missingRequirements.push('数字');
  }
  if (
    policy.requireSpecial &&
    ![...password].some((value) => SPECIAL_CHARACTERS.has(value))
  ) {
    missingRequirements.push('特殊字符');
  }
  if (missingRequirements.length > 0) {
    return `${fieldName}须包含${missingRequirements.join('、')}`;
  }
  if (new TextEncoder().encode(password).length > policy.maxUtf8Bytes) {
    return `${fieldName}内容过长，请适当缩短`;
  }
  return undefined;
}

/** 创建两次密码输入一致性校验器。 */
export function createConfirmPasswordValidator(
  getPassword: () => string,
): NonNullable<FormItemRule['validator']> {
  return (_rule, value, callback) => {
    callback(
      String(value ?? '') === getPassword()
        ? undefined
        : new Error('两次输入的密码不一致'),
    );
  };
}

/** 校验当前密码摘要算法可接受的明文长度。 */
export function createPasswordValidator(
  getPolicy: () => AuthApi.PasswordPolicy | undefined,
  fieldName = '密码',
): NonNullable<FormItemRule['validator']> {
  return (_rule, value, callback) => {
    const message = getPasswordValidationMessage(value, getPolicy(), fieldName);
    callback(message ? new Error(message) : undefined);
  };
}

/**
 * 创建异步唯一性校验器。
 *
 * 延迟期间发生的新校验会使旧校验正常结束但不再更新字段错误，避免慢响应覆盖新值。
 */
export function createUniqueValidator(
  options: UniqueValidatorOptions,
): NonNullable<FormItemRule['validator']> {
  let sequence = 0;
  return (_rule, value, callback) => {
    const current = ++sequence;
    const normalized = options.normalize
      ? options.normalize(String(value ?? ''))
      : String(value ?? '').trim();
    if (!normalized) {
      callback();
      return;
    }

    window.setTimeout(() => {
      if (current !== sequence) {
        callback();
        return;
      }
      void options
        .check(normalized)
        .then((available) => {
          if (current !== sequence) {
            callback();
            return;
          }
          callback(available ? undefined : new Error(options.message));
        })
        .catch(() => {
          if (current !== sequence) {
            callback();
            return;
          }
          callback(new Error('暂时无法完成唯一性校验，请稍后重试'));
        });
    }, options.delay ?? 300);
  };
}
