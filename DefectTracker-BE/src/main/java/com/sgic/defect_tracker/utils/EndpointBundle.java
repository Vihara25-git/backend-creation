package com.sgic.defect_tracker.utils;

public final class EndpointBundle {

    private EndpointBundle() {}

    public static final String BASE_URL   = "/api/v1";
    public static final String ID         = "/{id}";
    public static final String SEARCH     = "/search";

    //Defettype endpoint
    public static final String defect= BASE_URL+ "/defecttype";

    public static final String create= "/create";

    public static final String get= "/getAll";

    public static final String delete= "/delete/{id}";

    public static final String update= "/update/{defectTypeId}";

    //designation
    public static final String Designation = BASE_URL+ "/Designation";

    public static final String createdesignation =  "/createdesignation";

    public static final String getById= "/get/{designationId}";

    public static final String updatedesignationId= "/update/{designationId}";
    public static final String  deletedesignationId = "/delete/{designationId}";





    //statustype
    public static final String STATUS_TYPE= BASE_URL+ "/status-type";
    public static final String STATUS_TYPEID = "/{id}";


    // ---- Employee module ----

    public static final String Employee = BASE_URL + "/Employee";
    public static final String Employee_GET = "view";
    public static final String Employee_CREATE = "/createEmployee";
    public static final String Employee_UPDATE = "update/{empId}";
    public static final String Employee_DELETE = "DeleteEmployee/{tempId}";
    public static final String Employee_STATUS = "/status/{empId}";
    public static final String Employee_BY_DESIGNATION = "/designation/{designationId}";

    public static final String Employee_ID = "/{id}";
    public static final String Employee_SEARCH = "/search";
    public static final String Designation_Employee = "/{designationId}/employee";


   // public static final String Designation_Delete = BASE_URL+("/Designationdelete");



    //release_type
    public static final String RELEASE_TYPE = BASE_URL + "/release_types";

    //release view
    public static final String ReleaseView = BASE_URL+"/ReleaseView";

    public static final String PROJECT = BASE_URL + "/project";

    public static final String save="/save";

    public static final String Severity=BASE_URL + "/severity";



    //Workflowcontroller
    public static final String WORKFLOW=BASE_URL+"/status/workflow";
    //public static final String CREATEWORKFLOW="/createWorkflow";
    //public static final String GETALLWORKFLOW="/getAllWorkflow";
    public static final String GETFIRSTNODE="/start";
    public static final String DELETEALLWORKFLOW = "/deleteAll";


    //Role Controller
    public static final String Role = BASE_URL +"/Role";
    public static final String CREATE = "/save";
    public static final String UPDATE = "/update";
    public static final String DELETE = "/delete";
    public static final String VIEW = "/view";

    // Priority
    public static final String PRIORITY = BASE_URL + "/priority";


    //Defect
    public static final String DEFECT = BASE_URL + "/defect";
    public static final String PUTDEFECT = "/put/{id}";
    public static final String DELETEDEFECT =  "/delete/{defectId}";
    public static final String GETDEFECT = "/get";
    public static final String GETBYPROJECTIDDEFECT = "/project/{projectId}";
    public static final String GETBY_DEFECTID_DEFECT = "/{defectId}";
    public static final String GETBY_TestCaseId_DEFECT = "testcase/{testCaseId}";
    public static final String GETBY_Assignee_DEFECT = "assignee/{assiginee}";
    public static final String GETBY_PRIORITYID_DEFECT = "priority/{priorityId}";
    public static final String GETBY_STATUSID_DEFECT = "/status/{statusTypeId}";
    public static final String GETBY_SEVERITYID_DEFECT = "/severity/{severityId}";
    public static final String GETBY_DEFECTTYPEID_DEFECT = "/defectType/{defectTypeId}";

    //defects by module - for dashboard
    public static final String GETBY_MODULE_DEFECT = "/module-summary/{projectId}";


    /// Submodule
    public  static final String MODULE_SUBMODULE = "/module";
    public static final String SUBMODULE =
            BASE_URL + "/submodule";

    public static final String SUBMODULE_MODULE =
            BASE_URL + "/module";

    public static final String CREATESUBMODULE =
            "/{moduleId}/sub-module";

    public static final String SUBMODULE_BY_MODULE =
            "/module/{moduleId}";

