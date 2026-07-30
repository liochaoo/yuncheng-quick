<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { OrgOption } from '#/api/common/organization';
import type {
  OrgCreateRequest,
  OrgDetail,
  OrgType,
  OrgUpdateRequest,
} from '#/api/system/organization';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref, watch } from 'vue';

import { ElForm, ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus';

import {
  createOrgApi,
  getOrgDetailApi,
  updateOrgApi,
} from '#/api/system/organization';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { ORG_TYPE_SELECT_OPTIONS, OrgSelect } from '#/components/organization';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { allowedParentTypes, parentAllowsChild } from './org-type-rules';

interface DrawerOpenData extends BusinessFormDrawerOpenData {
  defaultParent?: OrgOption;
  defaultType?: OrgType;
}

interface OrgFormModel {
  description: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  parentId?: string;
  sortOrder: number;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): OrgFormModel => ({
  description: '',
  orgCode: '',
  orgName: '',
  orgType: 'ORGANIZATION',
  parentId: undefined,
  sortOrder: 0,
});

const formRef = ref<FormInstance>();
const model = reactive<OrgFormModel>(createDefaultModel());
const selectedParent = ref<OrgOption>();

const {
  Drawer,
  initializing,
  isCreate,
  title: drawerTitle,
} = useBusinessFormDrawer<DrawerOpenData, OrgDetail | undefined>({
  applyLoaded(detail, data) {
    model.parentId = data.defaultParent?.id;
    model.orgType = data.defaultType ?? 'ORGANIZATION';
    selectedParent.value = data.defaultParent;
    if (detail) fillForm(detail);
  },
  formRef,
  async load(data) {
    return data.id ? getOrgDetailApi(data.id) : undefined;
  },
  onSuccess: () => emit('success'),
  reset() {
    Object.assign(model, createDefaultModel());
    selectedParent.value = undefined;
  },
  resourceName: '组织',
  async save({ id, mode }) {
    if (mode === 'create') {
      await createOrgApi(buildCreateRequest());
    } else if (id) {
      await updateOrgApi(id, buildUpdateRequest());
    }
  },
});

const selectableParentTypes = computed(() => allowedParentTypes(model.orgType));

const rules = computed<FormRules<OrgFormModel>>(() => ({
  orgCode: [
    { message: '请输入组织编码', required: true, trigger: 'blur' },
    { max: 64, message: '组织编码不能超过 64 个字符', trigger: 'blur' },
    {
      message: '组织编码必须以字母开头，只能包含字母、数字、下划线和连字符',
      pattern: /^[A-Za-z][A-Za-z0-9_-]*$/,
      trigger: 'blur',
    },
  ],
  orgName: [
    { message: '请输入组织名称', required: true, trigger: 'blur' },
    { max: 100, message: '组织名称不能超过 100 个字符', trigger: 'blur' },
    {
      message: '组织名称不能包含斜杠',
      pattern: /^[^/]*$/,
      trigger: 'blur',
    },
  ],
  orgType: [{ message: '请选择组织类型', required: true, trigger: 'change' }],
  parentId:
    model.orgType === 'ORGANIZATION'
      ? []
      : [{ message: '请选择上级组织', required: true, trigger: 'change' }],
}));

function fillForm(detail: OrgDetail) {
  model.description = detail.description ?? '';
  model.orgCode = detail.orgCode;
  model.orgName = detail.orgName;
  model.orgType = detail.orgType;
  model.parentId = detail.parentId ?? undefined;
  model.sortOrder = detail.sortOrder;
}

function buildCreateRequest(): OrgCreateRequest {
  return {
    description: model.description.trim() || undefined,
    orgCode: model.orgCode.trim().toLowerCase(),
    orgName: model.orgName.trim(),
    orgType: model.orgType,
    parentId: model.parentId,
    sortOrder: model.sortOrder ?? 0,
  };
}

function buildUpdateRequest(): OrgUpdateRequest {
  return {
    description: model.description.trim() || undefined,
    orgCode: model.orgCode.trim().toLowerCase(),
    orgName: model.orgName.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}

function parentChanged(parent?: OrgOption) {
  selectedParent.value = parent;
}

watch(
  () => model.orgType,
  (orgType) => {
    if (
      isCreate.value &&
      selectedParent.value &&
      !parentAllowsChild(selectedParent.value.orgType, orgType)
    ) {
      model.parentId = undefined;
      selectedParent.value = undefined;
    }
  },
);
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
      <ElFormItem label="组织类型" prop="orgType">
        <ElSelect
          v-model="model.orgType"
          class="w-full"
          :disabled="!isCreate"
          placeholder="请选择组织类型"
        >
          <ElOption
            v-for="option in ORG_TYPE_SELECT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="上级组织" prop="parentId">
        <OrgSelect
          v-model="model.parentId"
          :clearable="model.orgType === 'ORGANIZATION'"
          :disabled="!isCreate"
          placeholder="不选择表示顶级组织"
          :selectable-types="selectableParentTypes"
          @change="parentChanged"
        />
      </ElFormItem>
      <ElFormItem label="组织编码" prop="orgCode">
        <ElInput
          v-model="model.orgCode"
          maxlength="64"
          placeholder="请输入组织编码"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="组织名称" prop="orgName">
        <ElInput
          v-model="model.orgName"
          maxlength="100"
          placeholder="请输入组织名称"
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
          placeholder="请输入组织说明"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
