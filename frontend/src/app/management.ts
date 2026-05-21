import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Observable, switchMap } from 'rxjs';
import { AuthService, ProfileResponse, ProfileUpdateRequest } from './auth.service';
import { DayOffCreateRequest, DayOffPage, DayOffRequestItem, DayOffService, DayOffType } from './day-off.service';
import { Employee, EmployeeRequest, EmployeeRole, HumanResourcesService } from './human-resources.service';
import { Project, ProjectRequest, ProjectService } from './project.service';
import { MonthlyFinance, RoleSlice, StatisticsPage, StatisticsService } from './statistics.service';
import { Task, TaskPriority, TaskService, TaskStatus } from './task.service';
import { Team, TeamEmployee, TeamService } from './team.service';
import { Ticket, TicketService, TicketStatus, TicketType } from './ticket.service';
import { Language, Theme, UiSettingsService } from './ui-settings.service';

type ManagementView = 'profile' | 'team' | 'task' | 'dayOff' | 'support' | 'hr' | 'project' | 'statistics';

const TEXT = {
  hu: {
    settings: 'Beallitasok',
    language: 'Nyelv',
    theme: 'Tema',
    black: 'Fekete',
    white: 'Feher',
    profile: 'Profil',
    team: 'Csapat',
    task: 'Feladatok',
    dayOff: 'Szabadság',
    homeOffice: 'Home office',
    usedWorkdays: 'munkanap felhasználva',
    newDayOffRequest: 'Új szabadság kérelem',
    newDayOffButton: 'Új szabadság',
    dayOffType: 'Típus',
    startDate: 'Kezdet',
    endDate: 'Vége',
    sendDayOffRequest: 'Kérelem elküldése',
    dayOffRequired: 'A típus, a kezdet és a vége kitöltése kötelező.',
    developerRequests: 'Developer kérelmek',
    myDayOffRequests: 'Saját kérelmek',
    requestName: 'Név',
    days: 'Napok',
    approve: 'Jóváhagy',
    reject: 'Elutasít',
    approvedBy: 'Jóváhagyta',
    noPendingDeveloperRequests: 'Nincs függő developer kérelem.',
    noOwnDayOffRequests: 'Még nincs Day off kérelmed.',
    support: 'IT tamogatas',
    humanResources: 'Emberi erőforrás',
    project: 'Projektek',
    statistics: 'Statisztika',
    logout: 'Kijelentkezes',
    firstName: 'Keresztnév',
    lastName: 'Vezetéknév',
    email: 'Email',
    name: 'Név',
    birthDate: 'Születési idő',
    phone: 'Telefonszám',
    address: 'Lakcím',
    city: 'Város',
    postalCode: 'Irányítószám',
    houseNumber: 'Házszám',
    role: 'Szerepkör',
    companyName: 'Cég név',
    taxId: 'Adóazonosító',
    identityCardNumber: 'Személyi igazolvány szám',
    socialSecurityNumber: 'TAJ szám',
    salary: 'Fizetés',
    active: 'Aktív',
    yes: 'Igen',
    no: 'Nem',
    edit: 'Módosít',
    changePassword: 'Password change',
    editProfile: 'Profil modositasa',
    oldPassword: 'Regi jelszo',
    newPassword: 'Uj jelszo',
    repeatNewPassword: 'Uj jelszo meg egyszer',
    saving: 'Mentés...',
    saveProfile: 'Profil mentese',
    savePassword: 'Save password',
    currentTeam: 'Jelenlegi csapatod',
    productOwner: 'Product owner',
    leaveTeam: 'Kilépés a csapatból',
    noTeamYet: 'Még nem vagy csapatban. Válassz egyet a listából.',
    availableTeams: 'Elérhető csapatok',
    refresh: 'Frissítés',
    membersLabel: 'Tagok',
    membersCount: 'tag',
    join: 'Csatlakozás',
    noTeams: 'Még nincs létrehozott csapat.',
    createTeam: 'Csapat létrehozása',
    teamName: 'Csapat neve',
    teamOwner: 'Csapat vezetője',
    teamMembers: 'Csapattagok',
    noDeveloperInTeam: 'Még nincs fejlesztő a csapatban.',
    availableDevelopers: 'Szabad fejlesztők',
    searchByName: 'Keresés név alapján',
    searchByTitle: 'Keresés cím alapján',
    add: 'Felvesz',
    noAvailableDevelopers: 'Nincs csapat nélküli aktív fejlesztő.',
    deleteTeam: 'Csapat megszüntetése',
    confirmDeleteTeam: 'Biztosan megszünteted ezt a csapatot?',
    teamNameRequired: 'A csapat neve kötelező.',
    teamCreated: 'A csapat létrejött.',
    teamCreateFailed: 'Nem sikerült létrehozni a csapatot.',
    developerAdded: 'Fejlesztő felvéve a csapatba.',
    developerAddFailed: 'Nem sikerült felvenni a fejlesztőt.',
    teamDeleteFailed: 'Nem sikerült megszüntetni a csapatot.',
    year: 'Év',
    loading: 'Betöltés...',
    users: 'Felhasználók',
    yearlyResult: 'havi eredmény',
    monthlyChartLabel: 'Havi nyereség veszteség oszlopdiagram',
    result: 'Eredmény',
    projectIncome: 'Projekt bevétel',
    salaryCost: 'Bérköltség',
    balance: 'Egyenleg',
    changeEmployeePassword: 'Alkalmazott jelszó módosítása',
    employeeEmail: 'Alkalmazott email',
    saveEmployeePassword: 'Alkalmazott jelszó mentése',
    employeesTitle: 'Alkalmazottak',
    newEmployeeButton: 'Új alkalmazott',
    newEmployeeForm: 'Új alkalmazott felvétele',
    editEmployeeForm: 'Alkalmazott módosítása',
    newForm: 'Új űrlap',
    saveEmployee: 'Mentés',
    deactivate: 'Kirúgás',
    noEmployees: 'Még nincs alkalmazott.',
    employeeRequired: 'Minden kötelező alkalmazotti mezőt ki kell tölteni.',
    employeePasswordRequired: 'Új alkalmazottnál a jelszó kötelező.',
    invalidEmail: 'Adj meg egy érvényes email címet.',
    phoneNumberOnly: 'A telefonszám 8-15 számjegy legyen, opcionális + előtaggal.',
    postalCodeNumberOnly: 'Az irányítószám pontosan 4 számjegy legyen.',
    houseNumberOnly: 'A házszám csak szám lehet.',
    underage: '18 év alatti alkalmazott nem vehető fel.',
    minimumSalaryError: 'A fizetés nem lehet kisebb a minimálbérnél.',
    newTicket: 'Új jegy',
    ticketType: 'Típus',
    problem: 'Probléma',
    createdBy: 'Létrehozta',
    createTicketButton: 'Jegy létrehozása',
    manageableTickets: 'Kezelhető jegyek',
    myTickets: 'Saját jegyek',
    newTask: 'Új feladat',
    selectProject: 'Válassz projektet',
    title: 'Cím',
    description: 'Leírás',
    priority: 'Prioritás',
    createTaskButton: 'Feladat létrehozása',
    newProject: 'Új projekt',
    projectBudget: 'Projekt költség',
    deadline: 'Határidő',
    createProjectButton: 'Projekt létrehozása',
    pendingProjectsTitle: 'Pending projektek',
    noPendingProjects: 'Nincs pending projekt.',
    projectsTitle: 'Projektek',
    date: 'Dátum',
    cost: 'Költség',
    status: 'Státusz',
    months: [
      'Január',
      'Február',
      'Március',
      'Április',
      'Május',
      'Június',
      'Július',
      'Augusztus',
      'Szeptember',
      'Október',
      'November',
      'December'
    ]
  },
  en: {
    settings: 'Settings',
    language: 'Language',
    theme: 'Theme',
    black: 'Black',
    white: 'White',
    profile: 'Profile',
    team: 'Team',
    task: 'Task',
    dayOff: 'Day off',
    homeOffice: 'Home office',
    usedWorkdays: 'workdays used',
    newDayOffRequest: 'New day off request',
    newDayOffButton: 'New day off',
    dayOffType: 'Type',
    startDate: 'Start',
    endDate: 'End',
    sendDayOffRequest: 'Send request',
    dayOffRequired: 'Type, start and end are required.',
    developerRequests: 'Developer requests',
    myDayOffRequests: 'My requests',
    requestName: 'Name',
    days: 'Days',
    approve: 'Approve',
    reject: 'Reject',
    approvedBy: 'Approved by',
    noPendingDeveloperRequests: 'There are no pending developer requests.',
    noOwnDayOffRequests: 'You do not have any Day off requests yet.',
    support: 'IT support',
    humanResources: 'Human resources',
    project: 'Project',
    statistics: 'Statistics',
    logout: 'Log out',
    firstName: 'First name',
    lastName: 'Last name',
    email: 'Email',
    name: 'Name',
    birthDate: 'Birth date',
    phone: 'Phone',
    address: 'Address',
    city: 'City',
    postalCode: 'Postal code',
    houseNumber: 'House number',
    role: 'Role',
    companyName: 'Company name',
    taxId: 'Tax ID',
    identityCardNumber: 'Identity card number',
    socialSecurityNumber: 'Social security number',
    salary: 'Salary',
    active: 'Active',
    yes: 'Yes',
    no: 'No',
    edit: 'Edit',
    changePassword: 'Change password',
    editProfile: 'Edit profile',
    oldPassword: 'Old password',
    newPassword: 'New password',
    repeatNewPassword: 'Repeat new password',
    saving: 'Saving...',
    saveProfile: 'Save profile',
    savePassword: 'Save password',
    currentTeam: 'Your current team',
    productOwner: 'Product owner',
    leaveTeam: 'Leave team',
    noTeamYet: 'You are not in a team yet. Choose one from the list.',
    availableTeams: 'Available teams',
    refresh: 'Refresh',
    membersLabel: 'Members',
    membersCount: 'members',
    join: 'Join',
    noTeams: 'No teams have been created yet.',
    createTeam: 'Create team',
    teamName: 'Team name',
    teamOwner: 'Team owner',
    teamMembers: 'Team members',
    noDeveloperInTeam: 'There are no developers in the team yet.',
    availableDevelopers: 'Available developers',
    searchByName: 'Search by name',
    searchByTitle: 'Search by title',
    add: 'Add',
    noAvailableDevelopers: 'There are no active developers without a team.',
    deleteTeam: 'Delete team',
    confirmDeleteTeam: 'Are you sure you want to delete this team?',
    teamNameRequired: 'Team name is required.',
    teamCreated: 'The team was created.',
    teamCreateFailed: 'Could not create the team.',
    developerAdded: 'Developer added to the team.',
    developerAddFailed: 'Could not add the developer.',
    teamDeleteFailed: 'Could not delete the team.',
    year: 'Year',
    loading: 'Loading...',
    users: 'Users',
    yearlyResult: 'monthly result',
    monthlyChartLabel: 'Monthly profit and loss bar chart',
    result: 'Result',
    projectIncome: 'Project income',
    salaryCost: 'Salary cost',
    balance: 'Balance',
    changeEmployeePassword: 'Change employee password',
    employeeEmail: 'Employee email',
    saveEmployeePassword: 'Save employee password',
    employeesTitle: 'Employees',
    newEmployeeButton: 'New employee',
    newEmployeeForm: 'New employee',
    editEmployeeForm: 'Edit employee',
    newForm: 'New form',
    saveEmployee: 'Save',
    deactivate: 'Deactivate',
    noEmployees: 'There are no employees yet.',
    employeeRequired: 'All required employee fields must be filled.',
    employeePasswordRequired: 'Password is required for a new employee.',
    invalidEmail: 'Enter a valid email address.',
    phoneNumberOnly: 'Phone number must be 8-15 digits, with an optional + prefix.',
    postalCodeNumberOnly: 'Postal code must be exactly 4 digits.',
    houseNumberOnly: 'House number must contain numbers only.',
    underage: 'Employees under 18 cannot be added.',
    minimumSalaryError: 'Salary cannot be lower than the minimum wage.',
    newTicket: 'New ticket',
    ticketType: 'Type',
    problem: 'Problem',
    createdBy: 'Created by',
    createTicketButton: 'Create ticket',
    manageableTickets: 'Manageable tickets',
    myTickets: 'My tickets',
    newTask: 'New task',
    selectProject: 'Select project',
    title: 'Title',
    description: 'Description',
    priority: 'Priority',
    createTaskButton: 'Create task',
    newProject: 'New project',
    projectBudget: 'Project budget',
    deadline: 'Deadline',
    createProjectButton: 'Create project',
    pendingProjectsTitle: 'Pending projects',
    noPendingProjects: 'There are no pending projects.',
    projectsTitle: 'Projects',
    date: 'Date',
    cost: 'Cost',
    status: 'Status',
    months: [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December'
    ]
  }
};

