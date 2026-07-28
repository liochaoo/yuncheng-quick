import type { AuthApi } from '#/api/core/auth';
import type { CurrentUserInfo } from '#/api/core/user';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { resetAllStores, useAccessStore, useUserStore } from '@vben/stores';

import { ElNotification } from 'element-plus';
import { defineStore } from 'pinia';

import { getAccessCodesApi, getUserInfoApi, loginApi, logoutApi } from '#/api';
import { $t } from '#/locales';

type AuthenticatedUserInfo = Omit<CurrentUserInfo, 'avatar'> & {
  avatar: string;
};

export const useAuthStore = defineStore('auth', () => {
  const accessStore = useAccessStore();
  const userStore = useUserStore();
  const router = useRouter();

  const accessCodesLoaded = ref(false);
  const loginLoading = ref(false);

  /**
   * 异步处理登录操作
   * Asynchronously handle the login process
   * @param params 登录表单数据
   */
  async function authLogin(
    params: AuthApi.LoginParams,
    onSuccess?: () => Promise<void> | void,
  ) {
    // 异步处理用户登录操作并获取 accessToken
    let userInfo: AuthenticatedUserInfo;
    let sessionCreated = false;
    try {
      loginLoading.value = true;
      const loginResult = await loginApi(params);
      sessionCreated = true;
      const { accessToken } = loginResult;

      // 如果成功获取到 accessToken
      if (accessToken) {
        // 将 accessToken 存储到 accessStore 中
        accessStore.setAccessToken(accessToken);

        // 登录状态只有在用户信息和权限码都加载完成后才算初始化成功。
        const [fetchUserInfoResult] = await Promise.all([
          fetchUserInfo(),
          fetchAccessCodes(),
        ]);

        userInfo = fetchUserInfoResult;

        if (accessStore.loginExpired) {
          accessStore.setLoginExpired(false);
        } else {
          onSuccess
            ? await onSuccess?.()
            : await router.push(
                userInfo.homePath || preferences.app.defaultHomePath,
              );
        }

        if (userInfo?.realName) {
          ElNotification({
            message: `${$t('authentication.loginSuccessDesc')}:${userInfo?.realName}`,
            title: $t('authentication.loginSuccess'),
            type: 'success',
          });
        }
      } else {
        throw new Error('登录成功后未获取到 Access Token');
      }
    } catch (error) {
      if (sessionCreated) {
        try {
          await logoutApi();
        } catch {
          // 服务端会话清理失败时仍需清除本地半登录状态。
        }
        resetAllStores();
      }
      throw error;
    } finally {
      loginLoading.value = false;
    }

    return {
      userInfo,
    };
  }

  async function logout(redirect: boolean = true) {
    try {
      await logoutApi();
    } catch {
      // 不做任何处理
    }
    resetAllStores();
    accessStore.setLoginExpired(false);

    // 回登录页带上当前路由地址
    await router.replace({
      path: LOGIN_PATH,
      query: redirect
        ? {
            redirect: encodeURIComponent(router.currentRoute.value.fullPath),
          }
        : {},
    });
  }

  async function fetchUserInfo() {
    const response = await getUserInfoApi();
    const userInfo = {
      ...response,
      avatar: response.avatar || preferences.app.defaultAvatar,
    };
    userStore.setUserInfo(userInfo);
    return userInfo;
  }

  async function fetchAccessCodes() {
    const accessCodes = await getAccessCodesApi();
    accessStore.setAccessCodes(accessCodes);
    accessCodesLoaded.value = true;
    return accessCodes;
  }

  function $reset() {
    accessCodesLoaded.value = false;
    loginLoading.value = false;
  }

  return {
    $reset,
    accessCodesLoaded,
    authLogin,
    fetchAccessCodes,
    fetchUserInfo,
    loginLoading,
    logout,
  };
});
