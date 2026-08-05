import dayjs from 'dayjs';

/** 按界面可见的秒级精度规范化日志查询边界。 */
export function formatOccurredAtBoundary(
  value: Date | number | string,
  fieldName: string,
) {
  const time = dayjs(value);
  return (
    fieldName === 'occurredAtEnd'
      ? time.endOf('second')
      : time.startOf('second')
  ).toISOString();
}
