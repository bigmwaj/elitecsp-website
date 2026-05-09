import { JobSummary } from './models/job-summary.model';

export class JobData {
  static JOB_SUMMARIES: JobSummary[] = [
    {
      jobId: 1,
      slug: 'consultant-ibm-maximo',
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
        fr: 'Déployez et configurez les solutions IBM Maximo / MAS pour accompagner les clients dans leurs projets de transformation des actifs.',
        en: 'Deploy and configure IBM Maximo / MAS solutions to support clients through enterprise asset transformation projects.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 2,
      slug: 'developpeur-java',
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
        fr: 'Concevez des intégrations et composants sur mesure qui enrichissent les environnements IBM Maximo et MAS de nos clients.',
        en: 'Design integrations and custom components that extend our clients’ IBM Maximo and MAS environments.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 3,
      slug: 'analyste-fonctionnel',
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
        fr: 'Analysez les besoins métier et transformez-les en solutions EAM concrètes, claires et alignées sur les opérations clients.',
        en: 'Analyze business needs and translate them into clear, practical EAM solutions aligned with client operations.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 4,
      slug: 'ingenieur-devops',
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
        fr: 'Automatisez les déploiements, sécurisez les environnements et améliorez la fiabilité des plateformes Maximo et MAS.',
        en: 'Automate deployments, secure environments, and improve the reliability of Maximo and MAS platforms.'
      },
      postedDate: new Date('2026-03-01')
    },
    {
      jobId: 5,
      slug: 'expert-telecom',
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
        fr: 'Contribuez à la conception et à l’optimisation de réseaux critiques dans le cadre d’un mandat stratégique de longue durée.',
        en: 'Contribute to the design and optimization of critical networks as part of a long-term strategic mandate.'
      },
      postedDate: new Date('2026-03-01'),
      expirationDate: new Date('2026-05-30')
    }
  ];
}
