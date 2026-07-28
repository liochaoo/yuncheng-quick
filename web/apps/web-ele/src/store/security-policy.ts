import type { AuthApi } from '#/api/core/auth';

import { shallowRef } from 'vue';

import { defineStore } from 'pinia';

import { getSecurityPolicyApi } from '#/api';

/** 当前部署环境公开的安全策略，只在内存中保存。 */
export const useSecurityPolicyStore = defineStore('security-policy', () => {
  const policy = shallowRef<AuthApi.SecurityPolicy>();
  let loadingPromise: Promise<AuthApi.SecurityPolicy> | undefined;

  async function load(force = false) {
    if (!force && policy.value) return policy.value;
    if (!force && loadingPromise) return loadingPromise;

    loadingPromise = getSecurityPolicyApi()
      .then((value) => {
        policy.value = value;
        return value;
      })
      .finally(() => {
        loadingPromise = undefined;
      });
    return loadingPromise;
  }

  function $reset() {
    policy.value = undefined;
    loadingPromise = undefined;
  }

  return { $reset, load, policy };
});
