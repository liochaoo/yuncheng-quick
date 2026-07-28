export type BusinessFormMode = 'create' | 'edit';

export interface BusinessFormDrawerOpenData {
  id?: string;
  mode: BusinessFormMode;
}

export const BUSINESS_FORM_DRAWER_WIDTH = {
  large: 'w-[800px] max-w-[100vw]',
  medium: 'w-[640px] max-w-[100vw]',
  mediumWide: 'w-[720px] max-w-[100vw]',
  small: 'w-[480px] max-w-[100vw]',
  xlarge: 'w-[1080px] max-w-[100vw]',
} as const;
