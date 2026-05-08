export interface JobSummary {
  jobId: string;
  icon: string;
  type: string;
  category: string;
  title: string;
  location: string;
  summary: string;
  postedDate: Date;
  expirationDate: Date | null;
}