export interface HelpHierarchy {
  categories: HelpCategory[];
}

export interface HelpCategory {
  id: string;
  name: string;
  order: number;
  subcategories: HelpSubcategory[];
  articles?: HelpArticle[];
}

export interface HelpSubcategory {
  id: string;
  name: string;
  order: number;
  categoryId?: string;
  articles: HelpArticle[];
}

export interface HelpArticle {
  id: string;
  title: string;
  slug: string;
  order: number;
  categoryId?: string;
  subcategoryId?: string;
  lastUpdate?: string;
}

export interface HierarchyApiResponse {
  categories: CategoryApiDto[];
}

export interface CategoryApiDto {
  id: string;
  name: string;
  order: number;
  subcategories: SubcategoryApiDto[];
  articles?: ArticleApiDto[];
}

export interface SubcategoryApiDto {
  id: string;
  name: string;
  order: number;
  articles: ArticleApiDto[];
}

export interface ArticleApiDto {
  id: string;
  title: string;
  slug: string;
  order: number;
}

export interface AccordionState {
  expandedCategoryId: string | null;
  expandedSubcategoryMap: Map<string, string | null>;
}
