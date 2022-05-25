/* eslint-disable @typescript-eslint/no-explicit-any */
export interface FileUploadItem {
  id: string;
  file: File;
  selected?: boolean;
  progress?: number;
  uploadStatus?: string;
  name: string;
  title?: { [key: string]: string };
  description?: { [key: string]: string };
  keywords?: string[];
  author?: string;
  reference?: string;
  expirationDate?: string;
  securityRanking?: string;
  status?: string;
  isPivot?: boolean;
  isTranslation?: boolean;
  translationOf?: string;
  lang?: string;
  nodeRef?: string;
  dynAttr1?: any;
  dynAttr2?: any;
  dynAttr3?: any;
  dynAttr4?: any;
  dynAttr5?: any;
  dynAttr6?: any;
  dynAttr7?: any;
  dynAttr8?: any;
  dynAttr9?: any;
  dynAttr10?: any;
  dynAttr11?: any;
  dynAttr12?: any;
  dynAttr13?: any;
  dynAttr14?: any;
  dynAttr15?: any;
  dynAttr16?: any;
  dynAttr17?: any;
  dynAttr18?: any;
  dynAttr19?: any;
  dynAttr20?: any;

  [key: string]: any;
}
