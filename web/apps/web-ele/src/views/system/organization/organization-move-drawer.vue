<script lang="ts" setup>
import type {
  OrganizationNodeMoveImpact,
  OrganizationNodeOption,
  OrganizationNodeType,
} from '#/api/system/organization';

import { computed, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElMessage,
} from 'element-plus';

import {
  getOrganizationNodeMoveImpactApi,
  moveOrganizationNodeApi,
} from '#/api/system/organization';
import { OrganizationNodeSelect } from '#/components/organization';
import { useLatestRequest } from '#/hooks/use-latest-request';
import { BUSINESS_FORM_DRAWER_WIDTH } from '#/types/business-form';

import { allowedParentTypes } from './organization-node-type-rules';

interface DrawerOpenData {
  node: OrganizationNodeOption;
}

const emit = defineEmits<{
  success: [];
}>();

const currentNode = ref<OrganizationNodeOption>();
const impact = ref<OrganizationNodeMoveImpact>();
const parentId = ref<string>();
const originalParentId = ref<string>();
const impactRequest = useLatestRequest();
const impactLoading = impactRequest.loading;
const title = computed(() =>
  currentNode.value
    ? `移动组织节点：${currentNode.value.nodeName}`
    : '移动组织节点',
);
const selectableParentTypes = computed<OrganizationNodeType[]>(() =>
  currentNode.value ? allowedParentTypes(currentNode.value.nodeType) : [],
);
const canMoveToRoot = computed(
  () => currentNode.value?.nodeType === 'ORGANIZATION',
);

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const node = currentNode.value;
    if (!node) return;
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
      await moveOrganizationNodeApi(node.id, parentId.value);
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
      currentNode.value = undefined;
      parentId.value = undefined;
      originalParentId.value = undefined;
      return;
    }
    const data = drawerApi.getData<DrawerOpenData>();
    currentNode.value = data.node;
    parentId.value = data.node.parentId ?? undefined;
    originalParentId.value = data.node.parentId ?? undefined;
    drawerApi.setState({
      confirmText: '确认移动',
      showCancelButton: true,
    });
  },
});

async function loadImpact() {
  const node = currentNode.value;
  if (!node || parentId.value === originalParentId.value) {
    impact.value = undefined;
    return undefined;
  }
  const result = await impactRequest.execute(() =>
    getOrganizationNodeMoveImpactApi(node.id, parentId.value),
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
    <div v-if="currentNode" class="space-y-5 px-4">
      <ElDescriptions border :column="1">
        <ElDescriptionsItem label="当前节点">
          {{ currentNode.nodeName }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="当前路径">
          {{ currentNode.fullPath }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <div>
        <div class="mb-2 text-sm font-medium">新的上级节点</div>
        <OrganizationNodeSelect
          v-model="parentId"
          :clearable="canMoveToRoot"
          :exclude-subtree-root-id="currentNode.id"
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
        本次调整将移动 {{ impact.nodeCount }} 个组织节点。调整后的路径为：
        {{ impact.newFullPath }}
      </ElAlert>
    </div>
  </Drawer>
</template>
