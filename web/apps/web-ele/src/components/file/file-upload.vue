<script lang="ts" setup>
import type { UploadRequestOptions } from 'element-plus';

import type { FileApi } from '#/api/core';

import { computed, ref, watch } from 'vue';

import { ElButton, ElMessage, ElUpload } from 'element-plus';

import { deleteFileApi, listBusinessFilesApi, uploadFileApi } from '#/api/core';

import FileList from './file-list.vue';

const props = withDefaults(
  defineProps<{
    accept?: string;
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
    accept: '',
    association: undefined,
    autoLoad: true,
    deleteHandler: undefined,
    disabled: false,
    downloadable: true,
    limit: 1,
    loadHandler: undefined,
    multiple: false,
    policyCode: 'attachment',
    previewable: true,
    removable: true,
    uploadHandler: undefined,
  },
);

const emit = defineEmits<{
  removed: [file: FileApi.Record];
  uploadComplete: [files: FileApi.Record[]];
}>();

const files = defineModel<FileApi.Record[]>({ default: () => [] });
const pendingUploads = ref(0);
const loading = ref(false);
const uploading = computed(() => pendingUploads.value > 0);
const associationKey = computed(() => {
  const association = props.association;
  return association
    ? `${association.businessType}:${association.businessId}:${association.businessPosition}`
    : '';
});
const canUpload = computed(
  () => !props.disabled && !loading.value && files.value.length < props.limit,
);
let loadSequence = 0;
let batchSucceeded = false;
let completionQueued = false;

function exceedLimit() {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`);
}

async function upload(options: UploadRequestOptions) {
  if (pendingUploads.value === 0) {
    batchSucceeded = false;
  }
  pendingUploads.value += 1;
  try {
    const uploaded = props.uploadHandler
      ? await props.uploadHandler(options.file)
      : await uploadFileApi(options.file, {
          association: props.association,
          policyCode: props.policyCode,
        });
    files.value = props.multiple ? [...files.value, uploaded] : [uploaded];
    batchSucceeded = true;
    return uploaded;
  } finally {
    pendingUploads.value -= 1;
    scheduleUploadComplete();
  }
}

function scheduleUploadComplete() {
  if (completionQueued) return;
  completionQueued = true;
  queueMicrotask(() => {
    completionQueued = false;
    if (pendingUploads.value === 0 && batchSucceeded) {
      batchSucceeded = false;
      emit('uploadComplete', [...files.value]);
    }
  });
}

async function reload() {
  const association = props.association;
  if (!association) return;

  const sequence = ++loadSequence;
  loading.value = true;
  try {
    const loaded = props.loadHandler
      ? await props.loadHandler(association)
      : await listBusinessFilesApi({
          businessId: association.businessId,
          businessPosition: association.businessPosition,
          businessType: association.businessType,
        });
    if (sequence === loadSequence) {
      files.value = loaded;
    }
  } catch {
    if (sequence === loadSequence) {
      files.value = [];
    }
    // 请求客户端已统一提示错误，此处避免监听回调产生未处理的 Promise。
  } finally {
    if (sequence === loadSequence) {
      loading.value = false;
    }
  }
}

async function remove(file: FileApi.Record) {
  await (props.deleteHandler
    ? props.deleteHandler(file)
    : deleteFileApi(file.id));
  files.value = files.value.filter((item) => item.id !== file.id);
  emit('removed', file);
  ElMessage.success('文件已删除');
}

watch(
  associationKey,
  (currentKey, previousKey) => {
    if (props.autoLoad && currentKey) {
      void reload();
    } else if (props.autoLoad && previousKey) {
      loadSequence += 1;
      loading.value = false;
      files.value = [];
    }
  },
  { immediate: true },
);

defineExpose({ reload, remove });
</script>

<template>
  <div class="space-y-3">
    <ElUpload
      v-if="canUpload"
      :accept="accept"
      :disabled="disabled || uploading"
      :http-request="upload"
      :limit="limit"
      :multiple="multiple"
      :on-exceed="exceedLimit"
      :show-file-list="false"
    >
      <slot name="trigger" :uploading="uploading">
        <ElButton :loading="uploading" type="primary">选择文件</ElButton>
      </slot>
    </ElUpload>
    <slot name="list" :files="files" :loading="loading" :remove="remove">
      <FileList
        :disabled="disabled"
        :downloadable="downloadable"
        :files="files"
        :loading="loading"
        :previewable="previewable"
        :removable="removable"
        @remove="remove"
      />
    </slot>
  </div>
</template>
