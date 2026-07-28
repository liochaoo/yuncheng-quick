/** 文件管理权限码。 */
export const FILE_PERMISSION_CODES = {
  DELETE: 'system:file:delete',
  DOWNLOAD: 'system:file:download',
  PREVIEW: 'system:file:preview',
  UPLOAD: 'system:file:upload',
} as const;
