/** 平台分页响应。 */
export interface PageResult<T> {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
}