    public static final String SUBMODULE_BY_ID = "/{moduleId}/sub-module/{submoduleId}";
    //module
    public static final String MODULE = BASE_URL + "/project/{projectId}/module";
    public static final String UPDATEMODULE = "/{moduleId}";
    public static final String DELETEMODULE = "/{moduleId}";




   // public  static final String DeleteModule= "/DeleteModule";

    // Bench Allocation
  //  public static final String BENCH_ALLOCATION = BASE_URL + "/bench";

    // Bench Allocation
    public static final String BENCH_ALLOCATION = BASE_URL + "/bench-allocation";
    public static final String BENCH_ALLOCATION_UPDATE = "/{id}";
    public static final String BENCH_ALLOCATION_DEALLOCATE = "/{id}/deallocate";
    public static final String BENCH_GET_BY_PROJECTID="/{projectId}/project";
    public static final String BENCH_ALLOCATION_HISTORY_GET_BY_PROJECTID="/{projectId}/employee-history";


    public static final String BENCH_FILTER = "/filter/{projectId}";
    // BenchAvailabilityViewController
    public static final String BENCH=  BASE_URL +"/bench-availability-view";
    public static final String BENCH_EMPLOYEE= "/employee/{empId}";
    public static final String BENCH_GET_BY_ID = "/{benchAllocationId}";
    public static final String BENCH_AVAILABILITY=  BASE_URL +"/bench-availability-view";


    // TEST CASE
    public static final String TEST_CASE =
           BASE_URL + "/project/{projectId}";
    public static final String TEST_CASE_ALL =
            "/module/{moduleId}/submodule/{submoduleId}/testcase";

    public static final String TEST_CASE_BY_ID =
            "/{testCaseId}";


  public static final String EMAIL_TEMPLATE = BASE_URL+"/email-templates";
//"/api/v1/email-templates"
    public static final String EMAIL_TEMPLATE_ID ="/{id}";


    public static final String ROLE_BASED_PREFERENCES = BASE_URL+"role-preferences";
    //"/api/v1/role-preferences"

    public static final String ROLE_BASED_PREFERENCES_ID= "/{id}";

    public static final String ROLE_BASED_PREFERENCES_ROLEID=  "/role/{roleId}";

    public static final String ROLE_ASSIGNED_POINTS =
            BASE_URL + "/role/{roleId}/assigned-points";



    public static final String ROLE_NOTIFICATIONS_UPDATE =
            BASE_URL + "/role-notifications/update";
    public static final String USER_BASED_PREFERENCES =
            BASE_URL + "/user-based-preferences";

    public static final String USER_BASED_PREFERENCES_ID =
            "/{id}";

    public static final String USER_NOTIFICATION_PREFERENCES =
            BASE_URL + "/user/{userId}/notification-preferences";

    public static final String USER_NOTIFICATION_PREFERENCES_UPDATE =
            BASE_URL + "/user/notification-preferences/update";


    // Release Test Case
    public static final String RELEASE_TEST_CASE =
            BASE_URL + "/release-test-case";

    public static final String RELEASE_TEST_CASE_COUNT =
            "/count";


    // Privileges Configuration (Role/User permission matrix)
    public static final String PERMISSION =
            BASE_URL + "/permission";

    public static final String ASSIGN_PERMISSION_MATRIX_ID =
            BASE_URL + "/assign-permission/matrix/{roleId}";

    public static final String EMPLOYEE_PERMISSION =
            BASE_URL + "/employee/{employeeId}/permission";

    public static final String PRIVILEGE_TEMPLATES =
            BASE_URL + "/privilege-templates";

    public static final String PRIVILEGE_TEMPLATES_ID =
            "/{id}";

    //dashboard
    public static final String dashboard = BASE_URL+"/project/{projectId}/dashboard";
    public static final String dashboardByRelease = BASE_URL + "/project/{projectId}/release/{releaseId}/dashboard";

    // Auth
    public static final String AUTH = BASE_URL + "/auth";
    public static final String CHANGE_PASSWORD = "/change-password";
    public static final String LOG_OUT = "/log-out";
    public static final String LOGOUT = "/logout";

    //forget password
    public static final String FORGET_PASSWORD = "/forget-password";
    public static final String RESET_PASSWORD ="/reset-password";
    public static final String VALIDATE_TOKEN = "/validate-reset-token";
}
