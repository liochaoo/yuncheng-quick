import type { MaybeRefOrGetter } from 'vue';

import type { DictionaryOption } from '#/api/common/dictionary';

import { onScopeDispose, ref, toValue, watch } from 'vue';

import { getDictionaryOptionsApi } from '#/api/common/dictionary';

/** 加载公共数据字典选项，并提供统一的历史值翻译能力。 */
export function useDictionaryOptions(dictionaryCode: MaybeRefOrGetter<string>) {
  const loaded = ref(false);
  const loading = ref(false);
  const options = ref<DictionaryOption[]>([]);
  let requestSequence = 0;

  async function load() {
    const code = toValue(dictionaryCode).trim();
    const sequence = ++requestSequence;
    if (!code) {
      loaded.value = false;
      options.value = [];
      loading.value = false;
      return;
    }
    loaded.value = false;
    options.value = [];
    loading.value = true;
    try {
      const result = await getDictionaryOptionsApi(code);
      if (sequence === requestSequence) {
        options.value = result;
        loaded.value = true;
      }
    } catch {
      if (sequence === requestSequence) {
        loaded.value = false;
        options.value = [];
      }
    } finally {
      if (sequence === requestSequence) loading.value = false;
    }
  }

  function labelOf(value?: null | string) {
    if (value === null || value === undefined || value === '') return '';
    return (
      options.value.find((option) => option.value === value)?.label ?? value
    );
  }

  function optionOf(value?: null | string) {
    return options.value.find((option) => option.value === value);
  }

  watch(
    () => toValue(dictionaryCode),
    () => void load(),
    { immediate: true },
  );
  onScopeDispose(() => {
    requestSequence++;
  });

  return {
    labelOf,
    load,
    loaded,
    loading,
    optionOf,
    options,
    reload: load,
  };
}
