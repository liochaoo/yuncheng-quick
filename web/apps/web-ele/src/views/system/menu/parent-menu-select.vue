<script lang="ts" setup>
import type { MenuItem, MenuType } from '#/api/system/menu';

import { computed, watch } from 'vue';

import { ElTreeSelect } from 'element-plus';

import { menuTypeAllowsChild } from './menu-type-rules';

const props = defineProps<{
  currentId?: string;
  disabled?: boolean;
  menuTree: MenuItem[];
  menuType: MenuType;
  modelValue?: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value?: string];
}>();

interface MenuTreeOption {
  children?: MenuTreeOption[];
  disabled: boolean;
  label: string;
  menuType: MenuType;
  value: string;
}

function toOption(menu: MenuItem, excludedBranch = false): MenuTreeOption {
  const excluded = excludedBranch || menu.id === props.currentId;
  return {
    children: menu.children?.map((child) => toOption(child, excluded)),
    disabled: excluded || !menuTypeAllowsChild(menu.menuType, props.menuType),
    label: menu.menuName,
    menuType: menu.menuType,
    value: menu.id,
  };
}

function findMenu(menus: MenuItem[], id?: string): MenuItem | undefined {
  if (!id) return undefined;
  for (const menu of menus) {
    if (menu.id === id) return menu;
    const child = findMenu(menu.children ?? [], id);
    if (child) return child;
  }
}

function containsId(menu: MenuItem | undefined, id?: string): boolean {
  if (!menu || !id) return false;
  if (menu.id === id) return true;
  return (menu.children ?? []).some((child) => containsId(child, id));
}

const treeData = computed(() => props.menuTree.map((menu) => toOption(menu)));

watch(
  () => [props.currentId, props.menuTree, props.menuType, props.modelValue],
  () => {
    const selected = findMenu(props.menuTree, props.modelValue);
    const current = findMenu(props.menuTree, props.currentId);
    if (
      (selected && !menuTypeAllowsChild(selected.menuType, props.menuType)) ||
      containsId(current, props.modelValue)
    ) {
      emit('update:modelValue', undefined);
    }
  },
);
</script>

<template>
  <ElTreeSelect
    check-strictly
    class="w-full"
    clearable
    :data="treeData"
    :disabled="disabled"
    filterable
    :model-value="modelValue"
    node-key="value"
    placeholder="不选择表示一级菜单"
    :props="{
      children: 'children',
      disabled: 'disabled',
      label: 'label',
    }"
    render-after-expand
    value-key="value"
    @update:model-value="emit('update:modelValue', $event || undefined)"
  />
</template>
