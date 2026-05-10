import { Component, effect, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { CurrentLangEntryPipe } from '../../pipes/current-lang-entry.pipe';
import { JobSummary } from '../../models/job-summary.model';
import { ApplicationService } from '../../services/application.service';

const ALLOWED_EXTENSIONS = ['.pdf', '.doc', '.docx'];

@Component({
  selector: 'app-application-form',
  imports: [ReactiveFormsModule, TranslatePipe, CurrentLangEntryPipe],
  templateUrl: './application-form.html',
  styleUrl: './application-form.scss'
})
export class ApplicationFormComponent {
  readonly jobs = input<JobSummary[]>([]);
  readonly selectedJobSlug = input<string | null>(null);
  readonly lockSelectedJob = input(false);

  private fb = inject(FormBuilder);
  private translate = inject(TranslateService);
  private applicationService = inject(ApplicationService);

  readonly submitted = signal(false);
  readonly submitting = signal(false);
  readonly selectedFile = signal<File | null>(null);
  readonly fileError = signal<string | null>(null);

  readonly form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-()]{7,}$/), Validators.maxLength(20)]],
    city: ['', Validators.required],
    jobSlug: ['', Validators.required],
    coverLetter: ['', [Validators.required, Validators.minLength(50)]]
  });

  constructor() {
    effect(() => {
      const locked = this.lockSelectedJob();
      const slug = this.selectedJobSlug() ?? '';
      const jobControl = this.form.controls.jobSlug;

      if (slug) {
        jobControl.patchValue(slug, { emitEvent: false });
      }

      if (locked) {
        jobControl.clearValidators();
      } else {
        jobControl.setValidators(Validators.required);
      }

      jobControl.updateValueAndValidity({ emitEvent: false });
    });
  }

  get f() {
    return this.form.controls;
  }

  currentSelectedJob(): JobSummary | null {
    const slug = this.lockSelectedJob() ? this.selectedJobSlug() : this.form.controls.jobSlug.value;
    if (!slug) {
      return null;
    }

    return this.jobs().find(job => job.slug === slug) ?? null;
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);

    if (file) {
      const isAllowed = ALLOWED_EXTENSIONS.some(ext => file.name.toLowerCase().endsWith(ext));
      this.fileError.set(isAllowed ? null : 'CV_TYPE_ERROR');
      return;
    }

    this.fileError.set(null);
  }

  onSubmit(): void {
    this.form.markAllAsTouched();

    const file = this.selectedFile();
    if (!file) {
      this.fileError.set('CV_ERROR');
    } else {
      const isAllowed = ALLOWED_EXTENSIONS.some(ext => file.name.toLowerCase().endsWith(ext));
      if (!isAllowed) {
        this.fileError.set('CV_TYPE_ERROR');
      }
    }

    const selectedJob = this.currentSelectedJob();
    if (!selectedJob && !this.lockSelectedJob()) {
      this.form.controls.jobSlug.setErrors({ required: true });
    }

    if (this.form.invalid || this.fileError() || !selectedJob) {
      return;
    }

    this.submitting.set(true);
    const value = this.form.getRawValue();

    this.readFileAsBase64(file!).then(base64Content => {
      this.applicationService
        .submit({
          name: value.fullName ?? '',
          email: value.email ?? '',
          city: value.city ?? '',
          phone: value.phone ?? '',
          subject: selectedJob.slug,
          message: value.coverLetter ?? '',
          attachment: base64Content,
          attachmentFileName: file!.name,
          type: 'JOB_APPLICATION'
        })
        .subscribe({
          next: () => {
            this.submitting.set(false);
            this.submitted.set(true);
            this.form.reset();
            this.form.patchValue({ jobSlug: this.selectedJobSlug() ?? '' }, { emitEvent: false });
            this.selectedFile.set(null);
            this.fileError.set(null);
          },
          error: () => {
            this.submitting.set(false);
          }
        });
    }).catch(() => {
      this.submitting.set(false);
    });
  }

  resetForm(): void {
    this.submitted.set(false);
    this.form.patchValue({ jobSlug: this.selectedJobSlug() ?? '' }, { emitEvent: false });
  }

  private readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        const base64 = result.includes(',') ? result.split(',')[1] : result;
        resolve(base64);
      };
      reader.onerror = () => reject(new Error(`Failed to read file: ${reader.error?.message}`));
      reader.readAsDataURL(file);
    });
  }
}
