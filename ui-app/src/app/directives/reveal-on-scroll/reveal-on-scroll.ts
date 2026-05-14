import { Directive, ElementRef, OnDestroy, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Directive({
  selector: '[appRevealOnScroll]',
  standalone: true,
  host: {
    class: 'reveal-on-scroll'
  }
})
export class RevealOnScrollDirective implements OnInit, OnDestroy {
  private readonly elementRef = inject(ElementRef<HTMLElement>);
  private readonly platformId = inject(PLATFORM_ID);
  private observer?: IntersectionObserver;

  ngOnInit(): void {
    const element = this.elementRef.nativeElement;

    if (!isPlatformBrowser(this.platformId) || typeof IntersectionObserver === 'undefined') {
      element.classList.add('is-visible');
      return;
    }

    this.observer = new IntersectionObserver(
      entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            element.classList.add('is-visible');
            this.observer?.unobserve(element);
          }
        });
      },
      {
        threshold: 0.2,
        rootMargin: '0px 0px -8% 0px'
      }
    );

    this.observer.observe(element);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
