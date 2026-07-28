<script lang="ts" setup>
import type {
  GetDisabledHours,
  GetDisabledMinutes,
  GetDisabledSeconds,
} from 'element-plus';

import type { LogCleanPolicy, LogType } from '#/api/system/log';

import { computed, ref } from 'vue';

import dayjs from 'dayjs';
import {
  ElAlert,
  ElButton,
  ElDatePicker,
  ElDialog,
  ElMessage,
} from 'element-plus';

import { cleanSystemLogsApi, getLogCleanPolicyApi } from '#/api/system/log';

const props = defineProps<{ type: LogType }>();
const emit = defineEmits<{ success: [] }>();

const visible = ref(false);
const submitting = ref(false);
const policy = ref<LogCleanPolicy>();
const before = ref<Date>();
const validationMessage = ref('');

const latestCleanableBefore = computed(() =>
  policy.value ? dayjs(policy.value.latestCleanableBefore) : undefined,
);

function numberRange(start: number, end: number) {
  return Array.from(
    { length: Math.max(0, end - start) },
    (_, index) => start + index,
  );
}

function isLatestCleanableDay(comparingDate?: dayjs.Dayjs) {
  return Boolean(
    comparingDate && latestCleanableBefore.value?.isSame(comparingDate, 'day'),
  );
}

function disabledDate(date: Date) {
  const latest = latestCleanableBefore.value;
  return latest ? dayjs(date).isAfter(latest, 'day') : true;
}

const disabledHours: GetDisabledHours = (_role, comparingDate) => {
  const latest = latestCleanableBefore.value;
  if (!latest || !isLatestCleanableDay(comparingDate)) {
    return [];
  }
  return numberRange(latest.hour() + 1, 24);
};

const disabledMinutes: GetDisabledMinutes = (hour, _role, comparingDate) => {
  const latest = latestCleanableBefore.value;
  if (
    !latest ||
    !isLatestCleanableDay(comparingDate) ||
    hour !== latest.hour()
  ) {
    return [];
  }
  return numberRange(latest.minute() + 1, 60);
};

const disabledSeconds: GetDisabledSeconds = (
  hour,
  minute,
  _role,
  comparingDate,
) => {
  const latest = latestCleanableBefore.value;
  if (
    !latest ||
    !isLatestCleanableDay(comparingDate) ||
    hour !== latest.hour() ||
    minute !== latest.minute()
  ) {
    return [];
  }
  return numberRange(latest.second() + 1, 60);
};

function validateBefore() {
  const value = dayjs(before.value);
  if (!before.value || !value.isValid()) {
    validationMessage.value = '请选择清理截止时间';
    return false;
  }
  if (
    latestCleanableBefore.value &&
    value.isAfter(latestCleanableBefore.value)
  ) {
    validationMessage.value = `只能清理 ${policy.value?.retentionDays} 天以前的日志`;
    return false;
  }
  validationMessage.value = '';
  return true;
}

async function open() {
  try {
    const result = await getLogCleanPolicyApi();
    policy.value = result;
    before.value = new Date(result.latestCleanableBefore);
    validationMessage.value = '';
    visible.value = true;
  } catch {
    // 请求客户端已统一提示错误。
  }
}

async function submit() {
  if (!validateBefore() || !before.value || submitting.value) {
    return;
  }
  submitting.value = true;
  try {
    const result = await cleanSystemLogsApi(
      props.type,
      dayjs(before.value).toISOString(),
    );
    visible.value = false;
    ElMessage.success(`已清理 ${result.deletedCount} 条日志`);
    emit('success');
  } catch {
    // 请求客户端已统一提示错误。
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
    title="清理日志"
    width="520px"
  >
    <ElAlert
      :closable="false"
      show-icon
      title="将删除所选时间以前的日志，删除后无法恢复。"
      type="warning"
    />

    <div class="mt-5">
      <div class="mb-2 text-sm font-medium">清理截止时间</div>
      <ElDatePicker
        v-model="before"
        class="w-full"
        :clearable="false"
        :disabled-date="disabledDate"
        :disabled-hours="disabledHours"
        :disabled-minutes="disabledMinutes"
        :disabled-seconds="disabledSeconds"
        :editable="false"
        format="YYYY-MM-DD HH:mm:ss"
        placeholder="请选择清理截止时间"
        type="datetime"
        @change="validateBefore"
      />
      <div v-if="validationMessage" class="text-destructive mt-1 text-sm">
        {{ validationMessage }}
      </div>
      <div v-else-if="policy" class="mt-2 text-sm text-muted-foreground">
        系统至少保留近 {{ policy.retentionDays }} 天日志，最晚可选择
        {{
          dayjs(policy.latestCleanableBefore).format('YYYY-MM-DD HH:mm:ss')
        }}。
      </div>
    </div>

    <template #footer>
      <ElButton :disabled="submitting" @click="visible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="submit">
        清理
      </ElButton>
    </template>
  </ElDialog>
</template>
