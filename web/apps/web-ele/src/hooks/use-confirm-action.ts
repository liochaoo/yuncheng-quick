import type { ElMessageBoxOptions } from 'element-plus';

import { ref } from 'vue';

import { ElMessage, ElMessageBox } from 'element-plus';

interface ConfirmActionOptions {
  action: () => Promise<unknown>;
  cancelButtonText?: string;
  confirmButtonText?: string;
  message: string;
  onSuccess?: () => Promise<void> | void;
  successMessage?: string;
  title: string;
  type?: ElMessageBoxOptions['type'];
}

function isCancelAction(error: unknown) {
  if (error === 'cancel' || error === 'close') {
    return true;
  }
  if (typeof error !== 'object' || error === null) {
    return false;
  }
  const action = (error as { action?: string }).action;
  return action === 'cancel' || action === 'close';
}

/** 统一执行需要用户确认的危险操作。 */
function useConfirmAction() {
  const confirming = ref(false);

  async function runConfirmAction(options: ConfirmActionOptions) {
    if (confirming.value) {
      return false;
    }
    confirming.value = true;
    try {
      await ElMessageBox.confirm(options.message, options.title, {
        cancelButtonText: options.cancelButtonText ?? '取消',
        confirmButtonText: options.confirmButtonText ?? '确认',
        type: options.type ?? 'warning',
      });
      await options.action();
      if (options.successMessage) {
        ElMessage.success(options.successMessage);
      }
      await options.onSuccess?.();
      return true;
    } catch (error: unknown) {
      if (!isCancelAction(error)) {
        // 请求层已经负责展示后端业务错误，这里只记录非取消异常。
        console.error('确认操作执行失败', error);
      }
      return false;
    } finally {
      confirming.value = false;
    }
  }

  return {
    confirming,
    runConfirmAction,
  };
}

export { useConfirmAction };
