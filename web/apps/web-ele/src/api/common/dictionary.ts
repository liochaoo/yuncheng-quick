import { requestClient } from '#/api/request';

/** 跨业务模块消费的数据字典选项。 */
export interface DictionaryOption {
  enabled: boolean;
  label: string;
  value: string;
}

/** 获取指定字典的全部选项，包含已停用选项，供选择和历史值翻译共同使用。 */
export async function getDictionaryOptionsApi(dictionaryCode: string) {
  return requestClient.get<DictionaryOption[]>(
    `/dictionaries/${encodeURIComponent(dictionaryCode)}/options`,
  );
}
