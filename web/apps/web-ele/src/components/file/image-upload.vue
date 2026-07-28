<script lang="ts" setup>
import type { FileApi } from '#/api/core';

import { ref } from 'vue';

import { ElButton } from 'element-plus';

import AuthenticatedImage from './authenticated-image.vue';
import FileUpload from './file-upload.vue';

withDefaults(
  defineProps<{
    association?: FileApi.Association;
    autoLoad?: boolean;
    deleteHandler?: (file: FileApi.Record) => Promise<void>;
    disabled?: boolean;
    downloadable?: boolean;
    limit?: number;
    loadHandler?: (
      association: FileApi.Association,
    ) => Promise<FileApi.Record[]>;
    multiple?: boolean;
    policyCode?: string;
    previewable?: boolean;
    removable?: boolean;
    uploadHandler?: (file: File) => Promise<FileApi.Record>;
  }>(),
  {
    association: undefined,
    autoLoad: true,
    deleteHandler: undefined,
    disabled: false,
    downloadable: true,
    limit: 1,
    loadHandler: undefined,
    multiple: false,
    policyCode: 'image',
    previewable: true,
    removable: true,
    uploadHandler: undefined,
  },
);

const files = defineModel<FileApi.Record[]>({ default: () => [] });
const fileUploadRef = ref<InstanceType<typeof FileUpload>>();

function reload() {
  return fileUploadRef.value?.reload();
}

defineExpose({ reload });
</script>

<template>
  <FileUpload
    ref="fileUploadRef"
    v-model="files"
    accept="image/jpeg,image/png,image/gif,image/webp,image/bmp,image/x-icon"
    :association="association"
    :auto-load="autoLoad"
    :delete-handler="deleteHandler"
    :disabled="disabled"
    :downloadable="downloadable"
    :limit="limit"
    :load-handler="loadHandler"
    :multiple="multiple"
    :policy-code="policyCode"
    :previewable="previewable"
    :removable="removable"
    :upload-handler="uploadHandler"
  >
    <template #trigger="{ uploading }">
      <ElButton :loading="uploading" type="primary">选择图片</ElButton>
    </template>
    <template #list="{ files: uploadedFiles, loading, remove }">
      <div v-loading="loading" class="flex min-h-8 flex-wrap gap-3">
        <div
          v-for="file in uploadedFiles"
          :key="file.id"
          class="group relative h-28 w-28 overflow-hidden rounded-md border"
        >
          <AuthenticatedImage :file="file" :preview="previewable" />
          <button
            v-if="!disabled && removable"
            class="absolute right-1 top-1 hidden rounded bg-black/60 px-2 py-1 text-xs text-white group-hover:block"
            type="button"
            @click="remove(file)"
          >
            删除
          </button>
        </div>
      </div>
    </template>
  </FileUpload>
</template>
