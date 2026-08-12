<script lang="ts" setup>
import type { UploadFile, UploadFiles, UploadUserFile } from 'element-plus';

import type { ExcelImportResult } from './types';

import { ref } from 'vue';

import { Download } from '@vben/icons';
import { downloadFileFromBlob } from '@vben/utils';

import {
  ElAlert,
  ElButton,
  ElDialog,
  ElMessage,
  ElTable,
  ElTableColumn,
  ElUpload,
} from 'element-plus';

const props = withDefaults(
  defineProps<{
    downloadTemplate: () => Promise<Blob>;
    importFile: (file: File) => Promise<ExcelImportResult>;
    maxFileSizeMb?: number;
    maxRows?: number;
    templateFilename?: string;
    title?: string;
  }>(),
  {
    maxFileSizeMb: 20,
    maxRows: 10_000,
    templateFilename: '导入模板.xlsx',
    title: '导入数据',
  },
);

const emit = defineEmits<{ success: [result: ExcelImportResult] }>();

const visible = ref(false);
const downloading = ref(false);
const submitting = ref(false);
const selectedFile = ref<File>();
const fileList = ref<UploadUserFile[]>([]);
const result = ref<ExcelImportResult>();

function open() {
  selectedFile.value = undefined;
  fileList.value = [];
  result.value = undefined;
  visible.value = true;
}

function changeFile(file: UploadFile, files: UploadFiles) {
  selectedFile.value = file.raw;
  fileList.value = files;
  result.value = undefined;
}

function removeFile() {
  selectedFile.value = undefined;
  result.value = undefined;
}

function rejectExtraFiles() {
  ElMessage.warning('每次只能导入一个 Excel 文件');
}

async function handleDownloadTemplate() {
  downloading.value = true;
  try {
    const source = await props.downloadTemplate();
    downloadFileFromBlob({ fileName: props.templateFilename, source });
  } finally {
    downloading.value = false;
  }
}

async function submit() {
  const file = selectedFile.value;
  if (!file) {
    ElMessage.warning('请选择需要导入的 Excel 文件');
    return;
  }
  if (!file.name.toLowerCase().endsWith('.xlsx')) {
    ElMessage.warning('只支持 .xlsx 格式的 Excel 文件');
    return;
  }
  if (file.size > props.maxFileSizeMb * 1024 * 1024) {
    ElMessage.warning(`Excel 文件不能超过 ${props.maxFileSizeMb} MB`);
    return;
  }
  submitting.value = true;
  try {
    const importResult = await props.importFile(file);
    result.value = importResult;
    if (importResult.success) {
      ElMessage.success(`成功导入 ${importResult.importedCount} 条数据`);
      emit('success', importResult);
      visible.value = false;
    }
  } finally {
    submitting.value = false;
  }
}

defineExpose({ open });
</script>

<template>
  <ElDialog
    v-model="visible"
    :close-on-click-modal="false"
    destroy-on-close
    :title="title"
    width="680px"
  >
    <ElAlert :closable="false" show-icon type="info">
      <template #title>
        请使用系统模板或本系统导出的用户文件。导入只新增数据，全部校验通过后才会写入。
      </template>
    </ElAlert>

    <div class="mt-4 flex items-center justify-between rounded-md border p-3">
      <div>
        <div class="font-medium">填写模板</div>
        <div class="mt-1 text-sm text-muted-foreground">
          模板包含填写说明及当前系统的组织、角色编码参考。
        </div>
      </div>
      <ElButton :loading="downloading" @click="handleDownloadTemplate">
        <Download class="mr-1 size-4" />
        下载模板
      </ElButton>
    </div>

    <ElUpload
      v-model:file-list="fileList"
      accept=".xlsx"
      :auto-upload="false"
      class="mt-4"
      drag
      :limit="1"
      :on-change="changeFile"
      :on-exceed="rejectExtraFiles"
      :on-remove="removeFile"
    >
      <div class="py-3">
        <div class="font-medium">将 Excel 文件拖到此处，或点击选择</div>
        <div class="mt-2 text-sm text-muted-foreground">
          仅支持 .xlsx，文件不超过 {{ maxFileSizeMb }} MB，用户数据不超过
          {{ maxRows }} 行
        </div>
      </div>
    </ElUpload>

    <ElAlert
      v-if="result && !result.success"
      class="mt-4"
      :closable="false"
      show-icon
      :title="`校验未通过，共发现 ${result.errorCount} 个问题，未写入任何数据。`"
      type="error"
    />
    <ElTable
      v-if="result && !result.success"
      class="mt-3"
      :data="result.errors"
      max-height="260"
      size="small"
    >
      <ElTableColumn label="行号" prop="rowNumber" width="80" />
      <ElTableColumn label="字段" prop="field" width="130" />
      <ElTableColumn label="问题" min-width="320" prop="message" />
    </ElTable>
    <div
      v-if="result && result.errorCount > result.errors.length"
      class="mt-2 text-sm text-muted-foreground"
    >
      当前仅展示前 {{ result.errors.length }} 个问题，请修正后重新导入。
    </div>

    <template #footer>
      <ElButton :disabled="submitting" @click="visible = false">取消</ElButton>
      <ElButton
        :disabled="!selectedFile"
        :loading="submitting"
        type="primary"
        @click="submit"
      >
        开始导入
      </ElButton>
    </template>
  </ElDialog>
</template>
