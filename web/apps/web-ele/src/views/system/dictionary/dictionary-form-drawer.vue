<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type {
  DictionaryCreateRequest,
  DictionaryDetail,
  DictionaryUpdateRequest,
} from '#/api/system/dictionary';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref } from 'vue';

import { ElForm, ElFormItem, ElInput } from 'element-plus';

import {
  checkDictionaryCodeApi,
  createDictionaryApi,
  getDictionaryDetailApi,
  updateDictionaryApi,
} from '#/api/system/dictionary';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import { createUniqueValidator } from '#/utils/form-validation';

interface DictionaryFormModel {
  description: string;
  dictionaryCode: string;
  dictionaryName: string;
  sortOrder: number;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): DictionaryFormModel => ({
  description: '',
  dictionaryCode: '',
  dictionaryName: '',
  sortOrder: 0,
});

const formRef = ref<FormInstance>();
const model = reactive<DictionaryFormModel>(createDefaultModel());

const {
  Drawer,
  initializing,
  isCreate,
  title: drawerTitle,
} = useBusinessFormDrawer<
  BusinessFormDrawerOpenData,
  DictionaryDetail | undefined
>({
  applyLoaded(detail) {
    if (detail) fillForm(detail);
  },
  formRef,
  async load(data) {
    return data.id ? getDictionaryDetailApi(data.id) : undefined;
  },
  onSuccess: () => emit('success'),
  reset: () => Object.assign(model, createDefaultModel()),
  resourceName: '数据字典',
  async save({ id, mode }) {
    if (mode === 'create') {
      await createDictionaryApi(buildCreateRequest());
    } else if (id) {
      await updateDictionaryApi(id, buildUpdateRequest());
    }
  },
});

const rules = computed<FormRules<DictionaryFormModel>>(() => ({
  dictionaryCode: isCreate.value
    ? [
        { message: '请输入字典编码', required: true, trigger: 'blur' },
        { max: 50, message: '字典编码不能超过 50 个字符', trigger: 'blur' },
        {
          message: '字典编码必须以字母开头，只能包含字母、数字、下划线和连字符',
          pattern: /^[A-Za-z][A-Za-z0-9_-]*$/,
          trigger: 'blur',
        },
        {
          trigger: 'blur',
          validator: createUniqueValidator({
            check: async (value) => {
              const result = await checkDictionaryCodeApi(value);
              return result.available;
            },
            message: '字典编码已存在',
            normalize: (value) => value.trim().toLowerCase(),
          }),
        },
      ]
    : [],
  dictionaryName: [
    { message: '请输入字典名称', required: true, trigger: 'blur' },
    { max: 100, message: '字典名称不能超过 100 个字符', trigger: 'blur' },
  ],
}));

function fillForm(detail: DictionaryDetail) {
  model.dictionaryCode = detail.dictionaryCode;
  model.dictionaryName = detail.dictionaryName;
  model.description = detail.description ?? '';
  model.sortOrder = detail.sortOrder;
}

function buildCreateRequest(): DictionaryCreateRequest {
  return {
    description: model.description.trim() || undefined,
    dictionaryCode: model.dictionaryCode.trim().toLowerCase(),
    dictionaryName: model.dictionaryName.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}

function buildUpdateRequest(): DictionaryUpdateRequest {
  return {
    description: model.description.trim() || undefined,
    dictionaryName: model.dictionaryName.trim(),
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
      <ElFormItem label="字典编码" prop="dictionaryCode">
        <ElInput
          v-model="model.dictionaryCode"
          :disabled="!isCreate"
          maxlength="50"
          placeholder="请输入字典编码"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="字典名称" prop="dictionaryName">
        <ElInput
          v-model="model.dictionaryName"
          maxlength="100"
          placeholder="请输入字典名称"
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
          placeholder="请输入字典说明"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
