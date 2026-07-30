import type { OrganizationNodeType } from '#/api/common/organization';

export function allowedParentTypes(
  nodeType: OrganizationNodeType,
): OrganizationNodeType[] {
  switch (nodeType) {
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

export function defaultChildType(
  parentType: OrganizationNodeType,
): OrganizationNodeType {
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

export function parentAllowsChild(
  parentType: OrganizationNodeType,
  childType: OrganizationNodeType,
) {
  return allowedParentTypes(childType).includes(parentType);
}
