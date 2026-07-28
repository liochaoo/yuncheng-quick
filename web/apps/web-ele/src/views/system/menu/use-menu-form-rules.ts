import type { FormRules } from 'element-plus';

import type { Ref } from 'vue';

import type { MenuFormModel } from './menu-form-model';

import { computed } from 'vue';

import { checkMenuUniquenessApi } from '#/api/system/menu';
import { isLocalMenuIcon } from '#/components/icons';
import { createUniqueValidator } from '#/utils/form-validation';

function validHttpUrl(value: string) {
  try {
    const url = new URL(value);
    return (
      Boolean(url.hostname) &&
      (url.protocol === 'http:' || url.protocol === 'https:')
    );
  } catch {
    return false;
  }
}

function validLocalPath(value: string) {
  return (
    value.startsWith('/') &&
    !/[\\?#\s]/.test(value) &&
    !value.includes('://') &&
    !value.includes('..')
  );
}

function optionalLocalPathRule(fieldName: string) {
  return {
    trigger: 'blur',
    validator: (
      _rule: unknown,
      value: string,
      callback: (error?: Error) => void,
    ) => {
      callback(
        !value || validLocalPath(value)
          ? undefined
          : new Error(`${fieldName}必须是以 / 开头的安全路径`),
      );
    },
  };
}

function optionalLocalIconRule(fieldName: string) {
  return {
    trigger: ['blur', 'change'],
    validator: (
      _rule: unknown,
      value: string,
      callback: (error?: Error) => void,
    ) => {
      callback(
        !value || isLocalMenuIcon(value)
          ? undefined
          : new Error(`${fieldName}必须从平台本地图标集中选择`),
      );
    },
  };
}

/** 菜单表单的动态校验规则。 */
export function useMenuFormRules(
  model: MenuFormModel,
  recordId: Ref<string | undefined>,
) {
  function uniqueValidator(
    field: 'MENU_NAME' | 'PERMISSION_CODE' | 'ROUTE_NAME' | 'ROUTE_PATH',
    message: string,
  ) {
    return createUniqueValidator({
      check: async (value) => {
        const result = await checkMenuUniquenessApi({
          field,
          id: recordId.value,
          parentId: model.parentId,
          value,
        });
        return result.available;
      },
      message,
      normalize: (value) => value.trim(),
    });
  }

  return computed<FormRules<MenuFormModel>>(() => {
    const isButton = model.menuType === 'BUTTON';
    const isRoute = !isButton;
    return {
      activeIcon: isRoute ? [optionalLocalIconRule('激活图标')] : [],
      activePath:
        model.menuType === 'EMBEDDED' || model.menuType === 'MENU'
          ? [
              {
                max: 255,
                message: '激活菜单路径不能超过 255 个字符',
                trigger: 'blur',
              },
              optionalLocalPathRule('激活菜单路径'),
            ]
          : [],
      componentPath:
        model.menuType === 'MENU'
          ? [
              {
                message: '请输入页面组件路径',
                required: true,
                trigger: 'blur',
              },
              {
                max: 255,
                message: '组件路径不能超过 255 个字符',
                trigger: 'blur',
              },
              {
                trigger: 'blur',
                validator: (_rule, value: string, callback) => {
                  callback(
                    value.startsWith('/') && !value.endsWith('.vue')
                      ? undefined
                      : new Error('组件路径必须以 / 开头且不能包含 .vue 后缀'),
                  );
                },
              },
            ]
          : [],
      iframeSrc:
        model.menuType === 'EMBEDDED'
          ? [
              {
                message: '请输入内嵌页面地址',
                required: true,
                trigger: 'blur',
              },
              {
                trigger: 'blur',
                validator: (_rule, value: string, callback) => {
                  callback(
                    validHttpUrl(value)
                      ? undefined
                      : new Error(
                          '请输入以 http:// 或 https:// 开头的完整地址',
                        ),
                  );
                },
              },
            ]
          : [],
      icon: isRoute ? [optionalLocalIconRule('图标')] : [],
      link:
        model.menuType === 'LINK'
          ? [
              {
                message: '请输入链接地址',
                required: true,
                trigger: 'blur',
              },
              {
                trigger: 'blur',
                validator: (_rule, value: string, callback) => {
                  callback(
                    validHttpUrl(value)
                      ? undefined
                      : new Error(
                          '请输入以 http:// 或 https:// 开头的完整地址',
                        ),
                  );
                },
              },
            ]
          : [],
      menuName: [
        { message: '请输入菜单名称', required: true, trigger: 'blur' },
        { max: 100, message: '菜单名称不能超过 100 个字符', trigger: 'blur' },
        {
          trigger: 'blur',
          validator: uniqueValidator('MENU_NAME', '同级菜单名称已存在'),
        },
      ],
      parentId: isButton
        ? [
            {
              message: '按钮必须选择上级菜单',
              required: true,
              trigger: 'change',
            },
          ]
        : [],
      permissionCode:
        isButton || model.menuType === 'MENU'
          ? [
              ...(isButton
                ? [
                    {
                      message: '请输入权限码',
                      required: true,
                      trigger: 'blur' as const,
                    },
                  ]
                : []),
              {
                max: 128,
                message: '权限码不能超过 128 个字符',
                trigger: 'blur',
              },
              {
                message: '权限码必须使用 domain:resource:action 格式',
                pattern: /^[a-z0-9-]+:[a-z0-9-]+:[a-z0-9-]+$/,
                trigger: 'blur',
              },
              {
                trigger: 'blur',
                validator: uniqueValidator('PERMISSION_CODE', '权限码已存在'),
              },
            ]
          : [],
      redirect:
        model.menuType === 'CATALOG' || model.menuType === 'MENU'
          ? [
              {
                max: 255,
                message: '重定向路径不能超过 255 个字符',
                trigger: 'blur',
              },
              optionalLocalPathRule('重定向路径'),
            ]
          : [],
      routeName: isRoute
        ? [
            { message: '请输入路由名称', required: true, trigger: 'blur' },
            {
              max: 100,
              message: '路由名称不能超过 100 个字符',
              trigger: 'blur',
            },
            {
              message: '路由名称必须使用大驼峰格式',
              pattern: /^[A-Z][A-Za-z0-9]*$/,
              trigger: 'blur',
            },
            {
              trigger: 'blur',
              validator: uniqueValidator('ROUTE_NAME', '路由名称已存在'),
            },
          ]
        : [],
      routePath: isRoute
        ? [
            { message: '请输入路由路径', required: true, trigger: 'blur' },
            {
              max: 255,
              message: '路由路径不能超过 255 个字符',
              trigger: 'blur',
            },
            {
              trigger: 'blur',
              validator: (_rule, value: string, callback) => {
                if (!validLocalPath(value)) {
                  callback(new Error('路由路径必须是以 / 开头的安全路径'));
                  return;
                }
                callback();
              },
            },
            {
              trigger: 'blur',
              validator: uniqueValidator('ROUTE_PATH', '路由路径已存在'),
            },
          ]
        : [],
    };
  });
}
