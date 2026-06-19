import { JobSummary } from '../models/job-summary.model';

export class JobSummaryData {
  static JOB_SUMMARIES: JobSummary[] = [
    {
      jobId: 1,
      sortIndex: 5,
      slug: 'consultant-ibm-maximo',
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
      sortIndex: 2,
      slug: 'developpeur-java',
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
      sortIndex: 3,
      slug: 'analyste-fonctionnel',
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
      sortIndex: 4,
      slug: 'ingenieur-devops',
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
      jobId: 6,
      sortIndex: 6,
      slug: 'gestionnaire-de-projet',
      icon: '📋',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Gestionnaire de projet',
        en: 'Project Manager'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Pilotez la planification, le budget et la gouvernance d’un projet de déploiement et de support IBM Maximo, en coordonnant les équipes techniques et fonctionnelles.',
        en: 'Lead the planning, budget, and governance of an IBM Maximo deployment and support project, coordinating technical and functional teams.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 7,
      sortIndex: 7,
      slug: 'architecte-maximo',
      icon: '🏗️',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Architecte Maximo',
        en: 'Maximo Architect'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Concevez l’architecture technique et fonctionnelle de la solution IBM Maximo et encadrez les choix d’intégration avec les systèmes tiers.',
        en: 'Design the technical and functional architecture of the IBM Maximo solution and guide integration choices with third-party systems.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 8,
      sortIndex: 8,
      slug: 'analystes-fonctionnels',
      icon: '📊',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Analystes fonctionnels',
        en: 'Functional Analysts'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Recueillez et documentez les besoins d’affaires, rédigez les spécifications fonctionnelles Maximo et accompagnez les utilisateurs lors des tests et de la formation.',
        en: 'Gather and document business needs, write Maximo functional specifications, and support users through testing and training.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 9,
      sortIndex: 9,
      slug: 'developpeurs-maximo',
      icon: '💻',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Développeurs Maximo',
        en: 'Maximo Developers'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Développez et configurez des solutions sur la plateforme IBM Maximo, incluant scripts d’automatisation, workflows et intégrations.',
        en: 'Develop and configure solutions on the IBM Maximo platform, including automation scripts, workflows, and integrations.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 10,
      sortIndex: 10,
      slug: 'dba',
      icon: '🗄️',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'DBA',
        en: 'Database Administrator'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Administrez et optimisez les bases de données soutenant la plateforme Maximo, en assurant performance, sécurité et continuité des opérations.',
        en: 'Administer and optimize the databases supporting the Maximo platform, ensuring performance, security, and operational continuity.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 11,
      sortIndex: 11,
      slug: 'devops-systeme',
      icon: '🖥️',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'DevOps/Système',
        en: 'DevOps/Systems'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Déployez et maintenez l’infrastructure du projet Maximo, automatisez les processus de déploiement et assurez la disponibilité des systèmes.',
        en: 'Deploy and maintain the Maximo project infrastructure, automate deployment processes, and ensure system availability.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 12,
      sortIndex: 12,
      slug: 'qa',
      icon: '✅',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'QA',
        en: 'QA'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Élaborez et exécutez les plans de test de la solution Maximo, automatisez les tests de régression et validez la qualité avant les mises en production.',
        en: 'Develop and execute test plans for the Maximo solution, automate regression testing, and validate quality before go-live.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 13,
      sortIndex: 13,
      slug: 'formateurs',
      icon: '🎓',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Formateurs',
        en: 'Trainers'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Concevez et animez les formations utilisateurs et techniques sur Maximo, et accompagnez les équipes lors du démarrage et du support post-implantation.',
        en: 'Design and deliver user and technical training on Maximo, and support teams during go-live and post-implementation support.'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 14,
      sortIndex: 14,
      slug: 'architecte-integration',
      icon: '🔌',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Architecte intégration',
        en: 'Integration Architect'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Poste optionnel, selon les besoins du projet : concevez les interfaces et flux d’intégration entre Maximo et les systèmes tiers (ERP, GIS, IoT, bus de services).',
        en: 'Optional role, based on project needs: design the interfaces and integration flows between Maximo and third-party systems (ERP, GIS, IoT, service bus).'
      },
      postedDate: new Date('2026-06-18')
    },
    {
      jobId: 15,
      sortIndex: 15,
      slug: 'expert-iam-securite',
      icon: '🔐',
      type: 'contract',
      category: 'career',
      title: {
        fr: 'Expert IAM/Sécurité',
        en: 'IAM/Security Expert'
      },
      location: 'Montréal, QC (Hybrid)',
      summary: {
        fr: 'Poste optionnel, selon les besoins du projet : définissez et mettez en œuvre la gestion des identités et des accès pour la solution Maximo.',
        en: 'Optional role, based on project needs: define and implement identity and access management for the Maximo solution.'
      },
      postedDate: new Date('2026-06-18')
    }
  ];
}
