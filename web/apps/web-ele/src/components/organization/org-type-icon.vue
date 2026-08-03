<script lang="ts" setup>
import type { OrgType } from '#/api/common/organization';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

const props = defineProps<{ type: OrgType }>();

const icon = computed(
  () =>
    ({
      DEPARTMENT: 'lucide:network',
      GROUP: 'lucide:users-round',
      ORGANIZATION: 'lucide:building-2',
    })[props.type],
);
</script>

<template>
  <IconifyIcon :icon="icon" class="size-4 shrink-0" />
</template>
