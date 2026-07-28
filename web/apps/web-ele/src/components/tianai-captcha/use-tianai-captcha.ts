import type {
  CaptchaScene,
  TacConstructor,
  TacInstance,
  TacRequestParam,
  TianaiCaptchaApiResponse,
  TianaiCaptchaVerificationData,
} from './types';

import { onBeforeUnmount, onDeactivated } from 'vue';

import { useAppConfig } from '@vben/hooks';

import { ElMessage } from 'element-plus';

import './tianai-captcha.css';

let loadPromise: null | Promise<TacConstructor> = null;
let containerSequence = 0;

const RESOURCE_LOAD_TIMEOUT = 15_000;
const RESOURCE_STATE_KEY = 'tianaiCaptchaLoadState';

function joinUrl(base: string, path: string) {
  return `${base.replace(/\/$/, '')}/${path.replace(/^\//, '')}`;
}

function getPublicAssetUrl(path: string) {
  return joinUrl(import.meta.env.BASE_URL || '/', path);
}

function loadStylesheet(href: string) {
  return new Promise<void>((resolve, reject) => {
    let link = document.querySelector<HTMLLinkElement>(`link[href="${href}"]`);
    if (link?.sheet || link?.dataset[RESOURCE_STATE_KEY] === 'loaded') {
      resolve();
      return;
    }

    if (link && link.dataset[RESOURCE_STATE_KEY] !== 'loading') {
      link.remove();
      link = null;
    }

    const target = link ?? document.createElement('link');
    let settled = false;
    const cleanup = () => {
      window.clearTimeout(timeoutId);
      target.removeEventListener('load', handleLoad);
      target.removeEventListener('error', handleError);
    };
    const handleLoad = () => {
      if (settled) return;
      settled = true;
      target.dataset[RESOURCE_STATE_KEY] = 'loaded';
      cleanup();
      resolve();
    };
    const handleError = () => {
      if (settled) return;
      settled = true;
      target.dataset[RESOURCE_STATE_KEY] = 'failed';
      cleanup();
      target.remove();
      reject(new Error(`图形验证码样式加载失败：${href}`));
    };
    const timeoutId = window.setTimeout(handleError, RESOURCE_LOAD_TIMEOUT);

    target.addEventListener('load', handleLoad, { once: true });
    target.addEventListener('error', handleError, { once: true });

    if (!link) {
      target.rel = 'stylesheet';
      target.href = href;
      target.dataset[RESOURCE_STATE_KEY] = 'loading';
      document.head.append(target);
    }
  });
}

function loadScript(src: string) {
  return new Promise<void>((resolve, reject) => {
    if (window.TAC) {
      resolve();
      return;
    }

    let script = document.querySelector<HTMLScriptElement>(
      `script[src="${src}"]`,
    );
    if (script && script.dataset[RESOURCE_STATE_KEY] !== 'loading') {
      script.remove();
      script = null;
    }

    const target = script ?? document.createElement('script');
    let settled = false;
    const cleanup = () => {
      window.clearTimeout(timeoutId);
      target.removeEventListener('load', handleLoad);
      target.removeEventListener('error', handleError);
    };
    const handleLoad = () => {
      if (settled) return;
      settled = true;
      target.dataset[RESOURCE_STATE_KEY] = 'loaded';
      cleanup();
      resolve();
    };
    const handleError = () => {
      if (settled) return;
      settled = true;
      target.dataset[RESOURCE_STATE_KEY] = 'failed';
      cleanup();
      target.remove();
      reject(new Error(`图形验证码脚本加载失败：${src}`));
    };
    const timeoutId = window.setTimeout(handleError, RESOURCE_LOAD_TIMEOUT);

    target.addEventListener('load', handleLoad, { once: true });
    target.addEventListener('error', handleError, { once: true });

    if (!script) {
      target.async = true;
      target.src = src;
      target.dataset[RESOURCE_STATE_KEY] = 'loading';
      document.head.append(target);
    }
  });
}

export function getCaptchaAssetUrl(path: string) {
  return getPublicAssetUrl(`tianai-captcha/${path}`);
}

