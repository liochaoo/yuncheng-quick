<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type {
  DictionaryListItem,
  DictionaryOptionListItem,
} from '#/api/system/dictionary';
import type { RowAction } from '#/components/table/row-actions.types';
import type { BusinessFormMode } from '#/types/business-form';

import { computed, nextTick, ref, watch } from 'vue';

import { useAccess } from '@vben/access';
import { ColPage, useVbenDrawer } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import { ElButton } from 'element-plus';

import { refreshAfterRowsRemoved, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  changeDictionaryOptionStatusApi,
  deleteDictionaryApi,
  deleteDictionaryOptionApi,
  pageDictionariesApi,
  pageDictionaryOptionsApi,
} from '#/api/system/dictionary';
import EnabledStatus from '#/components/display/enabled-status.vue';
import RowActions from '#/components/table/row-actions.vue';
import TableToolbarActions from '#/components/table/table-toolbar-actions.vue';
import { useConfirmAction } from '#/hooks/use-confirm-action';
import { actionColumn, centerColumn, textColumn } from '#/utils/table-columns';

import DictionaryDetailDrawer from './dictionary-detail-drawer.vue';
import DictionaryFormDrawer from './dictionary-form-drawer.vue';
import DictionaryOptionDetailDrawer from './dictionary-option-detail-drawer.vue';
import DictionaryOptionFormDrawer from './dictionary-option-form-drawer.vue';
import { DICTIONARY_PERMISSION_CODES } from './permission-codes';

interface DictionarySearchValues {
  keyword?: string;
}

interface DictionaryOptionSearchValues {
  enabled?: boolean;
  keyword?: string;
}

const { hasAccessByCodes } = useAccess();
const canAccess = {
  add: hasAccessByCodes([DICTIONARY_PERMISSION_CODES.ADD]),
  changeStatus: hasAccessByCodes([DICTIONARY_PERMISSION_CODES.CHANGE_STATUS]),
  delete: hasAccessByCodes([DICTIONARY_PERMISSION_CODES.DELETE]),
  edit: hasAccessByCodes([DICTIONARY_PERMISSION_CODES.EDIT]),
};

const activeDictionary = ref<DictionaryListItem>();
const activeDictionaryId = computed(() => activeDictionary.value?.id);
const { runConfirmAction } = useConfirmAction();
let optionQueryActive = false;
let optionQueryPending = false;

const [DictionaryFormDrawerView, dictionaryFormDrawerApi] = useVbenDrawer({
  connectedComponent: DictionaryFormDrawer,
  destroyOnClose: true,
});
const [DictionaryDetailDrawerView, dictionaryDetailDrawerApi] = useVbenDrawer({
  connectedComponent: DictionaryDetailDrawer,
  destroyOnClose: true,
});
const [OptionFormDrawerView, optionFormDrawerApi] = useVbenDrawer({
  connectedComponent: DictionaryOptionFormDrawer,
  destroyOnClose: true,
});
const [OptionDetailDrawerView, optionDetailDrawerApi] = useVbenDrawer({
  connectedComponent: DictionaryOptionDetailDrawer,
  destroyOnClose: true,
});

const dictionaryFormOptions: VbenFormProps = {
  actionLayout: 'inline',
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '字典编码/名称',
      },
      fieldName: 'keyword',
      label: '关键字',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-[minmax(0,1fr)_auto]',
};

const dictionaryColumns = [
  centerColumn<DictionaryListItem>({ type: 'radio', width: 44 }),
  textColumn<DictionaryListItem>({
    field: 'dictionaryName',
    minWidth: 130,
    slots: { default: 'dictionaryName' },
    title: '字典名称',
  }),
  textColumn<DictionaryListItem>({
    field: 'dictionaryCode',
    minWidth: 140,
    title: '字典编码',
  }),
  centerColumn<DictionaryListItem>({
    field: 'sortOrder',
    title: '排序号',
    width: 76,
  }),
  actionColumn<DictionaryListItem>({ width: 126 }),
];

const [DictionaryGrid, dictionaryGridApi] = useVbenVxeGrid({
  formOptions: dictionaryFormOptions,
  gridOptions: {
    columns: dictionaryColumns,
    height: 'auto',
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: DictionarySearchValues,
        ) => {
          const result = await pageDictionariesApi({
            keyword: values.keyword?.trim() || undefined,
            page: page.currentPage,
            pageSize: page.pageSize,
          });
          const next =
            result.items.find((item) => item.id === activeDictionaryId.value) ??
            result.items[0];
          activeDictionary.value = next;
          if (next) {
            void nextTick(() => dictionaryGridApi.grid.setRadioRow(next));
          }
          return result;
        },
      },
    },
    radioConfig: { highlight: true, strict: true },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<DictionaryListItem>,
});

