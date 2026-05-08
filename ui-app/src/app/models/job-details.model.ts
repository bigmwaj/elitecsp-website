import { JobSummary } from "./job-summary.model";

export interface JobDetails extends JobSummary{
    description: string;
    responsibilities: string[];
    requirements: string[];
    benefits: string[];
}