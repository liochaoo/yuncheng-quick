<script lang="ts" setup>
defineProps<{
  description?: string;
  title: string;
}>();
</script>

<template>
  <header class="mb-3 border-b border-border pb-3">
    <div class="flex items-center gap-2">
      <span class="h-4 w-1 rounded-full bg-primary"></span>
      <h3 class="text-base font-semibold text-foreground">{{ title }}</h3>
    </div>
    <p v-if="description" class="mt-1 pl-3 text-sm text-muted-foreground">
      {{ description }}
    </p>
  </header>
</template>