const optionFormOptions: VbenFormProps = {
  actionLayout: 'inline',
  collapsed: false,
  schema: [
    {
      component: 'Input',
      componentProps: {
        placeholder: '选项值/标签',
      },
      fieldName: 'keyword',
      label: '关键字',
    },
  ],
  showCollapseButton: false,
  submitOnEnter: true,
  wrapperClass: 'grid-cols-[minmax(0,1fr)_auto]',
};

const optionColumns = [
  textColumn<DictionaryOptionListItem>({
    field: 'optionLabel',
    minWidth: 130,
    slots: { default: 'optionLabel' },
    title: '选项标签',
  }),
  textColumn<DictionaryOptionListItem>({
    field: 'optionValue',
    minWidth: 130,
    title: '选项值',
  }),
  centerColumn<DictionaryOptionListItem>({
    field: 'enabled',
    slots: { default: 'enabled' },
    title: '状态',
    width: 78,
  }),
  centerColumn<DictionaryOptionListItem>({
    field: 'sortOrder',
    title: '排序号',
    width: 76,
  }),
  actionColumn<DictionaryOptionListItem>({ width: 126 }),
];

function emptyOptionPage(page: number, pageSize: number) {
  return {
    items: [],
    page,
    pageSize,
    total: 0,
  };
}

async function queryActiveOptions() {
  if (optionQueryActive) {
    optionQueryPending = true;
    return;
  }
  await optionGridApi.query();
}

const [OptionGrid, optionGridApi] = useVbenVxeGrid({
  formOptions: optionFormOptions,
  gridOptions: {
    columns: optionColumns,
    height: 'auto',
    pagerConfig: { enabled: true, pageSize: 20 },
    proxyConfig: {
      ajax: {
        query: async (
          { page }: { page: { currentPage: number; pageSize: number } },
          values: DictionaryOptionSearchValues,
        ) => {
          const dictionaryId = activeDictionaryId.value;
          optionQueryActive = true;
          try {
            if (!dictionaryId) {
              return emptyOptionPage(page.currentPage, page.pageSize);
            }
            const result = await pageDictionaryOptionsApi(dictionaryId, {
              enabled: values.enabled,
              keyword: values.keyword?.trim() || undefined,
              page: page.currentPage,
              pageSize: page.pageSize,
            });
            return dictionaryId === activeDictionaryId.value
              ? result
              : emptyOptionPage(page.currentPage, page.pageSize);
          } finally {
            // VXE 会在当前 query Promise 完成后才清除 loading，延后一轮再补发最新查询。
            globalThis.setTimeout(() => {
              optionQueryActive = false;
              if (optionQueryPending) {
                optionQueryPending = false;
                void queryActiveOptions();
              }
            }, 0);
          }
        },
      },
    },
    rowConfig: { keyField: 'id' },
    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  } as VxeTableGridOptions<DictionaryOptionListItem>,
});

watch(activeDictionaryId, () => void queryActiveOptions());

function selectDictionary(event: { row: DictionaryListItem }) {
  activeDictionary.value = event.row;
}

function openDictionaryForm(mode: BusinessFormMode, row?: DictionaryListItem) {
  dictionaryFormDrawerApi.setData({ id: row?.id, mode }).open();
}

function openDictionaryDetail(row: DictionaryListItem) {
  dictionaryDetailDrawerApi.setData({ id: row.id }).open();
}

function openOptionForm(
  mode: BusinessFormMode,
  row?: DictionaryOptionListItem,
) {
  const dictionary = activeDictionary.value;
  if (!dictionary) return;
  optionFormDrawerApi
    .setData({
      dictionaryId: dictionary.id,
      dictionaryName: dictionary.dictionaryName,
      id: row?.id,
      mode,
    })
    .open();
}

function openOptionDetail(row: DictionaryOptionListItem) {
  const dictionaryId = activeDictionaryId.value;
  if (!dictionaryId) return;
  optionDetailDrawerApi.setData({ dictionaryId, id: row.id }).open();
}

function removeDictionary(row: DictionaryListItem) {
  void runConfirmAction({
    action: () => deleteDictionaryApi(row.id),
    confirmButtonText: '删除',
    message: `确认删除数据字典【${row.dictionaryName}】？`,
    onSuccess: () => dictionaryGridApi.query(),
    successMessage: '删除成功',
    title: '删除数据字典',
  });
}

