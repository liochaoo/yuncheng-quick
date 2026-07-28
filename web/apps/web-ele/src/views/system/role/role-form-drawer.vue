<script lang="ts" setup>
import type { FormInstance, FormRules } from 'element-plus';

import type {
  RoleCreateRequest,
  RoleDetail,
  RoleUpdateRequest,
} from '#/api/system/role';
import type { RoleType } from '#/api/system/types';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref } from 'vue';

import {
  ElForm,
  ElFormItem,
  ElInput,
  ElRadioButton,
  ElRadioGroup,
} from 'element-plus';

import {
  checkRoleUniquenessApi,
  createRoleApi,
  getRoleDetailApi,
  updateRoleApi,
} from '#/api/system/role';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { useIsSuperAdmin } from '#/hooks/use-super-admin';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import { createUniqueValidator } from '#/utils/form-validation';

import { ROLE_TYPE_TAG_OPTIONS } from '../_shared/display-options';

interface RoleFormModel {
  roleCode: string;
  roleName: string;
  roleType: RoleType;
  sortOrder: number;
}

const emit = defineEmits<{
  success: [];
}>();

const createDefaultModel = (): RoleFormModel => ({
  roleCode: '',
  roleName: '',
  roleType: 'CUSTOM',
  sortOrder: 0,
});

const formRef = ref<FormInstance>();
const model = reactive<RoleFormModel>(createDefaultModel());
const isSuperAdmin = useIsSuperAdmin();

const {
  Drawer,
  initializing,
  isCreate,
  recordId,
  title: drawerTitle,
} = useBusinessFormDrawer<BusinessFormDrawerOpenData, RoleDetail | undefined>({
  applyLoaded(detail) {
    if (detail) fillForm(detail);
  },
  formRef,
  async load(data) {
    return data.id ? getRoleDetailApi(data.id) : undefined;
  },
  onSuccess: () => emit('success'),
  reset: resetModel,
  resourceName: '角色',
  async save({ id, mode }) {
    if (mode === 'create') {
      await createRoleApi(buildCreateRequest());
    } else if (id) {
      await updateRoleApi(id, buildUpdateRequest());
    }
  },
});

const rules = computed<FormRules<RoleFormModel>>(() => ({
  roleCode: isCreate.value
    ? [
        { message: '请输入角色编码', required: true, trigger: 'blur' },
        { max: 50, message: '角色编码不能超过 50 个字符', trigger: 'blur' },
        {
          message: '角色编码必须以字母开头，只能包含字母、数字、下划线和连字符',
          pattern: /^[A-Za-z][A-Za-z0-9_-]*$/,
          trigger: 'blur',
        },
        {
          trigger: 'blur',
          validator: createUniqueValidator({
            check: async (value) => {
              const result = await checkRoleUniquenessApi({
                field: 'ROLE_CODE',
                value,
              });
              return result.available;
            },
            message: '角色编码已存在',
            normalize: (value) => value.trim().toLowerCase(),
          }),
        },
      ]
    : [],
  roleName: [
    { message: '请输入角色名称', required: true, trigger: 'blur' },
    { max: 100, message: '角色名称不能超过 100 个字符', trigger: 'blur' },
    {
      trigger: 'blur',
      validator: createUniqueValidator({
        check: async (value) => {
          const result = await checkRoleUniquenessApi({
            field: 'ROLE_NAME',
            id: recordId.value,
            value,
          });
          return result.available;
        },
        message: '角色名称已存在',
      }),
    },
  ],
  roleType: [{ message: '请选择角色类型', required: true, trigger: 'change' }],
}));

function resetModel() {
  Object.assign(model, createDefaultModel());
}

function fillForm(detail: RoleDetail) {
  model.roleCode = detail.roleCode;
  model.roleName = detail.roleName;
  model.roleType = detail.roleType;
  model.sortOrder = detail.sortOrder;
}

function buildCreateRequest(): RoleCreateRequest {
  return {
    roleCode: model.roleCode.trim().toLowerCase(),
    roleName: model.roleName.trim(),
    roleType: model.roleType,
    sortOrder: model.sortOrder ?? 0,
  };
}

function buildUpdateRequest(): RoleUpdateRequest {
  return {
    roleName: model.roleName.trim(),
    sortOrder: model.sortOrder ?? 0,
  };
}
</script>

<template>
  <Drawer
    :loading="initializing"
    :title="drawerTitle"
    :class="BUSINESS_FORM_DRAWER_WIDTH.small"
  >
    <ElForm
      ref="formRef"
      class="px-4"
      label-width="100px"
      :model="model"
      :rules="rules"
      :validate-on-rule-change="false"
    >
      <ElFormItem label="角色编码" prop="roleCode">
        <ElInput
          v-model="model.roleCode"
          :disabled="!isCreate"
          maxlength="50"
          placeholder="请输入角色编码"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="角色名称" prop="roleName">
        <ElInput
          v-model="model.roleName"
          maxlength="100"
          placeholder="请输入角色名称"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem v-if="isCreate" label="角色类型" prop="roleType">
        <ElRadioGroup v-model="model.roleType">
          <ElRadioButton value="CUSTOM">自定义</ElRadioButton>
          <ElRadioButton v-if="isSuperAdmin" value="SYSTEM">
            系统角色
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem v-else label="角色类型">
        <ElInput
          :model-value="ROLE_TYPE_TAG_OPTIONS[model.roleType].label"
          disabled
        />
      </ElFormItem>
      <ElFormItem label="排序号" prop="sortOrder">
        <SortOrderInput v-model="model.sortOrder" />
      </ElFormItem>
    </ElForm>
  </Drawer>
</template>
