<script lang="ts" setup>
import type { UploadFile } from 'element-plus';

import type { FileApi } from '#/api/core';

import { computed, onBeforeUnmount, ref } from 'vue';

import { VCropper } from '@vben/common-ui';

import {
  ElButton,
  ElDialog,
  ElMessage,
  ElMessageBox,
  ElUpload,
} from 'element-plus';

import AuthenticatedImage from './authenticated-image.vue';

const props = withDefaults(
  defineProps<{
    deleteHandler: (file: FileApi.Record) => Promise<void>;
    disabled?: boolean;
    serverManagedReplacement?: boolean;
    size?: number;
    uploadHandler: (file: File) => Promise<FileApi.Record>;
  }>(),
  {
    disabled: false,
    serverManagedReplacement: false,
    size: 128,
  },
);

const file = defineModel<FileApi.Record | null>({ default: null });
const cropperVisible = ref(false);
const cropperImage = ref('');
const cropperRef = ref<InstanceType<typeof VCropper> | null>(null);
const uploading = ref(false);
const displaySize = computed(() => `${props.size}px`);

function releaseCropperImage() {
  if (cropperImage.value) {
    URL.revokeObjectURL(cropperImage.value);
    cropperImage.value = '';
  }
}

function selectImage(uploadFile: UploadFile) {
  if (!uploadFile.raw) return;
  releaseCropperImage();
  cropperImage.value = URL.createObjectURL(uploadFile.raw);
  cropperVisible.value = true;
}

function closeCropper() {
  cropperVisible.value = false;
  releaseCropperImage();
}

async function confirmCrop() {
  const blob = await cropperRef.value?.getCropImage(
    'image/png',
    0.92,
    'blob',
    512,
    512,
  );
  if (!(blob instanceof Blob) || blob.size === 0) {
    ElMessage.error('生成头像失败，请重新选择图片');
    return;
  }

  uploading.value = true;
  try {
    const avatarFile = new File([blob], 'avatar.png', {
      type: 'image/png',
    });
    const uploaded = await props.uploadHandler(avatarFile);
    const previous = file.value;
    if (previous && !props.serverManagedReplacement) {
      try {
        await props.deleteHandler(previous);
      } catch (error) {
        try {
          await props.deleteHandler(uploaded);
        } catch {
          // 清理新文件失败不会覆盖原始错误，服务端仍会保留完整错误日志。
        }
        throw error;
      }
    }
    file.value = uploaded;
    closeCropper();
  } catch {
    // 请求层已统一展示错误信息，保留当前裁剪结果供用户重试。
  } finally {
    uploading.value = false;
  }
}

async function removeAvatar() {
  if (!file.value) return;
  await ElMessageBox.confirm('确定删除当前头像吗？', '删除确认', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await props.deleteHandler(file.value);
  file.value = null;
}

onBeforeUnmount(releaseCropperImage);
</script>

<template>
  <div class="flex flex-wrap items-center gap-4">
    <div
      class="shrink-0 overflow-hidden rounded-full border bg-gray-100"
      :style="{ height: displaySize, width: displaySize }"
    >
      <AuthenticatedImage v-if="file" :file="file" />
      <div
        v-else
        class="flex h-full w-full items-center justify-center text-sm text-gray-400"
      >
        暂无头像
      </div>
    </div>
    <div v-if="!disabled" class="flex items-center gap-2">
      <ElUpload
        accept="image/jpeg,image/png,image/webp"
        :auto-upload="false"
        class="inline-flex"
        :on-change="selectImage"
        :show-file-list="false"
      >
        <ElButton class="w-24" :loading="uploading" type="primary">
          {{ file ? '更换头像' : '上传头像' }}
        </ElButton>
      </ElUpload>
      <ElButton
        v-if="file"
        class="w-24"
        type="danger"
        plain
        @click="removeAvatar"
      >
        删除头像
      </ElButton>
    </div>

    <ElDialog
      v-model="cropperVisible"
      align-center
      append-to-body
      destroy-on-close
      title="裁剪头像"
      width="548px"
      @closed="releaseCropperImage"
    >
      <VCropper
        v-if="cropperImage"
        ref="cropperRef"
        aspect-ratio="1:1"
        :height="400"
        :img="cropperImage"
        :width="500"
      />
      <template #footer>
        <ElButton @click="closeCropper">取消</ElButton>
        <ElButton :loading="uploading" type="primary" @click="confirmCrop">
          确定
        </ElButton>
      </template>
    </ElDialog>
  </div>
</template>
