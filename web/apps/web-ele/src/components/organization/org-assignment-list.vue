<script lang="ts" setup>
import { computed } from 'vue';

import { ElTag } from 'element-plus';

import OrgPath from './org-path.vue';

interface OrgAssignmentItem {
  fullPath: string;
  id: string;
}

const props = withDefaults(
  defineProps<{
    emptyText?: string;
    items: OrgAssignmentItem[];
    primaryOrgId: string;
  }>(),
  {
    emptyText: '暂无归属组织',
  },
);

const displayedItems = computed(() =>
  props.items.toSorted((left, right) => {
    if (left.id === props.primaryOrgId) return -1;
    if (right.id === props.primaryOrgId) return 1;
    return left.fullPath.localeCompare(right.fullPath);
  }),
);
</script>

<template>
  <div v-if="displayedItems.length > 0" class="overflow-hidden rounded border">
    <div
      v-for="item in displayedItems"
      :key="item.id"
      class="flex items-center gap-3 border-b px-4 py-3 last:border-b-0"
    >
      <OrgPath class="min-w-0 flex-1 text-sm" :full-path="item.fullPath" />
      <ElTag
        v-if="item.id === primaryOrgId"
        class="shrink-0"
        effect="dark"
        type="primary"
      >
        主组织
      </ElTag>
    </div>
  </div>
  <div
    v-else
    class="rounded border border-dashed px-4 py-2.5 text-center text-sm text-muted-foreground"
  >
    {{ emptyText }}
  </div>
</template>
