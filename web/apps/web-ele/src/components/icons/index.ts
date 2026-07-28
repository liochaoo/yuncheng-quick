import { addCollection, addIcon } from '@vben/icons';

import lucideIcons from '@iconify-json/lucide/icons.json';

/** 平台菜单统一使用的本地图标集。 */
const MENU_ICON_PREFIX = lucideIcons.prefix;
const MENU_ICON_NAMES = new Set(
  [
    ...Object.keys(lucideIcons.icons),
    ...Object.keys(lucideIcons.aliases ?? {}),
  ].map((name) => `${MENU_ICON_PREFIX}:${name}`),
);

/** Vben现有布局使用的少量非Lucide图标，随应用注册以避免Iconify在线回退。 */
const FRAMEWORK_ICONS = {
  'ep:expand': {
    body: '<path fill="currentColor" d="M128 192h768v128H128zm0 256h512v128H128zm0 256h768v128H128zm576-352l192 160l-192 128z"/>',
    height: 1024,
    width: 1024,
  },
  'ep:fold': {
    body: '<path fill="currentColor" d="M896 192H128v128h768zm0 256H384v128h512zm0 256H128v128h768zM320 384L128 512l192 128z"/>',
    height: 1024,
    width: 1024,
  },
  'fluent-mdl2:world-clock': {
    body: '<path fill="currentColor" d="M896 768H512V256h128v384h256zm1152 640q0 87-22 168t-64 152t-100 130t-128 101t-152 66t-168 23q-134 0-251-49t-205-136t-139-204t-51-251q0-132 50-248t138-204t203-137t249-51q132 0 248 50t204 138t137 203t51 249m-640 512q21 0 37-15t29-40t21-53t15-58t9-53t5-37h-230q1 13 5 37t10 52t15 58t21 54t27 39t36 16m125-384q3-64 3-128q0-63-3-128h-250q-3 65-3 128q0 64 3 128zm-637-128q0 32 4 64t12 64h243q-6-128 0-256H912q-8 32-12 64t-4 64m512-512q-19 0-34 15t-27 40t-21 54t-15 58t-11 53t-5 36h225q-1-11-5-34t-11-52t-16-59t-21-54t-27-41t-32-16m253 384q3 64 3 128t-2 128h242q8-32 12-64t4-64t-4-64t-12-64zm190-128q-43-75-108-131t-145-89q20 53 32 108t20 112zm-637-218q-78 32-142 88t-107 130h200q7-56 18-110t31-108m-249 730q42 73 105 129t142 88q-20-52-30-107t-17-110zm643 215q77-32 139-87t104-128h-198q-5 55-15 109t-30 106M640 0q88 0 170 23t153 64t129 100t100 130t65 153t23 170h-128q0-106-40-199t-110-162t-163-110t-199-41t-199 40t-162 110t-110 163t-41 199t40 199t110 162t163 110t199 41v128q-88 0-170-23t-153-64t-129-100T88 963T23 810T0 640q0-132 50-248t138-204T391 51T640 0"/>',
    height: 2048,
    width: 2048,
  },
  'mdi:home-outline': {
    body: '<path fill="currentColor" d="m12 5.69l5 4.5V18h-2v-6H9v6H7v-7.81zM12 3L2 12h3v8h6v-6h2v6h6v-8h3"/>',
    height: 24,
    width: 24,
  },
};

/** 在应用渲染前注册本地图标，图标选择和菜单展示都不依赖网络。 */
function registerLocalIconCollections() {
  addCollection(lucideIcons);
  Object.entries(FRAMEWORK_ICONS).forEach(([name, icon]) => {
    addIcon(name, icon);
  });
}

function isLocalMenuIcon(icon: string) {
  return MENU_ICON_NAMES.has(icon);
}

export {
  isLocalMenuIcon,
  MENU_ICON_NAMES,
  MENU_ICON_PREFIX,
  registerLocalIconCollections,
};
