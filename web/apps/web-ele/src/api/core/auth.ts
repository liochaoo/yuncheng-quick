import { authRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    captchaVerification?: string;
    password: string;
    username: string;
  }

  /** Token 接口返回值 */
  export interface TokenResult {
    accessToken: string;
  }

  /** 注册邮箱验证码参数 */
  export interface RegisterEmailCodeParams {
    captchaVerification: string;
    email: string;
    username: string;
  }

  /** 注册参数 */
  export interface RegisterParams {
    code: string;
    email: string;
    password: string;
    realName: string;
    username: string;
  }

  /** 找回密码邮箱验证码参数 */
  export interface PasswordEmailCodeParams {
    captchaVerification: string;
    email: string;
    username: string;
  }

  /** 找回密码参数 */
  export interface PasswordResetParams {
    code: string;
    email: string;
    newPassword: string;
    username: string;
  }

  /** 前端可使用的安全策略 */
  export interface SecurityPolicy {
    captcha: {
      loginEnabled: boolean;
    };
    feature: {
      passwordRecoveryEnabled: boolean;
      profileEmailEnabled: boolean;
      registrationEnabled: boolean;
    };
    password: PasswordPolicy;
  }

  /** 新密码输入规则 */
  export interface PasswordPolicy {
    maxLength: number;
    maxUtf8Bytes: number;
    minLength: number;
    historyCount: number;
    requireDigit: boolean;
    requireLowercase: boolean;
    requireSpecial: boolean;
    requireUppercase: boolean;
    ruleText: string;
  }
}

/** 获取公开的安全策略。 */
export async function getSecurityPolicyApi() {
  return authRequestClient.get<AuthApi.SecurityPolicy>('/auth/security-policy');
}

/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  return authRequestClient.post<AuthApi.TokenResult>('/auth/login', {
    ...data,
    clientType: 'WEB',
  });
}

/**
 * 刷新accessToken
 */
export async function refreshTokenApi() {
  return authRequestClient.post<AuthApi.TokenResult>('/auth/refresh');
}

/**
 * 退出登录
 */
export async function logoutApi() {
  return authRequestClient.post<null>('/auth/logout');
}

/**
 * 发送注册邮箱验证码
 */
export async function sendRegisterEmailCodeApi(
  data: AuthApi.RegisterEmailCodeParams,
) {
  return authRequestClient.post<null>('/auth/register/email-code', data);
}

/**
 * 注册用户
 */
export async function registerApi(data: AuthApi.RegisterParams) {
  return authRequestClient.post<null>('/auth/register', data);
}

/**
 * 发送找回密码邮箱验证码
 */
export async function sendPasswordEmailCodeApi(
  data: AuthApi.PasswordEmailCodeParams,
) {
  return authRequestClient.post<null>('/auth/password/email-code', data);
}

/**
 * 通过邮箱验证码重置密码
 */
export async function resetPasswordApi(data: AuthApi.PasswordResetParams) {
  return authRequestClient.post<null>('/auth/password/reset', data);
}

/**
 * 获取用户权限码
 */
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/auth/codes');
}
