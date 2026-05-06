import { Component, inject, OnInit, signal } from '@angular/core';
import { Title, Meta } from '@angular/platform-browser';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { HeroComponent } from '../../components/hero/hero';
import { ContactService } from '../../services/contact.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [HeroComponent, ReactiveFormsModule, TranslatePipe],
  templateUrl: './contact.html',
  styleUrl: './contact.scss'
})
export class ContactComponent implements OnInit {
  private title = inject(Title);
  private meta = inject(Meta);
  private fb = inject(FormBuilder);
  private contactService = inject(ContactService);

  submitted = signal(false);
  submitting = signal(false);

  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    company: [''],
    subject: ['', Validators.required],
    message: ['', [Validators.required, Validators.minLength(20)]]
  });

  ngOnInit() {
    this.title.setTitle('Contact — Elite CSP');
    this.meta.updateTag({ name: 'description', content: 'Contactez Elite CSP. Parlez à nos experts en conseil IBM Maximo pour une consultation gratuite.' });
  }

  get f() { return this.form.controls; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const v = this.form.value;
    this.contactService.submit({
      name: v.name!,
      email: v.email!,
      company: v.company ?? '',
      subject: v.subject!,
      message: v.message!,
      type: "CONTACT"
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.submitted.set(true);
        this.form.reset();
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

