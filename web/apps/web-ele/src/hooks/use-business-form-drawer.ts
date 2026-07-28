import type { FormInstance } from 'element-plus';

import type { Ref } from 'vue';

import type {
  BusinessFormDrawerOpenData,
  BusinessFormMode,
} from '#/types/business-form';

import { computed, nextTick, ref, shallowRef } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

interface BusinessFormSaveContext<TData extends BusinessFormDrawerOpenData> {
  data: TData;
  id?: string;
  mode: BusinessFormMode;
}

interface UseBusinessFormDrawerOptions<
  TData extends BusinessFormDrawerOpenData,
  TLoaded,
> {
  applyLoaded?: (loaded: TLoaded, data: TData) => Promise<void> | void;
  beforeSave?: () => boolean | Promise<boolean>;
  formRef: Ref<FormInstance | undefined>;
  load?: (data: TData) => Promise<TLoaded> | TLoaded;
  onClose?: () => void;
  onSuccess?: () => void;
  reset: () => void;
  resourceName: string;
  save: (context: BusinessFormSaveContext<TData>) => Promise<void>;
  successMessage?: string;
}

/** 统一新增、编辑表单抽屉的稳定生命周期。 */
export function useBusinessFormDrawer<
  TData extends BusinessFormDrawerOpenData,
  TLoaded = void,
>(options: UseBusinessFormDrawerOptions<TData, TLoaded>) {
  const data = shallowRef<TData>();
  const initialized = ref(false);
  const initializing = ref(false);
  const mode = ref<BusinessFormMode>('create');
  let requestSequence = 0;

  const isCreate = computed(() => mode.value === 'create');
  const recordId = computed(() => data.value?.id);
  const title = computed(
    () => `${isCreate.value ? '新增' : '编辑'}${options.resourceName}`,
  );

  const [Drawer, drawerApi] = useVbenDrawer({
    async onConfirm() {
      if (!initialized.value || initializing.value) return;
      const valid = await options.formRef.value?.validate().catch(() => false);
      if (!valid || !data.value) return;
      if (options.beforeSave && !(await options.beforeSave())) return;

      drawerApi.lock();
      try {
        await options.save({
          data: data.value,
          id: data.value.id,
          mode: mode.value,
        });
        ElMessage.success(options.successMessage ?? '保存成功');
        options.onSuccess?.();
        drawerApi.close();
      } finally {
        drawerApi.unlock();
      }
    },
    async onOpenChange(isOpen) {
      const sequence = ++requestSequence;
      if (!isOpen) {
        initialized.value = false;
        initializing.value = false;
        options.onClose?.();
        return;
      }

      initialized.value = false;
      data.value = undefined;
      options.reset();
      const openData = drawerApi.getData<TData>();
      data.value = openData;
      mode.value = openData.mode;
      drawerApi.setState({
        confirmText: '保存',
        showCancelButton: true,
      });

      initializing.value = true;
      try {
        const loaded = await options.load?.(openData);
        if (sequence !== requestSequence) return;
        if (options.applyLoaded) {
          await options.applyLoaded(loaded as TLoaded, openData);
        }
        if (sequence !== requestSequence) return;
        await nextTick();
        options.formRef.value?.clearValidate();
        initialized.value = true;
      } catch {
        // 请求层统一提示错误，不保留尚未完成初始化的表单。
        if (sequence === requestSequence) drawerApi.close();
      } finally {
        if (sequence === requestSequence) initializing.value = false;
      }
    },
  });

  return {
    data,
    Drawer,
    drawerApi,
    initializing,
    isCreate,
    mode,
    recordId,
    title,
  };
}
