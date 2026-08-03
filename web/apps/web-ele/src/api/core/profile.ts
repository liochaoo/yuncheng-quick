import type { FileApi } from './file';

import { requestClient } from '#/api/request';

export namespace ProfileApi {
  /** 当前用户的归属组织路径摘要。 */
  export interface Org {
    fullPath: string;
    id: string;
  }

  /** 当前登录用户个人资料 */
  export interface Info {
    avatar: null | string;
    avatarFile: FileApi.Record | null;
    createdAt: string;
    email: null | string;
    enabled: boolean;
    orgs: Org[];
    phone: null | string;
    passwordChangedAt: string;
    primaryOrgId: string;
    realName: string;
    roleNames: string[];
    userId: string;
    username: string;
  }

  /** 发送修改邮箱验证码参数 */
  export interface EmailCodeParams {
    captchaVerification: string;
    currentPassword: string;
    email: string;
  }

  /** 修改邮箱参数 */
  export interface EmailChangeParams {
    code: string;
    currentPassword: string;
    email: string;
  }

  /** 修改密码参数 */
  export interface PasswordChangeParams {
    currentPassword: string;
    newPassword: string;
  }
}

/** 获取当前用户个人资料。 */
export async function getProfileApi() {
  return requestClient.get<ProfileApi.Info>('/user/profile');
}

/** 发送修改邮箱验证码。 */
export async function sendProfileEmailCodeApi(
  data: ProfileApi.EmailCodeParams,
) {
  return requestClient.post<null>('/user/profile/email-code', data);
}

/** 修改当前用户邮箱。 */
export async function changeProfileEmailApi(
  data: ProfileApi.EmailChangeParams,
) {
  return requestClient.put<null>('/user/profile/email', data);
}

/** 修改当前用户密码。 */
export async function changeProfilePasswordApi(
  data: ProfileApi.PasswordChangeParams,
) {
  return requestClient.put<null>('/user/profile/password', data);
}

/** 上传并替换当前用户头像。 */
export async function uploadProfileAvatarApi(file: File) {
  return requestClient.upload<FileApi.Record>('/user/profile/avatar', {
    file,
  });
}

/** 删除当前用户头像。 */
export async function deleteProfileAvatarApi() {
  return requestClient.delete<null>('/user/profile/avatar');
}
