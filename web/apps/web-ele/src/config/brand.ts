import type { Preferences } from '@vben/preferences';

import { reactive, readonly } from 'vue';

export interface RuntimeBrandConfig {
  appName?: string;
  copyrightCompanyName?: string;
  loginDescription?: string;
  loginTitle?: string;
}

type RuntimeBrandPreferences = {
  app?: Pick<Preferences['app'], 'name'>;
  copyright?: Pick<Preferences['copyright'], 'companyName'>;
};

const BRAND_CONFIG_URL = `${import.meta.env.BASE_URL}brand-config.json`;
const runtimeBrandConfigState = reactive<RuntimeBrandConfig>({});

function normalizeText(value: unknown) {
  return typeof value === 'string' && value.trim().length > 0
    ? value.trim()
    : undefined;
}

function normalizeRuntimeBrandConfig(value: unknown): RuntimeBrandConfig {
  if (!value || typeof value !== 'object') {
    return {};
  }
  const source = value as Record<string, unknown>;
  return {
    appName: normalizeText(source.appName),
    copyrightCompanyName: normalizeText(source.copyrightCompanyName),
    loginDescription: normalizeText(source.loginDescription),
    loginTitle: normalizeText(source.loginTitle),
  };
}

export const runtimeBrandConfig = readonly(runtimeBrandConfigState);

export async function loadRuntimeBrandConfig() {
  try {
    const response = await fetch(BRAND_CONFIG_URL, { cache: 'no-store' });
    if (!response.ok) {
      return {};
    }
    const config = normalizeRuntimeBrandConfig(await response.json());
    Object.assign(runtimeBrandConfigState, config);
    return config;
  } catch {
    return {};
  }
}

export function toBrandPreferences(config: RuntimeBrandConfig) {
  const preferences: RuntimeBrandPreferences = {};
  if (config.appName) {
    preferences.app = { name: config.appName };
  }
  if (config.copyrightCompanyName) {
    preferences.copyright = { companyName: config.copyrightCompanyName };
  }
  return preferences;
}