@Component({
  selector: 'app-management',
  imports: [FormsModule],
  templateUrl: './management.html',
  styleUrl: './management.css'
})
export class Management implements OnInit {
  firstName = signal('');
  lastName = signal('');
  email = signal('');
  active = signal(false);
  role = signal('');
  profile = signal<ProfileResponse | null>(null);
  selectedView = signal<ManagementView>('profile');
  editProfileVisible = signal(false);
  changePasswordVisible = signal(false);
  profileLoading = signal(false);
  passwordLoading = signal(false);
  profileMessage = signal('');
  profileError = signal('');
  passwordMessage = signal('');
  passwordError = signal('');
  team = signal<Team | null>(null);
  adminTeam = signal<Team | null>(null);
  teams = signal<Team[]>([]);
  availableDevelopers = signal<TeamEmployee[]>([]);
  developerSearch = signal('');
  teamLoading = signal(false);
  teamMessage = signal('');
  teamError = signal('');
  employees = signal<Employee[]>([]);
  employeeSearch = signal('');
  selectedEmployee = signal<Employee | null>(null);
  hrLoading = signal(false);
  hrMessage = signal('');
  hrError = signal('');
  hrFormVisible = signal(false);
  projects = signal<Project[]>([]);
  pendingProjects = signal<Project[]>([]);
  pendingProjectSearch = signal('');
  projectLoading = signal(false);
  projectMessage = signal('');
  projectError = signal('');
  projectFormVisible = signal(false);
  tasks = signal<Task[]>([]);
  draggedTaskId = signal<number | null>(null);
  dropTargetStatus = signal<TaskStatus | null>(null);
  taskLoading = signal(false);
  taskMessage = signal('');
  taskError = signal('');
  taskFormVisible = signal(false);
  dayOffPage = signal<DayOffPage | null>(null);
  dayOffLoading = signal(false);
  dayOffMessage = signal('');
  dayOffError = signal('');
  dayOffFormVisible = signal(false);
  developerRequestSearch = signal('');
  tickets = signal<Ticket[]>([]);
  ticketLoading = signal(false);
  ticketMessage = signal('');
  ticketError = signal('');
  adminPasswordPanelVisible = signal(false);
  ticketFormVisible = signal(false);
  statistics = signal<StatisticsPage | null>(null);
  statisticsLoading = signal(false);
  statisticsError = signal('');
  statisticsYear = signal(new Date().getFullYear());
  readonly statisticsYears = Array.from({ length: new Date().getFullYear() - 1999 }, (_, index) => 2000 + index);
  readonly minimumGrossSalary = 322800;
  readonly employeeRoles: EmployeeRole[] = ['HR', 'IT', 'PRODUCT_OWNER', 'DEVELOPER'];
  readonly taskStatuses: TaskStatus[] = ['TO_DO', 'IN_PROGRESS', 'DONE'];
  readonly taskPriorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  readonly dayOffTypes: DayOffType[] = ['HOME_OFFICE', 'DAY_OFF'];
  readonly ticketTypes: TicketType[] = ['BUG', 'FEATURE', 'HELP'];
  readonly ticketStatuses: TicketStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

