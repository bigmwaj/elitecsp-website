export interface JobSummary {
  sortIndex?: number;
  jobId: number;
  slug: string;
  icon: string;
  type: string;
  category: string;
  title: {
    fr: string;
    en: string;
  };
  location: string;
  summary: {
    fr: string;
    en: string;
  };
  postedDate: Date | null;
  expirationDate?: Date | null;
}
