<script lang="ts" setup>
import type { FileApi } from '#/api/core';

import { ElButton } from 'element-plus';

import { useConfirmAction } from '#/hooks/use-confirm-action';

import {
  downloadFileRecord,
  formatFileSize,
  isPreviewableFile,
  previewFileRecord,
} from './file-display';

withDefaults(
  defineProps<{
    disabled?: boolean;
    downloadable?: boolean;
    files: FileApi.Record[];
    loading?: boolean;
    previewable?: boolean;
    removable?: boolean;
  }>(),
  {
    disabled: false,
    downloadable: true,
    loading: false,
    previewable: true,
    removable: true,
  },
);

const emit = defineEmits<{
  remove: [file: FileApi.Record];
}>();

const { confirming, runConfirmAction } = useConfirmAction();

function confirmRemove(file: FileApi.Record) {
  void runConfirmAction({
    action: async () => emit('remove', file),
    confirmButtonText: '删除',
    message: `确定删除文件“${file.originalName}”吗？`,
    title: '删除确认',
  });
}

async function preview(file: FileApi.Record) {
  try {
    await previewFileRecord(file);
  } catch {
    // 请求客户端已统一提示错误，此处仅避免模板事件产生未处理的 Promise。
  }
}

async function download(file: FileApi.Record) {
  try {
    await downloadFileRecord(file);
  } catch {
    // 请求客户端已统一提示错误，此处仅避免模板事件产生未处理的 Promise。
  }
}
</script>

<template>
  <div v-loading="loading" class="min-h-8">
    <div v-if="files.length > 0" class="space-y-2">
      <div
        v-for="file in files"
        :key="file.id"
        class="flex items-center gap-3 rounded-md border px-3 py-2"
      >
        <div class="min-w-0 flex-1">
          <div class="truncate">{{ file.originalName }}</div>
          <div class="text-xs text-gray-400">
            {{ formatFileSize(file.fileSize) }}
          </div>
        </div>
        <div class="flex shrink-0 items-center gap-1">
          <ElButton
            v-if="previewable && isPreviewableFile(file)"
            link
            type="primary"
            @click="preview(file)"
          >
            预览
          </ElButton>
          <ElButton
            v-if="downloadable"
            link
            type="primary"
            @click="download(file)"
          >
            下载
          </ElButton>
          <ElButton
            v-if="removable && !disabled"
            :disabled="confirming"
            link
            type="danger"
            @click="confirmRemove(file)"
          >
            删除
          </ElButton>
        </div>
      </div>
    </div>
  </div>
</template>
