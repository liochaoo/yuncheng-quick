interface RequestConfigLike {
  method?: string;
  url?: string;
}

const EXPERIENCE_ENABLED_KEY = 'yc-quick-experience-enabled';
const FORBIDDEN_MESSAGE = '当前体验环境不允许该操作';

interface ForbiddenApiRule {
  method: string;
  pattern: RegExp;
}

const forbiddenApiRules: ForbiddenApiRule[] = [
  {
    method: 'PUT',
    pattern: /^\/user\/profile\/password$/,
  },
  {
    method: 'PUT',
    pattern: /^\/system\/users\/[^/]+\/password$/,
  },
  {
    method: 'POST',
    pattern: /^\/auth\/password\/reset$/,
  },
  {
    method: 'PUT',
    pattern: /^\/system\/menus\/[^/]+$/,
  },
  {
    method: 'DELETE',
    pattern: /^\/system\/menus\/[^/]+$/,
  },
];

export function cacheExperienceEnabled(enabled: boolean) {
  window.localStorage.setItem(
    EXPERIENCE_ENABLED_KEY,
    enabled ? 'true' : 'false',
  );
}

export function isExperienceEnabled() {
  return window.localStorage.getItem(EXPERIENCE_ENABLED_KEY) === 'true';
}

export function shouldBlockExperienceRequest(config: RequestConfigLike) {
  if (!isExperienceEnabled()) {
    return false;
  }
  const method = (config.method ?? 'GET').toUpperCase();
  const path = normalizeRequestPath(config.url ?? '');
  return forbiddenApiRules.some(
    (rule) => rule.method === method && rule.pattern.test(path),
  );
}

export function createExperienceForbiddenError() {
  return Object.assign(new Error(FORBIDDEN_MESSAGE), {
    __experienceForbidden: true,
  });
}

export function isExperienceForbiddenError(error: unknown) {
  return Boolean(
    error && typeof error === 'object' && '__experienceForbidden' in error,
  );
}

export function experienceForbiddenMessage() {
  return FORBIDDEN_MESSAGE;
}

function normalizeRequestPath(url: string) {
  if (!url) {
    return '';
  }
  try {
    return stripApiPrefix(new URL(url, window.location.origin).pathname);
  } catch {
    return stripApiPrefix(url.split('?')[0] ?? '');
  }
}

function stripApiPrefix(path: string) {
  const normalized = path.startsWith('/') ? path : `/${path}`;
  if (normalized === '/api') {
    return '/';
  }
  return normalized.startsWith('/api/')
    ? normalized.slice('/api'.length)
    : normalized;
}
