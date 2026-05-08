import { Component, inject, OnInit, signal, ElementRef, ViewChild } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { JobCardComponent } from '../../components/job-card/job-card';
import { JobService } from '../../services/job.service';
import { ApplicationService } from '../../services/application.service';
import { JobSummary } from '../../models/job-summary.model';

const ALLOWED_EXTENSIONS = ['.pdf', '.docx'];

@Component({
  selector: 'app-careers',
  standalone: true,
  imports: [HeroComponent, JobCardComponent, ReactiveFormsModule, TranslatePipe],
  templateUrl: './careers.html',
  styleUrl: './careers.scss'
})
export class CareersComponent implements OnInit {
  @ViewChild('applySection') applySection!: ElementRef<HTMLElement>;

  private titleService = inject(Title);
  private meta = inject(Meta);
  private fb = inject(FormBuilder);
  private translate = inject(TranslateService);
  readonly jobService = inject(JobService);
  private applicationService = inject(ApplicationService);

  submitted = signal(false);
  submitting = signal(false);
  selectedFile = signal<File | null>(null);
  fileError = signal<string | null>(null);
  jobSummaries = signal<JobSummary[]>([]);

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.pattern(/^\+?[0-9\s\-()]{7,}$/), Validators.maxLength(20)]],
    city: ['', Validators.required],
    jobId: ['', Validators.required],
    coverLetter: ['', [Validators.required, Validators.minLength(50)]]
  });

  ngOnInit(): void {
    this.translate.get('HERO.CAREERS.TITLE').subscribe(title => {
      this.titleService.setTitle(`${title} — Elite CSP`);
    });
    this.translate.get('HERO.CAREERS.SUBTITLE').subscribe(desc => {
      this.meta.updateTag({ name: 'description', content: desc });
    });
    this.jobService.loadJobs().subscribe({
      next: summaries => this.jobSummaries.set(summaries),
      error: err => console.error('Failed to load job summaries:', err)
    });
  }

  get f() { return this.form.controls; }

  get formErrors(): string[] {
    const fieldErrorKeys: Record<string, string> = {
      fullName: 'CAREERS_PAGE.FORM.FULL_NAME_ERROR',
      email: 'CAREERS_PAGE.FORM.EMAIL_ERROR',
      phone: 'CAREERS_PAGE.FORM.PHONE_ERROR',
      city: 'CAREERS_PAGE.FORM.CITY_ERROR',
      jobId: 'CAREERS_PAGE.FORM.JOB_ERROR',
      coverLetter: 'CAREERS_PAGE.FORM.COVER_LETTER_ERROR'
    };
    const errors: string[] = [];
    for (const key of Object.keys(fieldErrorKeys)) {
      if (this.form.get(key)?.invalid) {
        errors.push(fieldErrorKeys[key]);
      }
    }
    const fe = this.fileError();
    if (fe) {
      errors.push(`CAREERS_PAGE.FORM.${fe}`);
    }
    return errors;
  }

  onApply(jobId: string): void {
    this.form.patchValue({ jobId: String(jobId) });
    setTimeout(() => {
      this.applySection?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 50);
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile.set(file);
    if (file) {
      const isAllowed = ALLOWED_EXTENSIONS.some(ext => file.name.toLowerCase().endsWith(ext));
      this.fileError.set(isAllowed ? null : 'CV_TYPE_ERROR');
    } else {
      this.fileError.set(null);
    }
  }

  onSubmit(): void {
    console.log('Form submission initiated');
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

    if (this.form.invalid || this.fileError()) {
      return;
    }

    this.submitting.set(true);
    const v = this.form.value;

    this.readFileAsBase64(file!).then(base64Content => {
      this.applicationService.submit({
        name: v.fullName!,
        email: v.email!,
        city: v.city!,
        phone: v.phone!,
        subject: this.jobService.getJobTranslatedTitle(Number(v.jobId)),
        message: v.coverLetter!,
        attachment: base64Content,
        attachmentFileName: file!.name,
        type: 'JOB_APPLICATION'
      }).subscribe({
        next: () => {
          this.submitting.set(false);
          this.submitted.set(true);
          this.form.reset();
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
  }

  private readFileAsBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => {
        const result = reader.result as string;
        // Strip the data URI prefix (e.g. "data:application/pdf;base64,") if present
        const base64 = result.includes(',') ? result.split(',')[1] : result;
        resolve(base64);
      };
      reader.onerror = () => reject(new Error('Failed to read file: ' + reader.error?.message));
      reader.readAsDataURL(file);
    });
  }
}
