import { JobSummary } from "./models/job-summary.model";

export class JobData {
  static JOB_SUMMARIES: JobSummary[] = [
    {
      jobId: 1,
      businessName: 'Elite CSP',
      icon: '⚙️',
      type: 'full_time',
      category: 'career',
      title: {
        fr: 'Consultant IBM Maximo',
        en: 'IBM Maximo Consultant'
      },
      location: 'Québec, QC (Hybrid)',
      summary: {
        fr: 'Déployez et configurez les solutions IBM Maximo / MAS pour nos clients dans des secteurs variés.',
        en: 'Deploy and configure IBM Maximo / MAS solutions for clients across a range of industries.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 2,
      businessName: 'Elite CSP',
      icon: '☕',
      type: 'full_time',
      category: 'career',
      title: {
        fr: 'Développeur Java',
        en: 'Java Developer'
      },
      location: 'Remote, Canada',
      summary: {
        fr: 'Développez des extensions et intégrations personnalisées pour les plateformes IBM Maximo et MAS.',
        en: 'Build custom extensions and integrations for IBM Maximo and MAS platforms.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 3,
      businessName: 'Elite CSP',
      icon: '📊',
      type: 'full_time',
      category: 'career',
      title: {
        fr: 'Analyste fonctionnel',
        en: 'Functional Analyst'
      },
      location: 'Québec, QC (Hybrid)',
      summary: {
        fr: 'Analysez les besoins métier et traduisez-les en solutions EAM adaptées aux processus clients.',
        en: 'Analyze business requirements and translate them into tailored EAM solutions aligned with client processes.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 4,
      businessName: 'Elite CSP',
      icon: '🔧',
      type: 'full_time',
      category: 'career',
      title: {
        fr: 'Ingénieur DevOps',
        en: 'DevOps Engineer'
      },
      location: 'Remote, Canada',
      summary: {
        fr: 'Automatisez et gérez les pipelines CI/CD pour les déploiements IBM Maximo et les environnements MAS.',
        en: 'Automate and manage CI/CD pipelines for IBM Maximo deployments and MAS cloud environments.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 5,
      businessName: 'Elite CSP',
      icon: '🛜',
      type: 'contract',
      category: 'opportunity',
      title: {
        fr: 'Expert en Télécom',
        en: 'Telecom Expert'
      },
      location: 'Québec, QC (Hybrid)',
      summary: {
        fr: 'Un expert en télécommunication conçoit et optimise les réseaux pour assurer des communications rapides, sécurisées et fiables.',
        en: 'A telecommunications expert designs and optimizes networks to ensure fast, secure, and reliable communications.'
      },
      postedDate: new Date('2026-03-01'),
      expirationDate: new Date('2026-05-30')
    }
  ];
}