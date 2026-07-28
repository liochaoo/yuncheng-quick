import type { ButtonProps } from 'element-plus';

export interface RowAction {
  disabled?: boolean;
  label: string;
  onClick: () => void;
  type?: ButtonProps['type'];
  visible?: boolean;
}
