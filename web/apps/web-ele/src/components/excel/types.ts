export interface ExcelImportError {
  field: string;
  message: string;
  rowNumber: number;
}

export interface ExcelImportResult {
  errorCount: number;
  errors: ExcelImportError[];
  importedCount: number;
  success: boolean;
  totalCount: number;
}
