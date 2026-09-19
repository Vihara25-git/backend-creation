

import React, { useState, useEffect,useRef  } from "react";

import { useNavigate, useParams } from "react-router-dom";

import { Card, CardContent } from "../components/ui/Card";

import { Button } from "../components/ui/Button";


import { usePermission } from "../context/PermissionContext";

import {
  ChevronLeft,
  Eye,
  ChevronRight,
  FileText,
  Calendar,
  Trash2,
} from "lucide-react";

import { useApp } from "../context/AppContext";

import { Modal } from "../components/ui/Modal";



import { ProjectSelector } from "../components/ui/ProjectSelector";



import { projectReleaseCardView,getReleaseTestCaseCountsLoad  } from "../api/releaseView/ProjectReleaseCardView";

import { getAllSubmoduleAllocatedDevBySubmoduleId } from "../api/subModuleDevAlloc";
import { getDevelopersWithRolesByProjectId } from "../api/bench/projectAllocation";
import AuthService from "../services/authService";

import { Toast } from "../components/ui/Toast";

import { updateReleaseStatus } from "../api/projectAllocationHistory/updateReleaseStatus";

import { getModulesByProject } from "../api/module/getModuleByProject";

import { getSubmodulesByModule } from "../api/submodule/getSubmodulesByModule";

import { getTestCasesByFilter } from "../api/testExecution/getTestCasesByFilter";

import { ImagePicker } from "../components/ui/ImagePicker";

import {
  getExecutionStatuses as getSavedExecutionStatuses,
  setExecutionStatus as persistExecutionStatus,
  ExecutionStatus,
  updateReleaseTestCaseStatus,
  updateReleaseTestCaseStatusWithImage,
  deleteReleaseTestCase
} from "../api/testExecution/testExecution";

import { getSeverities } from "../api/severity";

import { getDefectTypes } from "../api/defectType";

import { getAllPriorities } from "../api/priority";

import { getAllDefectStatuses } from "../api/defectStatus";

import {
  getUsersByModuleSubmoduleAllocation,
} from "../api/module/getUsersByAllocation";

import {
  filterDefectsForTest,
} from "../api/defect/filterDefectByProject";

import { getDefectTestCaseCounts } from "../api/releasetestcase";
import { useAccessibleProjects } from "../api/useAccessibleProjects";



interface TestCase {
  priority: string | null | undefined;

  id: string;
  no:string;

  module: string;

  subModule: string;

  description: string;

  steps: string;

  type: "functional" | "regression" | "smoke" | "integration";

  severity: "low" | "medium" | "high" | "critical";

  projectId: string;

  releaseId?: string;

  executionStatus?:
    | "not-started"
    | "in-progress"
    | "passed"
    | "failed"
    | "blocked";

  assignee?: string;

  defectId?: string;

  assignedto?: string;

  assignedTo?: string;
}



const mockQA: any[] = [
  
];








const mockTestCases: any[] = [];
const DEV_ROLE_TYPES = [
  "DEV_LEAD",
  "SENIOR_DEVELOPER",
  "DEVELOPER",
  "JUNIOR_DEVELOPER",
];

const mockReleases: any[] = [];

