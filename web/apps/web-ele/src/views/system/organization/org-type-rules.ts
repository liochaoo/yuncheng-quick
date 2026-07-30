import type { OrgType } from '#/api/common/organization';

export function allowedParentTypes(orgType: OrgType): OrgType[] {
  switch (orgType) {
    case 'DEPARTMENT': {
      return ['ORGANIZATION', 'DEPARTMENT'];
    }
    case 'GROUP': {
      return ['DEPARTMENT', 'GROUP'];
    }
    case 'ORGANIZATION': {
      return ['ORGANIZATION'];
    }
  }
}

export function defaultChildType(parentType: OrgType): OrgType {
  switch (parentType) {
    case 'DEPARTMENT':
    case 'GROUP': {
      return 'GROUP';
    }
    case 'ORGANIZATION': {
      return 'DEPARTMENT';
    }
  }
}

export function parentAllowsChild(parentType: OrgType, childType: OrgType) {
  return allowedParentTypes(childType).includes(parentType);
}
