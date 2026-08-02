<script lang="ts" setup>
import { ElButton, ElDialog, ElScrollbar } from 'element-plus';

withDefaults(
  defineProps<{
    clearable?: boolean;
    loading?: boolean;
    selectedCount: number;
    title: string;
    width?: string;
  }>(),
  {
    clearable: true,
    loading: false,
    width: 'clamp(860px, 76vw, 1180px)',
  },
);

const emit = defineEmits<{
  clear: [];
  confirm: [];
}>();

const visible = defineModel<boolean>({ required: true });
</script>

<template>
  <ElDialog
    v-model="visible"
    align-center
    append-to-body
    destroy-on-close
    :title="title"
    :width="width"
  >
    <div
      class="grid h-[540px] min-h-0 grid-cols-[minmax(0,2fr)_minmax(260px,1fr)] gap-4"
    >
      <section class="flex min-h-0 flex-col rounded-md border">
        <div class="border-b p-3">
          <slot name="search"></slot>
        </div>
        <div class="min-h-0 flex-1">
          <slot name="list"></slot>
        </div>
      </section>

      <section
        v-loading="loading"
        class="flex min-h-0 flex-col rounded-md border"
      >
        <header class="flex items-center justify-between border-b px-3 py-2.5">
          <span class="font-medium">已选择 {{ selectedCount }} 项</span>
          <ElButton
            v-if="clearable"
            :disabled="loading"
            link
            type="primary"
            @click="emit('clear')"
          >
            清空
          </ElButton>
        </header>
        <ElScrollbar class="min-h-0 flex-1 p-3">
          <slot name="selected"></slot>
        </ElScrollbar>
      </section>
    </div>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :disabled="loading" type="primary" @click="emit('confirm')">
        确定
      </ElButton>
    </template>
  </ElDialog>
</template>
