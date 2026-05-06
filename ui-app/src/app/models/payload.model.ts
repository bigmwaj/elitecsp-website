export type ContactType = "CONTACT" | "JOB_APPLICATION";

export interface BaseContactPayload {
  name: string;
  email: string;
  subject?: string;
  message: string;
  type: ContactType;
}

export interface ContactPayload  extends BaseContactPayload {
  company: string;
}

export interface ApplicationPayload extends BaseContactPayload {
  cvFileName?: string;
  city: string;
}