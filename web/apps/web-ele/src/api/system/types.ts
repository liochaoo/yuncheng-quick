export type RoleType = 'CUSTOM' | 'SYSTEM';

export interface RoleOption {
  disabled: boolean;
  id: string;
  roleCode: string;
  roleName: string;
  roleType: RoleType;
}

export interface RoleSummary {
  id: string;
  roleCode: string;
  roleName: string;
  roleType: RoleType;
}

export interface UniquenessCheckResult {
  available: boolean;
}
