<script lang="ts" setup>
import type { FormInstance } from 'element-plus';

import type { UserFormModel } from './user-form-model';

import type { UserFormData } from '#/api/system/user';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { nextTick, reactive, ref } from 'vue';

import { ElButton, ElForm, ElFormItem, ElInput } from 'element-plus';

import {
  createUserApi,
  getUserFormDataApi,
  updateUserApi,
} from '#/api/system/user';
import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';
import SortOrderInput from '#/components/form/sort-order-input.vue';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';
import { USERNAME_INPUT_GUIDE } from '#/utils/form-validation';

import { useUserFormRules } from './use-user-form-rules';
import {
  buildUserCreateRequest,
  buildUserUpdateRequest,
  createDefaultUserForm,
  fillUserForm,
} from './user-form-model';
import UserOrgAssignmentEditor from './user-org-assignment-editor.vue';
import UserRoleSelect from './user-role-select.vue';

type UserRoleSelectInstance = InstanceType<typeof UserRoleSelect>;
type UserOrgAssignmentEditorInstance = InstanceType<
  typeof UserOrgAssignmentEditor
>;

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const model = reactive<UserFormModel>(createDefaultUserForm());
const roleSelectRef = ref<UserRoleSelectInstance>();
const orgAssignmentRef = ref<UserOrgAssignmentEditorInstance>();

const {
  Drawer,
  initializing,
  isCreate,
  recordId,
  title: drawerTitle,
} = useBusinessFormDrawer<BusinessFormDrawerOpenData, undefined | UserFormData>(
  {
    async applyLoaded(detail) {
      if (detail) fillUserForm(model, detail);
      await nextTick();
      await Promise.all([
        roleSelectRef.value?.loadSelected(model.roleIds),
        orgAssignmentRef.value?.loadSelected(model.orgIds),
      ]);
    },
    formRef,
    async load(data) {
      return data.id ? getUserFormDataApi(data.id) : undefined;
    },
    onClose: () => {
      roleSelectRef.value?.clearOptions();
      orgAssignmentRef.value?.clearOptions();
    },
    onSuccess: () => emit('success'),
    reset: resetModel,
    resourceName: '用户',
    async save({ id, mode }) {
      if (mode === 'create') {
        await createUserApi(buildUserCreateRequest(model));
      } else if (id) {
        await updateUserApi(id, buildUserUpdateRequest(model));
      }
    },
  },
);

const rules = useUserFormRules(model, isCreate, recordId);

function resetModel() {
  Object.assign(model, createDefaultUserForm());
}

function openOrgSelection() {
  orgAssignmentRef.value?.openManage();
}
</script>

<template>
  <Drawer
    :loading="initializing"
    :title="drawerTitle"
    :class="BUSINESS_FORM_DRAWER_WIDTH.smallWide"
  >
    <ElForm
      ref="formRef"
      class="px-4"
      label-width="100px"
      :model="model"
      :rules="rules"
      scroll-to-error
      :validate-on-rule-change="false"
    >
      <FormSection title="基础信息">
        <FormGrid :columns="1">
          <ElFormItem label="登录名" prop="username">
            <div class="w-full">
              <ElInput
                v-model="model.username"
                autocomplete="off"
                :disabled="!isCreate"
                maxlength="50"
                placeholder="请输入登录名"
                show-word-limit
              />
              <p
                v-if="isCreate"
                class="mt-1 text-xs leading-5 text-[var(--el-text-color-secondary)]"
              >
                {{ USERNAME_INPUT_GUIDE }}
              </p>
            </div>
          </ElFormItem>
          <ElFormItem label="姓名" prop="realName">
            <ElInput
              v-model="model.realName"
              maxlength="64"
              placeholder="请输入姓名"
              show-word-limit
            />
          </ElFormItem>
          <ElFormItem v-if="isCreate" label="初始密码" prop="password">
            <ElInput
              v-model="model.password"
              autocomplete="new-password"
              placeholder="请输入初始密码"
              show-password
              type="password"
            />
          </ElFormItem>
          <ElFormItem v-if="isCreate" label="确认密码" prop="confirmPassword">
            <ElInput
              v-model="model.confirmPassword"
              autocomplete="new-password"
              placeholder="请再次输入初始密码"
              show-password
              type="password"
            />
          </ElFormItem>
          <ElFormItem label="手机号码" prop="phone">
            <ElInput
              v-model="model.phone"
              maxlength="32"
              placeholder="请输入手机号码"
            />
          </ElFormItem>
          <ElFormItem label="电子邮箱" prop="email">
            <ElInput
              v-model="model.email"
              maxlength="254"
              placeholder="请输入电子邮箱"
            />
          </ElFormItem>
          <ElFormItem label="排序号" prop="sortOrder">
            <SortOrderInput v-model="model.sortOrder" />
          </ElFormItem>
        </FormGrid>
      </FormSection>
      <FormSection title="归属组织">
        <template #actions>
          <ElButton type="primary" plain @click="openOrgSelection">
            添加归属组织
          </ElButton>
        </template>
        <ElFormItem label-width="0" prop="orgIds">
          <UserOrgAssignmentEditor
            ref="orgAssignmentRef"
            v-model="model.orgIds"
            v-model:primary-org-id="model.primaryOrgId"
          />
        </ElFormItem>
      </FormSection>
      <FormSection title="角色配置">
        <ElFormItem label="角色" prop="roleIds">
          <UserRoleSelect ref="roleSelectRef" v-model="model.roleIds" />
        </ElFormItem>
      </FormSection>
    </ElForm>
  </Drawer>
</template>
