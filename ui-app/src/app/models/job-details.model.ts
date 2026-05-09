import { JobSummary } from "./job-summary.model";


export interface JobDetails {
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