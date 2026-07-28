<script lang="ts" setup>
import type { MenuFormModel } from './menu-form-model';

import type { MenuItem } from '#/api/system/menu';

import { ElFormItem, ElInput, ElRadioButton, ElRadioGroup } from 'element-plus';

import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';
import SortOrderInput from '#/components/form/sort-order-input.vue';

import { MENU_TYPE_SELECT_OPTIONS } from '../_shared/display-options';
import ParentMenuSelect from './parent-menu-select.vue';

const props = defineProps<{
  currentId?: string;
  menuTree: MenuItem[];
  model: MenuFormModel;
}>();
const model = props.model;
</script>

<template>
  <FormSection title="基础信息">
    <FormGrid>
      <ElFormItem class="md:col-span-2" label="菜单类型" prop="menuType">
        <ElRadioGroup v-model="model.menuType">
          <ElRadioButton
            v-for="option in MENU_TYPE_SELECT_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </ElRadioButton>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="菜单名称" prop="menuName">
        <ElInput
          v-model="model.menuName"
          maxlength="100"
          placeholder="请输入菜单名称"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="上级菜单" prop="parentId">
        <ParentMenuSelect
          v-model="model.parentId"
          :current-id="props.currentId"
          :menu-tree="props.menuTree"
          :menu-type="model.menuType"
        />
      </ElFormItem>
      <ElFormItem label="排序号" prop="sortOrder">
        <SortOrderInput v-model="model.sortOrder" />
      </ElFormItem>
    </FormGrid>
  </FormSection>
</template>
