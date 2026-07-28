import { initPreferences, updatePreferences } from '@vben/preferences';
import { unmountGlobalLoading } from '@vben/utils';

import { loadRuntimeBrandConfig, toBrandPreferences } from './config/brand';
import { overridesPreferences } from './preferences';

/**
 * 运行时品牌配置加载完成后，同步更新启动加载动画中的应用名称。
 */
function updateGlobalLoadingTitle(appName?: string) {
  if (!appName) {
    return;
  }
  const titleElement = document.querySelector<HTMLElement>(
    '#__app-loading__ .title',
  );
  if (titleElement) {
    titleElement.textContent = appName;
  }
}

/**
 * 应用初始化完成之后再进行页面加载渲染
 */
async function initApplication() {
  // name用于指定项目唯一标识
  // 用于区分不同项目的偏好设置以及存储数据的key前缀以及其他一些需要隔离的数据
  const env = import.meta.env.PROD ? 'prod' : 'dev';
  const appVersion = import.meta.env.VITE_APP_VERSION;
  const namespace = `${import.meta.env.VITE_APP_NAMESPACE}-${appVersion}-${env}`;

  const runtimeBrandConfig = await loadRuntimeBrandConfig();
  const runtimeBrandPreferences = toBrandPreferences(runtimeBrandConfig);
  updateGlobalLoadingTitle(runtimeBrandConfig.appName);

  // app偏好设置初始化
  await initPreferences({
    namespace,
    overrides: {
      ...overridesPreferences,
      app: {
        ...overridesPreferences.app,
        ...runtimeBrandPreferences.app,
      },
      copyright: {
        ...overridesPreferences.copyright,
        ...runtimeBrandPreferences.copyright,
      },
    },
  });
  updatePreferences(runtimeBrandPreferences);

  // 启动应用并挂载
  // vue应用主要逻辑及视图
  const { bootstrap } = await import('./bootstrap');
  await bootstrap(namespace);

  // 移除并销毁loading
  unmountGlobalLoading();
}

initApplication();
