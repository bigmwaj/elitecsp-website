export type ContactType = "CONTACT" | "JOB_APPLICATION";

export interface BaseContactPayload {
  name: string;
  email: string;
  subject?: string;
  message: string;
  type: ContactType;
}

export interface ContactPayload extends BaseContactPayload {
  company: string;
}

export interface ApplicationPayload extends BaseContactPayload {
  city: string;
  /** Base64-encoded CV file content (required by the backend). */
  attachment: string;
  /** Original filename of the CV (e.g. "resume.pdf"). */
  attachmentFileName: string;
}