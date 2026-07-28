import type { VbenFormProps } from '#/adapter/form';

type FormSchema = NonNullable<VbenFormProps['schema']>;

/** 用户相关页面共享的身份查询条件。 */
export function userIdentitySearchSchema(): FormSchema {
  return [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'username',
      label: '登录名',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'realName',
      label: '姓名',
    },
  ];
}

/** 角色列表和角色选择场景共享的身份查询条件。 */
export function roleIdentitySearchSchema(): FormSchema {
  return [
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'roleCode',
      label: '角色编码',
    },
    {
      component: 'Input',
      componentProps: { clearable: true },
      fieldName: 'roleName',
      label: '角色名称',
    },
  ];
}
