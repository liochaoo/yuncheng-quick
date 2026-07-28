<script lang="ts" setup>
import type { FormInstance } from 'element-plus';

import type { MenuFormModel } from './menu-form-model';

import type { MenuItem, MenuType } from '#/api/system/menu';
import type { BusinessFormDrawerOpenData } from '#/types/business-form';

import { computed, reactive, ref } from 'vue';

import { ElForm, ElMessage } from 'element-plus';

import {
  createMenuApi,
  getMenuDetailApi,
  getMenuTreeApi,
  updateMenuApi,
} from '#/api/system/menu';
import { useBusinessFormDrawer } from '#/hooks/use-business-form-drawer';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import MenuAdvancedSection from './menu-advanced-section.vue';
import MenuBasicSection from './menu-basic-section.vue';
import MenuDisplaySection from './menu-display-section.vue';
import {
  buildMenuSaveRequest,
  createDefaultMenuForm,
  fillMenuForm,
  queryItemsError,
} from './menu-form-model';
import MenuRouteSection from './menu-route-section.vue';
import { useMenuFormRules } from './use-menu-form-rules';

interface DrawerOpenData extends BusinessFormDrawerOpenData {
  defaultParentId?: string;
  defaultType?: MenuType;
}

interface MenuFormLoadedData {
  detail?: MenuItem;
  menus: MenuItem[];
}

const emit = defineEmits<{
  success: [];
}>();

const formRef = ref<FormInstance>();
const menuTree = ref<MenuItem[]>([]);
const model = reactive<MenuFormModel>(createDefaultMenuForm());

const {
  Drawer,
  initializing,
  recordId,
  title: drawerTitle,
} = useBusinessFormDrawer<DrawerOpenData, MenuFormLoadedData>({
  applyLoaded({ detail, menus }, data) {
    model.parentId = data.defaultParentId;
    model.menuType = data.defaultType ?? 'CATALOG';
    menuTree.value = menus;
    if (detail) fillMenuForm(model, detail);
  },
  beforeSave() {
    const queryError = queryItemsError(model.queryItems);
    if (!queryError) return true;
    ElMessage.warning(queryError);
    return false;
  },
  formRef,
  async load(data) {
    const [menus, detail] = await Promise.all([
      getMenuTreeApi(),
      data.id
        ? getMenuDetailApi(data.id).then((result) => result.menu)
        : Promise.resolve(undefined),
    ]);
    return { detail, menus };
  },
  onSuccess: () => emit('success'),
  reset: resetForm,
  resourceName: '菜单',
  async save({ id, mode }) {
    const request = buildMenuSaveRequest(model);
    if (mode === 'create') {
      await createMenuApi(request);
    } else if (id) {
      await updateMenuApi(id, request);
    }
  },
});

const isRoute = computed(() => model.menuType !== 'BUTTON');
const rules = useMenuFormRules(model, recordId);

function resetForm() {
  Object.assign(model, createDefaultMenuForm());
  menuTree.value = [];
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
      label-position="top"
      :model="model"
      :rules="rules"
      scroll-to-error
      :validate-on-rule-change="false"
    >
      <MenuBasicSection
        :current-id="recordId"
        :menu-tree="menuTree"
        :model="model"
      />
      <MenuRouteSection :model="model" />
      <MenuDisplaySection v-if="isRoute" :model="model" />
      <MenuAdvancedSection v-if="isRoute" :model="model" />
    </ElForm>
  </Drawer>
</template>
