<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type {
  DictionaryOptionCreateRequest,
  DictionaryOptionDetail,
  DictionaryOptionUpdateRequest,
} from '#/api/system/dictionary';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref } from 'vue';

import { ElForm, ElFormItem, ElInput } from 'element-plus';

import {
  checkDictionaryOptionValueApi,
  createDictionaryOptionApi,
  getDictionaryOptionDetailApi,
  updateDictionaryOptionApi,
} from '#/api/system/dictionary';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import { createUniqueValidator } from '#/utils/form-validation';

interface DictionaryOptionDrawerData extends BusinessFormDrawerOpenData {
  dictionaryId: string;
  dictionaryName: string;
}

interface DictionaryOptionFormModel {
  description: string;
  optionLabel: string;
  optionValue: string;
  sortOrder: number;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): DictionaryOptionFormModel => ({
  description: '',
  optionLabel: '',
  optionValue: '',
  sortOrder: 0,
});

const formRef = ref<FormInstance>();
const model = reactive<DictionaryOptionFormModel>(createDefaultModel());

const {
  data,
  Drawer,
  initializing,
  isCreate,
  title: drawerTitle,
} = useBusinessFormDrawer<
  DictionaryOptionDrawerData,
  DictionaryOptionDetail | undefined
>({
  applyLoaded(detail) {
    if (detail) fillForm(detail);
  },
  formRef,
  async load(openData) {
    return openData.id
      ? getDictionaryOptionDetailApi(openData.dictionaryId, openData.id)
      : undefined;
  },
  onSuccess: () => emit('success'),
  reset: () => Object.assign(model, createDefaultModel()),
  resourceName: '字典选项',
  async save({ data: openData, id, mode }) {
    if (mode === 'create') {
      await createDictionaryOptionApi(
        openData.dictionaryId,
        buildCreateRequest(),
      );
    } else if (id) {
      await updateDictionaryOptionApi(
        openData.dictionaryId,
        id,
        buildUpdateRequest(),
      );
    }
  },
});

const rules = computed<FormRules<DictionaryOptionFormModel>>(() => ({
  optionLabel: [
    { message: '请输入选项标签', required: true, trigger: 'blur' },
    { max: 100, message: '选项标签不能超过 100 个字符', trigger: 'blur' },
  ],
  optionValue: isCreate.value
    ? [
        { message: '请输入选项值', required: true, trigger: 'blur' },
        { max: 100, message: '选项值不能超过 100 个字符', trigger: 'blur' },
        {
          message: '选项值只能包含字母、数字、点、下划线、冒号和连字符',
          pattern: /^[A-Za-z0-9][A-Za-z0-9._:-]*$/,
          trigger: 'blur',
        },
        {
          trigger: 'blur',
          validator: createUniqueValidator({
            check: async (value) => {
              const dictionaryId = data.value?.dictionaryId;
              if (!dictionaryId) return false;
              const result = await checkDictionaryOptionValueApi(
                dictionaryId,
                value,
              );
              return result.available;
            },
            message: '当前字典下的选项值已存在',
            normalize: (value) => value.trim(),
          }),
        },
      ]
    : [],
}));

function fillForm(detail: DictionaryOptionDetail) {
  model.optionValue = detail.optionValue;
  model.optionLabel = detail.optionLabel;
  model.description = detail.description ?? '';
  model.sortOrder = detail.sortOrder;
}

function buildCreateRequest(): DictionaryOptionCreateRequest {
  return {
    description: model.description.trim() || undefined,
    optionLabel: model.optionLabel.trim(),
    optionValue: model.optionValue.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}

function buildUpdateRequest(): DictionaryOptionUpdateRequest {
  return {
    description: model.description.trim() || undefined,
    optionLabel: model.optionLabel.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}
</script>

<template>
  <Drawer
    :class="BUSINESS_FORM_DRAWER_WIDTH.small"
    :loading="initializing"
    :title="drawerTitle"
  >
    <ElForm
      ref="formRef"
      class="px-4"
      label-width="100px"
      :model="model"
      :rules="rules"
      :validate-on-rule-change="false"
    >
      <ElFormItem label="所属字典">
        <ElInput :model-value="data?.dictionaryName" disabled />
      </ElFormItem>
      <ElFormItem label="选项值" prop="optionValue">
        <ElInput
          v-model="model.optionValue"
          :disabled="!isCreate"
          maxlength="100"
          placeholder="请输入选项值"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="选项标签" prop="optionLabel">
        <ElInput
          v-model="model.optionLabel"
          maxlength="100"
          placeholder="请输入选项标签"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="排序号" prop="sortOrder">
        <SortOrderInput v-model="model.sortOrder" />
      </ElFormItem>
      <ElFormItem label="说明" prop="description">
        <ElInput
          v-model="model.description"
          maxlength="500"
          :rows="4"
          placeholder="请输入选项说明"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