  teamForm = {
    name: ''
  };

  employeeForm: EmployeeRequest = this.emptyEmployeeForm();
  projectForm: ProjectRequest = {
    title: '',
    description: '',
    budget: 5000000,
    deadline: ''
  };
  taskForm = {
    projectId: null as number | null,
    title: '',
    description: '',
    priority: 'MEDIUM' as TaskPriority
  };
  dayOffForm: DayOffCreateRequest = {
    type: 'DAY_OFF',
    startDate: '',
    endDate: ''
  };
  ticketForm = {
    type: 'HELP' as TicketType,
    problem: ''
  };

  profileForm: ProfileUpdateRequest = this.emptyProfileForm();

  passwordForm = {
    oldPassword: '',
    newPassword: '',
    newPasswordAgain: ''
  };

  adminPasswordForm = {
    targetEmail: '',
    newPassword: '',
    newPasswordAgain: ''
  };

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService,
    private readonly dayOffService: DayOffService,
    private readonly humanResourcesService: HumanResourcesService,
    private readonly projectService: ProjectService,
    private readonly statisticsService: StatisticsService,
    private readonly taskService: TaskService,
    private readonly teamService: TeamService,
    private readonly ticketService: TicketService,
    readonly ui: UiSettingsService
  ) {}

  ngOnInit(): void {
    this.email.set(this.ui.readCookie('loggedInUserEmail'));
    this.firstName.set(this.ui.readCookie('loggedInUserFirstName'));
    this.lastName.set(this.ui.readCookie('loggedInUserLastName'));
    this.active.set(this.ui.readCookie('loggedInUserActive') === 'true');
    this.role.set(this.ui.readCookie('loggedInUserRole'));

    if (!this.email()) {
      this.router.navigate(['/login']);
    }

    this.profileForm.firstName = this.firstName();
    this.profileForm.lastName = this.lastName();
    this.loadProfile();
  }

  get t() {
    return TEXT[this.ui.language()];
  }

  get fullName(): string {
    const name = `${this.firstName()} ${this.lastName()}`.trim();
    return name || this.email();
  }

  get profileName(): string {
    const profile = this.profile();
    const firstName = profile?.firstName || this.firstName();
    const lastName = profile?.lastName || this.lastName();
    return `${firstName} ${lastName}`.trim() || '-';
  }

  get profileAddress(): string {
    const profile = this.profile();
    const parts = [
      profile?.postalCode,
      profile?.city,
      profile?.address,
      profile?.houseNumber
    ].filter((part): part is string => !!part);

    return parts.join(' ') || '-';
  }

  get profileRoleLabel(): string {
    return this.roleLabel(this.profile()?.role || this.role());
  }

  get isCustomerProfile(): boolean {
    return (this.profile()?.role || this.role()) === 'CUSTOMER';
  }

  setLanguage(language: Language): void {
    this.ui.setLanguage(language);
  }

  setTheme(theme: Theme): void {
    this.ui.setTheme(theme);
  }

  showProfile(): void {
    this.selectedView.set('profile');
    this.loadProfile();
  }

  showTeam(): void {
    this.selectedView.set('team');
    this.loadTeam();
  }

  showTask(): void {
    this.selectedView.set('task');
    this.loadTasks();
    if (this.canReviewProject || this.role() === 'ADMIN') {
      this.loadProjects();
    }
  }

  toggleTaskForm(): void {
    this.taskFormVisible.update((visible) => !visible);
    this.taskError.set('');
    this.taskMessage.set('');
  }

  showDayOff(): void {
    this.selectedView.set('dayOff');
    this.loadDayOff();
  }

  showSupport(): void {
    this.selectedView.set('support');
    this.loadTickets();
  }

  toggleAdminPasswordPanel(): void {
    this.adminPasswordPanelVisible.update((visible) => !visible);
    if (!this.adminPasswordPanelVisible()) {
      return;
    }

    this.ticketFormVisible.set(false);
    this.passwordMessage.set('');
    this.passwordError.set('');
  }

  toggleTicketForm(): void {
    this.ticketFormVisible.update((visible) => !visible);
    if (!this.ticketFormVisible()) {
      return;
    }

    this.adminPasswordPanelVisible.set(false);
    this.ticketMessage.set('');
    this.ticketError.set('');
  }

  showHumanResources(): void {
    this.selectedView.set('hr');
    this.loadEmployees();
  }

  toggleHrForm(): void {
    this.hrFormVisible.update((visible) => !visible);
    this.hrMessage.set('');
    this.hrError.set('');
  }

  showProject(): void {
    this.selectedView.set('project');
    this.loadProjects();
  }

  toggleProjectForm(): void {
    this.projectFormVisible.update((visible) => !visible);
    this.projectError.set('');
    this.projectMessage.set('');
  }

  toggleDayOffForm(): void {
    this.dayOffFormVisible.update((visible) => !visible);
    this.dayOffError.set('');
    this.dayOffMessage.set('');
  }

  showStatistics(): void {
    this.selectedView.set('statistics');
    this.loadStatistics();
  }

  get canModifyProfile(): boolean {
    return this.active() && ['ADMIN', 'CUSTOMER'].includes(this.role());
  }

  get canAdministerPasswords(): boolean {
    return this.active() && ['ADMIN', 'IT'].includes(this.role());
  }

  get canManageTeam(): boolean {
    return this.active() && ['ADMIN', 'PRODUCT_OWNER'].includes(this.role());
  }

  get canUseTeamMenu(): boolean {
    return this.canManageTeam || (this.active() && this.role() === 'DEVELOPER');
  }

  get canJoinTeam(): boolean {
    return this.active() && this.role() === 'DEVELOPER';
  }

  get canUseHumanResources(): boolean {
    return this.active() && ['ADMIN', 'HR'].includes(this.role());
  }

  get canUseProjectMenu(): boolean {
    return this.active() && ['ADMIN', 'CUSTOMER', 'PRODUCT_OWNER'].includes(this.role());
  }

  get canUseStatistics(): boolean {
    return this.active() && this.role() === 'ADMIN';
  }

  get canUseTaskMenu(): boolean {
    return this.active() && ['ADMIN', 'PRODUCT_OWNER', 'DEVELOPER'].includes(this.role());
  }

  get canUseDayOffMenu(): boolean {
    return this.active() && this.role() !== 'CUSTOMER';
  }

  get canUseSupportMenu(): boolean {
    return this.active();
  }

  get canHandleTickets(): boolean {
    return this.active() && ['ADMIN', 'IT'].includes(this.role());
  }

  get canCreateProject(): boolean {
    return this.active() && ['ADMIN', 'CUSTOMER'].includes(this.role());
  }

  get canReviewProject(): boolean {
    return this.active() && ['ADMIN', 'PRODUCT_OWNER'].includes(this.role());
  }

  get acceptedOwnerProjects(): Project[] {
    if (this.role() === 'ADMIN') {
      return this.projects().filter((project) => project.status === 'ACCEPTED');
    }

    return this.projects().filter((project) => project.status === 'ACCEPTED' && project.productOwnerEmail === this.email());
  }

  get filteredAvailableDevelopers(): TeamEmployee[] {
    const search = this.developerSearch().trim().toLowerCase();

    if (!search) {
      return this.availableDevelopers();
    }

    return this.availableDevelopers().filter((developer) =>
      `${developer.firstName} ${developer.lastName}`.toLowerCase().includes(search)
    );
  }

  get filteredEmployees(): Employee[] {
    const search = this.employeeSearch().trim().toLowerCase();

    if (!search) {
      return this.employees();
    }

    return this.employees().filter((employee) =>
      `${employee.firstName} ${employee.lastName}`.toLowerCase().includes(search)
    );
  }

  get filteredPendingProjects(): Project[] {
    const search = this.pendingProjectSearch().trim().toLowerCase();

    if (!search) {
      return this.pendingProjects();
    }

    return this.pendingProjects().filter((project) =>
      project.title.toLowerCase().includes(search)
    );
  }

  get filteredPendingTeamDayOffRequests(): DayOffRequestItem[] {
    const search = this.developerRequestSearch().trim().toLowerCase();
    const requests = this.pendingTeamDayOffRequests();

    if (!search) {
      return requests;
    }

    return requests.filter((request) =>
      request.employeeName.toLowerCase().includes(search)
    );
  }

  toggleProfileEdit(): void {
    this.editProfileVisible.update((visible) => !visible);
    this.profileMessage.set('');
    this.profileError.set('');
    this.fillProfileForm();
  }

  togglePasswordChange(): void {
    this.changePasswordVisible.update((visible) => !visible);
    this.passwordMessage.set('');
    this.passwordError.set('');
    this.passwordForm = {
      oldPassword: '',
      newPassword: '',
      newPasswordAgain: ''
    };
  }

  saveProfile(): void {
    this.profileMessage.set('');
    this.profileError.set('');

    const validationError = this.validateProfileForm();
    if (validationError) {
      this.profileError.set(validationError);
      return;
    }

    this.profileLoading.set(true);
    this.authService.updateProfile(this.email(), {
      ...this.profileForm,
      firstName: this.profileForm.firstName.trim(),
      lastName: this.profileForm.lastName.trim(),
      email: this.profileForm.email.trim(),
      phoneNumber: this.profileForm.phoneNumber.trim(),
      address: this.profileForm.address.trim(),
      city: this.profileForm.city.trim(),
      postalCode: this.profileForm.postalCode.trim(),
      houseNumber: this.profileForm.houseNumber.trim(),
      companyName: this.isCustomerProfile ? this.toTitleCase(this.profileForm.companyName) : ''
    }).subscribe({
      next: (response) => {
        this.firstName.set(response.firstName);
        this.lastName.set(response.lastName);
        this.email.set(response.email);
        this.ui.writeCookie('loggedInUserEmail', response.email, 1);
        this.ui.writeCookie('loggedInUserFirstName', response.firstName, 1);
        this.ui.writeCookie('loggedInUserLastName', response.lastName, 1);
        this.profileMessage.set(response.message);
        this.editProfileVisible.set(false);
        this.profileLoading.set(false);
        this.loadProfile();
      },
      error: (error) => {
        this.profileError.set(error.error?.message || 'Nem sikerult modositani a profilt.');
        this.profileLoading.set(false);
      }
    });
  }

  loadProfile(): void {
    if (!this.email()) {
      return;
    }

    this.authService.loadProfile(this.email()).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.firstName.set(profile.firstName);
        this.lastName.set(profile.lastName);
        this.role.set(profile.role);
        this.fillProfileForm();
      },
      error: () => {
        this.profile.set(null);
      }
    });
  }

  private emptyProfileForm(): ProfileUpdateRequest {
    return {
      firstName: '',
      lastName: '',
      email: '',
      birthDate: '',
      phoneNumber: '',
      address: '',
      city: '',
      postalCode: '',
      houseNumber: '',
      companyName: ''
    };
  }

  private fillProfileForm(): void {
    const profile = this.profile();
    this.profileForm = {
      firstName: profile?.firstName || this.firstName(),
      lastName: profile?.lastName || this.lastName(),
      email: profile?.email || this.email(),
      birthDate: profile?.birthDate || '',
      phoneNumber: profile?.phoneNumber || '',
      address: profile?.address || '',
      city: profile?.city || '',
      postalCode: profile?.postalCode || '',
      houseNumber: profile?.houseNumber || '',
      companyName: profile?.companyName || ''
    };
  }

  private validateProfileForm(): string {
    const requiredFields = [
      this.profileForm.firstName,
      this.profileForm.lastName,
      this.profileForm.email,
      this.profileForm.birthDate,
      this.profileForm.phoneNumber,
      this.profileForm.address,
      this.profileForm.city,
      this.profileForm.postalCode,
      this.profileForm.houseNumber
    ];

    if (requiredFields.some((value) => !String(value).trim())) {
      return 'Minden kotelezo profil mezot ki kell tolteni.';
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.profileForm.email.trim())) {
      return 'Adj meg egy ervenyes email cimet.';
    }

    if (!/^\+?\d{8,15}$/.test(this.profileForm.phoneNumber.trim())) {
      return 'A telefonszam 8-15 szamjegy legyen, opcionalis + elotaggal.';
    }

    if (!/^\d{4}$/.test(this.profileForm.postalCode.trim())) {
      return 'Az iranyitoszam pontosan 4 szamjegy legyen.';
    }

    if (!/^\d+$/.test(this.profileForm.houseNumber.trim())) {
      return 'A hazszam csak szam lehet.';
    }

    return '';
  }

  formatProfileCompanyName(): void {
    this.profileForm.companyName = this.toTitleCase(this.profileForm.companyName);
  }

  private toTitleCase(value: string): string {
    return value
      .trim()
      .replace(/\s+/g, ' ')
      .split(' ')
      .map((word) => word.charAt(0).toLocaleUpperCase('hu-HU') + word.slice(1))
      .join(' ');
  }

  savePassword(): void {
    this.passwordMessage.set('');
    this.passwordError.set('');

    if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword || !this.passwordForm.newPasswordAgain) {
      this.passwordError.set('Minden password mezot ki kell tolteni.');
      return;
    }

    if (this.passwordForm.newPassword !== this.passwordForm.newPasswordAgain) {
      this.passwordError.set('A ket uj password nem egyezik.');
      return;
    }

    this.passwordLoading.set(true);
    this.authService.changePassword(
      this.email(),
      this.passwordForm.oldPassword,
      this.passwordForm.newPassword,
      this.passwordForm.newPasswordAgain
    ).subscribe({
      next: (response) => {
        this.passwordMessage.set(response.message);
        this.passwordForm = {
          oldPassword: '',
          newPassword: '',
          newPasswordAgain: ''
        };
        this.changePasswordVisible.set(false);
        this.passwordLoading.set(false);
      },
      error: (error) => {
        this.passwordError.set(error.error?.message || 'Nem sikerult megvaltoztatni a passwordt.');
        this.passwordLoading.set(false);
      }
    });
  }

  saveAdminPassword(): void {
    this.passwordMessage.set('');
    this.passwordError.set('');

    if (!this.adminPasswordForm.targetEmail || !this.adminPasswordForm.newPassword || !this.adminPasswordForm.newPasswordAgain) {
      this.passwordError.set('Email es uj password mezok kitoltese kotelezo.');
      return;
    }

    if (this.adminPasswordForm.newPassword !== this.adminPasswordForm.newPasswordAgain) {
      this.passwordError.set('A ket uj password nem egyezik.');
      return;
    }

    this.passwordLoading.set(true);
    this.authService.adminChangePassword(
      this.email(),
      this.adminPasswordForm.targetEmail.trim(),
      this.adminPasswordForm.newPassword,
      this.adminPasswordForm.newPasswordAgain
    ).subscribe({
      next: (response) => {
        this.passwordMessage.set(response.message);
        this.adminPasswordForm = {
          targetEmail: '',
          newPassword: '',
          newPasswordAgain: ''
        };
        this.passwordLoading.set(false);
      },
      error: (error) => {
        this.passwordError.set(error.error?.message || 'Nem sikerult modositani a user passwordjet.');
        this.passwordLoading.set(false);
      }
    });
  }

  loadTeam(): void {
    if (this.canManageTeam) {
      this.loadOwnedTeam();
      return;
    }

    if (this.canJoinTeam) {
      this.loadAdminTeams();
    }
  }

  loadOwnedTeam(): void {
    if (!this.canManageTeam) {
      return;
    }

    this.teamLoading.set(true);
    this.teamError.set('');
    this.teamMessage.set('');

    this.teamService.getMyTeam(this.email()).subscribe({
      next: (team) => {
        this.team.set(team);
        this.teamLoading.set(false);
        this.loadAvailableDevelopers();
      },
      error: (error) => {
        this.team.set(null);
        this.availableDevelopers.set([]);
        if (error.status !== 404) {
          this.teamError.set(error.error?.message || 'Nem sikerult betolteni a teamot.');
        }
        this.teamLoading.set(false);
      }
    });
  }

  loadAdminTeams(): void {
    if (!this.canJoinTeam) {
      return;
    }

    this.teamLoading.set(true);
    this.teamError.set('');
    this.teamMessage.set('');

    this.teamService.getTeams(this.email()).subscribe({
      next: (teams) => {
        this.teams.set(teams);
        this.teamLoading.set(false);
      },
      error: (error) => {
        this.teamError.set(error.error?.message || 'Nem sikerult betolteni a teamokat.');
        this.teamLoading.set(false);
      }
    });

    this.teamService.getMembership(this.email()).subscribe({
      next: (team) => this.adminTeam.set(team),
      error: (error) => {
        if (error.status === 404) {
          this.adminTeam.set(null);
          return;
        }

        this.teamError.set(error.error?.message || 'Nem sikerult betolteni a sajat teamodat.');
      }
    });
  }

  createTeam(): void {
    const name = this.teamForm.name.trim();
    this.teamError.set('');
    this.teamMessage.set('');

    if (!name) {
      this.teamError.set(this.t.teamNameRequired);
      return;
    }

    this.teamLoading.set(true);
    this.teamService.createTeam(this.email(), name).subscribe({
      next: (team) => {
        this.team.set(team);
        this.teamForm.name = '';
        this.teamMessage.set(this.t.teamCreated);
        this.teamLoading.set(false);
        this.loadAvailableDevelopers();
      },
      error: (error) => {
        this.teamError.set(error.error?.message || this.t.teamCreateFailed);
        this.teamLoading.set(false);
      }
    });
  }

  loadAvailableDevelopers(): void {
    if (!this.canManageTeam) {
      return;
    }

    this.teamService.getAvailableDevelopers(this.email()).subscribe({
      next: (developers) => this.availableDevelopers.set(developers),
      error: (error) => this.teamError.set(error.error?.message || 'Nem sikerult betolteni a szabad fejlesztoket.')
    });
  }

  addDeveloper(developerId: number): void {
    this.teamError.set('');
    this.teamMessage.set('');
    this.teamLoading.set(true);

    this.teamService.addMember(this.email(), developerId).subscribe({
      next: (team) => {
        this.team.set(team);
        this.teamMessage.set(this.t.developerAdded);
        this.teamLoading.set(false);
        this.loadAvailableDevelopers();
      },
      error: (error) => {
        this.teamError.set(error.error?.message || this.t.developerAddFailed);
        this.teamLoading.set(false);
      }
    });
  }

  deleteTeam(): void {
    if (!confirm(this.t.confirmDeleteTeam)) {
      return;
    }

    this.teamError.set('');
    this.teamMessage.set('');
    this.teamLoading.set(true);

    this.teamService.deleteTeam(this.email()).subscribe({
      next: (response) => {
        this.team.set(null);
        this.availableDevelopers.set([]);
        this.teamForm.name = '';
        this.teamMessage.set(response.message);
        this.teamLoading.set(false);
      },
      error: (error) => {
        this.teamError.set(error.error?.message || this.t.teamDeleteFailed);
        this.teamLoading.set(false);
      }
    });
  }

  joinTeam(teamId: number): void {
    this.teamError.set('');
    this.teamMessage.set('');
    this.teamLoading.set(true);

    this.teamService.joinTeam(this.email(), teamId).subscribe({
      next: (team) => {
        this.adminTeam.set(team);
        this.teamMessage.set('Sirequesten csatlakoztal a teamhoz.');
        this.teamLoading.set(false);
        this.loadAdminTeams();
      },
      error: (error) => {
        this.teamError.set(error.error?.message || 'Nem sikerult csatlakozni a teamhoz.');
        this.teamLoading.set(false);
      }
    });
  }

  leaveTeam(): void {
    this.teamError.set('');
    this.teamMessage.set('');
    this.teamLoading.set(true);

    this.teamService.leaveTeam(this.email()).subscribe({
      next: (response) => {
        this.adminTeam.set(null);
        this.teamMessage.set(response.message);
        this.teamLoading.set(false);
        this.loadAdminTeams();
      },
      error: (error) => {
        this.teamError.set(error.error?.message || 'Nem sikerult kilepni a teambol.');
        this.teamLoading.set(false);
      }
    });
  }

  loadTasks(): void {
    if (!this.canUseTaskMenu) {
      return;
    }

    this.taskLoading.set(true);
    this.taskError.set('');
    this.taskMessage.set('');

    this.taskService.listTasks(this.email()).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.taskLoading.set(false);
      },
      error: (error) => {
        this.taskError.set(error.error?.message || 'Failed to load tasks.');
        this.taskLoading.set(false);
      }
    });
  }

  tasksByStatus(status: TaskStatus): Task[] {
    return this.tasks().filter((task) => task.status === status);
  }

  dragTask(event: DragEvent, task: Task): void {
    if (!this.canDragTask(task)) {
      event.preventDefault();
      return;
    }

    this.draggedTaskId.set(task.id);
    event.dataTransfer?.setData('text/plain', String(task.id));
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
    }
  }

  endTaskDrag(): void {
    this.draggedTaskId.set(null);
    this.dropTargetStatus.set(null);
  }

  allowTaskDrop(event: DragEvent, status: TaskStatus): void {
    const task = this.draggedTask();

    if (!task || !this.canDropTask(task, status)) {
      return;
    }

    event.preventDefault();
    this.dropTargetStatus.set(status);
    if (event.dataTransfer) {
      event.dataTransfer.dropEffect = 'move';
    }
  }

  leaveTaskDrop(status: TaskStatus): void {
    if (this.dropTargetStatus() === status) {
      this.dropTargetStatus.set(null);
    }
  }

  dropTask(event: DragEvent, status: TaskStatus): void {
    event.preventDefault();

    const task = this.draggedTask();
    this.endTaskDrag();

    if (!task || !this.canDropTask(task, status)) {
      return;
    }

    if (task.status === 'TO_DO' && status === 'IN_PROGRESS') {
      this.taskAction(this.taskService.takeTask(this.email(), task.id), 'A task rad kerult es WORKING allapotba lepett.');
      return;
    }

    if (task.status === 'IN_PROGRESS' && status === 'DONE') {
      this.taskAction(this.taskService.finishTask(this.email(), task.id), 'A task DONE allapotba kerult.');
      return;
    }

    if (task.status === 'TO_DO' && status === 'DONE') {
      this.taskAction(
        this.taskService.takeTask(this.email(), task.id).pipe(
          switchMap((takenTask) => this.taskService.finishTask(this.email(), takenTask.id))
        ),
        'A task rad kerult es DONE allapotba kerult.'
      );
    }
  }

  canDragTask(task: Task): boolean {
    return !this.taskLoading() &&
      (this.canTakeTask(task) || this.canFinishTask(task));
  }

  canDropTask(task: Task, status: TaskStatus): boolean {
    if (task.status === status) {
      return false;
    }

    if (task.status === 'TO_DO') {
      return status === 'IN_PROGRESS' || status === 'DONE';
    }

    if (task.status === 'IN_PROGRESS') {
      return status === 'DONE' && this.canFinishTask(task);
    }

    return false;
  }

  private draggedTask(): Task | null {
    const id = this.draggedTaskId();
    return id == null ? null : this.tasks().find((task) => task.id === id) || null;
  }

  createTask(): void {
    this.taskError.set('');
    this.taskMessage.set('');

    if (!this.taskForm.projectId || !this.taskForm.title.trim()) {
      this.taskError.set('Project and title are required.');
      return;
    }

    this.taskLoading.set(true);
    this.taskService.createTask(this.email(), {
      projectId: this.taskForm.projectId,
      title: this.taskForm.title.trim(),
      description: this.taskForm.description,
      priority: this.taskForm.priority
    }).subscribe({
      next: () => {
        this.taskMessage.set('A task bekerult a TO_DO oszlopba.');
        this.taskForm = {
          projectId: this.taskForm.projectId,
          title: '',
          description: '',
          priority: 'MEDIUM'
        };
        this.taskLoading.set(false);
        this.loadTasks();
      },
      error: (error) => {
        this.taskError.set(error.error?.message || 'Nem sikerult letrehozni a taskot.');
        this.taskLoading.set(false);
      }
    });
  }

  takeTask(taskId: number): void {
    this.taskAction(this.taskService.takeTask(this.email(), taskId), 'A task atkerult Working allapotba.');
  }

  finishTask(taskId: number): void {
    this.taskAction(this.taskService.finishTask(this.email(), taskId), 'A task DONE allapotba kerult.');
  }

  canTakeTask(task: Task): boolean {
    return ['ADMIN', 'DEVELOPER'].includes(this.role()) && task.status === 'TO_DO' && !task.employeeEmail;
  }

  canFinishTask(task: Task): boolean {
    return task.status === 'IN_PROGRESS' &&
      (this.role() === 'ADMIN' || (this.role() === 'DEVELOPER' && task.employeeEmail === this.email()));
  }

  private taskAction(action$: Observable<Task>, message: string): void {
    this.taskLoading.set(true);
    this.taskError.set('');
    this.taskMessage.set('');

    action$.subscribe({
      next: () => {
        this.taskMessage.set(message);
        this.taskLoading.set(false);
        this.loadTasks();
      },
      error: (error) => {
        this.taskError.set(error.error?.message || 'Nem sikerult vegrehajtani a muveletet.');
        this.taskLoading.set(false);
      }
    });
  }

  loadDayOff(): void {
    if (!this.canUseDayOffMenu) {
      return;
    }

    this.dayOffLoading.set(true);
    this.dayOffError.set('');
    this.dayOffMessage.set('');

    this.dayOffService.load(this.email()).subscribe({
      next: (page) => {
        this.dayOffPage.set(page);
        this.dayOffLoading.set(false);
      },
      error: (error) => {
        this.dayOffError.set(error.error?.message || 'Nem sikerult betolteni a dayOffeket.');
        this.dayOffLoading.set(false);
      }
    });
  }

  createDayOffRequest(): void {
    this.dayOffError.set('');
    this.dayOffMessage.set('');

    if (!this.dayOffForm.type || !this.dayOffForm.startDate || !this.dayOffForm.endDate) {
      this.dayOffError.set(this.t.dayOffRequired);
      return;
    }

    this.dayOffLoading.set(true);
    this.dayOffService.request(this.email(), this.dayOffForm).subscribe({
      next: () => {
        this.dayOffMessage.set('A dayOff kerelem PENDING statusba kerult.');
        this.dayOffForm = {
          type: this.dayOffForm.type,
          startDate: '',
          endDate: ''
        };
        this.dayOffLoading.set(false);
        this.loadDayOff();
      },
      error: (error) => {
        this.dayOffError.set(error.error?.message || 'Nem sikerult elkuldeni a kerelmet.');
        this.dayOffLoading.set(false);
      }
    });
  }

  approveDayOff(requestId: number): void {
    this.dayOffAction(this.dayOffService.approve(this.email(), requestId), 'A kerelem jovahagyva.');
  }

  rejectDayOff(requestId: number): void {
    this.dayOffAction(this.dayOffService.reject(this.email(), requestId), 'A kerelem elutasitva.');
  }

  pendingTeamDayOffRequests(): DayOffRequestItem[] {
    return this.dayOffPage()?.teamRequests.filter((request) => request.status === 'PENDING') || [];
  }

  private dayOffAction(action$: Observable<DayOffRequestItem>, message: string): void {
    this.dayOffLoading.set(true);
    this.dayOffError.set('');
    this.dayOffMessage.set('');

    action$.subscribe({
      next: () => {
        this.dayOffMessage.set(message);
        this.dayOffLoading.set(false);
        this.loadDayOff();
      },
      error: (error) => {
        this.dayOffError.set(error.error?.message || 'Nem sikerult vegrehajtani a muveletet.');
        this.dayOffLoading.set(false);
      }
    });
  }

  loadTickets(): void {
    if (!this.canUseSupportMenu) {
      return;
    }

    this.ticketLoading.set(true);
    this.ticketError.set('');
    this.ticketMessage.set('');

    this.ticketService.listTickets(this.email()).subscribe({
      next: (tickets) => {
        this.tickets.set(tickets);
        this.ticketLoading.set(false);
      },
      error: (error) => {
        this.ticketError.set(error.error?.message || 'Failed to load tickets.');
        this.ticketLoading.set(false);
      }
    });
  }

  createTicket(): void {
    this.ticketError.set('');
    this.ticketMessage.set('');

    if (!this.ticketForm.type || !this.ticketForm.problem.trim()) {
      this.ticketError.set('Type and problem are required.');
      return;
    }

    this.ticketLoading.set(true);
    this.ticketService.createTicket(this.email(), {
      type: this.ticketForm.type,
      problem: this.ticketForm.problem.trim()
    }).subscribe({
      next: () => {
        this.ticketMessage.set('A jegy letrejott CLOSED statusban.');
        this.ticketForm = {
          type: this.ticketForm.type,
          problem: ''
        };
        this.ticketLoading.set(false);
        this.loadTickets();
      },
      error: (error) => {
        this.ticketError.set(error.error?.message || 'Nem sikerult letrehozni a jegyet.');
        this.ticketLoading.set(false);
      }
    });
  }

  updateTicketStatus(ticketId: number, status: TicketStatus): void {
    this.ticketLoading.set(true);
    this.ticketError.set('');
    this.ticketMessage.set('');

    this.ticketService.updateTicket(this.email(), ticketId, status).subscribe({
      next: () => {
        this.ticketMessage.set('A jegy statusa modositva.');
        this.ticketLoading.set(false);
        this.loadTickets();
      },
      error: (error) => {
        this.ticketError.set(error.error?.message || 'Nem sikerult modositani a jegyet.');
        this.ticketLoading.set(false);
      }
    });
  }

  loadEmployees(): void {
    if (!this.canUseHumanResources) {
      return;
    }

    this.hrLoading.set(true);
    this.hrError.set('');
    this.hrMessage.set('');

    this.humanResourcesService.listEmployees(this.email()).subscribe({
      next: (employees) => {
        this.employees.set(employees);
        this.hrLoading.set(false);
      },
      error: (error) => {
        this.hrError.set(error.error?.message || 'Nem sikerult betolteni az employeeakat.');
        this.hrLoading.set(false);
      }
    });
  }

  newEmployee(): void {
    this.selectedEmployee.set(null);
    this.employeeForm = this.emptyEmployeeForm();
    this.hrFormVisible.set(true);
    this.hrMessage.set('');
    this.hrError.set('');
  }

  editEmployee(employee: Employee): void {
    this.selectedEmployee.set(employee);
    this.hrFormVisible.set(true);
    this.employeeForm = {
      firstName: employee.firstName,
      lastName: employee.lastName,
      email: employee.email,
      password: '',
      birthDate: employee.birthDate,
      phoneNumber: employee.phoneNumber,
      address: employee.address,
      city: employee.city,
      postalCode: employee.postalCode,
      houseNumber: employee.houseNumber,
      role: employee.role,
      active: employee.active,
      taxId: employee.taxId,
      identityCardNumber: employee.identityCardNumber,
      socialSecurityNumber: employee.socialSecurityNumber,
      salary: employee.salary
    };
    this.hrMessage.set('');
    this.hrError.set('');
  }

  saveEmployee(): void {
    this.hrMessage.set('');
    this.hrError.set('');

    const selected = this.selectedEmployee();

    const validationError = this.validateEmployeeForm(!selected);
    if (validationError) {
      this.hrError.set(validationError);
      return;
    }

    if (selected && !confirm('Biztos modositod ezt az employeeat?')) {
      return;
    }

    this.hrLoading.set(true);
    const request = { ...this.employeeForm };

    const save$ = selected
      ? this.humanResourcesService.updateEmployee(this.email(), selected.id, request, this.ui.language())
      : this.humanResourcesService.createEmployee(this.email(), request, this.ui.language());

    save$.subscribe({
      next: () => {
        this.hrMessage.set(selected ? 'Az employee modositva.' : 'Az uj employee felveve.');
        this.hrLoading.set(false);
        this.newEmployee();
        this.loadEmployees();
      },
      error: (error) => {
        this.hrError.set(error.error?.message || 'Nem sikerult menteni az employeeat.');
        this.hrLoading.set(false);
      }
    });
  }

  private validateEmployeeForm(requirePassword: boolean): string {
    const requiredFields = [
      this.employeeForm.firstName,
      this.employeeForm.lastName,
      this.employeeForm.email,
      this.employeeForm.birthDate,
      this.employeeForm.phoneNumber,
      this.employeeForm.address,
      this.employeeForm.city,
      this.employeeForm.postalCode,
      this.employeeForm.houseNumber,
      this.employeeForm.role,
      this.employeeForm.salary
    ];

    if (requiredFields.some((value) => value === null || value === undefined || !String(value).trim())) {
      return this.t.employeeRequired;
    }

    if (requirePassword && !this.employeeForm.password?.trim()) {
      return this.t.employeePasswordRequired;
    }

    if (!this.isValidEmail(this.employeeForm.email)) {
      return this.t.invalidEmail;
    }

    if (!this.isValidPhoneNumber(this.employeeForm.phoneNumber)) {
      return this.t.phoneNumberOnly;
    }

    if (!this.isValidPostalCode(this.employeeForm.postalCode)) {
      return this.t.postalCodeNumberOnly;
    }

    if (!this.isOnlyDigits(this.employeeForm.houseNumber)) {
      return this.t.houseNumberOnly;
    }

    if (this.isUnderage(this.employeeForm.birthDate || '')) {
      return this.t.underage;
    }

    if ((this.employeeForm.salary || 0) < this.minimumGrossSalary) {
      return this.t.minimumSalaryError;
    }

    return '';
  }

  deactivateEmployee(employee: Employee): void {
    if (!confirm('Biztos ki akarod rugni? A rekord megmarad, csak inaktiv lesz.')) {
      return;
    }

    this.hrLoading.set(true);
    this.hrMessage.set('');
    this.hrError.set('');

    this.humanResourcesService.deactivateEmployee(this.email(), employee.id).subscribe({
      next: () => {
        this.hrMessage.set('Az employee inaktiv lett, es kikerult a teambol.');
        this.hrLoading.set(false);
        this.loadEmployees();
      },
      error: (error) => {
        this.hrError.set(error.error?.message || 'Nem sikerult inaktivalni az employeeat.');
        this.hrLoading.set(false);
      }
    });
  }

  private emptyEmployeeForm(): EmployeeRequest {
    return {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      birthDate: null,
      phoneNumber: '',
      address: '',
      city: '',
      postalCode: '',
      houseNumber: '',
      role: 'DEVELOPER',
      active: true,
      taxId: '',
      identityCardNumber: '',
      socialSecurityNumber: '',
      salary: null
    };
  }

  loadProjects(): void {
    if (!this.canUseProjectMenu) {
      return;
    }

    this.projectLoading.set(true);
    this.projectError.set('');
    this.projectMessage.set('');

    this.projectService.listProjects(this.email()).subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.projectLoading.set(false);
      },
      error: (error) => {
        this.projectError.set(error.error?.message || 'Nem sikerult betolteni a projecteket.');
        this.projectLoading.set(false);
      }
    });

    if (this.canReviewProject) {
      this.projectService.listPendingProjects(this.email()).subscribe({
        next: (projects) => this.pendingProjects.set(projects),
        error: (error) => this.projectError.set(error.error?.message || 'Nem sikerult betolteni a pending projecteket.')
      });
    }
  }

  createProject(): void {
    this.projectError.set('');
    this.projectMessage.set('');

    if (!this.projectForm.title || !this.projectForm.deadline) {
      this.projectError.set(this.ui.language() === 'en'
        ? 'Title and deadline are required.'
        : 'Cim es deadline kotelezo.');
      return;
    }

    this.projectLoading.set(true);
    this.projectService.createProject(this.email(), this.projectForm, this.ui.language()).subscribe({
      next: () => {
        this.projectMessage.set('A project PENDING statusban letrejott.');
        this.projectForm = {
          title: '',
          description: '',
          budget: 5000000,
          deadline: ''
        };
        this.projectLoading.set(false);
        this.loadProjects();
      },
      error: (error) => {
        this.projectError.set(error.error?.message || 'Nem sikerult letrehozni a projectet.');
        this.projectLoading.set(false);
      }
    });
  }

  acceptProject(projectId: number): void {
    this.projectAction(
      this.projectService.acceptProject(this.email(), projectId, this.ui.language()),
      this.ui.language() === 'en' ? 'The project was accepted.' : 'A project elfogadva.'
    );
  }

  rejectProject(projectId: number): void {
    this.projectAction(this.projectService.rejectProject(this.email(), projectId), 'A project elutasitva.');
  }

  completeProject(projectId: number): void {
    this.projectAction(this.projectService.completeProject(this.email(), projectId), 'A project COMPLETED statusba kerult.');
  }

  loadStatistics(): void {
    if (!this.canUseStatistics) {
      return;
    }

    this.statisticsLoading.set(true);
    this.statisticsError.set('');

    this.statisticsService.load(this.email(), this.statisticsYear()).subscribe({
      next: (statistics) => {
        this.statistics.set(statistics);
        this.statisticsLoading.set(false);
      },
      error: (error) => {
        this.statisticsError.set(error.error?.message || 'Nem sikerult betolteni a statisztikat.');
        this.statisticsLoading.set(false);
      }
    });
  }

  setStatisticsYear(year: number): void {
    this.statisticsYear.set(year);
    this.loadStatistics();
  }

  get roleSlices(): RoleSlice[] {
    return this.statistics()?.roleSlices.filter((slice) => slice.count > 0) || [];
  }

  get monthlyFinances(): MonthlyFinance[] {
    return this.statistics()?.months || [];
  }

  get pieChartStyle(): string {
    const slices = this.roleSlices;
    const total = slices.reduce((sum, slice) => sum + slice.count, 0);
    if (!total) {
      return 'conic-gradient(#d0d7de 0deg 360deg)';
    }

    let current = 0;
    const parts = slices.map((slice) => {
      const start = current;
      current += (slice.count / total) * 360;
      return `${slice.color} ${start}deg ${current}deg`;
    });

    return `conic-gradient(${parts.join(', ')})`;
  }

  get maxMonthlyAbsProfit(): number {
    return Math.max(...this.monthlyFinances.map((month) => Math.abs(month.profit)), 1);
  }

  monthlyBarHeight(month: MonthlyFinance): number {
    return Math.max(8, Math.round((Math.abs(month.profit) / this.maxMonthlyAbsProfit) * 150));
  }

  monthLabel(month: MonthlyFinance): string {
    return this.t.months[month.month - 1] || month.label;
  }

  formatFt(value: number): string {
    return new Intl.NumberFormat('hu-HU').format(value) + ' Ft';
  }

  formatOptionalFt(value: number | null | undefined): string {
    return value == null ? '-' : this.formatFt(value);
  }

  private isValidPhoneNumber(value: string): boolean {
    return /^\+?\d{8,15}$/.test(value.trim());
  }

  private isValidPostalCode(value: string): boolean {
    return /^\d{4}$/.test(value.trim());
  }

  private isValidEmail(value: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
  }

  private isOnlyDigits(value: string): boolean {
    return /^\d+$/.test(value.trim());
  }

  private isUnderage(birthDate: string): boolean {
    if (!birthDate) {
      return true;
    }

    const today = new Date();
    const birthday = new Date(birthDate);
    let age = today.getFullYear() - birthday.getFullYear();
    const monthDiff = today.getMonth() - birthday.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthday.getDate())) {
      age -= 1;
    }

    return age < 18;
  }

  roleLabel(role: string): string {
    return role
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
      .join(' ');
  }

  private projectAction(action$: Observable<Project>, message: string): void {
    this.projectLoading.set(true);
    this.projectError.set('');
    this.projectMessage.set('');

    action$.subscribe({
      next: () => {
        this.projectMessage.set(message);
        this.projectLoading.set(false);
        this.loadProjects();
        if (this.selectedView() === 'task') {
          this.loadTasks();
        }
      },
      error: (error) => {
        this.projectError.set(error.error?.message || 'Nem sikerult vegrehajtani a muveletet.');
        this.projectLoading.set(false);
      }
    });
  }

  logout(): void {
    this.ui.deleteCookie('loggedInUserEmail');
    this.ui.deleteCookie('loggedInUserFirstName');
    this.ui.deleteCookie('loggedInUserLastName');
    this.ui.deleteCookie('loggedInUserActive');
    this.ui.deleteCookie('loggedInUserRole');
    this.router.navigate(['/login']);
  }
}
