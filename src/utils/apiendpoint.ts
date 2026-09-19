const BASE = "/api/v1";

export const ENDPOINTS = {
  designation: `${BASE}/Designation/getAll`,
  designationCreate: `${BASE}/Designation/createdesignation`,
  designationPagination : (page : number, size : number) =>  `${BASE}/Designation/getAll?page=${page}&size=${size}`,
  designationById: (id: number) => `${BASE}/Designation/get/${id}`,
  designationUpdate: (id: number) => `${BASE}/Designation/update/${id}`,
  designationDelete: (id: number) => `${BASE}/Designation/delete/${id}`,
  designationEmployee: (designationId: number) =>
    `${BASE}/Employee/designation/${designationId}`,

  role: (page: number = 0, size: number = 10) => `${BASE}/Role?page=${page}&size=${size}`,
  roleCreate: `${BASE}/Role/save`,
  roleById: (id: number) => `${BASE}/Role/view/${id}`,
  roleUpdate: (id: number) => `${BASE}/Role/update/${id}`,
  roleDelete: (id: number) => `${BASE}/Role/delete/${id}`,

  permission: `${BASE}/permission`,
  permissionById: (id: number) => `${BASE}/permission/${id}`,

  defectType: `${BASE}/defecttype/getAll`,
  defectTypeCreate: `${BASE}/defecttype/create`,
  defectTypeById: (id: number) => `${BASE}/defecttype/get/${id}`,
  defectTypeUpdate: (id: number) => `${BASE}/defecttype/update/${id}`,
  defectTypeDelete: (id: number) => `${BASE}/defecttype/delete/${id}`,
  defectTypePagination: (page: number = 0, size: number = 10) => `${BASE}/defecttype/getAll?page=${page}&size=${size}`,

  releaseType: `${BASE}/release_types`,
  releaseTypeById: (id: number) => `${BASE}/release_types/${id}`,
  releaseTypePagination: (page: number = 0, size: number = 10) => `${BASE}/release_types?page=${page}&size=${size}`,

  severity: `${BASE}/severity`,
  severityPagination: (page: number = 0, size: number = 10) =>
    `${BASE}/severity?page=${page}&size=${size}`,
  severityById: (id: number) => `${BASE}/severity/${id}`,

  priority: `${BASE}/priority/view`,
  priorityCreate: `${BASE}/priority/save`,
  priorityPagination: (page: number = 0, size: number = 10) =>
    `${BASE}/priority/view?page=${page}&size=${size}`,
  priorityById: (id: number) => `${BASE}/priority/view/${id}`,
  priorityUpdate: (id: number) => `${BASE}/priority/update/${id}`,
  priorityDelete: (id: number) => `${BASE}/priority/delete/${id}`,

  statusType: `${BASE}/status-type`,
  statusTypePagination: (page: number = 0, size: number = 10) =>
    `${BASE}/status-type?page=${page}&size=${size}`,
  statusTypeById: (id: number) => `${BASE}/status-type/${id}`,

  workflowNextStatus: (id: number) => `${BASE}/status/${id}/next`,
  workflow: `${BASE}/status/workflow`,
  workflowById: (id: number) => `${BASE}/status/workflow/${id}`,

  employee: `${BASE}/employee`,
  employeeById: (id: number) => `${BASE}/employee/${id}`,
  employeeStatus: (id: number) => `${BASE}/employee/${id}/status`,

  rolePermissionMatrix: `${BASE}/assign-permission/matrix`,
  rolePermissionMatrixByRoleId: (roleId: number) =>
    `${BASE}/assign-permission/matrix/${roleId}`,

  login: `${BASE}/auth/login`,
  changePassword: `${BASE}/auth/change-password`,
  forgetPassword: `${BASE}/auth/forget-password`,
  resetPassword: `${BASE}/auth/reset-password`,
  logout: `${BASE}/auth/log-out`,
  refreshToken: `${BASE}/auth/refresh-token`,

  employeePermission: (employeeId: number) =>
    `${BASE}/employee/${employeeId}/permission`,

  project: `${BASE}/project`,
  projectById: (id: number) => `${BASE}/project/${id}`,
  availableManagers: (designationId: number) =>
  `${BASE}/designation/${designationId}/available-managers`,
availableManagersForUpdate: (designationId: number, projectId: number) =>
  `${BASE}/designation/${designationId}/available-managers/project/${projectId}`,

  module: (projectId: number) => `${BASE}/project/${projectId}/module`,
  moduleById: (projectId: number, id: number) =>
    `${BASE}/project/${projectId}/module/${id}`,

  subModule: (moduleId: number) => `${BASE}/module/${moduleId}/sub-module`,
  subModuleById: (moduleId: number, id: number) =>
    `${BASE}/module/${moduleId}/sub-module/${id}`,
     subModuleBulk: () => `${BASE}/subModule/bulk-by-modules`,

  subModuleDev: (id: number) => `${BASE}/sub-module/${id}/employee`,
  subModuleDevDelete: (moduleId: number, employeeId: number) =>
    `${BASE}/sub-module/${moduleId}/employee/${employeeId}`,

  projectAllocation: `${BASE}/project-allocation`,
  projectAllocationExtend: (id: number) =>
    `${BASE}/project-allocation/employee/${id}/extend`,
  projectAllocationProjectEmployee: (projectId: number) =>
    `${BASE}/project-allocation/${projectId}/employee`,
  projectAllocationProjectEmployeeHistory: (projectId: number) =>
    `${BASE}/project-allocation/${projectId}/employee_history`,
  projectAllocationByEmployee: (userId: number) =>
    `${BASE}/project-allocation/employee/${userId}`,
  projectAllocationDeallocate: (id: number) =>
    `${BASE}/project-allocation/employee/${id}`,

testCaseBySubModule: (subModuleId: number, description?: string, defectTypeId?: number, severityId?: number, page?: number, size?: number) => {
  let url = `${BASE}/sub-module/${subModuleId}/test-case`;
  
  const queryParams: string[] = [];
  
  
  if (description && description.trim() !== "") {
    queryParams.push(`description=${encodeURIComponent(description)}`);
  }
  if (defectTypeId && defectTypeId > 0) {
    queryParams.push(`defectTypeId=${defectTypeId}`);
  }
  if (severityId && severityId > 0) {
    queryParams.push(`severityId=${severityId}`);
  }
  if (page && page > 0) {
    queryParams.push(`page=${page}`);
  }
  if (size && size > 0) {
    queryParams.push(`size=${size}`);
  }
  
  if (queryParams.length > 0) {
    url += `?${queryParams.join('&')}`;
  }
  
  console.log('Constructed URL:', url);
  return url;
},
  testCaseById: (subModuleId: number, id: number) =>
    `${BASE}/sub-module/${subModuleId}/test-case/${id}`,
  testCaseAll: `${BASE}/test-case`,
  testCaseBulkBySubModule: (subModuleId: number) =>
    `${BASE}/sub-module/${subModuleId}/test-case/bulk`,
  testCaseBulkAll: `${BASE}/test-case/bulk`,
  testCaseBulkExport: `${BASE}/test-case/bulk`,

  release: `${BASE}/release`,
  releaseCount: `${BASE}/release/counts`,
  releaseById: (id: number) => `${BASE}/release/${id}`,
  releaseStatus: (releaseId: number) => `${BASE}/release/${releaseId}/status`,
releaseActiveByProject: (projectId: number) =>
  `/api/v1/project/${projectId}/release/active`,
  releaseKloc: (id: number) => `${BASE}/release/${id}`,
  releaseKlocById: (releaseId: number) =>
    `${BASE}/release/${releaseId}/kloc`,
  projectKloc: (projectId: number) => `${BASE}/project/${projectId}/project-kilo-of-code`,
 gitKloc: `${BASE}/kloc`,

  releaseTestCase: (releaseId: number) =>
    `${BASE}/release/${releaseId}/test-case`,
  releaseTestCaseById: (releaseId: number, id: number) =>
    `${BASE}/release/${releaseId}/test-case/${id}`,
  releaseTestCaseQaAssign: (releaseId: number, testcaseId: number) =>
    `${BASE}/release/${releaseId}/test-case/${testcaseId}/employee`,
  releaseTestCaseQaAllocation: (releaseId: number) =>
    `${BASE}/release/${releaseId}/test-case-qa-allocation`,
  releaseTestCaseEmployeePatch: (releaseId: number, employeeId: number) =>
    `${BASE}/release/${releaseId}/test-case/employee/${employeeId}`,
  releaseTestCaseStatus: (releaseId: number, id: number) =>
    `${BASE}/release/${releaseId}/test-case/${id}/status`,
  testCaseAllocationLog: `${BASE}/testcase/allocation-log`,

  emailConfig: `${BASE}/email/config`,
  emailConfigById: (id: number) => `${BASE}/email/config/${id}`,
  emailConfigEnable: (id: number) => `${BASE}/email/config/${id}/enable`,

  emailPointSetup: `${BASE}/email/point-setup`,
  emailPointSetupEnable: (id: number) =>
    `${BASE}/email/point-setup/${id}/enable`,

  emailRecipientsRoleMatrix: `${BASE}/email/recipients/role/matrix`,
  emailRecipientsEmployee: (employeeId: number) =>
    `${BASE}/email/recipients/employee/${employeeId}`,

  emailSent : `${BASE}/email/sent`,

  roleAssignedPoints: (roleId: number) =>
  `${BASE}/role/${roleId}/assigned-points`,

  roleNotificationUpdate: `${BASE}/role-notifications/update`,

  userExtraPoints: (userId: number) => `${BASE}/user/${userId}/extra-points`,

  userExtraRulesUpdate: `${BASE}/user/extra-rules/update`,

emailTemplate: `${BASE}/email/template`,
  emailTemplateById: (id: number) => `${BASE}/email/template/${id}`,
  emailTemplateReset: (id: number) => `${BASE}/email/template/${id}/reset`,
  emailTemplateVariable: (templateId: number) =>
    `${BASE}/email/template/${templateId}/variable`,


  emailLog: `${BASE}/email/log`,

  defect: `${BASE}/defect`,
  defectById: (defectId: number) => `${BASE}/defect/${defectId}`,
  defectByProject: (projectId: number) =>
    `${BASE}/project/${projectId}/defect`,
  defectAssignDev: `${BASE}/defect/employee`,
  defectAllocation: (id: number) => `${BASE}/defect/allocation/defect/${id}`,
  defectStatus: (defectId: number) => `${BASE}/defect/${defectId}/status`,
  defectSeverityBreakdown: (projectId: number) =>
    `${BASE}/project/${projectId}/defect/severity-breakdown`,
  defectBulkImport: `${BASE}/defect/bulk`,
  defectBulkExport: `${BASE}/defect/bulk`,

  defectComment: (defectId: number) =>
    `${BASE}/defect/${defectId}/comment`,
  defectCommentBtId: (defectId: number) =>
    `${BASE}/defect/${defectId}/comment`,

  defectStatusLog: (projectId: number, releaseId: number) =>
    `${BASE}/project/${projectId}/release/${releaseId}/defect-status-log`,
  defectByModules: (projectId: number) =>
    `${BASE}/project/${projectId}/defect-module`,
  defectByType: (projectId: number) =>
    `${BASE}/project/${projectId}/defect-type`,
  defectCountByCreated: (projectId: number, releaseId: number) =>
    `${BASE}/project/${projectId}/release/${releaseId}/defect-created/count`,
  defectFixedCount: (projectId: number, releaseId: number) =>
    `${BASE}/project/${projectId}/release/${releaseId}/defect-fixed/count`,
  defectDensity: (projectId: number) =>
    `${BASE}/project/${projectId}/defect-density`,

  dashboard: (projectId: number, releaseId: number) =>
    `${BASE}/project/${projectId}/release/${releaseId}/dashboard`,
  dashboardReopenedByProject: (projectId: number) =>
  `${BASE}/project/${projectId}/dashboard/reopened`,

dashboardTimeToFind: (projectId: number, releaseId: number) =>
  `${BASE}/project/${projectId}/release/${releaseId}/dashboard/time-to-find`,
dashboardTimeToFix: (projectId: number, releaseId: number) =>
  `${BASE}/project/${projectId}/release/${releaseId}/dashboard/time-to-fixed`,
  benchEmployee: `${BASE}/bench`,
  
 ALLOCATE_MODULE_LEADER :`${BASE}/allocate-module-leader`,
DEALLOCATE_MODULE_LEADER :(allocateModuleId: number) => `${BASE}/allocate-module-leader/${allocateModuleId}`,
GET_MODULE_ALLOCATED_LEADER : (moduleId: number) => `${BASE}/module/${moduleId}/allocated-leader`,
GET_PROJECT_ALLOCATED_EMPLOYEES : (projectId: number) => `${BASE}/project-allocation/${projectId}/employees`,

currentUserPermissions: `${BASE}/user/me/permissions`,
  currentUserProjects: `${BASE}/user/me/projects`,
  currentUserProjectPermissions: (projectId: number) => `${BASE}/user/me/projects/${projectId}/permissions`,
  DEFECT_BULK_REASSIGN: `${BASE}/defects/bulk-reassign`
};