export const TestExecution: React.FC = () => {
  const { projectId, releaseId } = useParams();

  const navigate = useNavigate();

  const {

    releases,

    testCases,

    setSelectedProjectId,

    addDefect,

    testCaseDefectMap,

    setTestCaseDefectMap,

    defects,

    modulesByProject,
  } = useApp();

 const { projects, switchProject } = useAccessibleProjects();

  const [selectedProject, setSelectedProject] = useState<string | null>(
    projectId || null,
  );

  const [selectedRelease, setSelectedRelease] = useState<string | null>(
    releaseId || null,
  );
   const [defectAllocatedUsers, setDefectAllocatedUsers] = useState<
    { userId: number; userName: string; empId: number }[]
  >([]);
  const [defectAllocatedUsersLoading, setDefectAllocatedUsersLoading] = useState(false);

  const [selectedModule, setSelectedModule] = useState("");

  const [selectedSubmodule, setSelectedSubmodule] = useState("");

  const [isViewStepsModalOpen, setIsViewStepsModalOpen] = useState(false);

  const [isViewTestCaseModalOpen, setIsViewTestCaseModalOpen] = useState(false);

  const [viewingTestCase, setViewingTestCase] = useState<TestCase | null>(null);
  console.log({ viewingTestCase });

  const [executionStatuses, setExecutionStatuses] = useState<{
    [key: string]: TestCase["executionStatus"];
  }>({});

  const [defectModalOpen, setDefectModalOpen] = useState<string | null>(null);

  const {can} = usePermission();

  const [defectFormData, setDefectFormData] = useState({
    title: "",

    description: "",

    module: "",

    subModule: "",

    type: "",

    priority: "",

    severity: "",

    status: "",

    projectId: projectId || "",

    releaseId: releaseId || "",

    testCaseId: "",

    assignedTo: 0,

    reportedBy: "",

    rejectionComment: "",
    attachment: "",       
    attachmentFile: null as File | null,
  });

  const [releaseLoading, setReleaseLoading] = useState(false);

  const [releaseError, setReleaseError] = useState("");

  const [projectReleaseCard, setProjectReleaseCard] = useState<any[]>([]);

  const [toast, setToast] = useState<{
  isOpen: boolean;
  message: string;
  type: "success" | "error";     
}>({
  isOpen: false,
  message: "",
  type: "success",               
});
  

  const [modules, setModules] = useState<any[]>([]);

  const [modulesLoading, setModulesLoading] = useState(false);

  const [modulesError, setModulesError] = useState("");

  // Add submodule state

  const [submodules, setSubmodules] = useState<any[]>([]);

  const [submodulesLoading, setSubmodulesLoading] = useState(false);

  const [submodulesError, setSubmodulesError] = useState("");

  // Add state for release test case counts

  const [releaseTestCaseCounts, setReleaseTestCaseCounts] = useState<{
    [key: string]: number;
  }>({});

  const [releaseTestCaseCountsLoading, setReleaseTestCaseCountsLoading] =
    useState(false);

  // Add severities state

  const [severities, setSeverities] = useState<any[]>([]);

  // Add defect types state

  const [defectTypes, setDefectTypes] = useState<
    { id: number; defectTypeName: string }[]
  >([]);

  // Add priorities state

  const [priorities, setPriorities] = useState<
    { id: number; name: string; color: string }[]
  >([]);

  // Add defect statuses state

  const [defectStatuses, setDefectStatuses] = useState<
    { id: number; defectStatusName: string; colorCode: string }[]
  >([]);

  // Add allocated users state

  const [allocatedUsers, setAllocatedUsers] = useState<
    {
      userId: number;
      userName: string;
      userRole?: string;
      userWithRole: string;
    }[]
  >([]);

  const [allocatedUsersLoading, setAllocatedUsersLoading] = useState(false);

  // Add defect submission state

  const [defectSubmitting, setDefectSubmitting] = useState(false);

  // Add state for existing defects

  const [existingDefects, setExistingDefects] = useState<any[]>([]);

  const [defectsLoading, setDefectsLoading] = useState(false);

  // Add state to track test case that needs status update after defect creation

  const [pendingStatusUpdate, setPendingStatusUpdate] = useState<{
    testCase: any;

    status: TestCase["executionStatus"];
  } | null>(null);

  

  const [previousStatusBeforeFail, setPreviousStatusBeforeFail] = useState<{
    testCaseId: string;

    previousStatus: TestCase["executionStatus"];
  } | null>(null);

  

  const [editingDefect, setEditingDefect] = useState<any | null>(null);

  const [isEditingDefect, setIsEditingDefect] = useState(false);
  const currentUser = AuthService.getCurrentUser();
  const currentUserId = currentUser?.userId || currentUser?.employeeId || null;
  console.log("Current User ID:", currentUserId); 

  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [testCaseToDelete, setTestCaseToDelete] = useState<any>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const canUserExecute = (testCase: any): boolean => {
    if (!currentUserId) {
      return false;
    }
    const role = (currentUser?.role || currentUser?.roleName || "").toUpperCase();
    if (role === "ADMIN" || role === "QA_LEAD" || can.testCase.edit) {
      return true;
    }
    if (!testCase.assignedToId) {
      return true;
    }
    return Number(currentUserId) === Number(testCase.assignedToId);
  };

  const handleDeleteReleaseTestCase = async () => {
    if (!testCaseToDelete || !selectedRelease) return;
    setDeleteLoading(true);
    try {
      const releaseTestCaseId = testCaseToDelete.backendId || testCaseToDelete.releaseTestCaseId || testCaseToDelete.id;
      await deleteReleaseTestCase(selectedRelease, releaseTestCaseId);
      setToast({
        isOpen: true,
        message: "Test case removed from release execution successfully!",
        type: "success",
      });
      setDeleteConfirmOpen(false);
      setTestCaseToDelete(null);

      setFilteredTestCases((prev) =>
        prev.filter((t) => t.id !== testCaseToDelete.id && t.backendId !== releaseTestCaseId)
      );

      if (selectedProject && selectedRelease) {
        getReleaseTestCaseCountsLoad([parseInt(selectedRelease)]);
      }
    } catch (err: any) {
      setToast({
        isOpen: true,
        message: err?.response?.data?.message || err?.message || "Failed to remove test case",
        type: "error",
      });
    } finally {
      setDeleteLoading(false);
    }
  };

  

  useEffect(() => {
    console.log("Pending status update changed:", pendingStatusUpdate);
  }, [pendingStatusUpdate]);

  

  const [defectAssignments, setDefectAssignments] = useState<{
    [key: string]: string;
  }>({});

  

  const allocatedTestCasesMap = JSON.parse(
    localStorage.getItem("qaAllocatedTestCases") || "{}",
  );

  const qaAllocationsMap = JSON.parse(
    localStorage.getItem("qaAllocations") || "{}",
  );
  
  const submoduleHandledRef = useRef(false);
 const skipEffectUntilRef = useRef<number>(0);

  

  const [localTestCaseDefectMap, setLocalTestCaseDefectMap] = useState<{
    [key: string]: string;
  }>({});

  

  function getAssignedQAForRelease(testCaseId: string) {
    const allocations = qaAllocationsMap[selectedRelease || ""] || {};

    for (const [qaId, ids] of Object.entries(allocations)) {
      if ((ids as string[]).includes(testCaseId)) {
        // Find the QA name from the QA ID

        const qa = effectiveQA && effectiveQA.find((q: any) => q.id === qaId);

        return qa ? qa.name : qaId;
      }
    }

    return null;
  }

  // Helper to get assigned user for a defect

  function getAssignedUserForDefect(defectId: string) {
    return defectAssignments[defectId] || null;
  }

  // Fallback: try to find a matching defect for a test case when there is no direct mapping

  // Match by module, submodule and description coming from backend fields

  function normalize(text: string) {
    return (text || "")

      .toString()

      .toLowerCase()

      .replace(/\s+/g, " ")

      .trim();
  }

  // Helper function to extract field values with fallbacks for different naming conventions

  function extractFieldValue(obj: any, fieldNames: string[]): string {
    for (const fieldName of fieldNames) {
      if (
        obj[fieldName] !== undefined &&
        obj[fieldName] !== null &&
        obj[fieldName] !== ""
      ) {
        return obj[fieldName];
      }
    }

    return "";
  }

  function findMatchingDefectForTestCase(testCase: any) {
    if (!existingDefects || existingDefects.length === 0 || !testCase)
      return null;

    // Use helper function to extract field values with fallbacks

    const tcModule = normalize(
      extractFieldValue(testCase, ["module", "moduleName", "module_name"]),
    );

    const tcSubmodule = normalize(
      extractFieldValue(testCase, [
        "subModule",
        "subModuleName",
        "sub_module_name",
      ]),
    );

    const tcDesc = normalize(
      extractFieldValue(testCase, [
        "description",
        "release_test_case_description",
      ]),
    );

    console.log("Looking for defect match for test case:", {
      id: testCase.id,

      module: tcModule,

      submodule: tcSubmodule,

      description: tcDesc,

      rawTestCase: testCase,
    });

    const match = existingDefects.find((defect: any) => {
      

      const dModule = normalize(
        extractFieldValue(defect, ["module_name", "moduleName", "module"]),
      );

      const dSubmodule = normalize(
        extractFieldValue(defect, [
          "sub_module_name",
          "subModuleName",
          "subModule",
        ]),
      );

      const dDesc = normalize(
        extractFieldValue(defect, [
          "release_test_case_description",
          "description",
          "steps",
        ]),
      );

      

      const descMatches =
        dDesc === tcDesc ||
        (dDesc &&
          tcDesc &&
          (dDesc.includes(tcDesc) ||
            tcDesc.includes(dDesc) ||
            dDesc.toLowerCase().includes(tcDesc.toLowerCase()) ||
            tcDesc.toLowerCase().includes(dDesc.toLowerCase())));

      const moduleMatch = dModule === tcModule;

      const submoduleMatch = dSubmodule === tcSubmodule;

      console.log("Defect match attempt:", {
        defectId: defect.defectId || defect.id,

        dModule,
        dSubmodule,
        dDesc,

        moduleMatch,
        submoduleMatch,
        descMatches,

        rawDefect: defect,
      });

      return moduleMatch && submoduleMatch && descMatches;
    });

    if (match) {
      console.log("Found matching defect:", match.defectId || match.id);
    } else {
      console.log("No matching defect found for test case:", testCase.id);
    }

    return match || null;
  }

  

  const safeProjectId = selectedProject || projectId || "";

  // Fetch modules when project changes

  useEffect(() => {
    if (selectedProject) {
      setModulesLoading(true);

      setModulesError("");

      getModulesByProject(Number(selectedProject))
        .then((data) => setModules(data.data))

        .catch((err) => {
          setModulesError(err.message);

          setModules([]); // Do not fallback to mock data
        })

        .finally(() => setModulesLoading(false));
    } else {
      setModules([]);
    }
  }, [selectedProject]);

  const effectiveTestCases = testCases || [];
  const effectiveQA: any[] = [];

  

  const testCaseIdToQA: Record<string, string> = {};

  if (qaAllocationsMap && Array.isArray(qaAllocationsMap.allocations)) {
    qaAllocationsMap.allocations.forEach((alloc: any) => {
      alloc.testCaseIds.forEach((tcId: string) => {
        const qa =
          effectiveQA &&
          effectiveQA.find(
            (q: any) => q.id === alloc.qaId || q.name === alloc.qaName,
          );

        testCaseIdToQA[tcId] = qa ? qa.name : alloc.qaName || alloc.qaId;
      });
    });
  }

  const [filteredTestCases, setFilteredTestCases] = useState<any[]>([]);

  

  

 
  

  useEffect(() => {
    if (selectedProject && selectedRelease) {
      const saved = getSavedExecutionStatuses(selectedProject, selectedRelease);

      setExecutionStatuses(
        saved as Record<string, TestCase["executionStatus"]>,
      );
    } else {
      setExecutionStatuses({});
    }
  }, [selectedProject, selectedRelease]);

  

  const [moduleTestCaseCounts, setModuleTestCaseCounts] = useState<{
    [key: string]: number;
  }>({});

  const [submoduleTestCaseCounts, setSubmoduleTestCaseCounts] = useState<{
    [key: string]: number;
  }>({});

  const [countsLoading, setCountsLoading] = useState(false);

  

  const [testCasesLoading, setTestCasesLoading] = useState(false);

  

  useEffect(() => {
    const fetchModuleTestCaseCounts = async () => {
      if (selectedProject && selectedRelease && modules.length > 0) {
        setCountsLoading(true);

        const counts: { [key: string]: number } = {};

        try {
          

          for (const module of modules) {
            const testCases = await getTestCasesByFilter({
              projectId: parseInt(selectedProject),

              releaseId: parseInt(selectedRelease),

              moduleId: module.id,
            });

            console.log(`Module ${module.moduleName} test cases:`, testCases);

            counts[module.moduleName || module.name] = testCases.length;
          }

          setModuleTestCaseCounts(counts);
        } catch (err: any) {
          console.error("Failed to fetch module test case counts:", err);

          setModuleTestCaseCounts({});
        } finally {
          setCountsLoading(false);
        }
      }
    };

    fetchModuleTestCaseCounts();
  }, [selectedProject, selectedRelease, modules]);

  

  useEffect(() => {
    const fetchSubmoduleTestCaseCounts = async () => {
      if (
        selectedProject &&
        selectedRelease &&
        selectedModule &&
        submodules.length > 0
      ) {
        const counts: { [key: string]: number } = {};

        try {
          

          for (const submodule of submodules) {
            const testCases = await getTestCasesByFilter({
              projectId: parseInt(selectedProject),

              releaseId: parseInt(selectedRelease),

             moduleId: modules.find((m: any) => m.moduleName === selectedModule || m.name === selectedModule)?.id,

              subModuleId: submodule.id,
            });

            console.log(
              `Submodule ${submodule.subModuleName || submodule.name} test cases:`,
              testCases,
            );

            counts[submodule.subModuleName || submodule.name] =
              testCases.length;
          }

          setSubmoduleTestCaseCounts(counts);
        } catch (err: any) {
          console.error("Failed to fetch submodule test case counts:", err);

          setSubmoduleTestCaseCounts({});
        }
      }
    };

    fetchSubmoduleTestCaseCounts();
  }, [selectedProject, selectedRelease, selectedModule, submodules, modules]);

  useEffect(() => {
    if (selectedProject) {
      setSelectedProjectId(selectedProject);
    }
  }, [selectedProject, setSelectedProjectId]);

useEffect(() => {
  if (!selectedProject) return;

  const fetchProjectReleasesAndCounts = () => {
    setReleaseLoading(true);
    setReleaseError("");

    projectReleaseCardView(selectedProject)
      .then((releasesRes) => {
        if (releasesRes.status === "Success" || releasesRes.statusCode === "200" || releasesRes.status === "success") {
          const releaseList = releasesRes.data || [];
          const filtered = releaseList.filter(
            (r: any) =>
              String(r.project_id) === String(selectedProject) ||
              String(r.projectId) === String(selectedProject)
          );
          setProjectReleaseCard(filtered);

          const releaseIds = filtered
            .map((r: any) => r.id || r.releaseId)
            .filter((id: any) => id != null)
            .map(Number);

          if (releaseIds.length > 0) {
            getReleaseTestCaseCountsLoad(releaseIds)
              .then((countsRes) => {
                if (countsRes.status === "Success" || countsRes.statusCode === 200 || countsRes.status === "success") {
                  const countsMap: { [key: string]: number } = {};
                  const data = countsRes.data;
                  if (Array.isArray(data)) {
                    data.forEach((item: any) => {
                      const releaseId = item?.releaseId ?? item?.id;
                      const count = typeof item?.testCaseCount === "number" ? item.testCaseCount : (typeof item?.total === "number" ? item.total : 0);
                      if (releaseId != null) {
                        countsMap[String(releaseId)] = count;
                        countsMap[Number(releaseId) as any] = count;
                      }
                    });
                  } else if (data && typeof data === "object") {
                    Object.entries(data).forEach(([key, val]: [string, any]) => {
                      const count = typeof val === "number" ? val : (typeof val?.testCaseCount === "number" ? val.testCaseCount : (typeof val?.total === "number" ? val.total : 0));
                      countsMap[key] = count;
                      countsMap[Number(key) as any] = count;
                    });
                  }
                  setReleaseTestCaseCounts(countsMap);
                } else {
                  setReleaseTestCaseCounts({});
                }
              })
              .catch(() => {
                console.warn("Failed to fetch test case counts");
                setReleaseTestCaseCounts({});
              })
              .finally(() => setReleaseLoading(false));
          } else {
            setReleaseTestCaseCounts({});
            setReleaseLoading(false);
          }
        } else {
          setReleaseError(releasesRes.message || "No releases found");
          setProjectReleaseCard([]);
          setReleaseTestCaseCounts({});
          setReleaseLoading(false); 
        }
      })
      .catch((error) => {
        console.error("Error fetching releases:", error);
        setReleaseError("Failed to fetch releases. Please try again.");
        setReleaseTestCaseCounts({});
        setReleaseLoading(false); 
      })
      .finally(() => setReleaseLoading(false));
  };

  fetchProjectReleasesAndCounts();

  const handleRefreshEvents = () => {
    fetchProjectReleasesAndCounts();
  };

  window.addEventListener("testCaseCreated", handleRefreshEvents);
  window.addEventListener("testCaseAllocated", handleRefreshEvents);
  window.addEventListener("releaseTestCaseUpdated", handleRefreshEvents);
  window.addEventListener("storage", handleRefreshEvents);
  window.addEventListener("focus", handleRefreshEvents);

  return () => {
    window.removeEventListener("testCaseCreated", handleRefreshEvents);
    window.removeEventListener("testCaseAllocated", handleRefreshEvents);
    window.removeEventListener("releaseTestCaseUpdated", handleRefreshEvents);
    window.removeEventListener("storage", handleRefreshEvents);
    window.removeEventListener("focus", handleRefreshEvents);
  };
}, [selectedProject]);
  useEffect(() => {
    getSeverities()
      .then((res) => setSeverities(res.data.content))

      .catch((error) => {
        console.error("Failed to fetch severities:", error.message);

        setSeverities([]);
      });
  }, []);

  

  useEffect(() => {
    getDefectTypes()
      .then((res) => {
        console.log("Fetched defect types:", res.data);

        setDefectTypes(res.data.content);
      })

      .catch((error) => {
        console.error("Failed to fetch defect types:", error.message);

        setDefectTypes([]);
      });
  }, []);

  

  useEffect(() => {
    getAllPriorities()
      .then((res) => {
        console.log("Fetched priorities:", res.data.content);
        setPriorities(res.data.content);
      })

      .catch((error) => {
        console.error("Failed to fetch priorities:", error.message);

        setPriorities([]);
      });
  }, []);

  

  useEffect(() => {
    getAllDefectStatuses()
      .then((res) => {
        console.log("Fetched defect statuses:", res.data);

        setDefectStatuses(res.data);
      })

      .catch((error) => {
        console.error("Failed to fetch defect statuses:", error.message);

        setDefectStatuses([]);
      });
  }, []);


useEffect(() => {
  if (!selectedProject) {
    setAllocatedUsers([]);
    return;
  }

  setAllocatedUsersLoading(true);

  getUsersByModuleSubmoduleAllocation(parseInt(selectedProject))
    .then((response: any) => {
      console.log("response:", JSON.stringify(response, null, 2));

      let dataArray: any[] = [];

      if (Array.isArray(response)) {
        dataArray = response;
      } else if (Array.isArray(response?.data)) {
        dataArray = response.data;
      } else if (Array.isArray(response?.data?.data)) {
        dataArray = response.data.data;
      } else if (Array.isArray(response?.content)) {
        dataArray = response.content;
      }

      console.log("dataArray length:", dataArray.length);
      console.log("First item:", dataArray[0]);

      const filtered = dataArray.filter((emp: any) => {
        const roleType = (emp.roleType || "").toString().toUpperCase().trim();
        console.log(`${emp.firstName} → roleType: "${roleType}" → included: ${DEV_ROLE_TYPES.includes(roleType)}`);
        return DEV_ROLE_TYPES.includes(roleType);
      });

      console.log("filtered count:", filtered.length);

      const mapped = filtered.map((emp: any) => ({
        userId: emp.employeeId || emp.userId || emp.id,
        userName:
          emp.firstName && emp.lastName
            ? `${emp.firstName} ${emp.lastName}`.trim()
            : emp.userName || emp.name || "Unknown User",
        userRole: emp.roleName || emp.role || "",
        userWithRole: emp.employeeName || emp.userName || emp.name || "",
      }));

      setAllocatedUsers(mapped);
    })
    .catch((error: any) => {
      console.error("Failed to fetch developers:", error.message);
      setAllocatedUsers([]);
    })
    .finally(() => {
      setAllocatedUsersLoading(false);
    });
}, [selectedProject]);
  useEffect(() => {
    const savedMapping = localStorage.getItem("testCaseDefectMapping");

    if (savedMapping) {
      try {
        const parsedMapping = JSON.parse(savedMapping);

        console.log(
          "Loaded test case defect mapping from localStorage:",
          parsedMapping,
        );

        setLocalTestCaseDefectMap(parsedMapping);

        setTestCaseDefectMap(parsedMapping);
      } catch (error) {
        console.error(
          "Failed to parse test case defect mapping from localStorage:",
          error,
        );
      }
    }

    

    const savedAssignments = localStorage.getItem("defectAssignments");

    if (savedAssignments) {
      try {
        const parsedAssignments = JSON.parse(savedAssignments);

        console.log(
          "Loaded defect assignments from localStorage:",
          parsedAssignments,
        );

        setDefectAssignments(parsedAssignments);
      } catch (error) {
        console.error(
          "Failed to parse defect assignments from localStorage:",
          error,
        );
      }
    }
  }, []);

  

  useEffect(() => {
    if (Object.keys(testCaseDefectMap).length > 0) {
      console.log("Global test case defect map updated:", testCaseDefectMap);

      setLocalTestCaseDefectMap(testCaseDefectMap);

      

      localStorage.setItem(
        "testCaseDefectMapping",
        JSON.stringify(testCaseDefectMap),
      );
    }
  }, [testCaseDefectMap]);

  

  useEffect(() => {
    return () => {
      

      

      setLocalTestCaseDefectMap({});

      setDefectAssignments({});

      setPendingStatusUpdate(null);

      console.log("Clearing pending status update - component unmounting");
    };
  }, []);

  

  useEffect(() => {
    const fetchExistingDefects = async () => {
      if (selectedProject) {
        setDefectsLoading(true);

        try {
          console.log(
            "Fetching existing defects for project:",
            selectedProject,
          );

          

          const defects = await filterDefectsForTest({
            projectId: String(selectedProject),
            releaseId: selectedRelease ? parseInt(selectedRelease) : undefined,
          });

          console.log("Fetched existing defects:", defects);

          console.log("First defect full object:", defects[0]);

          

          defects.forEach((defect: any, index: number) => {
            console.log(`Defect ${index + 1}:`, {
              id: defect.id,

              defectId: defect.defectId,

              description: defect.description,

              moduleName: defect.moduleName,

              module_name: defect.module_name,

              subModuleName: defect.subModuleName,

              sub_module_name: defect.sub_module_name,

              assignedToName: defect.assignedToName,

              assigned_to_name: defect.assigned_to_name,

              testCaseId: defect.testCaseId,

              ReleaseTestCaseId: defect.ReleaseTestCaseId,

              releaseTestCaseId: defect.releaseTestCaseId,
            });
          });

          setExistingDefects(Array.isArray(defects) ? defects : []);
          console.log('wawa',defects);
          

          

          const extractReleaseTestCaseIdFromDefect = (defect: any) =>
            defect.testCaseId ||
            defect.ReleaseTestCaseId ||
            defect.releaseTestCaseId ||
            defect.ReleaseTestCaseID ||
            defect.release_testcase_id ||
            defect.release_test_case_id ||
            defect.test_case_id;

          

          const defectMappings: { [key: string]: string } = {};

          const defectUserAssignments: { [key: string]: string } = {};

          defects.forEach((defect: any) => {
            

            let testCaseId =
              defect.testCaseId ||
              defect.ReleaseTestCaseId ||
              defect.releaseTestCaseId;

            console.log("Processing defect:", {
              defectId: defect.defectId,

              testCaseId: defect.testCaseId,

              moduleName: defect.moduleName,

              subModuleName: defect.subModuleName,

              description: defect.description,

              assignedToName: defect.assignedToName,
            });

            

            if (
              !testCaseId &&
              (defect.module_name || defect.moduleName) &&
              (defect.sub_module_name || defect.subModuleName)
            ) {
              

              const matchingTestCase = filteredTestCases.find((tc: any) => {
                const tcModule = tc.module || tc.moduleName;

                const tcSubmodule = tc.subModule || tc.subModuleName;

                const dModule = defect.module_name || defect.moduleName;

                const dSub = defect.sub_module_name || defect.subModuleName;

                const dDesc =
                  defect.description || defect.release_test_case_description;

                const moduleMatch = tcModule === dModule;

                const submoduleMatch = tcSubmodule === dSub;

                const descMatch =
                  tc.description === dDesc ||
                  tc.description === defect.release_test_case_description ||
                  (dDesc &&
                    tc.description &&
                    (dDesc
                      .toLowerCase()
                      .includes(tc.description.toLowerCase()) ||
                      tc.description
                        .toLowerCase()
                        .includes(dDesc.toLowerCase())));

                console.log("Matching attempt:", {
                  tcModule,
                  tcSubmodule,
                  tcDescription: tc.description,

                  dModule,
                  dSub,
                  dDesc,

                  moduleMatch,
                  submoduleMatch,
                  descMatch,
                });

                return moduleMatch && submoduleMatch && descMatch;
              });

              if (matchingTestCase) {
                testCaseId = matchingTestCase.id;

                console.log("Found matching test case:", matchingTestCase.id);
              }
            }

            if (testCaseId) {
              

              const defectId =defect.defectNo ||`DEF-${defect.id.toString().padStart(4, "0")}`;

              defectMappings[testCaseId] = defectId;

              

              const assignedUser =
                defect.assigned_to_name || defect.assignedToName;

              if (assignedUser) {
                defectUserAssignments[defectId] = assignedUser;
              }
            } else {
              console.log(
                `Could not map defect ${defect.defectId} - no test case match found`,
              );
            }
          });

          

          if (Object.keys(defectMappings).length > 0) {
            console.log(
              "Merging defect mappings from backend:",
              defectMappings,
            );

            setTestCaseDefectMap((prev: any) => {
              const merged = { ...(prev || {}), ...defectMappings };

              setLocalTestCaseDefectMap(merged);

              localStorage.setItem(
                "testCaseDefectMapping",
                JSON.stringify(merged),
              );

              return merged;
            });
          }

          

          console.log(
            "Current defect assignments before merge:",
            defectAssignments,
          );

          console.log(
            "Backend defect assignments to merge:",
            defectUserAssignments,
          );

          setDefectAssignments((prev) => {
            

            const merged = { ...(prev || {}), ...defectUserAssignments };

            localStorage.setItem("defectAssignments", JSON.stringify(merged));

            console.log("Final merged defect assignments:", merged);

            return merged;
          });
        } catch (error: any) {
          

          setExistingDefects([]);
        } finally {
          setDefectsLoading(false);
        }
      } else {
        setExistingDefects([]);
      }
    };

    fetchExistingDefects();
  }, [selectedProject, selectedRelease, filteredTestCases]);

  

  useEffect(() => {
    if (selectedProject && selectedRelease && filteredTestCases.length > 0) {
      

      const timer = setTimeout(() => {
        refreshDefectData(false); 
      }, 500);

      return () => clearTimeout(timer);
    }
  }, [selectedProject, selectedRelease, filteredTestCases.length]);

  

  useEffect(() => {
    if (selectedProject && selectedRelease) {
      

      const interval = setInterval(() => {
        if (filteredTestCases.length > 0) {
          refreshDefectData(false); 
        }
      }, 30000); 

      return () => clearInterval(interval);
    }
  }, [selectedProject, selectedRelease, filteredTestCases.length]);

  

 const safeReleases = Array.isArray(projectReleaseCard) ? projectReleaseCard : [];

  const projectReleases = safeReleases.filter(
  (r: any) =>
    String(r.project_id) === String(selectedProject) ||
    String(r.projectId) === String(selectedProject)
);
  

  

  const handleProjectSelect = (projectId: string) => {
    setSelectedProject(projectId);

    setSelectedProjectId(projectId); 

    setSelectedRelease(null);

    setSelectedModule("");

    setSelectedSubmodule("");
    setSubmodules([]);                    
    setSubmoduleTestCaseCounts({});       
    setModuleTestCaseCounts({});          
    setFilteredTestCases([]);    

    // Clear test case defect mapping when changing projects

    // (defects from different projects shouldn't be mixed)

    setTestCaseDefectMap({});

    setLocalTestCaseDefectMap({});

    setDefectAssignments({});

    setPendingStatusUpdate(null); // Clear pending status update

    console.log("Clearing pending status update - project changed");

    localStorage.removeItem("testCaseDefectMapping");

    localStorage.removeItem("defectAssignments");
  };

  

  const handleReleaseSelect = (releaseId: string) => {
    setSelectedRelease(releaseId);

    setSelectedModule("");

    setSelectedSubmodule("");

    setSubmodules([]);                    
    setSubmoduleTestCaseCounts({});       
    setModuleTestCaseCounts({});         
    setFilteredTestCases([]);    
    // Clear test case defect mapping when changing releases

    // (defects from different releases shouldn't be mixed)

    setTestCaseDefectMap({});

    setLocalTestCaseDefectMap({});

    setDefectAssignments({});

    setPendingStatusUpdate(null); // Clear pending status update

    console.log("Clearing pending status update - release changed");

    localStorage.removeItem("testCaseDefectMapping");

    localStorage.removeItem("defectAssignments");
  };

  

  const handleModuleSelect = async (moduleName: string) => {
    skipEffectUntilRef.current = Date.now() + 5000;
    setSelectedModule(moduleName);
    
    setSelectedSubmodule("");

    setSubmodules([]);

    setSubmodulesLoading(true);

    setSubmodulesError("");

    setFilteredTestCases([]);
    setExecutionStatuses({});

    try {
      const moduleObj = modules.find((m: any) => m.name === moduleName);

      if (moduleObj && moduleObj.id) {
        // Fetch submodules
        const response = await getSubmodulesByModule(Number(moduleObj.id));
        const submods = response?.data;
        console.log("getSubmodulesByModule",submods);
        
        setSubmodules(Array.isArray(submods) ? submods : []);
        
        if (selectedProject && selectedRelease) {
          setTestCasesLoading(true);

          try {
            const testCases = await getTestCasesByFilter({
              projectId: parseInt(selectedProject),
              releaseId: parseInt(selectedRelease),
              moduleId: moduleObj.id,
            });

            console.log("Raw module test cases from API:", testCases);

          const mappedTestCases = testCases.map((tc: any) => {
              return {
                ...tc,
                backendId: tc.id,          
                id: tc.id?.toString(),
                testCaseId: tc.no || tc.testCaseId,
                description: tc.description || "",
                steps: tc.detailsSteps || tc.steps || "No steps provided",
                type: tc.defectTypeName || tc.type || "functional",
                severity: tc.severityName?.toLowerCase() || tc.severity || "medium",
                module: tc.moduleName || "Unknown Module",
                subModule: tc.subModuleName || "",
                executionStatus: tc.status || "",
                assignedTo: tc.assignedTo || null,
                assignedToId: tc.assignedToId || null,      
                defectId: tc.defectNo || null,          
                priority: tc.priorityName || null,      
                projectId: String(selectedProject),
                releaseId: selectedRelease,
              };
            });
           setFilteredTestCases(mappedTestCases);

          // ADD THIS BLOCK — was missing in handleModuleSelect
          const databaseStatuses: { [key: string]: TestCase["executionStatus"] } = {};
          mappedTestCases.forEach((tc: any) => {
            if (tc.executionStatus && tc.executionStatus !== "not-started") {
              databaseStatuses[tc.id] = tc.executionStatus as TestCase["executionStatus"];
            }
          });
          setExecutionStatuses(databaseStatuses);
          } catch (testCaseError: any) {
            console.error(
              "Error fetching test cases for module:",
              testCaseError,
            );
            setFilteredTestCases([]);
          } finally {
            setTestCasesLoading(false);
          }
        }
      } else {
        setSubmodules([]);

        setFilteredTestCases([]);
      }
    } catch (err: any) {
      setSubmodulesError(err.message || "Failed to fetch submodules");

      setSubmodules([]);

      setFilteredTestCases([]);
    } finally {
      setSubmodulesLoading(false);
    }
  };

  

  const handleSubmoduleSelect = async (submoduleName: string) => {
    skipEffectUntilRef.current = Date.now() + 5000;
    setSelectedSubmodule(submoduleName);

    setFilteredTestCases([]);
    setExecutionStatuses({});
    
    const moduleObj = modules.find((m: any) => m.name === selectedModule);

    const submoduleObj = submodules.find(
      (s: any) => (s.name) === submoduleName,
    );

    if (moduleObj && moduleObj.id && submoduleObj && submoduleObj.id) {
      if (selectedProject && selectedRelease) {
        const testCases = await getTestCasesByFilter({
          projectId: parseInt(selectedProject),

          releaseId: parseInt(selectedRelease),

          moduleId: moduleObj.id,

          subModuleId: submoduleObj.id,
        });

        console.log("Raw submodule test cases from API:", testCases);

        

       const mappedTestCases = testCases.map((tc: any) => {
          return {
            ...tc,
            backendId: tc.id,             
            id: tc.id?.toString(),
            testCaseId: tc.no || tc.testCaseId,
            description: tc.description || "",
            steps: tc.detailsSteps || tc.steps || "No steps provided",
            type: tc.defectTypeName || tc.type || "functional",
            severity: tc.severityName?.toLowerCase() || tc.severity || "medium",
            module: tc.moduleName  || " ",
            subModule: tc.subModuleName || "",
            executionStatus: tc.status,
            assignedTo: tc.assignedTo || null,
            assignedToId: tc.assignedToId || null,      
            defectId: tc.defectNo || null,          
            priority: tc.priorityName || null,     
            projectId: String(selectedProject),
            releaseId: selectedRelease,
          };
        });
        setFilteredTestCases(mappedTestCases);
        console.log("Mapped submodule test cases:", mappedTestCases);

        // Update execution statuses with database values, preserving localStorage values where database has no status

        const databaseStatuses: { [key: string]: TestCase["executionStatus"] } =
          {};

        mappedTestCases.forEach((tc: any) => {
          if (tc.executionStatus && tc.executionStatus !== "not-started") {
            databaseStatuses[tc.id] = tc.executionStatus;
          }
        });

       setExecutionStatuses(databaseStatuses);

      }
    }
  };

   useEffect(() => {
    
     if (Date.now() < skipEffectUntilRef.current) {
    return;
    }
      
    const fetchTestCases = async () => {
      setFilteredTestCases([]); 

      if (selectedProject && selectedRelease && selectedModule) {
        setTestCasesLoading(true);

        try {
          const moduleObj = modules.find(
            (m: any) => m.name === selectedModule,
          );
          const submoduleObj = selectedSubmodule
            ? submodules.find(
                (s: any) => ( s.name) === selectedSubmodule,
              )
            : null;

          const testCases = await getTestCasesByFilter({
            projectId: parseInt(selectedProject),

            releaseId: parseInt(selectedRelease),

            moduleId: moduleObj?.id,

            subModuleId: submoduleObj?.id,
          });

          console.log("Raw test cases from API:", testCases);

          

          const enhancedTestCases = testCases.map((tc: any) => {
            

            const mapBackendStatusToFrontend = (
              backendStatus: string,
            ): TestCase["executionStatus"] => {
              if (!backendStatus) return "not-started";
              switch (backendStatus.toUpperCase()) {
                    case "PASS":
                    case "PASSED":
                      return "passed";
                    case "FAIL":
                    case "FAILED":
                      return "failed";
                    case "IN_PROGRESS":
                    case "IN-PROGRESS":
                      return "in-progress";
                    case "BLOCKED":
                      return "blocked";
                    default:
                      return "not-started";
                  }
            };

            

            const backendStatus =
              tc.testCaseStatus ||
              tc.status ||
              tc.executionStatus ||
              tc.execution_status ||
              tc.test_case_status ||
              tc.releaseTestCaseStatus ||
              tc.release_test_case_status;

            return {
              ...tc,
              backendId: tc.id,
              id: tc.id?.toString(),
              module: tc.moduleName ||selectedModule || "Unknown Module",
              subModule:tc.subModuleName || selectedSubmodule || "Unknown Submodule",
              description: tc.description ||"",
              steps: tc.detailsSteps || tc.steps || "No steps provided",
              type: tc.defectTypeName || " ",
              severity: tc.severityName || " ",
              executionStatus: mapBackendStatusToFrontend(backendStatus),
              assignee: tc.assignedTo || tc.assignee || "",
              assignedTo: tc.assignedTo || tc.assignee || "",
              assignedToId: tc.assignedToId || null,
            };
          });

          // Update execution statuses with database values, preserving localStorage values where database has no status

          const databaseStatuses: {
            [key: string]: TestCase["executionStatus"];
          } = {};

          enhancedTestCases.forEach((tc: any) => {
            if (tc.executionStatus && tc.executionStatus !== "not-started") {
              databaseStatuses[tc.id] = tc.executionStatus;
            }
          });

          

          let localStorageStatuses: Record<
            string,
            TestCase["executionStatus"]
          > = {};

          if (selectedProject && selectedRelease) {
            const saved = getSavedExecutionStatuses(
              selectedProject,
              selectedRelease,
            );

            if (saved && Object.keys(saved).length > 0) {
              localStorageStatuses = saved as Record<
                string,
                TestCase["executionStatus"]
              >;
            }
          }

          

          const mergedStatuses = {
            ...localStorageStatuses,
            ...databaseStatuses,
          };

          console.log(
            "Merged execution statuses (localStorage + database):",
            mergedStatuses,
          );

          setExecutionStatuses(mergedStatuses);

          setFilteredTestCases(enhancedTestCases);

          
          try {
            console.log(
              "🔴 DEBUG: Calling getDefectTestCaseCounts API with releaseId:",
              selectedRelease,
            );

            if (selectedRelease) {
              console.log("🔴 DEBUG: Calling getDefectTestCaseCounts API...");
              const defectInfo = await getDefectTestCaseCounts(selectedRelease);
              console.log(
                "🔴 DEBUG: Received response from new API:",
                defectInfo.data,
              );

              const testCasesWithDefectInfo = enhancedTestCases.map(
                (tc: any) => {
                  
                  const defectData = defectInfo.data?.find(
                    (d: any) => d.testCaseId === (tc.testCaseId || tc.id),
                  );
                  console.log(
                    `🔴 DEBUG: Mapping test case ${tc.testCaseId || tc.id} with defect data:`,
                    defectData,
                  );

                  return {
                    ...tc,

                    defectId: defectData?.defectId || null,
                    assignedTo: defectData?.assignedTo || null,
                    priority: defectData?.priority || null,
                  };
                },
              );

              console.log(
                "🔴 DEBUG: Final mapped test cases with defect info:",
                testCasesWithDefectInfo,
              );
              setFilteredTestCases(testCasesWithDefectInfo);
            }
          } catch (defectError) {
            console.error(
              "🔴 DEBUG: Error fetching defect information:",
              defectError,
            );
          }
        } catch (err: any) {
          setFilteredTestCases([]);

          setModulesError(err.message || "Failed to fetch test cases");
        } finally {
          setTestCasesLoading(false);
        }
      }
    };

    fetchTestCases();
  }, [
    selectedProject,
    selectedRelease,
    selectedModule,
    selectedSubmodule,
    modules,
    submodules,
  ]);

  const refreshDefectData = async (showToast: boolean = true) => {
    if (selectedProject) {
      try {

        setDefectsLoading(true);

        

        const defects = await filterDefectsForTest({
          projectId: String(selectedProject),
          releaseId: selectedRelease ? parseInt(selectedRelease) : undefined,
        });

        setExistingDefects(Array.isArray(defects) ? defects : []);

        

        const defectMappings: { [key: string]: string } = {};

        const defectUserAssignments: { [key: string]: string } = {};

        defects.forEach((defect: any) => {

          let testCaseId =
            defect.testCaseId ||
            defect.ReleaseTestCaseId ||
            defect.releaseTestCaseId;

          console.log("Processing defect:", {
            defectId: defect.defectId,

            testCaseId: defect.testCaseId,

            moduleName: defect.moduleName,

            subModuleName: defect.subModuleName,

            description: defect.description,

            assignedToName: defect.assignedToName,
          });

          

          if (
            !testCaseId &&
            (defect.module_name || defect.moduleName) &&
            (defect.sub_module_name || defect.subModuleName)
          ) {
            

            const matchingTestCase = filteredTestCases.find((tc: any) => {
              const tcModule = tc.module || tc.moduleName;

              const tcSubmodule = tc.subModule || tc.subModuleName;

              const dModule = defect.module_name || defect.moduleName;

              const dSub = defect.sub_module_name || defect.subModuleName;

              const dDesc =
                defect.description || defect.release_test_case_description;

              const moduleMatch = tcModule === dModule;

              const submoduleMatch = tcSubmodule === dSub;

              const descMatch =
                tc.description === dDesc ||
                tc.description === defect.release_test_case_description ||
                (dDesc &&
                  tc.description &&
                  (dDesc.toLowerCase().includes(tc.description.toLowerCase()) ||
                    tc.description
                      .toLowerCase()
                      .includes(dDesc.toLowerCase())));

              console.log("Matching attempt:", {
                tcModule,
                tcSubmodule,
                tcDescription: tc.description,

                dModule,
                dSub,
                dDesc,

                moduleMatch,
                submoduleMatch,
                descMatch,
              });

              return moduleMatch && submoduleMatch && descMatch;
            });

            if (matchingTestCase) {
              testCaseId = matchingTestCase.id;

              console.log("Found matching test case:", matchingTestCase.id);
            }
          }

          if (testCaseId) {
            

            const defectId =
              defect.defectId || `DEF-${defect.id.toString().padStart(4, "0")}`;

            defectMappings[testCaseId] = defectId;

            if (defect.assigned_to_name || defect.assignedToName) {
              defectUserAssignments[defectId] =
                defect.assigned_to_name || defect.assignedToName;
            }

            console.log(
              `Mapped defect ${defectId} to test case ${testCaseId}, assigned to ${defect.assigned_to_name || defect.assignedToName}`,
            );
          } else {
            console.log(
              `Could not map defect ${defect.defectId} - no test case match found`,
            );
          }
        });

        

        if (Object.keys(defectMappings).length > 0) {
          console.log("Merging defect mappings from backend:", defectMappings);

          setTestCaseDefectMap((prev: any) => {
            const merged = { ...(prev || {}), ...defectMappings };

            setLocalTestCaseDefectMap(merged);

            localStorage.setItem(
              "testCaseDefectMapping",
              JSON.stringify(merged),
            );

            return merged;
          });
        }

        

        if (Object.keys(defectUserAssignments).length > 0) {
          console.log(
            "Merging defect assignments from backend:",
            defectUserAssignments,
          );

          setDefectAssignments((prev) => {
            const merged = { ...(prev || {}), ...defectUserAssignments };

            localStorage.setItem("defectAssignments", JSON.stringify(merged));

            return merged;
          });
        }

        

        if (showToast) {
          setToast({
            isOpen: true,

            message: "Defect data refreshed successfully!",

            type: "success",
          });
        }
      } catch (error: any) {
        console.error("Failed to refresh defect data:", error.message);

        if (showToast) {
          setToast({
            isOpen: true,

            message: "Failed to refresh defect data",

            type: "error",
          });
        }
      } finally {
        setDefectsLoading(false);
      }
    }
  };

  

  const handleExecutionStatusChange = (
    testCaseId: string,

    status: TestCase["executionStatus"],
  ) => {
    setExecutionStatuses((prev) => {
      const updated = { ...prev, [testCaseId]: status };

      if (selectedProject && selectedRelease) {
        

        persistExecutionStatus(
          selectedProject,

          selectedRelease,

          testCaseId,

          status as ExecutionStatus,
        );
      }

      return updated;
    });
  };

  

  const getDefaultPriorityId = (): number => {
    const medium = priorities.find(
      (p) => (p.name || "").toLowerCase() === "medium",
    );

    return medium?.id || priorities[0]?.id || 1;
  };

  const getDefaultDefectStatusId = (): number => {
    const newStatus = defectStatuses.find(
      (s) => (s.defectStatusName || "").toLowerCase() === "new",
    );

    return newStatus?.id || defectStatuses[0]?.id || 3;
  };

  const getReleaseTestCaseId = (tc: any): string | number => {
    
    if (tc && tc.backendId != null) return tc.backendId;
    return tc?.releaseTestCaseId || tc?.ReleaseTestCaseId;
  };

  const persistStatusToBackend = async (
    tc: any,
    status: TestCase["executionStatus"],
  ) => {
    if (!tc) {
      console.error("No test case provided for status update");

      return;
    }

    if (status !== "passed" && status !== "failed") {
      console.log("Status not supported by API:", status);

      return; 
    }

    try {
      const releaseTestCaseId = getReleaseTestCaseId(tc);

      if (!releaseTestCaseId) {
        console.error("No releaseTestCaseId found for test case:", tc);

        setToast({
          isOpen: true,
          message: "Cannot update status: Missing release test case ID",
          type: "error",
        });

        return;
      }

      console.log("Updating status for test case:", {
        testCaseId: tc.id,

        releaseTestCaseId,

        status,

        testCase: tc,
      });

      const payload = {
        testCaseStatus:
          status === "passed" ? ("PASS" as const) : ("FAIL" as const),

        priorityId: getDefaultPriorityId(),

        assignedTo:
          defectFormData.assignedTo === 0
            ? undefined
            : defectFormData.assignedTo,
      };

      console.log("API payload:", payload);

      await updateReleaseTestCaseStatus(releaseTestCaseId, payload);

      console.log("Status update successful for:", releaseTestCaseId);

      setToast({
        isOpen: true,

        message: `Test case ${tc.id} status updated to ${status}`,

        type: "success",
      });
    } catch (e: any) {
      console.error("Failed to update execution status:", {
        testCaseId: tc.id,

        error: e?.message || e,

        fullError: e,
      });

      setToast({
        isOpen: true,

        message: `Failed to update status for ${tc.id}: ${e?.message || "Unknown error"}`,

        type: "error",
      });
    }
  };

  

  const renderColoredSpan = (
    text: string | undefined | null,
    colorResult: string | React.CSSProperties,
  ) => {
    const displayText = text || "-";

    if (typeof colorResult === "string") {
      

      return (
        <span
          className={`px-2 py-1 rounded-full text-xs font-medium ${colorResult}`}
        >
          {displayText}
        </span>
      );
    } else {
      

      return (
        <span
          className="px-2 py-1 rounded-full text-xs font-medium"
          style={colorResult}
        >
          {displayText}
        </span>
      );
    }
  };

  const getSeverityColor = (severityName: string | undefined | null) => {
    if (!severityName) return "bg-gray-100 text-gray-800";

    const severity = severities.find(
      (s) => s.name.toLowerCase() === severityName.toLowerCase(),
    );

    if (severity && severity.color) {
      

      const hexColor = severity.color.startsWith("#")
        ? severity.color
        : `#${severity.color}`;

      return { backgroundColor: hexColor, color: "white" };
    }

    

    switch (severityName.toLowerCase()) {
      case "critical":
        return "bg-red-100 text-red-800";

      case "high":
        return "bg-orange-100 text-orange-800";

      case "medium":
        return "bg-yellow-100 text-yellow-800";

      case "low":
        return "bg-green-100 text-green-800";

      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getExecutionStatusColor = (status: TestCase["executionStatus"]) => {
    switch (status) {
      case "passed":
        return "bg-green-100 text-green-800";

      case "failed":
        return "bg-red-100 text-red-800";

      case "in-progress":
        return "bg-blue-100 text-blue-800";

      case "blocked":
        return "bg-yellow-100 text-yellow-800";

      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const handleViewSteps = (testCase: TestCase) => {
    setViewingTestCase(testCase);

    setIsViewStepsModalOpen(true);
  };

  const handleViewTestCase = (testCase: TestCase) => {
    setViewingTestCase(testCase);

    setIsViewTestCaseModalOpen(true);
  };

  

  const getNextDefectId = () => {
    const projectDefects = defects.filter(
      (d) => d.projectId === selectedProject,
    );

    const ids = projectDefects

      .map((d) => d.id)

      .map((id) => parseInt(id.replace("DEF-", "")))

      .filter((n) => !isNaN(n));

    const nextNum = ids.length > 0 ? Math.max(...ids) + 1 : 1;

    return `DEF-${nextNum.toString().padStart(4, "0")}`;
  };

  // Helper function to format test case ID with abbreviation

  const formatTestCaseId = (id: string | number): string => {
    // Convert to string and extract numeric part

    const idStr = String(id);

    // If ID already contains "TC" or "TCTC", extract just the numeric part

    if (idStr.includes("TC")) {
      // Extract numbers from the end of the string

      const numericMatch = idStr.match(/\d+$/);

      if (numericMatch) {
        const numericId = numericMatch[0];

        return `TC${numericId}`;
      }
    }

    // Fallback: try to extract any numbers from the string

    const numericMatch = idStr.match(/\d+/);

    if (numericMatch) {
      const numericId = numericMatch[0];

      return `TC${numericId}`;
    }

    // If no numbers found, return original ID

    return idStr;
  };

  // Handler for input changes

  const handleDefectInputChange = (field: string, value: string | number) => {
    setDefectFormData((prev) => ({ ...prev, [field]: value }));
  };

  // Handler for submitting the defect form

  // const handleDefectFormSubmit = async (e: React.FormEvent) => {
  //   e.preventDefault();

  //   console.log("Submitting defect form with data:", defectFormData);
  //   console.log(
  //     "assignedTo value:",
  //     defectFormData.assignedTo,
  //     "type:",
  //     typeof defectFormData.assignedTo,
  //   );

  //   try {
  //     setDefectSubmitting(true);

  //     // Validate form data completeness

  //     if (!defectFormData.description || !defectFormData.title) {
  //       throw new Error(
  //         "Please fill in all required fields: description and steps.",
  //       );
  //     }

  //     // Helper function to get ID from name

  //     const getIdFromName = (
  //       array: any[],
  //       name: string,
  //       idField: string = "id",
  //       nameField: string = "name",
  //     ): number => {
  //       if (!array || array.length === 0) {
  //         console.warn(`Array is empty for ${nameField}:`, array);

  //         return 0;
  //       }

  //       // First try exact match

  //       let item = array.find(
  //         (item) =>
  //           item[nameField] === name || item[`${nameField}Name`] === name,
  

  

  
  
  
  
  
  
  
  

  

  
  
  
  
  
  
  
  
  
  
  

  
  
  
  
  

  
  

  
  

  

  

  

  
  

  
  

  

  

  

  

  
  
  
  
  
  
  

  
  
  
  
  
  
  

  

  

  
  
  
  

  
  
  
  

  

  
  
  
  
  

  

  

  
  
  

  
  
  

  
  

  

  
  

  

  
  

  

  

  

  
  

  

  

  

  
  

  
  

  

  

  
  

  

  

  
  
  
  
  

  
  
  
  

  

  
  

  
  
  
  
  

  

  

  

  
  

  

  
  
  
  
  
  
  
  

  
  

  

  
  
  

  

  

  
  

  
  
  

  
  

  

  
  
  

  
  

  

  
  
  

  

  

  
  
  
  
  

  
  

  
  

  

  
  
  

  

  

  
  
  
  

  

  

  

  
  
  
  
  
  

  

  
  
  
  
  
  
  
  

  
  

  

  

  
  

  

  
  
  

  

  
  

  

  

  

  

  

  

  

  

  

  

  

  

  
  

  

  

  

  

  

  

  

  
  
  
  
  

  

  

  
  
  
  
  
  
  
  
  
  
  
  

  

  
  

  

  
  
  
  
  
  
const handleDefectFormSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setDefectSubmitting(true);

  try {
    const tc = pendingStatusUpdate?.testCase;
    if (!tc) throw new Error("No test case selected.");

    
    const priorityObj = priorities.find(
      (p: any) =>
        (p.priority || p.name || "").toLowerCase() ===
        defectFormData.priority.toLowerCase()
    );
    if (!priorityObj) throw new Error("Please select a valid priority.");
    if (!defectFormData.assignedTo || defectFormData.assignedTo === 0) {
      throw new Error("Please select an assigned user.");
    }

    const releaseTestCaseId = Number(tc.backendId || tc.id);
    const releaseId = Number(selectedRelease);

    if (!releaseTestCaseId || !releaseId) {
      throw new Error("Missing release or test case information.");
    }

    const payload = {
      passOrFail: "FAIL",
      status: "FAIL",
      priorityId: Number(priorityObj.id),
      assignedTo: Number(defectFormData.assignedTo),
      briefDescription: defectFormData.title || tc.description,
      steps: defectFormData.description || tc.steps,
    };

    const formData = new FormData();
    formData.append(
      "data",
      new Blob([JSON.stringify(payload)], { type: "application/json" })
    );

    const attachmentFile = (defectFormData as any).attachmentFile;
    if (attachmentFile) {
      formData.append("attachmentFile", attachmentFile);
    }

    const response = await updateReleaseTestCaseStatusWithImage(
      releaseId,
      releaseTestCaseId,
      formData
    );

    console.log("Status update response:", response);

    const rawDefectId = response?.data?.defectId || response?.data?.defectNo || null;
    const defectNo = rawDefectId
      ? (String(rawDefectId).startsWith("DEF") ? String(rawDefectId) : `DEF-${rawDefectId}`)
      : null;
    const executerDefectName =
      response?.data?.assignToName ||
      response?.data?.assignedTo ||
      (defectAllocatedUsers.find((u: any) => String(u.userId) === String(defectFormData.assignedTo))?.userName) ||
      null;
    const priorityName = response?.data?.priorityName || priorityObj.name || priorityObj.priority || null;

    setFilteredTestCases((prev) =>
      prev.map((t) =>
        t.id === tc.id
          ? {
              ...t,
              executionStatus: "failed",
              status: "failed",
              defectId: defectNo,
              defectNo: defectNo,
              assignedTo: executerDefectName || t.assignedTo,
              executerDefect: executerDefectName,
              priority: priorityName,
            }
          : t
      )
    );

    setExecutionStatuses((prev) => ({ ...prev, [tc.id]: "failed" }));
    if (selectedProject && selectedRelease) {
      persistExecutionStatus(
        selectedProject,
        selectedRelease,
        tc.id,
        "failed" as ExecutionStatus
      );
    }

    if (defectNo) {
      setTestCaseDefectMap((prev: any) => ({ ...prev, [tc.id]: defectNo }));
      if (executerDefectName) {
        setDefectAssignments((prev) => ({ ...prev, [defectNo]: executerDefectName }));
      }
    }

    setToast({
      isOpen: true,
      message: "Test case marked as failed and defect created!",
      type: "success",
    });
    setPendingStatusUpdate(null);
    setPreviousStatusBeforeFail(null);
    setIsEditingDefect(false);
    setEditingDefect(null);
    setDefectModalOpen(null);
    setDefectFormData({
      title: "",
      description: "",
      module: "",
      subModule: "",
      type: "",
      priority: "",
      severity: "",
      status: "",
      projectId: projectId || "",
      releaseId: releaseId?.toString() || "",
      testCaseId: "",
      assignedTo: 0,
      reportedBy: "",
      rejectionComment: "",
      attachment: "",        
      attachmentFile: null, 
    });

  } catch (error: any) {
    console.error("Error updating status:", error);
    setToast({
      isOpen: true,
      message: error?.response?.data?.message || error.message || "Failed to update test case status.",
      type: "error",
    });
  } finally {
    setDefectSubmitting(false);
  }
};

  const handleFailClick = async (testCase: any) => {
  
  const existingDefectId = testCaseDefectMap[testCase.id];
  let existingDefect = null;

  if (existingDefectId) {
    existingDefect = existingDefects.find(
      (d) => d.defectId === existingDefectId,
    );
    console.log("Found existing defect for test case:", testCase.id, existingDefect);
  }

  
  setIsEditingDefect(!!existingDefect);
  setEditingDefect(existingDefect);

  
  const mapTestCaseTypeToDefectType = (testCaseType: string) => {
    if (defectTypes.length > 0) {
      const testCaseTypeLower = (testCaseType || "").toLowerCase();
      let defectType = defectTypes.find((dt) =>
        (dt.defectTypeName || "").toLowerCase().includes(testCaseTypeLower),
      );
      if (
        !defectType &&
        (testCaseTypeLower === "functional" ||
          testCaseTypeLower === "regression" ||
          testCaseTypeLower === "smoke" ||
          testCaseTypeLower === "integration")
      ) {
        defectType = defectTypes.find(
          (dt) =>
            (dt.defectTypeName || "").toLowerCase().includes("functional") ||
            (dt.defectTypeName || "").toLowerCase().includes("bug"),
        );
      }
      if (defectType) {
        return defectType.defectTypeName;
      }
    }
    switch (testCaseType) {
      case "functional":
        return "Functional Bug";
      case "regression":
        return "Functional Bug";
      case "smoke":
        return "Functional Bug";
      case "integration":
        return "Functional Bug";
      default:
        return defectTypes.length > 0
          ? defectTypes[0]?.defectTypeName || "Bug"
          : "Bug";
    }
  };

  const moduleName =
    testCase.module ||
    testCase.moduleName ||
    selectedModule ||
    "Not specified";
  const submoduleName =
    testCase.subModule ||
    testCase.subModuleName ||
    selectedSubmodule ||
    "Not specified";
  const mappedDefectType = mapTestCaseTypeToDefectType(testCase.type);

  console.log("Setting pending status update for test case:", testCase.id, "status: failed");
  setPendingStatusUpdate({
    testCase: testCase,
    status: "failed",
  });

  if (existingDefect) {
    setDefectFormData({
      title: existingDefect.description || testCase.description,
      description: existingDefect.steps || testCase.steps,
      module:
        (existingDefect as any).module_name ||
        (existingDefect as any).moduleName ||
        moduleName,
      subModule:
        (existingDefect as any).submodule_name ||
        (existingDefect as any).subModuleName ||
        submoduleName,
      type:
        (existingDefect as any).defect_type_name ||
        (existingDefect as any).defectTypeName ||
        mappedDefectType,
      priority:
        (existingDefect as any).priority_name ||
        (existingDefect as any).priorityName ||
        "medium",
      severity:
        (existingDefect as any).severity_name ||
        (existingDefect as any).severityName ||
        testCase.severity ||
        "medium",
      status:
        (existingDefect as any).defect_status_name ||
        (existingDefect as any).status ||
        "NEW",
      projectId: String(selectedProject) || "",
      releaseId: selectedRelease || "",
      testCaseId: testCase.id,
      assignedTo: (existingDefect as any).assignedTo || 0,
      reportedBy:
        (existingDefect as any).assigned_by_name ||
        (existingDefect as any).assignedByName ||
        "",
      rejectionComment: (existingDefect as any).rejectionComment || "",
      attachment:(existingDefect as any).attachment || "",
      attachmentFile:(existingDefect as any).attachmentFile || null
    });
  } else {
    setDefectFormData({
      title: testCase.description,
      description: testCase.steps,
      module: moduleName,
      subModule: submoduleName,
      type: testCase.defectTypeName || mappedDefectType,
      priority: "medium",
      severity: testCase.severity || "medium",
      status: "NEW",
      projectId: String(selectedProject) || "",
      releaseId: selectedRelease || "",
      testCaseId: testCase.id,
      assignedTo: 0, 
      reportedBy: "",
      rejectionComment: "",
      attachment:"",
      attachmentFile: null as File | null
    });
  }

  // ✅ Fetch submodule-allocated developers for the dropdown
  const selectedSubmoduleObj = submodules.find(
    (s: any) => s.name === selectedSubmodule || s.subModuleName === selectedSubmodule || String(s.id) === String(testCase.subModuleId)
  );
  const selectedModuleObj = modules.find(
    (m: any) => m.name === selectedModule || m.moduleName === selectedModule || String(m.id) === String(testCase.moduleId)
  );
  const effectiveSubModuleId = testCase.subModuleId || testCase.submoduleId || selectedSubmoduleObj?.id || selectedSubmoduleObj?.subModuleId;
  const effectiveModuleId = testCase.moduleId || selectedModuleObj?.id || 0;

  if (effectiveSubModuleId) {
    setDefectAllocatedUsersLoading(true);
    try {
      const subModuleDevRes = await getAllSubmoduleAllocatedDevBySubmoduleId(
        Number(effectiveSubModuleId),
        Number(effectiveModuleId)
      );
      const devs = (subModuleDevRes?.data || [])
        .filter((d: any) => d.employeeId)
        .map((d: any) => ({
          userId: Number(d.employeeId),
          userName: d.employeeName || `Employee ${d.employeeId}`,
          empId: Number(d.employeeId),
        }));

      setDefectAllocatedUsers(devs);
    } catch (error) {
      console.error("Failed to fetch developers for submodule:", error);
      setDefectAllocatedUsers([]);
    } finally {
      setDefectAllocatedUsersLoading(false);
    }
  } else {
    setDefectAllocatedUsers([]);
    setDefectAllocatedUsersLoading(false);
  }

  setDefectModalOpen(testCase.id);
};

  

  if (selectedRelease) {
    const currentRelease =
      releases && releases.find((r) => r.id === selectedRelease);

    const currentProject =
      projects && projects.find((p) => p.id === selectedProject);

    

    const allocatedIds = allocatedTestCasesMap[selectedRelease || ""] || [];

    let allocatedTestCases = effectiveTestCases.filter((tc: any) =>
      allocatedIds.includes(tc.id),
    );

    // If a module is selected, filter to that module

    if (selectedModule) {
      allocatedTestCases = allocatedTestCases.filter(
        (tc: any) => tc.module === selectedModule,
      );
    }

    // If a submodule is selected, filter to that submodule (must also match selected module)

    if (selectedSubmodule) {
      allocatedTestCases = allocatedTestCases.filter(
        (tc: any) =>
          tc.subModule === selectedSubmodule &&
          (!selectedModule || tc.module === selectedModule),
      );
    }

    // Get submodules for selected module

    // const selectedModuleObj =
    //   modules && modules.find((m: any) => m.moduleName || m.name === selectedModule);

    // const submodulesToShow =
    //   submodules.length > 0 ? submodules : selectedModuleObj?.submodules || [];

    const submodulesToShow = submodules;

    return (
      <div className="max-w-6xl mx-auto py-8">
        {}

        <div className="flex-none p-6 pb-4">
          <div className="flex justify-between items-center mb-4">
            <div className="space-y-1">
              <h1 className="text-2xl font-bold text-gray-900">
                Test Execution
              </h1>

              <p className="text-sm text-gray-500">
                {currentProject?.name} - {currentRelease?.name}
              </p>
            </div>

            <Button
              variant="secondary"
              onClick={() => setSelectedRelease(null)}
              className="flex items-center space-x-2"
            >
              <ChevronLeft className="w-4 h-4" />

              <span>Back</span>
            </Button>
          </div>

          {}

          <Card className="mb-4">
            <CardContent className="p-4">
              <h2 className="text-lg font-semibold text-gray-900 mb-3">
                Module Selection
              </h2>

              <div className="relative flex items-center">
                <button
                  onClick={() => {
                    const container = document.getElementById("module-scroll");

                    if (container) container.scrollLeft -= 200;
                  }}
                  className="flex-shrink-0 z-10 bg-white shadow-md rounded-full p-1 hover:bg-gray-50 mr-2"
                >
                  <ChevronLeft className="w-5 h-5 text-gray-600" />
                </button>

                <div
                  id="module-scroll"
                  className="flex space-x-2 overflow-x-auto pb-2 scroll-smooth flex-1"
                  style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}
                >
                  {countsLoading ? (
                    <span className="text-gray-400 px-4 py-2">
                      Loading test case counts...
                    </span>
                  ) : modules.length > 0 ? (
                    modules.map((module) => {
                      const moduleTestCaseCount =
                        moduleTestCaseCounts[ module.name] || 0;

                      return (
                        <Button
                          key={ module.id}
                          variant={
                            selectedModule ===  module.name
                              ? "primary"
                              : "secondary"
                          }
                          onClick={() => handleModuleSelect( module.name)}
                          className="whitespace-nowrap m-2"
                        >
                          { module.name} ({moduleTestCaseCount})
                        </Button>
                      );
                    })
                  ) : (
                    <span className="text-gray-400 px-4 py-2">
                      No modules found for this project.
                    </span>
                  )}
                </div>

                <button
                  onClick={() => {
                    const container = document.getElementById("module-scroll");

                    if (container) container.scrollLeft += 200;
                  }}
                  className="flex-shrink-0 z-10 bg-white shadow-md rounded-full p-1 hover:bg-gray-50 ml-2"
                >
                  <ChevronRight className="w-5 h-5 text-gray-600" />
                </button>
              </div>
            </CardContent>
          </Card>

          {}

          <Card className="mb-4">
            <CardContent className="p-4">
              <h2 className="text-lg font-semibold text-gray-900 mb-3">
                Submodule Selection
              </h2>

              <div className="relative flex items-center min-h-[44px]">
                <button
                  onClick={() => {
                    const container =
                      document.getElementById("submodule-scroll");

                    if (container) container.scrollLeft -= 200;
                  }}
                  className="flex-shrink-0 z-10 bg-white shadow-md rounded-full p-1 hover:bg-gray-50 mr-2"
                >
                  <ChevronLeft className="w-5 h-5 text-gray-600" />
                </button>

                <div
                  id="submodule-scroll"
                  className="flex space-x-2 overflow-x-auto p-2 scroll-smooth flex-1"
                  style={{
                    scrollbarWidth: "none",

                    msOverflowStyle: "none",

                    maxWidth: "100%",
                  }}
                >
                  {submodulesLoading || countsLoading ? (
                    <span className="text-gray-400 px-4 py-2">Loading...</span>
                  ) : submodulesError ? (
                    <span className="text-red-400 px-4 py-2">
                      {submodulesError}
                    </span>
                  ) : submodulesToShow.length > 0 ? (
                    submodulesToShow.map((submodule: any) => {
                      console.log("submodulesToShow",submodule);
                      
                      const submoduleTestCaseCount =
                        submoduleTestCaseCounts[
                          submodule.name
                        ] || 0;

                      return (
                        <Button
                          key={submodule.id}
                          variant={
                            selectedSubmodule ===
                            (submodule.name)
                              ? "primary"
                              : "secondary"
                          }
                          onClick={() =>
                            handleSubmoduleSelect(
                               submodule.name,
                            )
                          }
                          className="whitespace-nowrap m-2"
                        >
                          {submodule.name} (
                          {submoduleTestCaseCount})
                        </Button>
                      );
                    })
                  ) : (
                    <span className="text-gray-400 px-4 py-2">
                      No submodules
                    </span>
                  )}
                </div>

                <button
                  onClick={() => {
                    const container =
                      document.getElementById("submodule-scroll");

                    if (container) container.scrollLeft += 200;
                  }}
                  className="flex-shrink-0 z-10 bg-white shadow-md rounded-full p-1 hover:bg-gray-50 ml-2"
                >
                  <ChevronRight className="w-5 h-5 text-gray-600" />
                </button>
              </div>
            </CardContent>
          </Card>
        </div>

        {}

        <div className="flex-1 px-6 pb-6">
          <div className="mb-4">
            <h3 className="text-lg font-semibold text-gray-900">Test Cases</h3>
          </div>

          <Card>
            <CardContent className="p-0">
              <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr className="border-b border-gray-200">
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider sticky left-0 z-20 bg-gray-50 border-r border-gray-200">
                      Test Case ID
                    </th>

                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider sticky left-[120px] z-20 bg-gray-50 border-r border-gray-200">
                      Description
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Steps
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Severity
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Executer(QA)
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Assigned To
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Execution Status
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Defect ID
                      {defectsLoading && (
                        <div className="inline-block ml-2">
                          <div className="w-3 h-3 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                        </div>
                      )}
                    </th>

                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>

                <tbody className="bg-white divide-y divide-gray-200">
                  {testCasesLoading ? (
                    <tr>
                      <td colSpan={9} className="px-6 py-8 text-center">
                        <div className="flex items-center justify-center space-x-2">
                          <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>

                          <span className="text-gray-500">
                            Loading test cases...
                          </span>
                        </div>
                      </td>
                    </tr>
                  ) : filteredTestCases.length === 0 ? (
                    <tr>
                      <td
                        colSpan={9}
                        className="px-6 py-8 text-center text-gray-500"
                      >
                        {selectedModule && selectedSubmodule
                          ? "No test cases found for the selected module and submodule."
                          : "Please select a module and submodule to view test cases."}
                      </td>
                    </tr>
                  ) : (
                    filteredTestCases.map((testCase: any) => {

                      const databaseStatus = testCase.executionStatus;
                      const localStorageStatus = executionStatuses[testCase.id];

                      const rawStatus = (databaseStatus && databaseStatus !== "not-started" && databaseStatus !== "NOT_RUN")
                        ? (databaseStatus.toUpperCase() === "PASS" || databaseStatus.toUpperCase() === "PASSED"
                            ? "passed"
                            : databaseStatus.toUpperCase() === "FAIL" || databaseStatus.toUpperCase() === "FAILED"
                              ? "failed"
                              : databaseStatus.toLowerCase())
                        : (localStorageStatus || "not-started");

                      const status = rawStatus;

                      const isFailed = status === "failed" || status === "FAILED" || status === "FAIL";
                      const isPassed = status === "passed" || status === "PASSED" || status === "PASS";

                      const fallbackDefect =
                        findMatchingDefectForTestCase(testCase);

                      const computedDefectId =
                        testCaseDefectMap[testCase.id] ||
                        (fallbackDefect
                          ? fallbackDefect.defectId ||
                            `DEF-${String(fallbackDefect.id).padStart(4, "0")}`
                          : undefined);

                      const computedAssignedUser = computedDefectId
                        ? getAssignedUserForDefect(computedDefectId) ||
                          (fallbackDefect
                            ? fallbackDefect.assigned_to_name ||
                              fallbackDefect.assignedToName
                            : undefined)
                        : fallbackDefect
                          ? fallbackDefect.assigned_to_name ||
                            fallbackDefect.assignedToName
                          : undefined;

                      

                      if (isFailed) {
                        console.log(
                          `Debug for failed test case ${testCase.id}:`,
                          {
                            status,

                            isFailed,

                            testCaseDefectMap: testCaseDefectMap[testCase.id],

                            fallbackDefect,

                            computedDefectId,

                            computedAssignedUser,

                            defectAssignments,

                            existingDefectsCount: existingDefects.length,

                            testCaseDefectMapKeys:
                              Object.keys(testCaseDefectMap),

                            defectAssignmentsKeys:
                              Object.keys(defectAssignments),
                          },
                        );
                      }

                      return (
                        <tr key={testCase.testcaseId} className="hover:bg-gray-50">
                          <td className="px-6 py-4 whitespace-nowrap sticky left-0 z-10 bg-white border-r border-gray-200 text-sm text-gray-500">
                            {formatTestCaseId(testCase.no)}
                          </td>

                          <td
                              className="px-6 py-4 text-sm text-gray-500 sticky left-[110px] z-10 bg-white border-r border-gray-200"
                              style={{ minWidth: 200, maxWidth: 200 }}
                              title={testCase.description}
                          >
                            <div className="w-[170px] overflow-hidden whitespace-nowrap text-ellipsis">
                              {testCase.description}
                            </div>
                          </td>

                          <td className="px-6 py-4 text-sm text-gray-500">
                            <button
                              onClick={() => handleViewSteps(testCase)}
                              className="text-blue-600 hover:text-blue-800 flex items-center space-x-1"
                              title="View Steps"
                            >
                              <Eye className="w-4 h-4" />

                              <span>View</span>
                            </button>
                          </td>

                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {testCase.type}
                          </td>

                          <td className="px-6 py-4 whitespace-nowrap">
                            {renderColoredSpan(
                              testCase.severity,
                              getSeverityColor(testCase.severity),
                            )}
                          </td>
                           <td
                            className="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                            title={testCase.assignedTo}
                          >
                            {testCase.assignedTo || "No Execution"}
                          </td>

                          {}
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {isFailed ? (
                              <span className="font-medium text-blue-700">
                                {testCase.executerDefect || computedAssignedUser || "No Assignment"}
                              </span>
                            ) : (
                              <span className="text-gray-400">-</span>
                            )}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            <div className="flex rounded border border-gray-200 bg-white shadow overflow-hidden w-fit">
                              <button
                                type="button"
                                className={`px-3 py-1 text-xs font-semibold focus:outline-none transition-colors duration-200 ${
                                  isPassed
                                    ? "bg-green-500 text-white"
                                    : "bg-white text-gray-700 hover:bg-green-100"
                                }`}
                                style={{
                                  borderTopLeftRadius: 6,

                                  borderBottomLeftRadius: 6,
                                }}
                                onClick={async () => {
                                    if (!canUserExecute(testCase)) {
                                      setToast({
                                        isOpen: true,
                                        message: "You are not authorized to execute this test case. Only the assigned QA can execute.",
                                        type: "error",
                                      });
                                      return;
                                    }
                                    setPreviousStatusBeforeFail(null);

                                    
                                    setExecutionStatuses((prev) => ({ ...prev, [testCase.id]: "passed" }));
                                    if (selectedProject && selectedRelease) {
                                      persistExecutionStatus(selectedProject, selectedRelease, testCase.id, "passed" as ExecutionStatus);
                                    }
                                    setDefectModalOpen(null);

                                    
                                    try {
                                      const releaseTestCaseId = testCase.backendId || Number(testCase.id);
                                      await updateReleaseTestCaseStatus(
                                        Number(selectedRelease),
                                        releaseTestCaseId,
                                        { status: "PASSED" }
                                      );
                                      setToast({ isOpen: true, message: "Test case passed!", type: "success" });
                                    } catch (e: any) {
                                      setToast({ isOpen: true, message: e?.response?.data?.message || "Failed to update status", type: "error" });
                                    }
                                  }}
                                disabled={isFailed}
                                aria-pressed={isPassed}
                              >
                                Pass
                              </button>

                              <button
                                type="button"
                                className={`px-3 py-1 text-xs font-semibold focus:outline-none transition-colors duration-200 ${
                                  isFailed
                                    ? "bg-red-500 text-white"
                                    : "bg-white text-gray-700 hover:bg-red-100"
                                }`}
                                style={{
                                  borderTopRightRadius: 6,

                                  borderBottomRightRadius: 6,

                                  borderLeft: "1px solid #e5e7eb",
                                }}
                                onClick={() => {
                                  if (!canUserExecute(testCase)) {
                                    setToast({
                                      isOpen: true,
                                      message: "You are not authorized to execute this test case. Only the assigned QA can execute.",
                                      type: "error",
                                    });
                                    return;
                                  }
                                  if (!isFailed) {
                                    

                                    const currentStatus = status;

                                    console.log(
                                      "Storing previous status before fail:",
                                      {
                                        testCaseId: testCase.id,

                                        currentStatus,

                                        databaseStatus:
                                          testCase.executionStatus,

                                        localStorageStatus:
                                          executionStatuses[testCase.id],
                                      },
                                    );

                                    setPreviousStatusBeforeFail({
                                      testCaseId: testCase.id,

                                      previousStatus: currentStatus,
                                    });

                                    

                                    

                                    handleFailClick(testCase);
                                  } else {
                                    

                                    handleFailClick(testCase);
                                  }
                                }}
                                disabled={isFailed}
                                aria-pressed={isFailed}
                              >
                                Fail
                              </button>
                            </div>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            {(() => {
                              
                              const defectIdToShow = testCase.defectId || computedDefectId;
                              const defectData = existingDefects.find(
                                  (d: any) => d.defectNo === defectIdToShow || String(d.id) === String(defectIdToShow));
                              if (isFailed && defectIdToShow) {
                                return (
                                  <button
                                    className="px-3 py-1 rounded-full bg-blue-100 text-blue-800 font-semibold hover:bg-blue-200 transition text-xs"
                                   onClick={() => {
                                     if (defectData?.id) {
                                      navigate(`/projects/${selectedProject}/defects?view=${defectData.id}`);
                                    } else {
                                      
                                      navigate(`/projects/${selectedProject}/defects?view=${defectIdToShow}`);
                                    }
                                  }}
                                  >
                                    {defectIdToShow}
                                  </button>
                                );
                              }

                              if (isFailed && !defectIdToShow) {
                                return <span className="text-xs text-red-600">Failed - No Defect</span>;
                              }

                              return <span className="text-gray-400">-</span>;
                            })()}
                          </td>
                          {}

                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            <button
                              onClick={() => handleViewTestCase(testCase)}
                              className="p-1 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded"
                              title="View"
                            >
                              <Eye className="w-4 h-4" />
                            </button>
                            {can.testCase.delete && (
                              <button
                                onClick={() => {
                                  setTestCaseToDelete(testCase);
                                  setDeleteConfirmOpen(true);
                                }}
                                className="p-1 text-red-600 hover:text-red-800 hover:bg-red-50 rounded ml-1"
                                title="Delete"
                              >
                                <Trash2 className="w-4 h-4" />
                              </button>
                            )}
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
              </div>
            </CardContent>
          </Card>
        </div>

        {}

        <Modal
          isOpen={isViewStepsModalOpen}
          onClose={() => {
            setIsViewStepsModalOpen(false);

            setViewingTestCase(null);
            
          }}
          title={`Test Steps - ${viewingTestCase?.no ? formatTestCaseId(viewingTestCase.no) : "N/A"}`}
        >
          <div className="space-y-4">
            <div className="bg-gray-50 rounded-lg p-4">
              <p className="text-gray-700 whitespace-pre-line">
                {viewingTestCase?.steps}
              </p>
            </div>

            <div className="flex justify-end pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setIsViewStepsModalOpen(false);

                  setViewingTestCase(null);
                }}
              >
                Close
              </Button>
            </div>
          </div>
        </Modal>

        {}

        <Modal
          isOpen={isViewTestCaseModalOpen}
          onClose={() => {
            setIsViewTestCaseModalOpen(false);

            setViewingTestCase(null);
          }}
          title={`Test Case Details - ${viewingTestCase?.no ? formatTestCaseId(viewingTestCase.no) : "N/A"}`}
          size="xl"
        >
          {viewingTestCase && (
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-gray-500">
                    Description
                  </h3>

                  <p className="mt-1 text-sm text-gray-900">
                    {viewingTestCase.description}
                  </p>
                </div>
              </div>

              <div>
                <h3 className="text-sm font-medium text-gray-500 mb-2">
                  Test Steps
                </h3>

                <div className="bg-gray-50 rounded-lg p-4">
                  <p className="text-gray-700 whitespace-pre-line">
                    {viewingTestCase.steps}
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-4">
                <div>
                  <h3 className="text-sm font-medium text-gray-500">
                    Severity
                  </h3>

                  {renderColoredSpan(
                    viewingTestCase.severity,
                    getSeverityColor(viewingTestCase.severity),
                  )}
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-500">
                    Priority
                  </h3>

                  {renderColoredSpan(
                    viewingTestCase.priority,
                    getSeverityColor(viewingTestCase.priority),
                  )}
                </div>

                <div>
                  <h3 className="text-sm font-medium text-gray-500">Module</h3>

                  <p className="mt-1 text-sm text-gray-900">
                    {viewingTestCase.module} / {viewingTestCase.subModule}
                  </p>
                </div>
              </div>

              <div className="flex justify-end pt-4">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setIsViewTestCaseModalOpen(false);

                    setViewingTestCase(null);
                  }}
                >
                  Close
                </Button>
              </div>
            </div>
          )}
        </Modal>

        {}

        <Modal
          isOpen={!!defectModalOpen}
          onClose={() => {
            console.log(
              "Modal onClose triggered. Previous status data:",
              previousStatusBeforeFail,
            );
            setDefectAllocatedUsers([]);
            setDefectAllocatedUsersLoading(false);

            

            if (previousStatusBeforeFail) {
              console.log(
                "Modal close - Reverting status from failed to:",
                previousStatusBeforeFail.previousStatus,
              );

              setExecutionStatuses((prev) => ({
                ...prev,

                [previousStatusBeforeFail.testCaseId]:
                  previousStatusBeforeFail.previousStatus,
              }));

              

              if (selectedProject && selectedRelease) {
                persistExecutionStatus(
                  selectedProject,

                  selectedRelease,

                  previousStatusBeforeFail.testCaseId,

                  previousStatusBeforeFail.previousStatus as ExecutionStatus,
                );
              }

              

              setPreviousStatusBeforeFail(null);
            }

            setDefectModalOpen(null);

            

            console.log("Clearing pending status update - modal closed");

            setPendingStatusUpdate(null);

            setDefectFormData({
              title: "",

              description: "",

              module: "",

              subModule: "",

              type: "bug",

              priority: "medium",

              severity: "medium",

              status: "NEW",

              projectId: projectId || "",

              releaseId: releaseId || "",

              testCaseId: "",

              assignedTo: 0,

              reportedBy: "",

              rejectionComment: "",
              attachment: "",         
              attachmentFile: null
            });
          }}
          title="Add New Defect"
          size="lg"
        >
          <form onSubmit={handleDefectFormSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Brief Description
              </label>

              <input
                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100"
                value={defectFormData.title}
                disabled
                readOnly
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Steps
              </label>

              <textarea
                value={defectFormData.description}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100"
                rows={3}
                disabled
                readOnly
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Modules
                </label>

                <input
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  value={defectFormData.module}
                  disabled
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Submodules
                </label>

                <input
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                  value={defectFormData.subModule}
                  disabled
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Severity
                </label>

                <input
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100"
                  value={defectFormData.severity}
                  disabled
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Type
                </label>

                <input
                  value={defectFormData.type || "Not specified"}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  disabled
                  readOnly
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Priority
                </label>

                <select
                  value={defectFormData.priority}
                  onChange={(e) =>
                    handleDefectInputChange("priority", e.target.value)
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="">Select priority</option>

                  {priorities.length > 0 ? (
                    priorities.map((priority) => (
                      <option key={priority.id} value={priority.name}>
                        {priority.name}
                      </option>
                    ))
                  ) : (
                    <>
                      <option value="high">High</option>

                      <option value="medium">Medium</option>

                      <option value="low">Low</option>
                    </>
                  )}
                </select>
              </div>
             <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Assigned To
                </label>
                <select
                  value={defectFormData.assignedTo}
                  onChange={(e) => handleDefectInputChange("assignedTo", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                  required
                >
                  <option value="">Select assigned user</option>
                  {defectAllocatedUsersLoading ? (
                    <option value="" disabled>Loading users...</option>
                  ) : defectAllocatedUsers.length > 0 ? (
                    defectAllocatedUsers.map((user) => (
                      <option key={user.userId} value={user.userId}>
                        {user.userName}
                      </option>
                    ))
                  ) : (
                    <option value="" disabled>No developers to this submodule</option>
                  )}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
             {}
              <div className="col-span-12">
              <ImagePicker
                label="Attachment Image (Optional)"
                value={defectFormData.attachmentFile}
                existingAttachment={defectFormData.attachment}
                isEditing={!!editingDefect}
                onChange={(file) => {
                  if (file === null) {
                    setDefectFormData((prev) => ({
                      ...prev,
                      attachmentFile: null,
                      attachment: "",
                    }));
                  } else {
                    setDefectFormData((prev) => ({
                      ...prev,
                      attachmentFile: file,
                    }));
                  }
                }}
              />
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  

                  console.log(
                    "Cancel button clicked. Previous status data:",
                    previousStatusBeforeFail,
                  );

                  if (previousStatusBeforeFail) {
                    console.log(
                      "Cancel clicked - Reverting status from failed to:",
                      previousStatusBeforeFail.previousStatus,
                    );

                    console.log(
                      "Previous status data:",
                      previousStatusBeforeFail,
                    );

                    setExecutionStatuses((prev) => ({
                      ...prev,

                      [previousStatusBeforeFail.testCaseId]:
                        previousStatusBeforeFail.previousStatus,
                    }));

                    

                    if (selectedProject && selectedRelease) {
                      persistExecutionStatus(
                        selectedProject,

                        selectedRelease,

                        previousStatusBeforeFail.testCaseId,

                        previousStatusBeforeFail.previousStatus as ExecutionStatus,
                      );
                    }

                    

                    setPreviousStatusBeforeFail(null);
                  }

                  setDefectModalOpen(null);

                  

                  console.log(
                    "Clearing pending status update - cancel clicked",
                  );

                  setPendingStatusUpdate(null);

                  

                  setIsEditingDefect(false);

                  setEditingDefect(null);

                  setDefectFormData({
                    title: "",

                    description: "",

                    module: "",

                    subModule: "",

                    type: "bug",

                    priority: "medium",

                    severity: "medium",

                    status: "NEW",

                    projectId: projectId || "",

                    releaseId: releaseId || "",

                    testCaseId: "",

                    assignedTo: 0,

                    reportedBy: "",

                    rejectionComment: "",
                    attachment:"",
                    attachmentFile: null as File | null
                  });
                }}
              >
                Cancel
              </Button>

              <Button type="submit" disabled={defectSubmitting}>
                {defectSubmitting
                  ? isEditingDefect
                    ? "Updating..."
                    : "Creating..."
                  : isEditingDefect
                    ? "Update Defect"
                    : "Save Defect"}
              </Button>
            </div>
          </form>
        </Modal>
        {deleteConfirmOpen && (
          <div className="fixed inset-0 z-[60] flex justify-center items-start bg-black bg-opacity-40">
            <div
              className="mt-8 bg-[#444] text-white rounded-lg shadow-2xl min-w-[400px] max-w-[95vw]"
              style={{ boxShadow: "0 8px 24px rgba(0,0,0,0.25)" }}
            >
              <div className="px-6 pb-4 pt-5 text-base text-white">
                Are you sure you want to remove this test case from release execution?
              </div>
              <div className="px-6 pb-5 flex justify-end gap-3">
                <button
                  className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold px-6 py-2 rounded mr-2"
                  onClick={() => {
                    setDeleteConfirmOpen(false);
                    setTestCaseToDelete(null);
                  }}
                  type="button"
                  disabled={deleteLoading}
                >
                  Cancel
                </button>
                <button
                  className="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-2 rounded"
                  onClick={handleDeleteReleaseTestCase}
                  type="button"
                  disabled={deleteLoading}
                >
                  {deleteLoading ? "Removing..." : "Delete"}
                </button>
              </div>
            </div>
          </div>
        )}
        <Toast
          isOpen={toast.isOpen}
          message={toast.message}
          type={toast.type}
          onClose={() => setToast({ ...toast, isOpen: false })}
        />
      </div>
    );
  }

  

  return (
    <div className="max-w-6xl mx-auto">
      {}

      <div className="mb-4 flex justify-end">
        <Button
          variant="secondary"
          onClick={() =>
            navigate(`/projects/${selectedProject || projectId}/releases`)
          }
          className="flex items-center"
        >
          <ChevronLeft className="w-5 h-5 mr-2" /> Back
        </Button>
      </div>

      <h1 className="text-2xl font-bold text-gray-900 mb-8">Test Execution</h1>

      {}

      <ProjectSelector
        projects={projects}
        selectedProjectId={selectedProject}
        onSelect={handleProjectSelect}
        className="mb-6"
      />

      <Toast
        isOpen={toast.isOpen}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ ...toast, isOpen: false })}
      />

      {}

      {releaseLoading && (
        <div className="text-center text-gray-500 mb-4">
          Loading releases...
        </div>
      )}

      {releaseError && (
        <div className="text-center text-red-500 mb-4">{releaseError}</div>
      )}

      {selectedProject && !releaseLoading && !releaseError && (
        <div className="mb-6">
          <div className="mb-4">
            <h2 className="text-lg font-semibold text-gray-900">
              Releases for Project
            </h2>
          </div>

          {projectReleaseCard.length === 0 ? (
            <Card>
              <CardContent className="p-6 text-center">
                <p className="text-gray-500">
                  No releases found for the selected project.
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {projectReleaseCard.map((release: any) => {
                const currentProject =
                  projects &&
                  projects.find((p: any) => p.id === selectedProject);

                const isActive = release.status === "Active" || release.status === "ACTIVE" || release.status === true;

                

                console.log("Release data:", {
                  releaseId: release.releaseId,

                  id: release.id,

                  name: release.name,

                  status: release.status,

                  testCaseCount:
                    releaseTestCaseCounts[release.releaseId] ??
                    releaseTestCaseCounts[release.id] ??
                    releaseTestCaseCounts[String(release.releaseId)] ??
                    releaseTestCaseCounts[String(release.id)] ??
                    (typeof release.totalTestCases === "number" ? release.totalTestCases : undefined) ??
                    (typeof release.testCaseCount === "number" ? release.testCaseCount : undefined) ??
                    (Array.isArray(release.testCases) ? release.testCases.length : 0),
                });

                return (
                  <Card
                    key={release.id}
                    hover
                    className={`group transition-all duration-300 hover:shadow-lg hover:scale-[1.02] ${
                      isActive
                        ? "cursor-pointer border-blue-500 ring-2 ring-blue-300"
                        : "cursor-default border-gray-200"
                    }`}
                    onClick={() => {
                      

                      if (isActive) {
                        handleReleaseSelect(release.id || release.releaseId);
                      }
                    }}
                  >
                    <CardContent className="p-6">
                      {}

                      <div className="mb-4">
                        <h3 className="text-lg font-semibold text-gray-900 mb-1">
                          {release.name}
                        </h3>
                      </div>

                      {}

                      <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                        {release.description}
                      </p>

                      {}

                      <div className="grid grid-cols-2 gap-4 mb-4">
                        <div className="flex items-center space-x-2">
                          <FileText className="w-4 h-4 text-gray-400" />

                          <div>
                            <p className="text-xs text-gray-500">Test Cases</p>

                            <p className="text-sm font-medium text-gray-900">
                              {releaseLoading ? (
                                <span className="flex items-center space-x-1">
                                  <div className="w-3 h-3 border-2 border-blue-500 border-t-transparent rounded-full animate-spin"></div>
                                  <span>Loading...</span>
                                </span>
                              ) : (
                                (() => {
                                  const count =
                                    releaseTestCaseCounts[release.releaseId] ??
                                    releaseTestCaseCounts[release.id] ??
                                    releaseTestCaseCounts[String(release.releaseId)] ??
                                    releaseTestCaseCounts[String(release.id)] ??
                                    (typeof release.totalTestCases === "number" ? release.totalTestCases : undefined) ??
                                    (typeof release.testCaseCount === "number" ? release.testCaseCount : undefined) ??
                                    (Array.isArray(release.testCases) ? release.testCases.length : 0);
                                  return count;
                                })()
                              )}
                            </p>

                            {Object.keys(releaseTestCaseCounts).length > 0 && (
                              <p className="text-xs text-gray-400">
                                Last updated: {new Date().toLocaleTimeString()}
                              </p>
                            )}
                          </div>
                        </div>

                        <div className="flex items-center space-x-2">
                          <Calendar className="w-4 h-4 text-gray-400" />

                          <div>
                            <p className="text-xs text-gray-500">
                              Release Date
                            </p>

                            <p className="text-sm font-medium text-gray-900">
                              {release.releaseDate
                                ? new Date(
                                    release.releaseDate,
                                  ).toLocaleDateString()
                                : "TBD"}
                            </p>
                          </div>
                        </div>
                      </div>

                      {}

                      {currentProject && (
                        <div className="pt-4 border-t border-gray-200">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-2">
                              <span className="text-xs text-gray-500">
                                Project: {currentProject.name}
                              </span>
                            </div>
                          </div>
                        </div>
                      )}

                      {}

                      {can.release.statusUpdate && 
                      <div className="flex gap-2 mt-4">
                        <button
                          type="button"
                          className={`px-3 py-1 rounded-full text-xs font-semibold border transition-colors duration-150 ${
                            isActive
                              ? "bg-blue-100 text-blue-700 border-blue-400"
                              : "bg-white text-gray-500 border-gray-300 hover:bg-blue-50"
                          }`}
                          onClick={async (e) => {
                            e.stopPropagation();
                            if (isActive) {
                                setToast({
                                  isOpen: true,
                                  message: "This release is already Active",
                                  type: "error",
                                });
                                return;
                              }

                            const alreadyActiveRelease = projectReleaseCard.find(
                              (r: any) =>
                                (r.status === "Active" || r.status === "ACTIVE" || r.status === true) &&
                                (r.id || r.releaseId) !== (release.id || release.releaseId)
                            );

                            if (alreadyActiveRelease) {
                              setToast({
                                isOpen: true,
                                message: `"${alreadyActiveRelease.name}" is already Active. Please Hold it first before activating another release.`,
                                type: "error",
                              });
                              return;
                            }

                           try {
                              await updateReleaseStatus(parseInt(release.id || release.releaseId), "ACTIVE");
                              setReleaseLoading(true);

                              const res = await projectReleaseCardView(selectedProject);
                              if (res.status === "Success" || res.statusCode === "200") {
                                const releaseList = res.data || [];
                                const filtered = releaseList.filter(
                                  (r: any) =>
                                    String(r.project_id) === String(selectedProject) ||
                                    String(r.projectId) === String(selectedProject)
                                );
                                setProjectReleaseCard(filtered);

                                const releaseIds = filtered
                                  .map((r: any) => r.id || r.releaseId)
                                  .filter((id: any) => id != null)
                                  .map(Number);

                                if (releaseIds.length > 0) {
                                  const countsRes = await getReleaseTestCaseCountsLoad(releaseIds);
                                  if (countsRes.status === "Success" || countsRes.statusCode === 200 || countsRes.status === "success") {
                                    const countsMap: { [key: string]: number } = {};
                                    const data = countsRes.data;
                                    if (Array.isArray(data)) {
                                      data.forEach((item: any) => {
                                        const releaseId = item?.releaseId ?? item?.id;
                                        const count = typeof item?.testCaseCount === "number" ? item.testCaseCount : (typeof item?.total === "number" ? item.total : 0);
                                        if (releaseId != null) {
                                          countsMap[String(releaseId)] = count;
                                          countsMap[Number(releaseId) as any] = count;
                                        }
                                      });
                                    } else if (data && typeof data === "object") {
                                      Object.entries(data).forEach(([key, val]: [string, any]) => {
                                        const count = typeof val === "number" ? val : (typeof val?.testCaseCount === "number" ? val.testCaseCount : (typeof val?.total === "number" ? val.total : 0));
                                        countsMap[key] = count;
                                        countsMap[Number(key) as any] = count;
                                      });
                                    }
                                    setReleaseTestCaseCounts(countsMap);
                                  } else {
                                    setReleaseTestCaseCounts({});
                                  }
                                } else {
                                  setReleaseTestCaseCounts({});
                                }
                              } else {
                                setReleaseError(res.message || "No releases found");
                              }

                              setToast({
                                isOpen: true,
                                message: "Release activated and other releases set to Hold for this project",
                                type: "success",
                              });
                            } catch (err: any) {
                              console.error("Failed to activate release:", err);
                              const message = err?.response?.data?.message || "Failed to activate release";
                              setToast({ isOpen: true, message, type: "error" });
                            } finally {
                              setReleaseLoading(false);
                            }
                          }}
                        >
                          Active
                        </button>

                        <button
                          type="button"
                          className={`px-3 py-1 rounded-full text-xs font-semibold border transition-colors duration-150 ${
                            !isActive
                              ? "bg-red-100 text-red-700 border-red-400"
                              : "bg-white text-gray-500 border-gray-300 hover:bg-red-50"
                          }`}
                          onClick={async (e) => {
                            e.stopPropagation();

                            

                             try {
                              await updateReleaseStatus(parseInt(release.id || release.releaseId), "HOLD");
                              setReleaseLoading(true);

                              const res = await projectReleaseCardView(selectedProject);
                              if (res.status === "Success" || res.statusCode === "200") {
                                const releaseList = res.data || [];
                                const filtered = releaseList.filter(
                                  (r: any) =>
                                    String(r.project_id) === String(selectedProject) ||
                                    String(r.projectId) === String(selectedProject)
                                );
                                setProjectReleaseCard(filtered);

                                const releaseIds = filtered
                                  .map((r: any) => r.id || r.releaseId)
                                  .filter((id: any) => id != null)
                                  .map(Number);

                                if (releaseIds.length > 0) {
                                  const countsRes = await getReleaseTestCaseCountsLoad(releaseIds);
                                  if (countsRes.status === "Success" || countsRes.statusCode === 200 || countsRes.status === "success") {
                                    const countsMap: { [key: string]: number } = {};
                                    const data = countsRes.data;
                                    if (Array.isArray(data)) {
                                      data.forEach((item: any) => {
                                        const releaseId = item?.releaseId ?? item?.id;
                                        const count = typeof item?.testCaseCount === "number" ? item.testCaseCount : (typeof item?.total === "number" ? item.total : 0);
                                        if (releaseId != null) {
                                          countsMap[String(releaseId)] = count;
                                          countsMap[Number(releaseId) as any] = count;
                                        }
                                      });
                                    } else if (data && typeof data === "object") {
                                      Object.entries(data).forEach(([key, val]: [string, any]) => {
                                        const count = typeof val === "number" ? val : (typeof val?.testCaseCount === "number" ? val.testCaseCount : (typeof val?.total === "number" ? val.total : 0));
                                        countsMap[key] = count;
                                        countsMap[Number(key) as any] = count;
                                      });
                                    }
                                    setReleaseTestCaseCounts(countsMap);
                                  } else {
                                    setReleaseTestCaseCounts({});
                                  }
                                } else {
                                  setReleaseTestCaseCounts({});
                                }
                              } else {
                                setReleaseError(res.message || "No releases found");
                              }

                              setToast({
                                isOpen: true,
                                message: "Release status set to Hold for this project",
                                type: "success",
                              });
                            } catch (err: any) {
                              console.error("Failed to hold release:", err);
                              const message = err?.response?.data?.message || "Failed to hold release";
                              setToast({ isOpen: true, message, type: "error" });
                            } finally {
                              setReleaseLoading(false);
                            }
                          }}
                        >
                          Hold
                        </button>
                      </div>}

                      {}

                      {isActive && (
                        <div className="mt-3 p-2 bg-blue-50 border border-blue-200 rounded-lg">
                          <div className="flex items-center space-x-2">
                            <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>

                            <span className="text-xs font-medium text-blue-700">
                              Active - Click to enter test execution
                            </span>
                          </div>
                        </div>
                      )}
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
