import type { TagProps } from 'element-plus';

export interface EnumTagOption {
  label: string;
  type?: TagProps['type'];
}

export type EnumTagOptions = Record<string, EnumTagOption>;
