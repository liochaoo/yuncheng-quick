import { requestClient } from '#/api/request';

export interface SecurityPolicy {
  captcha: {
    loginEnabled: boolean;
  };
  feature: {
    passwordRecoveryEnabled: boolean;
    profileEmailEnabled: boolean;
    registrationEnabled: boolean;
  };
  loginFailure: {
    lockMinutes: number;
    maxFailedAttempts: number;
    windowMinutes: number;
  };
  defaultPassword: {
    configured: boolean;
  };
  password: {
    historyCount: number;
    maxLength: number;
    minLength: number;
    requireDigit: boolean;
    requireLowercase: boolean;
    requireSpecial: boolean;
    requireUppercase: boolean;
  };
}

export type SecurityPolicyUpdateRequest = Omit<
  SecurityPolicy,
  'defaultPassword'
> & {
  defaultPassword: {
    password?: string;
  };
};

/** 读取当前实际生效的安全策略。 */
export async function getSecurityPolicyManagementApi() {
  return requestClient.get<SecurityPolicy>('/system/security');
}

/** 完整保存安全策略。 */
export async function updateSecurityPolicyApi(
  data: SecurityPolicyUpdateRequest,
) {
  return requestClient.put<SecurityPolicy>('/system/security', data);
}
