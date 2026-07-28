<!--
  Element Plus 业务表单参考模板。

  本文件不参与应用运行。创建业务表单时按实际需要选取结构，不要整份复制后保留无用字段。
-->
<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref } from 'vue';

import {
  ElCheckboxGroup,
  ElDatePicker,
  ElForm,
  ElFormItem,
  ElInput,
  ElOption,
  ElRadioGroup,
  ElSelect,
  ElSwitch,
} from 'element-plus';

import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import { createUniqueValidator } from '#/utils/form-validation';

type DrawerOpenData = BusinessFormDrawerOpenData;

interface ExampleDetail {
  code: string;
  date?: string;
  description?: string;
  enabled: boolean;
  id: string;
  name: string;
  number: number;
  optionIds: string[];
  type: string;
}

interface ExampleFormModel {
  code: string;
  confirmPassword: string;
  date?: string;
  description: string;
  enabled: boolean;
  name: string;
  number: number;
  optionIds: string[];
  password: string;
  type: string;
}

interface ExampleSaveRequest {
  code: string;
  date?: string;
  description?: string;
  enabled: boolean;
  name: string;
  number: number;
  optionIds: string[];
  password?: string;
  type: string;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): ExampleFormModel => ({
  code: '',
  confirmPassword: '',
  date: undefined,
  description: '',
  enabled: true,
  name: '',
  number: 0,
  optionIds: [],
  password: '',
  type: '',
});

const formRef = ref<FormInstance>();
const model = reactive<ExampleFormModel>(createDefaultModel());

const {
  Drawer,
  initializing,
  isDetail,
  mode,
  recordId,
  title: drawerTitle,
} = useBusinessFormDrawer<DrawerOpenData>({
  formRef,
  async load(data) {
    if (data.id) fillForm(await getDetailApi(data.id));
  },
  onSuccess: () => emit('success'),
  reset: resetModel,
  resourceName: '示例',
  async save({ id, mode }) {
    const request = buildRequest();
    await (mode === 'edit' && id ? updateApi(id, request) : createApi(request));
  },
  snapshot: () => snapshot(),
});

const isEdit = computed(() => mode.value === 'edit');

// 替换成业务模块真实 API。
async function getDetailApi(id: string): Promise<ExampleDetail> {
  return {
    code: id,
    enabled: true,
    id,
    name: '示例',
    number: 0,
    optionIds: [],
    type: 'DEFAULT',
  };
}

async function checkCodeAvailableApi(_value: string, _id?: string) {
  return true;
}

function createApi(_request: ExampleSaveRequest) {
  return Promise.resolve();
}

function updateApi(_id: string, _request: ExampleSaveRequest) {
  return Promise.resolve();
}

const rules: FormRules<ExampleFormModel> = {
  code: [
    { message: '请输入编码', required: true, trigger: 'blur' },
    { max: 50, message: '编码不能超过 50 个字符', trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createUniqueValidator({
        check: (value) => checkCodeAvailableApi(value, recordId.value),
        message: '编码已存在',
      }),
    },
  ],
  confirmPassword: [
    {
      trigger: 'blur',
      validator: (_rule, value, callback) => {
        if (model.password && value !== model.password) {
          callback(new Error('两次输入的密码不一致'));
          return;
        }
        callback();
      },
    },
  ],
  name: [
    { message: '请输入名称', required: true, trigger: 'blur' },
    { max: 100, message: '名称不能超过 100 个字符', trigger: 'blur' },
  ],
  optionIds: [
    {
      message: '请至少选择一项',
      required: true,
      trigger: 'change',
      type: 'array',
    },
  ],
  type: [{ message: '请选择类型', required: true, trigger: 'change' }],
};

function resetModel() {
  Object.assign(model, createDefaultModel());
}

function fillForm(detail: ExampleDetail) {
  model.code = detail.code;
  model.date = detail.date;
  model.description = detail.description ?? '';
  model.enabled = detail.enabled;
  model.name = detail.name;
  model.number = detail.number;
  model.optionIds = [...detail.optionIds];
  model.type = detail.type;
}

function buildRequest(): ExampleSaveRequest {
  return {
    code: model.code.trim(),
    date: model.date,
    description: model.description.trim() || undefined,
    enabled: model.enabled,
    name: model.name.trim(),
    number: model.number ?? 0,
    optionIds: [...model.optionIds],
    password: model.password || undefined,
    type: model.type,
  };
}

function snapshot(): string {
  return JSON.stringify(buildRequest());
}
</script>

<template>
  <Drawer
    :loading="initializing"
    :title="drawerTitle"
    :class="BUSINESS_FORM_DRAWER_WIDTH.large"
  >
    <ElForm
      ref="formRef"
      class="px-4"
      label-position="right"
      label-width="100px"
      :disabled="isDetail"
      :model="model"
      :rules="rules"
      scroll-to-error
      :validate-on-rule-change="false"
    >
      <FormSection title="基础信息">
        <FormGrid>
          <ElFormItem label="编码" prop="code">
            <ElInput
              v-model="model.code"
              :disabled="isEdit"
              maxlength="50"
              placeholder="请输入编码"
              show-word-limit
            />
          </ElFormItem>
          <ElFormItem label="名称" prop="name">
            <ElInput
              v-model="model.name"
              maxlength="100"
              placeholder="请输入名称"
              show-word-limit
            />
          </ElFormItem>
          <ElFormItem label="类型" prop="type">
            <ElRadioGroup v-model="model.type">
              <!-- 按业务提供选项 -->
            </ElRadioGroup>
          </ElFormItem>
          <ElFormItem label="排序号" prop="number">
            <SortOrderInput v-model="model.number" />
          </ElFormItem>
          <ElFormItem label="日期" prop="date">
            <ElDatePicker
              v-model="model.date"
              class="!w-full"
              placeholder="请选择日期"
              type="date"
              value-format="YYYY-MM-DD"
            />
          </ElFormItem>
          <ElFormItem label="是否启用" prop="enabled">
            <ElSwitch v-model="model.enabled" />
          </ElFormItem>
          <ElFormItem class="md:col-span-2" label="选择项" prop="optionIds">
            <ElSelect
              v-model="model.optionIds"
              class="w-full"
              collapse-tags
              filterable
              multiple
              placeholder="请选择"
            >
              <ElOption label="示例" value="example" />
            </ElSelect>
          </ElFormItem>
          <ElFormItem class="md:col-span-2" label="多选示例" prop="optionIds">
            <ElCheckboxGroup v-model="model.optionIds">
              <!-- 按业务提供选项 -->
            </ElCheckboxGroup>
          </ElFormItem>
          <ElFormItem class="md:col-span-2" label="说明" prop="description">
            <ElInput
              v-model="model.description"
              :autosize="{ minRows: 3, maxRows: 8 }"
              maxlength="500"
              placeholder="请输入说明"
              show-word-limit
              type="textarea"
            />
          </ElFormItem>
        </FormGrid>
      </FormSection>

      <FormSection v-if="mode === 'create'" title="安全信息">
        <FormGrid>
          <ElFormItem label="密码" prop="password">
            <ElInput
              v-model="model.password"
              autocomplete="new-password"
              placeholder="请输入密码"
              show-password
              type="password"
            />
          </ElFormItem>
          <ElFormItem label="确认密码" prop="confirmPassword">
            <ElInput
              v-model="model.confirmPassword"
              autocomplete="new-password"
              placeholder="请再次输入密码"
              show-password
              type="password"
            />
          </ElFormItem>
        </FormGrid>
      </FormSection>
    </ElForm>
  </Drawer>
</template>
