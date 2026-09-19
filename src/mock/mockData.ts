/**
 * Mock data has been completely removed and disabled.
 * All frontend operations now communicate directly with the Spring Boot backend APIs.
 */

export const mockDb = {
  getUsers: () => [],
  getUserById: () => null,
  getProjects: () => [],
  getProjectById: () => null,
  getModules: () => [],
  getModuleById: () => null,
  getSubmodules: () => [],
  getTestCases: () => [],
  getTestCaseById: () => null,
  getReleaseTestCases: () => [],
  getReleases: () => [],
  getReleaseById: () => null,
  getDefects: () => [],
  getDefectById: () => null,
  getDefectTypes: () => [],
  getSeverities: () => [],
  getPriorities: () => [],
  getStatuses: () => [],
  getDesignations: () => [],
  getRoles: () => [],
  getProjectAllocations: () => [],
  getSubmoduleAllocations: () => [],
  getWorkflowTransitions: () => [],
  getWorkflowNodes: () => [],
  getEmailConfigs: () => [],
  getUserPreferences: () => ({ emailNotifications: true }),
};

export const INITIAL_PERMISSIONS: any[] = [];
export const INITIAL_EMAIL_POINT_SETUPS: any[] = [];
export const INITIAL_EMAIL_TEMPLATES: any[] = [];
