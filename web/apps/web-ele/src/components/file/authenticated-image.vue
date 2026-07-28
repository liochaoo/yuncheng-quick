<script lang="ts" setup>
import type { FileApi } from '#/api/core';

import { onBeforeUnmount, ref, watch } from 'vue';

import { ElImage } from 'element-plus';

import { getFilePreviewBlobApi } from '#/api/core';

import { isPreviewableImage } from './file-display';

const props = withDefaults(
  defineProps<{
    alt?: string;
    file: FileApi.Record;
    fit?: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down';
    preview?: boolean;
  }>(),
  {
    alt: '',
    fit: 'cover',
    preview: false,
  },
);

const imageUrl = ref('');
const loading = ref(false);
let loadSequence = 0;

function releaseImageUrl() {
  if (imageUrl.value) {
    URL.revokeObjectURL(imageUrl.value);
    imageUrl.value = '';
  }
}

async function loadImage() {
  const sequence = ++loadSequence;
  releaseImageUrl();
  if (!isPreviewableImage(props.file)) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const blob = await getFilePreviewBlobApi(props.file);
    if (sequence === loadSequence) {
      imageUrl.value = URL.createObjectURL(blob);
    }
  } catch {
    if (sequence === loadSequence) {
      imageUrl.value = '';
    }
  } finally {
    if (sequence === loadSequence) {
      loading.value = false;
    }
  }
}

watch(() => [props.file.id, props.file.contentType], loadImage, {
  immediate: true,
});

onBeforeUnmount(() => {
  loadSequence++;
  releaseImageUrl();
});
</script>

<template>
  <ElImage
    v-loading="loading"
    :alt="alt || file.originalName"
    class="h-full w-full"
    :fit="fit"
    :preview-src-list="preview && imageUrl ? [imageUrl] : []"
    :src="imageUrl"
  >
    <template #error>
      <div class="flex h-full w-full items-center justify-center text-gray-400">
        图片加载失败
      </div>
    </template>
  </ElImage>
</template>
