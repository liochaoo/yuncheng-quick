import { ref } from 'vue';

/**
 * 只允许最后一次异步请求更新调用方状态。
 *
 * 适用于搜索、分页和快速切换记录等可能发生响应乱序的交互。
 */
export function useLatestRequest() {
  const loading = ref(false);
  let sequence = 0;

  async function execute<T>(request: () => Promise<T>) {
    const current = ++sequence;
    loading.value = true;
    try {
      const result = await request();
      return current === sequence ? result : undefined;
    } catch (error) {
      if (current === sequence) throw error;
      return undefined;
    } finally {
      if (current === sequence) loading.value = false;
    }
  }

  function invalidate() {
    sequence++;
    loading.value = false;
  }

  return { execute, invalidate, loading };
}
