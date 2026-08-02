import type {
  UserCreateRequest,
  UserFormData,
  UserUpdateRequest,
} from '#/api/system/user';

export interface UserFormModel {
  confirmPassword: string;
  email: string;
  enabled: boolean;
  orgIds: string[];
  password: string;
  phone: string;
  primaryOrgId: string;
  realName: string;
  roleIds: string[];
  sortOrder: number;
  username: string;
}

export function createDefaultUserForm(): UserFormModel {
  return {
    confirmPassword: '',
    email: '',
    enabled: true,
    orgIds: [],
    password: '',
    phone: '',
    primaryOrgId: '',
    realName: '',
    roleIds: [],
    sortOrder: 0,
    username: '',
  };
}

/** 把编辑初始化响应转换为表单模型。 */
export function fillUserForm(model: UserFormModel, data: UserFormData) {
  model.email = data.email ?? '';
  model.enabled = data.enabled;
  model.phone = data.phone ?? '';
  model.orgIds = [...data.orgIds];
  model.primaryOrgId = data.primaryOrgId;
  model.realName = data.realName;
  model.roleIds = [...data.roleIds];
  model.sortOrder = data.sortOrder;
  model.username = data.username;
}

function normalizedOptional(value: string) {
  return value.trim() || undefined;
}

export function buildUserCreateRequest(
  model: UserFormModel,
): UserCreateRequest {
  return {
    email: normalizedOptional(model.email),
    password: model.password,
    phone: normalizedOptional(model.phone),
    orgIds: [...model.orgIds],
    primaryOrgId: model.primaryOrgId,
    realName: model.realName.trim(),
    roleIds: [...model.roleIds],
    sortOrder: model.sortOrder ?? 0,
    username: model.username.trim(),
  };
}

export function buildUserUpdateRequest(
  model: UserFormModel,
): UserUpdateRequest {
  return {
    email: normalizedOptional(model.email),
    phone: normalizedOptional(model.phone),
    orgIds: [...model.orgIds],
    primaryOrgId: model.primaryOrgId,
    realName: model.realName.trim(),
    roleIds: [...model.roleIds],
    sortOrder: model.sortOrder ?? 0,
  };
}
