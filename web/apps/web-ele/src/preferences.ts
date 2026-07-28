import { defineOverridesPreferences } from '@vben/preferences';

const defaultAvatar = `${import.meta.env.BASE_URL}avatar.png`;
const defaultLogo = `${import.meta.env.BASE_URL}logo.png`;

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  app: {
    // 业务菜单由后端返回，个人中心等不出现在菜单中的页面使用前端静态路由。
    accessMode: 'mixed',
    defaultAvatar,
    defaultHomePath: '/workspace',
    enableLanguageSetting: false,
    enableRefreshToken: true,
    enableTimezoneSetting: false,
    locale: 'zh-CN',
    name: import.meta.env.VITE_APP_TITLE,
    timezone: 'Asia/Shanghai',
  },
  copyright: {
    companySiteLink: '',
  },
  logo: {
    source: defaultLogo,
  },
  widget: {
    languageToggle: false,
    timezone: false,
  },
});
