export interface JobDetail {
  jobId: number;
  description?: {
    fr: string;
    en: string;
  };
  responsibilities?: {
    fr: string[];
    en: string[];
  };
  requirements?: {
    fr: string[];
    en: string[];
  };
  benefits?: {
    fr: string[];
    en: string[];
  };
}
