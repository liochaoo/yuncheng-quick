<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type { OrganizationNodeOption } from '#/api/common/organization';
import type {
  OrganizationNodeCreateRequest,
  OrganizationNodeDetail,
  OrganizationNodeType,
  OrganizationNodeUpdateRequest,
} from '#/api/system/organization';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref, watch } from 'vue';

import { ElForm, ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus';

import {
  createOrganizationNodeApi,
  getOrganizationNodeDetailApi,
  updateOrganizationNodeApi,
} from '#/api/system/organization';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import {
  ORGANIZATION_NODE_TYPE_SELECT_OPTIONS,
  OrganizationNodeSelect,
} from '#/components/organization';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import {
  allowedParentTypes,
  parentAllowsChild,
} from './organization-node-type-rules';

interface DrawerOpenData extends BusinessFormDrawerOpenData {
  defaultParent?: OrganizationNodeOption;
  defaultType?: OrganizationNodeType;
}

interface OrganizationFormModel {
  description: string;
  nodeCode: string;
  nodeName: string;
  nodeType: OrganizationNodeType;
  parentId?: string;
  sortOrder: number;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): OrganizationFormModel => ({
  description: '',
  nodeCode: '',
  nodeName: '',
  nodeType: 'ORGANIZATION',
  parentId: undefined,
  sortOrder: 0,
});

const formRef = ref<FormInstance>();
const model = reactive<OrganizationFormModel>(createDefaultModel());
const selectedParent = ref<OrganizationNodeOption>();

const {
  Drawer,
  initializing,
  isCreate,
  title: drawerTitle,
} = useBusinessFormDrawer<DrawerOpenData, OrganizationNodeDetail | undefined>({
  applyLoaded(detail, data) {
    model.parentId = data.defaultParent?.id;
    model.nodeType = data.defaultType ?? 'ORGANIZATION';
    selectedParent.value = data.defaultParent;
    if (detail) fillForm(detail);
  },
  formRef,
  async load(data) {
    return data.id ? getOrganizationNodeDetailApi(data.id) : undefined;
  },
  onSuccess: () => emit('success'),
  reset() {
    Object.assign(model, createDefaultModel());
    selectedParent.value = undefined;
  },
  resourceName: '组织节点',
  async save({ id, mode }) {
    if (mode === 'create') {
      await createOrganizationNodeApi(buildCreateRequest());
    } else if (id) {
      await updateOrganizationNodeApi(id, buildUpdateRequest());
    }
  },
});

const selectableParentTypes = computed(() =>
  allowedParentTypes(model.nodeType),
);

const rules = computed<FormRules<OrganizationFormModel>>(() => ({
  nodeCode: [
    { message: '请输入节点编码', required: true, trigger: 'blur' },
    { max: 64, message: '节点编码不能超过 64 个字符', trigger: 'blur' },
    {
      message: '节点编码必须以字母开头，只能包含字母、数字、下划线和连字符',
      pattern: /^[A-Za-z][A-Za-z0-9_-]*$/,
      trigger: 'blur',
    },
  ],
  nodeName: [
    { message: '请输入节点名称', required: true, trigger: 'blur' },
    { max: 100, message: '节点名称不能超过 100 个字符', trigger: 'blur' },
    {
      message: '节点名称不能包含斜杠',
      pattern: /^[^/]*$/,
      trigger: 'blur',
    },
  ],
  nodeType: [{ message: '请选择节点类型', required: true, trigger: 'change' }],
  parentId:
    model.nodeType === 'ORGANIZATION'
      ? []
      : [{ message: '请选择上级组织节点', required: true, trigger: 'change' }],
}));

function fillForm(detail: OrganizationNodeDetail) {
  model.description = detail.description ?? '';
  model.nodeCode = detail.nodeCode;
  model.nodeName = detail.nodeName;
  model.nodeType = detail.nodeType;
  model.parentId = detail.parentId ?? undefined;
  model.sortOrder = detail.sortOrder;
}

function buildCreateRequest(): OrganizationNodeCreateRequest {
  return {
    description: model.description.trim() || undefined,
    nodeCode: model.nodeCode.trim().toLowerCase(),
    nodeName: model.nodeName.trim(),
    nodeType: model.nodeType,
    parentId: model.parentId,
    sortOrder: model.sortOrder ?? 0,
  };
}

function buildUpdateRequest(): OrganizationNodeUpdateRequest {
  return {
    description: model.description.trim() || undefined,
    nodeCode: model.nodeCode.trim().toLowerCase(),
    nodeName: model.nodeName.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}

function parentChanged(parent?: OrganizationNodeOption) {
  selectedParent.value = parent;
}

watch(
  () => model.nodeType,
  (nodeType) => {
    if (
      isCreate.value &&
      selectedParent.value &&
      !parentAllowsChild(selectedParent.value.nodeType, nodeType)
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
      <ElFormItem label="节点类型" prop="nodeType">
        <ElSelect
          v-model="model.nodeType"
          class="w-full"
          :disabled="!isCreate"
          placeholder="请选择节点类型"
        >
          <ElOption
            v-for="option in ORGANIZATION_NODE_TYPE_SELECT_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="上级节点" prop="parentId">
        <OrganizationNodeSelect
          v-model="model.parentId"
          :clearable="model.nodeType === 'ORGANIZATION'"
          :disabled="!isCreate"
          placeholder="不选择表示顶级组织"
          :selectable-types="selectableParentTypes"
          @change="parentChanged"
        />
      </ElFormItem>
      <ElFormItem label="节点编码" prop="nodeCode">
        <ElInput
          v-model="model.nodeCode"
          maxlength="64"
          placeholder="请输入节点编码"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="节点名称" prop="nodeName">
        <ElInput
          v-model="model.nodeName"
          maxlength="100"
          placeholder="请输入节点名称"
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
          placeholder="请输入节点说明"
          show-word-limit
          type="textarea"
        />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
