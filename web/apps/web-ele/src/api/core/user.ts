import { requestClient } from '#/api/request';

/** 当前登录用户信息，与后端响应保持一致。 */
export interface CurrentUserInfo {
  avatar: null | string;
  homePath: string;
  realName: string;
  roles: string[];
  userId: string;
  username: string;
}

/**
 * 获取用户信息
 */
export async function getUserInfoApi() {
  return requestClient.get<CurrentUserInfo>('/user/info');
}
