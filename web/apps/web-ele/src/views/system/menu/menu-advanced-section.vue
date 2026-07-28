<script lang="ts" setup>
import type { MenuFormModel } from './menu-form-model';

import { Plus, Trash2 } from '@vben/icons';

import {
  ElButton,
  ElCheckbox,
  ElFormItem,
  ElInput,
  ElInputNumber,
} from 'element-plus';

import FormGrid from '#/components/form/form-grid.vue';
import FormSection from '#/components/form/form-section.vue';

import { createQueryItem } from './menu-form-model';

const props = defineProps<{ model: MenuFormModel }>();
const model = props.model;

function addQueryItem() {
  model.queryItems.push(createQueryItem());
}

function removeQueryItem(index: number) {
  model.queryItems.splice(index, 1);
}
</script>

<template>
  <FormSection title="高级设置">
    <FormGrid>
      <div class="form-group-title md:col-span-2">菜单与导航</div>
      <ElFormItem>
        <ElCheckbox v-model="model.hideInMenu">在菜单中隐藏</ElCheckbox>
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'CATALOG' || model.menuType === 'MENU'"
      >
        <ElCheckbox v-model="model.hideChildrenInMenu"> 隐藏子菜单 </ElCheckbox>
      </ElFormItem>
      <ElFormItem v-if="model.menuType !== 'LINK'">
        <ElCheckbox v-model="model.hideInBreadcrumb">
          在面包屑中隐藏
        </ElCheckbox>
      </ElFormItem>
      <ElFormItem v-if="model.menuType !== 'LINK'">
        <ElCheckbox v-model="model.hideInTab">在标签页中隐藏</ElCheckbox>
      </ElFormItem>
      <div class="form-group-title md:col-span-2">页面与标签页</div>
      <ElFormItem v-if="model.menuType === 'MENU'">
        <ElCheckbox v-model="model.keepAlive">缓存页面</ElCheckbox>
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'EMBEDDED' || model.menuType === 'MENU'"
      >
        <ElCheckbox v-model="model.affixTab">固定标签页</ElCheckbox>
      </ElFormItem>
      <ElFormItem>
        <ElCheckbox v-model="model.fullPathKey">
          使用完整路径作为标签页标识
        </ElCheckbox>
      </ElFormItem>
      <ElFormItem v-if="model.menuType === 'LINK'">
        <ElCheckbox v-model="model.openInNewWindow">在新窗口打开</ElCheckbox>
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'EMBEDDED' || model.menuType === 'MENU'"
      >
        <ElCheckbox v-model="model.noBasicLayout">不使用基础布局</ElCheckbox>
      </ElFormItem>
      <ElFormItem v-if="model.affixTab" label="固定标签顺序">
        <ElInputNumber v-model="model.affixTabOrder" class="!w-full" :min="0" />
      </ElFormItem>
      <ElFormItem
        v-if="model.menuType === 'EMBEDDED' || model.menuType === 'MENU'"
        label="最大标签页数量"
      >
        <ElInputNumber
          v-model="model.maxNumOfOpenTab"
          class="!w-full"
          :min="1"
        />
      </ElFormItem>
      <div class="form-group-title md:col-span-2">路由参数</div>
      <ElFormItem class="md:col-span-2" label="查询参数">
        <div class="w-full space-y-3">
          <div
            v-for="(item, index) in model.queryItems"
            :key="item.id"
            class="grid grid-cols-[1fr_1fr_auto] gap-3"
          >
            <ElInput v-model="item.key" placeholder="参数名" />
            <ElInput v-model="item.value" placeholder="参数值" />
            <ElButton
              aria-label="删除参数"
              circle
              plain
              type="danger"
              @click="removeQueryItem(index)"
            >
              <Trash2 class="size-4" />
            </ElButton>
          </div>
          <ElButton plain @click="addQueryItem">
            <Plus class="mr-1 size-4" />
            添加参数
          </ElButton>
        </div>
      </ElFormItem>
    </FormGrid>
  </FormSection>
</template>

<style scoped>
.form-group-title {
  padding-bottom: 8px;
  margin-bottom: 2px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  border-bottom: 1px dashed var(--el-border-color-lighter);
}
</style>
