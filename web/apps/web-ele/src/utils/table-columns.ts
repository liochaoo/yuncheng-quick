import type { VxeTableGridColumns } from '#/adapter/vxe-table';

import { formatEmptyValue } from '#/utils/display';

type StandardTableColumn<T> = NonNullable<VxeTableGridColumns<T>>[number];

const HEADER_ALIGN = 'center' as const;

export function textColumn<T>(
  column: StandardTableColumn<T>,
): StandardTableColumn<T> {
  return {
    align: 'left',
    formatter: ({ cellValue }) => formatEmptyValue(cellValue),
    headerAlign: HEADER_ALIGN,
    showOverflow: 'tooltip',
    ...column,
  };
}

export function centerColumn<T>(
  column: StandardTableColumn<T>,
): StandardTableColumn<T> {
  return {
    align: 'center',
    headerAlign: HEADER_ALIGN,
    ...column,
  };
}

export function dateColumn<T>(
  column: StandardTableColumn<T>,
): StandardTableColumn<T> {
  return centerColumn<T>({
    formatter: 'formatDateTime',
    width: 170,
    ...column,
  });
}

export function actionColumn<T>(
  column: StandardTableColumn<T> = {},
): StandardTableColumn<T> {
  return centerColumn<T>({
    fixed: 'right',
    showOverflow: false,
    slots: { default: 'action' },
    title: '操作',
    width: 240,
    ...column,
  });
}

export function checkboxColumn<T>(
  column: StandardTableColumn<T> = {},
): StandardTableColumn<T> {
  return centerColumn<T>({
    type: 'checkbox',
    width: 48,
    ...column,
  });
}
