<script lang="ts" setup>
import type { MenuFormModel } from './menu-form-model';

import { computed } from 'vue';

import { ElFormItem, ElInput } from 'element-plus';

import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';

const props = defineProps<{ model: MenuFormModel }>();
const model = props.model;

const isRoute = computed(() => model.menuType !== 'BUTTON');
</script>

<template>
  <FormSection :title="isRoute ? '路由与权限' : '权限配置'">
    <FormGrid>
      <ElFormItem v-if="isRoute" label="路由名称" prop="routeName">
        <ElInput
          v-model="model.routeName"
          maxlength="100"
          placeholder="例如 SystemUser"
        />
      </ElFormItem>
      <ElFormItem v-if="isRoute" label="路由路径" prop="routePath">
        <ElInput
          v-model="model.routePath"
          maxlength="255"
          placeholder="例如 /system/user"
        />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'MENU'"
        label="页面组件"
        prop="componentPath"
      >
        <ElInput
          v-model="model.componentPath"
          maxlength="255"
          placeholder="例如 /system/user/index"
        />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'EMBEDDED'"
        class="md:col-span-2"
        label="内嵌地址"
        prop="iframeSrc"
      >
        <ElInput
          v-model="model.iframeSrc"
          maxlength="1000"
          placeholder="https://..."
        />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'LINK'"
        class="md:col-span-2"
        label="链接地址"
        prop="link"
      >
        <ElInput
          v-model="model.link"
          maxlength="1000"
          placeholder="https://..."
        />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'BUTTON' || model.menuType === 'MENU'"
        :class="{ 'md:col-span-2': model.menuType === 'BUTTON' }"
        label="权限码"
        prop="permissionCode"
      >
        <ElInput
          v-model="model.permissionCode"
          maxlength="128"
          placeholder="例如 system:user:query"
        />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'CATALOG' || model.menuType === 'MENU'"
        label="重定向路径"
        prop="redirect"
      >
        <ElInput v-model="model.redirect" maxlength="255" placeholder="可选" />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'EMBEDDED' || model.menuType === 'MENU'"
        label="激活菜单路径"
        prop="activePath"
      >
        <ElInput
          v-model="model.activePath"
          maxlength="255"
          placeholder="隐藏菜单时可指定"
        />
      </ElFormItem>
    </FormGrid>
  </FormSection>
</template>
