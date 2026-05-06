import { Component, inject, OnInit, signal, ElementRef, ViewChild } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { JobCardComponent } from '../../components/job-card/job-card';
import { JobService } from '../../services/job.service';
import { ApplicationService } from '../../services/application.service';

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

  form = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
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
  }

  get f() { return this.form.controls; }

  onApply(jobId: number): void {
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
      if (!file.name.toLowerCase().endsWith('.pdf')) {
        this.fileError.set('CV_TYPE_ERROR');
      } else {
        this.fileError.set(null);
      }
    } else {
      this.fileError.set(null);
    }
  }

  onSubmit(): void {
    this.form.markAllAsTouched();

    if (!this.selectedFile()) {
      this.fileError.set('CV_ERROR');
    } else if (!this.selectedFile()!.name.toLowerCase().endsWith('.pdf')) {
      this.fileError.set('CV_TYPE_ERROR');
    }

    if (this.form.invalid || this.fileError()) {
      return;
    }

    this.submitting.set(true);
    const v = this.form.value;
    this.applicationService.submit({
      name: v.fullName!,
      email: v.email!,
      city: v.city!,
      subject: String(v.jobId),
      message: v.coverLetter!,
      cvFileName: this.selectedFile()?.name,
      type: "JOB_APPLICATION"
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
  }

  resetForm(): void {
    this.submitted.set(false);
  }
}
