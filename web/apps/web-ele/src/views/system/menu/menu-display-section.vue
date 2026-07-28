<script lang="ts" setup>
import type { VNode } from 'vue';

import type { MenuFormModel } from './menu-form-model';

import { IconPicker } from '@vben/common-ui';

import { ElFormItem, ElInput, ElOption, ElSelect } from 'element-plus';

import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';
import { MENU_ICON_PREFIX } from '#/components/icons';

const props = defineProps<{ model: MenuFormModel }>();
const model = props.model;

const iconInputComponent = ElInput as unknown as VNode;
const badgeTypeOptions = [
  { label: '圆点', value: 'dot' },
  { label: '文本', value: 'normal' },
];
const badgeVariantOptions = [
  { label: '默认', value: 'default' },
  { label: '主要', value: 'primary' },
  { label: '成功', value: 'success' },
  { label: '警告', value: 'warning' },
  { label: '危险', value: 'destructive' },
];
</script>

<template>
  <FormSection title="展示与图标">
    <FormGrid>
      <ElFormItem label="图标" prop="icon">
        <IconPicker
          v-model="model.icon"
          :auto-fetch-api="false"
          class="w-full"
          icon-slot="append"
          :input-component="iconInputComponent"
          model-value-prop="model-value"
          placeholder="请选择图标"
          :prefix="MENU_ICON_PREFIX"
        />
      </ElFormItem>
      <ElFormItem label="激活图标" prop="activeIcon">
        <IconPicker
          v-model="model.activeIcon"
          :auto-fetch-api="false"
          class="w-full"
          icon-slot="append"
          :input-component="iconInputComponent"
          model-value-prop="model-value"
          placeholder="请选择激活图标"
          :prefix="MENU_ICON_PREFIX"
        />
      </ElFormItem>
      <ElFormItem label="徽标类型">
        <ElSelect
          v-model="model.badgeType"
          class="w-full"
          clearable
          placeholder="请选择"
        >
          <ElOption
            v-for="option in badgeTypeOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem v-if="model.badgeType === 'normal'" label="徽标内容">
        <ElInput
          v-model="model.badge"
          maxlength="100"
          placeholder="请输入徽标文本"
        />
      </ElFormItem>
      <ElFormItem label="徽标样式">
        <ElSelect
          v-model="model.badgeVariants"
          class="w-full"
          clearable
          placeholder="请选择"
        >
          <ElOption
            v-for="option in badgeVariantOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </ElSelect>
      </ElFormItem>
    </FormGrid>
  </FormSection>
</template>
