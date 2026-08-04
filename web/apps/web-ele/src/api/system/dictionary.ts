import type { AvailabilityResult } from '#/api/system/types';
import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export interface DictionaryListItem {
  createdAt: string;
  description?: string;
  dictionaryCode: string;
  dictionaryName: string;
  id: string;
  sortOrder: number;
  updatedAt: string;
}

export interface DictionaryDetail extends DictionaryListItem {
  createdBy: string;
  updatedBy: string;
}

export interface DictionaryPageParams {
  keyword?: string;
  page: number;
  pageSize: number;
}

export interface DictionaryCreateRequest {
  description?: string;
  dictionaryCode: string;
  dictionaryName: string;
  sortOrder: number;
}

export interface DictionaryUpdateRequest {
  description?: string;
  dictionaryName: string;
  sortOrder: number;
}

export interface DictionaryOptionListItem {
  createdAt: string;
  description?: string;
  dictionaryId: string;
  enabled: boolean;
  id: string;
  optionLabel: string;
  optionValue: string;
  sortOrder: number;
  updatedAt: string;
}

export interface DictionaryOptionDetail extends DictionaryOptionListItem {
  createdBy: string;
  updatedBy: string;
}

export interface DictionaryOptionPageParams {
  enabled?: boolean;
  keyword?: string;
  page: number;
  pageSize: number;
}

export interface DictionaryOptionCreateRequest {
  description?: string;
  optionLabel: string;
  optionValue: string;
  sortOrder: number;
}

export interface DictionaryOptionUpdateRequest {
  description?: string;
  optionLabel: string;
  sortOrder: number;
}

export async function pageDictionariesApi(params: DictionaryPageParams) {
  return requestClient.get<PageResult<DictionaryListItem>>(
    '/system/dictionaries',
    { params },
  );
}

export async function getDictionaryDetailApi(id: string) {
  return requestClient.get<DictionaryDetail>(`/system/dictionaries/${id}`);
}

export async function createDictionaryApi(data: DictionaryCreateRequest) {
  return requestClient.post<string>('/system/dictionaries', data);
}

export async function updateDictionaryApi(
  id: string,
  data: DictionaryUpdateRequest,
) {
  return requestClient.put<null>(`/system/dictionaries/${id}`, data);
}

export async function checkDictionaryCodeApi(value: string) {
  return requestClient.post<AvailabilityResult>(
    '/system/dictionaries/uniqueness-check',
    { value },
  );
}

export async function deleteDictionaryApi(id: string) {
  return requestClient.delete<null>(`/system/dictionaries/${id}`);
}

export async function pageDictionaryOptionsApi(
  dictionaryId: string,
  params: DictionaryOptionPageParams,
) {
  return requestClient.get<PageResult<DictionaryOptionListItem>>(
    `/system/dictionaries/${dictionaryId}/options`,
    { params },
  );
}

export async function getDictionaryOptionDetailApi(
  dictionaryId: string,
  optionId: string,
) {
  return requestClient.get<DictionaryOptionDetail>(
    `/system/dictionaries/${dictionaryId}/options/${optionId}`,
  );
}

export async function createDictionaryOptionApi(
  dictionaryId: string,
  data: DictionaryOptionCreateRequest,
) {
  return requestClient.post<string>(
    `/system/dictionaries/${dictionaryId}/options`,
    data,
  );
}

export async function updateDictionaryOptionApi(
  dictionaryId: string,
  optionId: string,
  data: DictionaryOptionUpdateRequest,
) {
  return requestClient.put<null>(
    `/system/dictionaries/${dictionaryId}/options/${optionId}`,
    data,
  );
}

export async function checkDictionaryOptionValueApi(
  dictionaryId: string,
  value: string,
) {
  return requestClient.post<AvailabilityResult>(
    `/system/dictionaries/${dictionaryId}/options/uniqueness-check`,
    { value },
  );
}

export async function changeDictionaryOptionStatusApi(
  dictionaryId: string,
  optionId: string,
  enabled: boolean,
) {
  return requestClient.put<null>(
    `/system/dictionaries/${dictionaryId}/options/${optionId}/enabled`,
    { enabled },
  );
}

export async function deleteDictionaryOptionApi(
  dictionaryId: string,
  optionId: string,
) {
  return requestClient.delete<null>(
    `/system/dictionaries/${dictionaryId}/options/${optionId}`,
  );
}
