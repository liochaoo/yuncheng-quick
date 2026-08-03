<script lang="ts" setup>
defineProps<{
  description?: string;
  title: string;
}>();

defineSlots<{
  actions?(): unknown;
}>();
</script>

<template>
  <header class="mb-3 border-b border-border pb-3">
    <div class="flex items-center justify-between gap-3">
      <div class="flex min-w-0 items-center gap-2">
        <span class="h-4 w-1 shrink-0 rounded-full bg-primary"></span>
        <h3 class="truncate text-base font-semibold text-foreground">
          {{ title }}
        </h3>
      </div>
      <div v-if="$slots.actions" class="shrink-0">
        <slot name="actions"></slot>
      </div>
    </div>
    <p v-if="description" class="mt-1 pl-3 text-sm text-muted-foreground">
      {{ description }}
    </p>
  </header>
</template>
