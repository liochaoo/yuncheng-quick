import { ref, shallowRef } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

interface DetailDrawerOpenData {
  id: string;
}

interface UseBusinessDetailDrawerOptions<
  T,
  TData extends DetailDrawerOpenData,
> {
  load: (id: string, data: TData) => Promise<T>;
}

/** 统一详情抽屉的加载、关闭和过期请求处理。 */
export function useBusinessDetailDrawer<
  T,
  TData extends DetailDrawerOpenData = DetailDrawerOpenData,
>(options: UseBusinessDetailDrawerOptions<T, TData>) {
  const detail = shallowRef<T>();
  const loading = ref(false);
  let requestSequence = 0;

  const [Drawer, drawerApi] = useVbenDrawer({
    async onConfirm() {
      drawerApi.close();
    },
    async onOpenChange(isOpen) {
      const sequence = ++requestSequence;
      detail.value = undefined;
      if (!isOpen) {
        loading.value = false;
        return;
      }

      const data = drawerApi.getData<TData>();
      loading.value = true;
      try {
        const result = await options.load(data.id, data);
        if (sequence === requestSequence) detail.value = result;
      } catch {
        if (sequence === requestSequence) drawerApi.close();
      } finally {
        if (sequence === requestSequence) loading.value = false;
      }
    },
  });

  drawerApi.setState({
    confirmText: '关闭',
    showCancelButton: false,
  });

  return { detail, Drawer, drawerApi, loading };
}
