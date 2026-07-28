<script lang="ts" setup>
import type { FileApi } from '#/api/core';

import { ref } from 'vue';

import { useAccess } from '@vben/access';
import { useVbenDrawer } from '@vben/common-ui';

import { ElForm, ElFormItem, ElOption, ElSelect } from 'element-plus';

import { deleteSystemFileApi, uploadSystemFileApi } from '#/api/system/file';
import { FileUpload } from '#/components/file';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { FILE_POLICY_OPTIONS } from './file-options';
import { FILE_PERMISSION_CODES } from './permission-codes';

const emit = defineEmits<{
  success: [];
}>();

const { hasAccessByCodes } = useAccess();
const canDelete = hasAccessByCodes([FILE_PERMISSION_CODES.DELETE]);
const canDownload = hasAccessByCodes([FILE_PERMISSION_CODES.DOWNLOAD]);
const canPreview = hasAccessByCodes([FILE_PERMISSION_CODES.PREVIEW]);
const policyCode = ref('attachment');
const files = ref<FileApi.Record[]>([]);

const [Drawer, drawerApi] = useVbenDrawer({
  onConfirm() {
    drawerApi.close();
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      policyCode.value = 'attachment';
      files.value = [];
    }
  },
});

drawerApi.setState({
  confirmText: '关闭',
  showCancelButton: false,
});

async function upload(file: File) {
  return uploadSystemFileApi(file, policyCode.value);
}

async function remove(file: FileApi.Record) {
  await deleteSystemFileApi(file.id);
  emit('success');
}
</script>

<template>
  <Drawer title="上传文件" :class="BUSINESS_FORM_DRAWER_WIDTH.medium">
    <ElForm class="px-4" label-position="top">
      <ElFormItem label="文件策略">
        <ElSelect v-model="policyCode" placeholder="请选择文件策略">
          <ElOption
            v-for="option in FILE_POLICY_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="文件">
        <FileUpload
          v-model="files"
          :delete-handler="remove"
          :downloadable="canDownload"
          :limit="20"
          multiple
          :previewable="canPreview"
          :removable="canDelete"
          :upload-handler="upload"
          @upload-complete="emit('success')"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
