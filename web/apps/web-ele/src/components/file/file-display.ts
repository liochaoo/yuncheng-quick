import type { FileApi } from '#/api/core';

import { getFileDownloadBlobApi, getFilePreviewBlobApi } from '#/api/core';

const SAFE_PREVIEW_TYPES = [
  { contentType: 'application/pdf', image: false },
  { contentType: 'image/bmp', image: true },
  { contentType: 'image/gif', image: true },
  { contentType: 'image/jpeg', image: true },
  { contentType: 'image/png', image: true },
  { contentType: 'image/vnd.microsoft.icon', image: true },
  { contentType: 'image/webp', image: true },
  { contentType: 'image/x-icon', image: true },
];
const SAFE_PREVIEW_CONTENT_TYPES = new Set(
  SAFE_PREVIEW_TYPES.map(({ contentType }) => contentType),
);
const SAFE_IMAGE_CONTENT_TYPES = new Set(
  SAFE_PREVIEW_TYPES.filter(({ image }) => image).map(
    ({ contentType }) => contentType,
  ),
);

/** 以便于阅读的形式展示文件大小。 */
export function formatFileSize(size: number) {
  if (!Number.isFinite(size) || size <= 0) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const unitIndex = Math.min(
    Math.floor(Math.log(size) / Math.log(1024)),
    units.length - 1,
  );
  const value = size / 1024 ** unitIndex;
  return `${value >= 10 || unitIndex === 0 ? value.toFixed(0) : value.toFixed(1)} ${units[unitIndex]}`;
}

/** 当前文件是否适合直接在浏览器中预览。 */
export function isPreviewableFile(file: FileApi.Record) {
  return SAFE_PREVIEW_CONTENT_TYPES.has(file.contentType.toLowerCase());
}

/** 当前文件是否适合显示在图片组件中。 */
export function isPreviewableImage(file: FileApi.Record) {
  return SAFE_IMAGE_CONTENT_TYPES.has(file.contentType.toLowerCase());
}

/** 下载文件并使用后端记录的原始文件名保存。 */
export async function downloadFileRecord(file: FileApi.Record) {
  const blob = await getFileDownloadBlobApi(file);
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = file.originalName;
  link.style.display = 'none';
  document.body.append(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 100);
}

/** 在新窗口中预览文件。 */
export async function previewFileRecord(file: FileApi.Record) {
  const previewWindow = window.open('', '_blank');
  if (previewWindow) {
    previewWindow.opener = null;
  }
  try {
    const blob = await getFilePreviewBlobApi(file);
    const url = URL.createObjectURL(blob);
    if (previewWindow) {
      previewWindow.location.href = url;
    } else {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
    setTimeout(() => URL.revokeObjectURL(url), 60_000);
  } catch (error) {
    previewWindow?.close();
    throw error;
  }
}
