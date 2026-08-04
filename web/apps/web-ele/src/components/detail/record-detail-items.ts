import type { DetailTableItem } from './detail-table.types';

import { formatDateTime } from '@vben/utils';

interface RecordDetailSource {
  createdAt?: null | string;
  createdBy?: null | string;
  updatedAt?: null | string;
  updatedBy?: null | string;
}

interface RecordDetailItemsOptions {
  showOperators?: boolean;
}

/** 生成详情页统一的创建、更新记录信息。 */
function buildRecordDetailItems(
  source?: RecordDetailSource,
  options: RecordDetailItemsOptions = {},
): DetailTableItem[] {
  const items: DetailTableItem[] = [
    {
      key: 'createdAt',
      label: '创建时间',
      value: formatDateTime(source?.createdAt ?? undefined),
    },
  ];

  if (options.showOperators) {
    items.push({
      key: 'createdBy',
      label: '创建人 ID',
      value: source?.createdBy,
    });
  }

  items.push({
    key: 'updatedAt',
    label: '更新时间',
    value: formatDateTime(source?.updatedAt ?? undefined),
  });

  if (options.showOperators) {
    items.push({
      key: 'updatedBy',
      label: '更新人 ID',
      value: source?.updatedBy,
    });
  }

  return items;
}

export { buildRecordDetailItems };
export type { RecordDetailItemsOptions, RecordDetailSource };