/** 直接加载官方 TAC 样式和 SDK，资源全部位于 public 目录。 */
export async function loadTianaiCaptcha(): Promise<TacConstructor> {
  loadPromise ??= Promise.all([
    loadStylesheet(getCaptchaAssetUrl('tac/css/tac.css')),
    loadScript(getCaptchaAssetUrl('tac/js/tac.min.js')),
  ])
    .then(() => {
      if (!window.TAC) throw new Error('图形验证码 SDK 不可用');
      return window.TAC;
    })
    .catch((error) => {
      loadPromise = null;
      throw error;
    });
  return loadPromise;
}

/**
 * 按需创建图形验证码弹层。
 *
 * 验证码属于一次性交互，不参与业务页面布局，也不随页签缓存保留状态。
 */
export function useTianaiCaptcha() {
  const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

  let operationId = 0;
  let currentResolver: ((value: null | string) => void) | undefined;
  let overlayElement: HTMLDivElement | null = null;
  let tacInstance: null | TacInstance = null;

  function destroyCaptcha() {
    tacInstance?.destroyWindow();
    tacInstance = null;
    overlayElement?.remove();
    overlayElement = null;
  }

  function settle(value: null | string) {
    operationId += 1;
    const resolver = currentResolver;
    currentResolver = undefined;
    destroyCaptcha();
    resolver?.(value);
  }

  function close() {
    settle(null);
  }

  function createContainer(containerId: string) {
    const overlay = document.createElement('div');
    overlay.className = 'tianai-captcha-overlay';
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) close();
    });

    const container = document.createElement('div');
    container.id = containerId;
    container.className = 'tianai-captcha-container';
    overlay.append(container);
    document.body.append(overlay);
    overlayElement = overlay;
  }

  async function open(
    scene: CaptchaScene,
    currentOperationId: number,
    containerId: string,
  ) {
    const TAC = await loadTianaiCaptcha();
    if (currentOperationId !== operationId) return;

    const sceneQuery = `scene=${encodeURIComponent(scene)}`;
    const tac = new TAC(
      {
        bindEl: `#${containerId}`,
        btnCloseFun: close,
        btnRefreshFun: (_el: unknown, instance: TacInstance) => {
          instance.reloadCaptcha();
        },
        requestCaptchaDataUrl: joinUrl(
          apiURL,
          `/auth/captcha/get?${sceneQuery}`,
        ),
        validCaptchaUrl: joinUrl(apiURL, `/auth/captcha/check?${sceneQuery}`),
        validFail: (_res: unknown, _c: unknown, instance: TacInstance) => {
          instance.reloadCaptcha();
        },
        validSuccess: (
          response: TianaiCaptchaApiResponse<TianaiCaptchaVerificationData>,
        ) => {
          const verification = response?.data?.captchaVerification;
          if (!verification) {
            ElMessage.error('图形验证码校验结果无效，请重新验证');
            tac.reloadCaptcha();
            return;
          }
          settle(verification);
        },
      },
      { logoUrl: null },
    );

    if (currentOperationId !== operationId) {
      tac.destroyWindow();
      return;
    }

    tac.config?.addRequestChain?.({
      postRequest(
        type: string,
        _requestParam: TacRequestParam,
        response: TianaiCaptchaApiResponse,
      ) {
        if (type === 'requestCaptchaData' && response.data) {
          response.id = response.data.id;
          response.captcha = response.data;
        }
        return true;
      },
    });

    tacInstance = tac;
    tac.init();
  }

  function verify(scene: CaptchaScene) {
    settle(null);
    const currentOperationId = ++operationId;
    const containerId = `tianai-captcha-${++containerSequence}`;
    createContainer(containerId);

    return new Promise<null | string>((resolve) => {
      currentResolver = resolve;
      void open(scene, currentOperationId, containerId).catch(() => {
        if (currentOperationId !== operationId) return;
        ElMessage.error('图形验证码加载失败，请稍后重试');
        settle(null);
      });
    });
  }

  onDeactivated(close);
  onBeforeUnmount(close);

  return { close, verify };
}
