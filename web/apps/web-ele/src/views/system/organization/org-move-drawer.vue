<script lang="ts" setup>
import type {
  OrgMoveImpact,
  OrgOption,
  OrgType,
} from '#/api/system/organization';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
} from 'element-plus';

import { getOrgMoveImpactApi, moveOrgApi } from '#/api/system/organization';
import { OrgSelect } from '#/components/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { allowedParentTypes } from './org-type-rules';

interface DrawerOpenData {
  org: OrgOption;
}

const emit = defineEmits<{
  success: [];
}>();

const currentOrg = ref<OrgOption>();
const impact = ref<OrgMoveImpact>();
const parentId = ref<string>();
const originalParentId = ref<string>();
const impactRequest = useLatestRequest();
const impactLoading = impactRequest.loading;
const title = computed(() =>
  currentOrg.value ? `移动组织：${currentOrg.value.orgName}` : '移动组织',
);
const selectableParentTypes = computed<OrgType[]>(() =>
  currentOrg.value ? allowedParentTypes(currentOrg.value.orgType) : [],
);
const canMoveToRoot = computed(
  () => currentOrg.value?.orgType === 'ORGANIZATION',
);

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const org = currentOrg.value;
    if (!org) return;
    if (parentId.value === originalParentId.value) {
      ElMessage.warning('请选择不同的上级组织');
      return;
    }
    let currentImpact = impact.value;
    if (!currentImpact) {
      currentImpact = await loadImpact();
    }
    if (!currentImpact) return;
    drawerApi.lock();
    try {
      await moveOrgApi(org.id, parentId.value);
      ElMessage.success('移动成功');
      emit('success');
      drawerApi.close();
    } finally {
      drawerApi.unlock();
    }
  },
  onOpenChange(isOpen) {
    impactRequest.invalidate();
    impact.value = undefined;
    if (!isOpen) {
      currentOrg.value = undefined;
      parentId.value = undefined;
      originalParentId.value = undefined;
      return;
    }
    const data = drawerApi.getData<DrawerOpenData>();
    currentOrg.value = data.org;
    parentId.value = data.org.parentId ?? undefined;
    originalParentId.value = data.org.parentId ?? undefined;
    drawerApi.setState({
      confirmText: '确认移动',
      showCancelButton: true,
    });
  },
});

async function loadImpact() {
  const org = currentOrg.value;
  if (!org || parentId.value === originalParentId.value) {
    impact.value = undefined;
    return undefined;
  }
  const result = await impactRequest.execute(() =>
    getOrgMoveImpactApi(org.id, parentId.value),
  );
  if (result) impact.value = result;
  return result;
}

function parentChanged() {
  void loadImpact();
}
</script>

<template>
  <Drawer
    :class="BUSINESS_FORM_DRAWER_WIDTH.medium"
    :loading="impactLoading"
    :title="title"
  >
    <div v-if="currentOrg" class="space-y-5 px-4">
      <ElDescriptions border :column="1">
        <ElDescriptionsItem label="当前组织">
          {{ currentOrg.orgName }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前路径">
          {{ currentOrg.fullPath }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <div>
        <div class="mb-2 text-sm font-medium">新的上级组织</div>
        <OrgSelect
          v-model="parentId"
          :clearable="canMoveToRoot"
          :exclude-subtree-root-id="currentOrg.id"
          permission-scope="organization-management"
          placeholder="不选择表示移动为顶级组织"
          :selectable-types="selectableParentTypes"
          @change="parentChanged"
        />
      </div>

      <ElAlert
        v-if="impact"
        :closable="false"
        show-icon
        title="移动影响"
        type="warning"
      >
        本次调整将移动 {{ impact.orgCount }} 个组织，并影响
        {{ impact.userCount }} 名用户的
        {{ impact.relationCount }} 条组织归属关系。调整后的路径为：
        {{ impact.newFullPath }}
      </ElAlert>
    </div>
  </Drawer>
</template>