function removeOption(row: DictionaryOptionListItem) {
  const dictionaryId = activeDictionaryId.value;
  if (!dictionaryId) return;
  void runConfirmAction({
    action: () => deleteDictionaryOptionApi(dictionaryId, row.id),
    confirmButtonText: '删除',
    message:
      `确认删除选项【${row.optionLabel}】？` +
      '选项值可能已被业务数据引用，通常建议优先停用。',
    onSuccess: async () => {
      await refreshAfterRowsRemoved(
        { grid: optionGridApi.grid, query: queryActiveOptions },
        1,
      );
    },
    successMessage: '删除成功',
    title: '删除字典选项',
  });
}

function changeOptionStatus(row: DictionaryOptionListItem, enabled: boolean) {
  const dictionaryId = activeDictionaryId.value;
  if (!dictionaryId || enabled === row.enabled) return;
  const actionText = enabled ? '启用' : '停用';
  void runConfirmAction({
    action: () =>
      changeDictionaryOptionStatusApi(dictionaryId, row.id, enabled),
    confirmButtonText: actionText,
    message: `确认${actionText}选项【${row.optionLabel}】？`,
    onSuccess: queryActiveOptions,
    successMessage: `${actionText}成功`,
    title: `${actionText}字典选项`,
  });
}

function dictionaryRowActions(row: DictionaryListItem): RowAction[] {
  return [
    {
      label: '编辑',
      onClick: () => openDictionaryForm('edit', row),
      visible: canAccess.edit,
    },
    {
      label: '删除',
      onClick: () => removeDictionary(row),
      type: 'danger',
      visible: canAccess.delete,
    },
  ];
}

function optionRowActions(row: DictionaryOptionListItem): RowAction[] {
  return [
    {
      label: '编辑',
      onClick: () => openOptionForm('edit', row),
      visible: canAccess.edit,
    },
    {
      label: '删除',
      onClick: () => removeOption(row),
      type: 'danger',
      visible: canAccess.delete,
    },
  ];
}
</script>

<template>
  <ColPage
    auto-content-height
    :left-min-width="30"
    :left-width="42"
    :right-min-width="42"
    :right-width="58"
    split-handle
    split-line
  >
    <template #left>
      <div class="h-full min-h-0 pr-2">
        <DictionaryGrid table-title="字典列表" @radio-change="selectDictionary">
          <template #toolbar-tools>
            <TableToolbarActions>
              <ElButton
                v-if="canAccess.add"
                type="primary"
                @click="openDictionaryForm('create')"
              >
                <Plus class="mr-1 size-4" />
                新增字典
              </ElButton>
            </TableToolbarActions>
          </template>
          <template #dictionaryName="{ row }">
            <ElButton link type="primary" @click="openDictionaryDetail(row)">
              {{ row.dictionaryName }}
            </ElButton>
          </template>
          <template #action="{ row }">
            <RowActions :actions="dictionaryRowActions(row)" />
          </template>
        </DictionaryGrid>
      </div>
    </template>

    <DictionaryFormDrawerView @success="dictionaryGridApi.query()" />
    <DictionaryDetailDrawerView />
    <OptionFormDrawerView @success="queryActiveOptions" />
    <OptionDetailDrawerView />

    <div class="h-full min-h-0 pl-2">
      <OptionGrid
        :table-title="
          activeDictionary
            ? `${activeDictionary.dictionaryName} · 选项列表`
            : '请选择数据字典'
        "
      >
        <template #toolbar-tools>
          <TableToolbarActions>
            <ElButton
              v-if="canAccess.add"
              :disabled="!activeDictionary"
              type="primary"
              @click="openOptionForm('create')"
            >
              <Plus class="mr-1 size-4" />
              新增选项
            </ElButton>
          </TableToolbarActions>
        </template>
        <template #optionLabel="{ row }">
          <ElButton link type="primary" @click="openOptionDetail(row)">
            {{ row.optionLabel }}
          </ElButton>
        </template>
        <template #enabled="{ row }">
          <EnabledStatus
            :editable="canAccess.changeStatus"
            :model-value="row.enabled"
            @change="changeOptionStatus(row, $event)"
          />
        </template>
        <template #action="{ row }">
          <RowActions :actions="optionRowActions(row)" />
        </template>
      </OptionGrid>
    </div>
  </ColPage>
</template>
