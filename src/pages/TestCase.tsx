import React, { useState, useEffect, useMemo, useRef } from "react";
import { OrbitProgress } from "react-loading-indicators";
import {
  Plus,
  Edit2,
  Trash2,
  Eye,
  ChevronRight,
  ChevronLeft,
} from "lucide-react";
import { Card, CardContent } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { Badge } from "../components/ui/Badge";
import AlertModal from "../components/ui/AlertModal";
import { useParams, useNavigate } from "react-router-dom";

import * as XLSX from "xlsx";
import { TestCase as TestCaseType } from "../types/index";
import { ProjectSelector } from "../components/ui/ProjectSelector";
import ModuleSelector from "../components/ui/ModuleSelector";
import { getAllProjects } from "../api/projectget";
import {
  getTestCasesByProjectAndSubmodule,
  getTestCasesByProjectAndModule,
  deleteTestCase,
} from "../api/testCase/testCaseApi";
import { getSeverities } from "../api/severity";
import { getDefectTypes } from "../api/defectType";
import { updateTestCase } from "../api/testCase/updateTestCase";
import { getModulesByProjectId } from "../api/module/getModule";
import {
  getSubmodulesByModule,
  Submodule,
} from "../api/submodule/submoduleget";
import {
  createTestCaseSub,
  CreateTestCaseRequest,
} from "../api/testCase/createTestcase";
const BASE_URL = import.meta.env.VITE_BASE_URL;


import QuickAddTestCase from "./QuickAddTestCase";
import { useApp } from "../context/AppContext";
import apiClient from "../lib/api";
import { importTestCases } from "../api/importTestCase";
import { useAccessibleProjects } from "../api/useAccessibleProjects";
import { usePermission } from "../context/PermissionContext";











export const TestCase: React.FC = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const { setSelectedProjectId: setGlobalProjectId } = useApp();
  const [selectedProjectId, setSelectedProjectId] = useState<string>(
    String(projectId ?? ""),
  );
  const [selectedModuleId, setSelectedModuleId] = useState<string | null>(null);
  const [selectedSubmoduleId, setSelectedSubmoduleId] = useState<string | null>(
    null,
  );

  const { can } = usePermission();
  const { projects } = useAccessibleProjects();

  const [testCases, setTestCases] = useState<TestCaseType[]>([]);
  const [totalPagesFromServer, setTotalPagesFromServer] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [isServerPaginated, setIsServerPaginated] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [isImporting, setIsImporting] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [allModuleTestCases, setAllModuleTestCases] = useState<TestCaseType[]>(
    [],
  );
  const sortTestCasesByNo = (testCases: TestCaseType[]) => {
  return [...testCases].sort((a, b) => {
    const aNo =  a.testcaseNo || '';
    const bNo =  b.testcaseNo || '';
    return aNo.localeCompare(bNo);
  });
};
  // Add state for modules by project
  const [modulesByProject, setModulesByProject] = useState<
    Record<
      string,
      { id: string; name: string; submodules: { id: string; name: string }[] }[]
    >
  >({});
  const fetchAllTestCasesForProject = async (projId: string) => {
    try {
      const pId = projId || selectedProjectId;
      if (!pId) return;
      const res = await apiClient.get(`/api/v1/project/${pId}/filter`);
      const testCases = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      const merged = testCases.map((tc: any) => {
        const typeName = tc.defectTypeName || tc.type || tc.defectType || "";
        const tcNum = tc.testCaseNumber ? `TC-${tc.testCaseNumber}` : (tc.testcaseNo || tc.no || `TC-${tc.testCaseId || tc.id}`);
        return {
          ...tc,
          id: tc.testCaseId || tc.id,
          no: tcNum,
          testcaseNo: tcNum,
          testCaseId: tc.testCaseId || tc.id,
          description: tc.description,
          detailsSteps: tc.testSteps || tc.steps || tc.detailsSteps || "",
          steps: tc.testSteps || tc.steps || tc.detailsSteps || "",
          expectedResult: tc.expectedResult || '',
          severity: tc.severityName || tc.severity || "",
          severityName: tc.severityName || tc.severity || "",
          defectType: typeName,
          defectTypeName: typeName,
          type: typeName,
        };
      });

      const sorted = sortTestCasesByNo(merged as any);
      setAllModuleTestCases(sorted);
      setTestCases(sorted);
      setIsServerPaginated(false);
    } catch (err) {
      console.error("Error fetching project-level test cases:", err);
    }
  };

  
  useEffect(() => {
    setSelectedModuleId(null);
    setSelectedSubmoduleId(null);
    setCurrentPage(1);
  }, [selectedProjectId]);

  
  useEffect(() => {
    getSeverities().then((res) => setSeverities(res.data.content));
    getDefectTypes().then((res) => setDefectTypes(res.data.content));
  }, []);

  
  useEffect(() => {
    if (!selectedProjectId) return;
    getModulesByProjectId(selectedProjectId).then((res) => {
      console.log("Fetched modules for project", selectedProjectId, res.data);

      const modules = (res.data || []).map((mod: any) => ({
        id: String(mod.id),
        name: mod.moduleName || mod.name,
        submodules: (mod.submodules || []).map((sm: any) => ({
          id: String(sm.id),
          name: sm.getSubModuleName || sm.name,
        })),
      }));
      setModulesByProject((prev) => ({
        ...prev,
        [selectedProjectId]: modules,
      }));
    });
  }, [selectedProjectId]);
  console.log("modulesByProject", modulesByProject);

  
  const projectModules = useMemo(() => {
    return selectedProjectId
      ? modulesByProject[selectedProjectId] || []
      : [];
  }, [modulesByProject, selectedProjectId]);

  
  
  const [isViewStepsModalOpen, setIsViewStepsModalOpen] = useState(false);
  const [isViewTestCaseModalOpen, setIsViewTestCaseModalOpen] = useState(false);
  
  const [viewingTestCase, setViewingTestCase] = useState<TestCaseType | null>(
    null,
  );
  
  const [isDescriptionModalOpen, setIsDescriptionModalOpen] = useState(false);
  const [selectedDescription, setSelectedDescription] = useState("");
  const [selectedModules] = useState<string[]>([]); // setSelectedModules unused
  const [selectedSubmodules] = useState<string[]>([]); // setSelectedSubmodules unused
  // const [filterText, setFilterText] = useState(""); // Unused
  // const [filterType, setFilterType] = useState(""); // Unused
  // const [filterSeverity, setFilterSeverity] = useState(""); // Unused

  // --- Multi-modal state for bulk add like QuickAddTestCase ---
  // Extend ModalFormData to include moduleId and subModuleId
  interface ModalFormData {
    module: string;
    subModule: string;
    description: string;
    steps: string;
    type: string;
    severity: string;
    projectId: string | undefined;
    id?: string;
    moduleId?: string | number;
    subModuleId?: string | number;
  }
  const defaultFormData: ModalFormData = {
    module: "",
    subModule: "",
    description: "",
    steps: "",
    type: "",
    severity: "",
    projectId: selectedProjectId,
  };

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState<ModalFormData>({
    ...defaultFormData,
    projectId: selectedProjectId,
  });
  const isEditMode = !!formData.id;

  const [modals, setModals] = useState<
    {
      open: boolean;
      formData: ModalFormData;
    }[]
  >([
    {
      open: false,
      formData: {
        module: "",
        subModule: "",
        description: "",
        steps: "",
        type: "functional",
        severity: "", // changed from "medium"
        projectId: projectId,
      },
    },
  ]);
  console.log("modals", modals);

  const [currentModalIdx, setCurrentModalIdx] = useState(0);
  
  

  
  
  
  

  
  const fileInputRef = useRef<HTMLInputElement>(null);

  
  const [submodules, setSubmodules] = useState<Submodule[]>([]);
  const [submoduleError, setSubmoduleError] = useState<string>("");
  console.log({ submodules });

  // Fetch submodules when selectedModuleId changes
  useEffect(() => {
    if (!selectedModuleId) {
      setSubmodules([]);
      setSubmoduleError("");
      return;
    }
    getSubmodulesByModule(Number(selectedModuleId))
      .then((res) => {
        setSubmodules(res.data || []);
        setSubmoduleError("");
      })
      .catch((err) => {
        if (err?.response?.status === 404) {
          setSubmodules([]);
          setSubmoduleError("No submodules found for this module.");
        } else {
          setSubmodules([]);
          setSubmoduleError("Failed to fetch submodules. Please try again.");
        }
      });
  }, [selectedModuleId]);
  console.log("submodule", submodules);
  
  const [severities, setSeverities] = useState<
    { id: number; name: string; color: string }[]
  >([]);
  const [defectTypes, setDefectTypes] = useState<
    { id: number; name: string }[]
  >([]);

  
  const [toast, setToast] = useState<{
    isOpen: boolean;
    message: string;
    type: "success" | "error";
  }>({
    isOpen: false,
    message: "",
    type: "success",
  });

  
  const [createAlert, setCreateAlert] = useState({
    isOpen: false,
    message: "",
  });
  const [updateAlert, setUpdateAlert] = useState({
    isOpen: false,
    message: "",
  });
  const [deleteAlert, setDeleteAlert] = useState({
    isOpen: false,
    message: "",
  });
  // Pending success flags
  const [pendingCreateSuccess, setPendingCreateSuccess] = useState(false);
  const [pendingUpdateSuccess, setPendingUpdateSuccess] = useState(false);
  const [pendingDeleteSuccess, setPendingDeleteSuccess] = useState(false);

  // Fetch test cases when project, module, submodule, page, or page size changes
  useEffect(() => {
    if (selectedProjectId) {
      if (selectedModuleId) {
        refreshTestCases();
      } else if (modulesByProject[selectedProjectId]?.length > 0) {
        fetchAllTestCasesForProject(selectedProjectId);
      } else {
        setTestCases([]);
      }
    } else {
      setTestCases([]);
    }
  }, [
    selectedProjectId,
    selectedModuleId,
    selectedSubmoduleId,
    currentPage,
    rowsPerPage,
    modulesByProject,
  ]);

  // Add after state declarations
  const [allSubmoduleTestCases, setAllSubmoduleTestCases] = useState<{
    [submoduleId: string]: TestCaseType[];
  }>({});

  // Extract fetchAllSubmoduleTestCases as a callback
  const fetchAllSubmoduleTestCases = React.useCallback(() => {
    if (!selectedProjectId || !selectedModuleId || !Array.isArray(submodules) || submodules.length === 0) return;

    Promise.all(
      submodules.map((sm) =>
        getTestCasesByProjectAndSubmodule(
          selectedProjectId,
          String(sm.id),
          undefined,
          undefined,
          undefined,
          0,
          1 // Pass size = 1 to load counts instantly without fetching full records!
        ).then((data) => ({ submoduleId: String(sm.id), testCases: data })),
      ),
    ).then((results) => {
      const testCaseMap: { [submoduleId: string]: TestCaseType[] } = {};
      const moduleMap = Object.fromEntries(
        projectModules.map((m: any) => [m.id, m.name]),
      );
      const submoduleMap = Object.fromEntries(
        submodules.map((sm: any) => [sm.id, sm.name]),
      );

      results.forEach(({ submoduleId, testCases }) => {
        const mappedList = (testCases as any[]).map((tc: any) => {
          const typeVal =
            tc.defectTypeName ||
            tc.type ||
            tc.defectType ||
            (defectTypes && defectTypes.find((dt) => String(dt.id) === String(tc.defectTypeId))?.name) ||
            "";
          return {
            ...tc,
            no: tc.no || tc.testcaseNo,
            testcaseNo: tc.testcaseNo || tc.no,
            moduleId: tc.moduleId, // always keep the ID
            module: moduleMap[tc.moduleId] || tc.moduleName || tc.module, // display name
            subModuleId: tc.subModuleId, // always keep the ID
            subModule:
              submoduleMap[tc.subModuleId] || tc.subModuleName || tc.subModule, // display name
            severity: ((severities &&
              severities.find((s) => String(s.id) === String(tc.severityId))?.name) ||
              tc.severityName ||
              tc.severity ||
              "") as TestCaseType["severity"],
            type: typeVal as TestCaseType["type"],
            defectType: typeVal,
            defectTypeName: typeVal,
          };
        }) as TestCaseType[];

        
        (mappedList as any).totalPages = (testCases as any).totalPages;
        (mappedList as any).totalElements = (testCases as any).totalElements;
        (mappedList as any).isServerPaginated = (testCases as any).isServerPaginated;

        testCaseMap[submoduleId] = mappedList;
      });
      setAllSubmoduleTestCases(testCaseMap);
    });
  }, [
    selectedProjectId,
    selectedModuleId,
    submodules,
    projectModules,
    severities,
    defectTypes,
  ]);

  
  
  useEffect(() => {
    fetchAllSubmoduleTestCases();
  }, [fetchAllSubmoduleTestCases]);


  useEffect(() => {
    const handleTestCaseCreated = (event: Event) => {
      const detail = (event as CustomEvent).detail || {};
      const createdProjectId = detail.projectId;

      
      if (
        createdProjectId &&
        selectedProjectId &&
        String(createdProjectId) !== String(selectedProjectId)
      ) {
        return;
      }

      if (!selectedProjectId) return;

      if (selectedModuleId) {
        
        refreshTestCases();
      } else {
        
        fetchAllTestCasesForProject(selectedProjectId);
      }
      
      fetchAllSubmoduleTestCases();
    };

    window.addEventListener("testCaseCreated", handleTestCaseCreated);
    return () => {
      window.removeEventListener("testCaseCreated", handleTestCaseCreated);
    };
  }, [selectedProjectId, selectedModuleId, selectedSubmoduleId, fetchAllSubmoduleTestCases]);

  
  if (!selectedProjectId) {
    return (
      <div className="p-8 text-center text-gray-500">
        Please select a project to view its test cases.
      </div>
    );
  }

  
  const selectedTestCaseIds = useMemo(() => {
    let ids: string[] = [];
    if (selectedModules.length > 0) {
      ids = [
        ...new Set(
          testCases
            .filter(
              (tc) =>
                tc.projectId === String(selectedProjectId) &&
                selectedModules.includes(tc.module ?? ""),
            )
            .map((tc) => tc.id),
        ),
      ];
    }
    if (selectedSubmodules.length > 0) {
      ids = [
        ...ids,
        ...new Set(
          testCases
            .filter(
              (tc) =>
                tc.projectId === String(selectedProjectId) &&
                selectedSubmodules.includes(tc.subModule ?? ""),
            )
            .map((tc) => tc.id),
        ),
      ];
    }
    return Array.from(new Set(ids));
  }, [selectedModules, selectedSubmodules, testCases, selectedProjectId]);

  // Compute filtered test cases for the table (show all for module)
  const filteredTestCases = testCases;

  // Handle project selection
  // const handleProjectSelect = (projectId: string) => {
  //   setSelectedProjectId(projectId);
  //   navigate(`/projects/${projectId}/test-cases`);
  // };

  // Handle module selection
  // const handleModuleSelect = (moduleId: string) => {
  //   setSelectedModuleId(Number(moduleId));
  //   setSelectedSubmoduleId(null);
  //   setSelectedTestCases([]);
  //   fetch(`${BASE_URL}testcase/module/${moduleId}`)
  //     .then((res) => res.json())
  //     .then((data) => {
  //       const mapped = (data.data || []).map((tc: any) => ({
  //         id: tc.testCaseId || tc.id,
  //         description: tc.description,
  //         steps: tc.steps,
  //         subModule: tc.subModuleId || tc.subModule,
  //         module: tc.moduleId || tc.module,
  //         projectId: tc.projectId,
  //         severity: tc.severityName || tc.severityId || tc.severity,
  //         type: tc.typeId || tc.type,
  //       }));
  //       setTestCases(mapped);
  //     });
  // };

  // When selection changes, update selectedTestCases for bulk actions
  // useEffect(() => {
  //   if (selectedModules.length > 0 || selectedSubmodules.length > 0) {
  //     setSelectedTestCases(selectedTestCaseIds);
  //   }
  // }, [selectedTestCaseIds, selectedModules, selectedSubmodules]); // Removed

  // const handleInputChange = (idx: number, field: string, value: string) => {
  //   setModals((prev) =>
  //     prev.map((modal, i) =>
  //       i === idx
  //         ? { ...modal, formData: { ...modal.formData, [field]: value } }
  //         : modal,
  //     ),
  //   );
  // };
  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  // const handleAddAnother = () => {
  //   setModals((prev) => [
  //     ...prev,
  //     {
  //       open: true,
  //       formData: {
  //         module: "",
  //         subModule: "",
  //         description: "",
  //         steps: "",
  //         type: "functional",
  
  
  
  
  
  
  

  
  
  
  
  
  
  
  
  

  const handleImportExcelButton = () => {
    fileInputRef.current?.click();
  };
  const handleImportExcelInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!selectedProjectId) {
      showAlert("Please select a project before importing test cases.");
      return;
    }
    
    const allowedTypes = [
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "application/vnd.ms-excel",
      "text/csv",
    ];
    if (
      !allowedTypes.includes(file.type) &&
      !file.name.endsWith(".xlsx") &&
      !file.name.endsWith(".csv")
    ) {
      showAlert("Please select a valid Excel (.xlsx) or CSV (.csv) file.");
      return;
    }
    const formData = new FormData();
    
    formData.append("file", file); 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    setIsImporting(true);
    importTestCases(formData, selectedProjectId)
      .then((response) => {
        console.log("Import TestCase Response:", response);
        setCreateAlert({
          isOpen: true,
          message: response?.message || "Test cases imported successfully!",
        });
        if (selectedProjectId) {
          if (selectedModuleId) {
            
            refreshTestCases();
          } else {
            
            fetchAllTestCasesForProject(selectedProjectId);
          }
        }
        setSearchResults(null);
        setClientFilteredResults(null);
        setSearchFilters({ description: "", typeId: "", severityId: "", submoduleId: "" });
    
        if (fileInputRef.current) fileInputRef.current.value = "";
      })
      .catch((error) => {
        console.error("Import TestCase Error:", error);
        setCreateAlert({
          isOpen: true,
          message:
            error?.response?.data?.message ||
            "Failed to import test cases. Please try again.",
        });
        if (fileInputRef.current) fileInputRef.current.value = "";
      })
      .finally(() => {
        setIsImporting(false);
      });
  };
  const handleExportExcel = () => {
    const wsData = [
      [
        "Module",
        "Sub Module",
        "Description",
        "Steps",
        "Type",
        "Severity",
        "Test Case ID",
      ],
      ...filteredTestCases.map((tc) => [
        tc.module,
        tc.subModule,
        tc.description,
        tc.steps,
        tc.type,
        tc.severity,
        tc.id,
      ]),
    ];
    const ws = XLSX.utils.aoa_to_sheet(wsData);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "TestCases");
    XLSX.writeFile(
      wb,
      `TestCases_${selectedProjectId}_${selectedModuleId}.xlsx`,
    );
  };

  
  
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  
  
  
  
  
  
  
  
  

  
  
  
  

  
  
  
  
  
  
  
  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  
  
  
  

  
  
  
  
  
  
  
  
  

  
  
  
  
  
  
  

  
  

  
  
  
  
  
  
  
  

  

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  const handleSubmitAll = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    
    if (formData.id) {
      const severityId = severities.find(
        (s) => s.name === formData.severity,
      )?.id;
      const defectTypeId = defectTypes.find(
        (dt) => dt.name === formData.type,
      )?.id;

      const payload: any = {
        description: formData.description,
        detailsSteps: formData.steps,
        subModuleId: Number(formData.subModuleId),
        moduleId: Number(formData.moduleId || selectedModuleId),
        projectId: Number(formData.projectId || selectedProjectId),
        ...(typeof severityId === "number" && { severityId }),
        ...(typeof defectTypeId === "number" && { defectTypeId }),
      };

      try {
        const response = await updateTestCase(
          Number(formData.subModuleId),
          formData.id,
          payload,
        );
        if (response?.status === "Failure") {
          setUpdateAlert({
            isOpen: true,
            message: response?.message || "Failed to update.",
          });
          return;
        }
        setUpdateAlert({
          isOpen: true,
          message: response?.statusMessage || "Updated successfully!",
        });
        if (selectedModuleId) {
          await refreshTestCases();
          await fetchAllSubmoduleTestCases();
        } else {
          if (selectedProjectId) {
            await fetchAllTestCasesForProject(selectedProjectId);
          }
        }
        setTimeout(() => {
          setFormData(defaultFormData);
          setIsModalOpen(false);
          setUpdateAlert({ isOpen: false, message: "" });
        }, 1200);

      } catch (error: any) {
        setUpdateAlert({
          isOpen: true,
          message:
            error?.response?.data?.message || "Failed to update test case.",
        });
      }
      return;
    }

    // ─── CREATE MODE ─────────────────────────────────────
    const subModuleId = formData.subModuleId
      ? Number(formData.subModuleId)
      : null;
    const severityId = severities.find((s) => s.name === formData.severity)?.id;
    const defectTypeId = defectTypes.find(
      (dt) => dt.name === formData.type,
    )?.id;

    if (!subModuleId || !severityId || !defectTypeId) {
      setCreateAlert({ isOpen: true, message: "Missing required fields." });
      return;
    }

    const payload: CreateTestCaseRequest = {
      description: formData.description,
      detailsSteps: formData.steps,
      severityId,
      defectTypeId,
      projectId: Number(selectedProjectId || formData.projectId),
      moduleId: Number(formData.moduleId || selectedModuleId),
    };

    try {
      const response = await createTestCaseSub(subModuleId, payload);
      if (
        response?.statusCode === 201 ||
        response?.statusCode === 200 ||
        response?.status === "Created" ||
        response?.status === "success" ||
        response?.status === "Success"
      ) {
        setCreateAlert({
          isOpen: true,
          message: response?.statusMessage || (response as any)?.message || "Test case created successfully!",
        });

      if (selectedModuleId) {
        await refreshTestCases();
        await fetchAllSubmoduleTestCases();
      } else {
        if (selectedProjectId) {
          await fetchAllTestCasesForProject(selectedProjectId);
        }
      }
        setTimeout(() => resetFormAndRefresh(), 1200);
      } else {
        setCreateAlert({
          isOpen: true,
          message: response?.statusMessage || "Failed to create.",
        });
      }
    } catch (error: any) {
      setCreateAlert({
        isOpen: true,
        message:
          error?.response?.data?.message || "Failed to create test case.",
      });
    }
  };

  const resetFormAndRefresh = () => {
    setFormData(defaultFormData);
    setIsModalOpen(false);
    setCreateAlert({ isOpen: false, message: "" });
    setUpdateAlert({ isOpen: false, message: "" });
    refreshTestCases();
    fetchAllSubmoduleTestCases();
  };

  const getSeverityColor = (severityName: string | undefined | null) => {
    if (!severityName) return "bg-gray-100 text-gray-800";

    const severity = (severities || []).find(
      (s) => ((s?.name || (s as any)?.severityName || '').toLowerCase() === String(severityName).toLowerCase()),
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

  
  const formatTestCaseId = (
    testCase: TestCaseType | undefined | null,
  ): string => {
    
    if (!testCase) {
      return "N/A";
    }

    if (testCase.testcaseNo) {
      return testCase.testcaseNo;
    }
    if (testCase.no) {
      return testCase.no;
    }
    if ((testCase as any).testCaseNumber) {
      return `TC-${(testCase as any).testCaseNumber}`;
    }
    if (testCase.id) {
      return `TC-${testCase.id}`;
    }
    if ((testCase as any).testCaseId) {
      return `TC-${(testCase as any).testCaseId}`;
    }

    return "N/A";
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

  
  
  
  
  
  
  

  
  
  
  
  
  
  

  const handleViewSteps = (testCase: TestCaseType) => {
    setViewingTestCase(testCase);
    setIsViewStepsModalOpen(true);
  };

  const handleViewTestCase = (testCase: TestCaseType) => {
    setViewingTestCase(testCase);
    setIsViewTestCaseModalOpen(true);
  };
  

  
  
  
  
  
  
  
  
  
  
  

  
  
  
  

  
  const [searchFilters, setSearchFilters] = useState({
    description: "",
    typeId: "",
    severityId: "",
    submoduleId: "",
  });
  const [searchResults, setSearchResults] = useState<TestCaseType[] | null>(
    null,
  );
  console.log("searchResults", searchResults);

  const [isSearching, setIsSearching] = useState(false);

  
  const [clientFilteredResults, setClientFilteredResults] = useState<
    TestCaseType[] | null
  >(null);

  
  const searchTimeoutRef = useRef<number | null>(null);
  const filterTimeoutRef = useRef<number | null>(null);

  
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
      if (filterTimeoutRef.current) {
        clearTimeout(filterTimeoutRef.current);
      }
    };
  }, []);

  
  const handleSubmoduleSelect = (submoduleId: string | null) => {
    setSelectedSubmoduleId(submoduleId);
    setSearchResults(null);
    setClientFilteredResults(null);
    setSearchFilters({
      description: "",
      typeId: "",
      severityId: "",
      submoduleId: "",
    });
    setCurrentPage(1);
  };
  // Add state to track submodules for each modal
  // const [modalSubmodules, setModalSubmodules] = useState<Submodule[][]>([]);

  // Add a useEffect to fetch submodules for the selected module in the current modal
  // useEffect(() => {
  //   const currentModal = modals[currentModalIdx];
  //   // Skip fetching submodules if we're in edit mode (formData.id exists)
  //   if (currentModal?.formData.id) {
  //     return;
  //   }
  //   const currentModuleName = currentModal?.formData.module;
  //   const moduleObj =
  //     projectModules &&
  //     projectModules.find((m: any) => m.name === currentModuleName);
  //   if (moduleObj && moduleObj.id) {
  //     getSubmodulesByModule(Number(moduleObj.id)).then((res) => {
  //       setModalSubmodules((prev) => {
  //         const copy = [...prev];
  //         copy[currentModalIdx] = res.data || [];
  //         return copy;
  //       });
  //     });
  //   } else {
  //     setModalSubmodules((prev) => {
  //       const copy = [...prev];
  //       copy[currentModalIdx] = [];
  //       return copy;
  //     });
  //   }
  // }, [
  //   modals[currentModalIdx]?.formData.module,
  //   projectModules,
  //   currentModalIdx,
  // ]);

  // General helper for mapping
  const getNameByIdOrValue = (
    value: string | number | undefined | null,
    list: any[],
    idKey: string = "id",
    nameKey: string = "name",
  ) => {
    if (!value) return "";
    const found = list.find(
      (item) =>
        String(item[idKey]) === String(value) || item[nameKey] === value,
    );
    return found ? found[nameKey] : String(value); // fallback to value if not found
  };

  const getModuleName = (value: string | number | undefined | null) => {
    if (!value) return "";
    const found = projectModules.find(
      (m) => String(m.id) === String(value) || m.name === value,
    );
    return found ? found.name : "Unknown Module";
  };
  const getSubmoduleName = (value: string | number | undefined | null) => {
    if (!value) return "";
    for (const mod of projectModules) {
      const found = mod.submodules.find(
        (sm) => String(sm.id) === String(value) || sm.name === value,
      );
      if (found) return found.name;
    }
    if (Array.isArray(submodules)) {
      const foundGlobal = submodules.find(
        (sm) => String(sm.id) === String(value) || sm.name === value,
      );
      if (foundGlobal) return foundGlobal.name;
    }
    return "Unknown Submodule";
  };
  const getTypeName = (value: string | number | undefined | null) =>
    getNameByIdOrValue(value, defectTypes, "id", "name");
  const getSeverityName = (value: string | number | undefined | null) =>
    getNameByIdOrValue(value, severities, "id", "name");

  
  const handleDeleteTestCase = async (
    testCaseId: string,
    subModuleId: number,
  ) => {
    console.log("Deleting test case:", { testCaseId, subModuleId });
    try {
      const response = await deleteTestCase(
        subModuleId,
        testCaseId,
        Number(selectedProjectId || 1),
        Number(selectedModuleId || 1)
      );
      console.log("Delete response:", response);
      return response;
    } catch (error: any) {
      console.error("Delete error:", error);
      throw error;
    }
  };


  
  const tableData =
    searchResults !== null
      ? searchResults
      : clientFilteredResults !== null
        ? clientFilteredResults
        : filteredTestCases;
  const totalRows = isServerPaginated ? totalElements : tableData.length;
  const totalPages = isServerPaginated
    ? totalPagesFromServer
    : (Math.ceil(totalRows / rowsPerPage) || 1);
  const paginatedTestCases = isServerPaginated
    ? tableData
    : tableData.slice(
        (currentPage - 1) * rowsPerPage,
        currentPage * rowsPerPage,
      );

  
  useEffect(() => {
    setCurrentPage(1);
  }, [searchResults, clientFilteredResults]);

  
  useEffect(() => {
    setCurrentPage(1);
  }, [rowsPerPage]);

  
  
  
  
  
  
  
  

  console.log("paginatedTestCases", paginatedTestCases);

  
  useEffect(() => {
    console.log("Create alert state changed:", createAlert);
  }, [createAlert]);

  useEffect(() => {
    console.log("Update alert state changed:", updateAlert);
  }, [updateAlert]);

  
  function extractNumericId(id: string) {
    return id.replace(/\D/g, "").replace(/^0+/, "");
  }

  const [alert, setAlert] = useState({ isOpen: false, message: "" });
  const showAlert = (message: string) => setAlert({ isOpen: true, message });
  const closeAlert = () => setAlert((a) => ({ ...a, isOpen: false }));

  // Add a helper to check if the current modal form is valid
  const isCurrentModalValid = (modal: ModalFormData) => {
    return (
      modal.module &&
      modal.subModule &&
      modal.description &&
      modal.steps &&
      modal.type &&
      modal.severity
    );
  };

  const refreshTestCases = async () => {
  if (!selectedProjectId || !selectedModuleId) {
    return;
  }

    try {
      let responseArr;
      if (selectedSubmoduleId) {
        responseArr = await getTestCasesByProjectAndSubmodule(
          selectedProjectId,
          String(selectedSubmoduleId),
          undefined,
          undefined,
          undefined,
          currentPage - 1,
          rowsPerPage
        );
      } else {
        responseArr = await getTestCasesByProjectAndModule(
          selectedProjectId,
          selectedModuleId,
          currentPage - 1,
          rowsPerPage
        );
      }

      const mappedTestCases = (responseArr as any[]).map((tc: any) => {
        const typeVal =
          tc.defectTypeName ||
          tc.type ||
          tc.defectType ||
          (defectTypes || []).find((dt) => String(dt.id) === String(tc.defectTypeId))?.name ||
          "";
        return {
          ...tc,
          id: tc.id,
          no: tc.no || tc.testcaseNo,
          testcaseNo: tc.testcaseNo || tc.no,
          testCaseId: tc.id,
          moduleId: tc.moduleId,
          module: tc.moduleName || tc.module,
          subModuleId: tc.subModuleId,
          subModule: tc.subModuleName || tc.subModule,
          detailsSteps: tc.detailsSteps || tc.steps || "",
          steps: tc.detailsSteps || tc.steps || "",
          severity: ((severities || []).find((s) => String(s.id) === String(tc.severityId))?.name ||
            tc.severityName ||
            tc.severity ||
            "") as TestCaseType["severity"],
          severityName: tc.severityName || tc.severity || "",
          type: typeVal as TestCaseType["type"],
          defectType: typeVal,
          defectTypeName: typeVal,
        };
      }) as TestCaseType[];

      
      const sorted = sortTestCasesByNo(mappedTestCases);
      setAllModuleTestCases(sorted);


      
      if (selectedSubmoduleId) {
        setTestCases(
          mappedTestCases.filter(
            (tc) => String(tc.subModuleId) === String(selectedSubmoduleId),
          ),
        );
      } else {
        setTestCases(mappedTestCases);
      }

      
      setTotalPagesFromServer((responseArr as any).totalPages ?? 1);
      setTotalElements((responseArr as any).totalElements ?? responseArr.length);
      setIsServerPaginated((responseArr as any).isServerPaginated ?? false);

      if (pageAfterDeleteRef.current !== null) {
        setCurrentPage(pageAfterDeleteRef.current);
        pageAfterDeleteRef.current = null;
      }
    } catch (error) {
      console.error("Error refreshing test cases:", error);
    }
  };
  
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
  const [pendingDeleteSubModuleId, setPendingDeleteSubModuleId] = useState<
    number | null
  >(null);

  const pageAfterDeleteRef = useRef<number | null>(null);

  const [showQuickAdd, setShowQuickAdd] = useState(false);

  const exportTestCases = async () => {
    if (!selectedProjectId) {
      showAlert("Please select a project before exporting test cases.");
      return;
    }

    setIsExporting(true);
    try {
      const pId = selectedProjectId || '1';
      const res = await apiClient.get(`/api/v1/project/${pId}/filter`);
      const testCasesList = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      const headers = ["Test Case No", "Description", "Severity", "Defect Type", "Module", "Submodule"];
      const rows = testCasesList.map((t: any) => [
        t.testCaseNumber ? `TC-${t.testCaseNumber}` : (t.testcaseNo || t.no || ''),
        `"${(t.description || '').replace(/"/g, '""')}"`,
        t.severityName || 'Medium',
        t.defectTypeName || 'Functional Bug',
        t.moduleName || 'Module',
        t.subModuleName || 'Submodule',
      ]);

      const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(e => e.join(","))].join("\n");
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", `testcases_project_${selectedProjectId}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      showAlert("✅ Test cases exported successfully!");
    } catch (error: any) {
      showAlert("Failed to export test cases.");
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto ">
      {}
      <div className="flex-none p-6 pb-4">
        <div className="flex justify-between items-center mb-4">
          <div className="space-y-1">
            <h1 className="text-2xl font-bold text-gray-900">Test Cases</h1>
          </div>
          {}
        </div>
        {}
        <ProjectSelector
          projects={projects}
          selectedProjectId={selectedProjectId || ""}
          onSelect={(id) => {
            setSelectedProjectId(id);
            setGlobalProjectId(id); // keep global context in sync
            navigate(`/projects/${id}/test-cases`);
            if (modulesByProject[id]?.length > 0) {
              fetchAllTestCasesForProject(id);
            }
          }}
        />

        {/* Filter Options Above Table */}
        {selectedProjectId && (
          <div className="flex justify-end gap-2 mb-2 py-5 ">
            {can.testCase.create && (
              <button
                type="button"
                className="flex items-center px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded shadow font-medium"
                onClick={() => {
                  const currentMod = projectModules.find((m: any) => String(m.id) === String(selectedModuleId));
                  const currentSub = currentMod?.submodules?.find((s: any) => String(s.id) === String(selectedSubmoduleId));
                  setFormData({
                    ...defaultFormData,
                    projectId: selectedProjectId,
                    moduleId: selectedModuleId || (projectModules.length > 0 ? projectModules[0].id : undefined),
                    module: currentMod?.name || (projectModules.length > 0 ? projectModules[0].name : ""),
                    subModuleId: selectedSubmoduleId || (currentMod?.submodules?.length ? currentMod.submodules[0].id : ""),
                    subModule: currentSub?.name || (currentMod?.submodules?.length ? currentMod.submodules[0].name : ""),
                  });
                  setIsModalOpen(true);
                }}
              >
                <Plus className="w-4 h-4 mr-2" />
                Add Test Case
              </button>
            )}
            {can.testCase.create && (
              <button
                type="button"
                className="flex items-center px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded shadow"
                onClick={handleImportExcelButton}
              >
                <svg
                  className="w-4 h-4 mr-2"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5m0 0l5-5m-5 5V4"
                  />
                </svg>
                Import from Excel
              </button>
            )}
            <input
              type="file"
              accept=".xlsx,.csv"
              onChange={handleImportExcelInput}
              ref={fileInputRef}
              className="hidden"
            />
            <button
              type="button"
              className="flex items-center px-3 py-2 bg-green-600 hover:bg-green-700 text-white rounded shadow"
              onClick={exportTestCases}
            >
              <svg
                className="w-4 h-4 mr-2"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M12 12V4m0 0l-4 4m4-4l4 4"
                />
              </svg>
              Export to Excel
            </button>
          </div>
        )}
      </div>

      {}
      <div className="flex-1 px-6 pb-6">
        <div className="flex flex-col">
          {}
          {selectedProjectId && (
            <ModuleSelector
              modules={projectModules}
              selectedModuleId={selectedModuleId}
              onSelect={async (id) => {
                setSelectedModuleId(id);
                setSelectedSubmoduleId(null);
                setSearchResults(null);
                setClientFilteredResults(null);

                
                
                
                
                

                
                
                
                

                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                
                

                
                
                
                
                
                
                
                
                
                
                
                
                
                
                

                console.log(
                  "Module selected:",
                  id,
                  "Project:",
                  selectedProjectId,
                );
                setSelectedModuleId(id);
                setSelectedSubmoduleId(null);
                setSearchResults(null);
                setClientFilteredResults(null);
                setCurrentPage(1);
              }}
              className="mb-4"
            />
          )}

          {}
          {selectedProjectId &&
            (selectedModuleId ? (
              <Card className="mb-4">
                <CardContent className="p-4">
                  <div className="flex justify-between items-center mb-3">
                    <h2 className="text-lg font-semibold text-gray-900">
                      Submodule Selection
                    </h2>
                  </div>
                  {submoduleError && (
                    <div className="mb-2 text-red-600 text-sm">
                      {submoduleError}
                    </div>
                  )}
                  <div className="relative flex items-center">
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
                      className="flex space-x-2 overflow-x-auto pb-2 scroll-smooth flex-1"
                      style={{
                        scrollbarWidth: "none",
                        msOverflowStyle: "none",
                        maxWidth: "100%",
                      }}
                    >
                      {submodules.map((x: any) => {
                        const subCases = allSubmoduleTestCases[String(x.id)] || [];
                        const submoduleTestCasesCount = (subCases as any).totalElements ?? subCases.length;
                        return (
                          <div key={x.id} className="flex items-center">
                            <div className="flex items-center border border-gray-200 rounded-lg p-0.5 bg-white hover:border-gray-300 transition-colors">
                              <Button
                                variant={
                                  selectedSubmoduleId === x.id
                                    ? "primary"
                                    : "secondary"
                                }
                                onClick={() => handleSubmoduleSelect(x.id)}
                                className="whitespace-nowrap border-0 m-2"
                              >
                                {x.name}
                                <Badge variant="info" className="ml-2">
                                  {submoduleTestCasesCount}
                                </Badge>
                              </Button>
                           {can.testCase.create &&
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => {
                                  setSearchFilters({
                                    description: "",
                                    typeId: "",
                                    severityId: "",
                                    submoduleId: "",
                                  });
                                  setSearchResults(null);
                                  setClientFilteredResults(null);

                                  // If submodule selected, filter; otherwise show all
                                  if (selectedSubmoduleId) {
                                    setTestCases(
                                      allModuleTestCases.filter(
                                        (tc) =>
                                          String(tc.subModuleId) ===
                                          String(selectedSubmoduleId),
                                      ),
                                    );
                                  } else {
                                    setTestCases(allModuleTestCases);
                                  }
                                  const currentModule = projectModules.find(
                                    (m) => m.id === selectedModuleId,
                                  );
                                  const moduleName = currentModule?.name || "";
                                  const subModuleName =
                                    x.subModuleName || x.name || "";
                                  // setModals((prev) => {
                                  //   const newModals = [
                                  //     ...prev,
                                  //     {
                                  //       open: true,
                                  //       formData: {
                                  //         module: moduleName,
                                  //         subModule: subModuleName,
                                  //         description: "",
                                  //         steps: "",
                                  //         type: "",
                                  //         severity: "", // changed from "medium"
                                  
                                  
                                  
                                  
                                  
                                  
                                  
                                  
                                  
                                  setFormData({
                                    module: currentModule?.name || "",
                                    subModule: x.subModuleName || x.name || "",
                                    description: "",
                                    steps: "",
                                    type: "",
                                    severity: "",
                                    projectId: selectedProjectId,
                                    moduleId: selectedModuleId ?? undefined,
                                    subModuleId: String(x.id),
                                  });
                                  setIsModalOpen(true);
                                }}
                                className="p-1 border-0 hover:bg-gray-50"
                                disabled={selectedSubmoduleId !== x.id}
                              >
                                <Plus className="w-4 h-4" />
                              </Button>}
                            </div>
                          </div>
                        );
                      })}
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
            ) : null)}

          {}
          {}

          {}
          {selectedProjectId && selectedModuleId && (
            <Card className="mb-4">
              <CardContent className="p-4">
                <form
                  className="flex flex-wrap gap-4 items-end"
                  onSubmit={async (e) => {
                    e.preventDefault();
                    setIsSearching(true);
                    try {
                      setClientFilteredResults(null);

                      
                      const params = new URLSearchParams();
                      if (searchFilters.description)
                        params.append("description", searchFilters.description);
                      if (searchFilters.typeId)
                        params.append("defectTypeId", searchFilters.typeId);
                      if (searchFilters.severityId)
                        params.append("severityId", searchFilters.severityId);
                      params.append("page", "0");
                      params.append("size", "100000");

                      
                      let raw: any[] = [];
                      try {
                        const pId = selectedProjectId || '1';
                        const filterRes = await apiClient.get(`/api/v1/project/${pId}/filter?${params.toString()}`);
                        raw = Array.isArray(filterRes.data) ? filterRes.data : (filterRes.data?.data || []);
                      } catch {
                        raw = [];
                      }

                      const normalized = raw.map((tc: any) => {
                        const severityObj = severities.find(
                          (s) => s.id === tc.severityId,
                        );
                        const defectTypeObj = defectTypes.find(
                          (dt) => dt.id === tc.defectTypeId,
                        );
                        return {
                          ...tc,
                          id: tc.id,
                          no: tc.no,
                          testCaseId: tc.id,
                          description: tc.description,
                          detailsSteps: tc.detailsSteps,
                          steps: tc.detailsSteps,
                          expectedResult: tc.expectedResult,
                          subModuleId: tc.subModuleId,
                          subModuleName: tc.subModuleName,
                          subModule: tc.subModuleName,
                          severityId: tc.severityId,
                          severity: severityObj?.name || tc.severityName || "",
                          defectTypeId: tc.defectTypeId,
                          type: defectTypeObj?.name || tc.defectTypeName || "",
                          moduleId: tc.moduleId,
                          module: tc.moduleName || tc.module,
                        };
                      });

                      console.log("Normalized search results:", normalized);
                      setSearchResults(normalized);
                    } catch (error) {
                      console.error("Search error:", error);
                      setSearchResults([]);
                    } finally {
                      setIsSearching(false);
                    }
                  }}
                >
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Description
                    </label>
                    <input
                      type="text"
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="Type to filter or click Search for backend search"
                      value={searchFilters.description}
                      onChange={(e) => {
                        setSearchFilters((f) => ({
                          ...f,
                          description: e.target.value,
                        }));

                        
                        if (filterTimeoutRef.current) {
                          clearTimeout(filterTimeoutRef.current);
                        }

                        
                        if (e.target.value.trim()) {
                          filterTimeoutRef.current = setTimeout(() => {
                            
                            setSearchResults(null);

                            
                            const filtered = testCases.filter((tc) =>
                              (tc.description || "")
                                .toLowerCase()
                                .includes(e.target.value.toLowerCase()),
                            );
                            setClientFilteredResults(filtered);
                          }, 300); 
                        } else {
                          
                          setSearchResults(null);
                          setClientFilteredResults(null);
                        }
                      }}
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Type
                    </label>
                    <select
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      value={searchFilters.typeId}
                      onChange={(e) =>
                        setSearchFilters((f) => ({
                          ...f,
                          typeId: e.target.value,
                        }))
                      }
                    >
                      <option value="">All</option>
                      {defectTypes.map((type) => (
                        <option key={type.id} value={type.id}>
                          {type.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      Severity
                    </label>
                    <select
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      value={searchFilters.severityId}
                      onChange={(e) =>
                        setSearchFilters((f) => ({
                          ...f,
                          severityId: e.target.value,
                        }))
                      }
                    >
                      <option value="">All</option>
                      {severities.map((sev) => (
                        <option key={sev.id} value={sev.id}>
                          {sev.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      type="submit"
                      className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded flex items-center"
                      disabled={isSearching}
                    >
                      {isSearching ? (
                        <>
                          <svg
                            className="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                          >
                            <circle
                              className="opacity-25"
                              cx="12"
                              cy="12"
                              r="10"
                              stroke="currentColor"
                              strokeWidth="4"
                            ></circle>
                            <path
                              className="opacity-75"
                              fill="currentColor"
                              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                            ></path>
                          </svg>
                          Searching...
                        </>
                      ) : (
                        "Search"
                      )}
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      className="px-4 py-2 rounded"
                      onClick={() => {
                        
                        setSearchFilters({
                          description: "",
                          typeId: "",
                          severityId: "",
                          submoduleId: "",
                        });
                        setSearchResults(null);
                        setClientFilteredResults(null);

                        // IMPORTANT: Reset to show ALL test cases for the current module
                        // NOT just the selected submodule
                        if (selectedModuleId && allModuleTestCases.length > 0) {
                          // Show all test cases for the module
                          setTestCases(allModuleTestCases);

                          // Optional: Also reset submodule selection if you want
                          // setSelectedSubmoduleId(null);
                        }
                        // Fallback: refresh from API if needed
                        else if (selectedProjectId && selectedModuleId) {
                          getTestCasesByProjectAndModule(
                            selectedProjectId,
                            selectedModuleId,
                          ).then((data) => {
                            const moduleMap = Object.fromEntries(
                              projectModules.map((m: any) => [m.id, m.name]),
                            );
                            const submoduleMap = Object.fromEntries(
                              projectModules.flatMap((m: any) =>
                                m.submodules.map((sm: any) => [sm.id, sm.name]),
                              ),
                            );

                            setTestCases(
                              (data as any[]).map((tc: any) => ({
                                ...tc,
                                id: tc.id,
                                no: tc.no,
                                testCaseId: tc.id,
                                steps: tc.detailsSteps,
                                description: tc.description,
                                expectedResult: tc.expectedResult,
                                subModule: tc.subModuleName,
                                severity: tc.severityName,
                                type: tc.defectTypeName,
                                module: moduleMap[tc.moduleId] || tc.moduleName,
                              })),
                            );

                            // Update allModuleTestCases for future clears
                            setAllModuleTestCases(
                              (data as any[]).map((tc: any) => ({
                                ...tc,
                                id: tc.id,
                                no: tc.no,
                                testCaseId: tc.id,
                                steps: tc.detailsSteps,
                                description: tc.description,
                                expectedResult: tc.expectedResult,
                                subModule: tc.subModuleName,
                                severity: tc.severityName,
                                type: tc.defectTypeName,
                                module: moduleMap[tc.moduleId] || tc.moduleName,
                              })),
                            );
                          });
                        }
                      }}
                    >
                      Clear
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* Test Cases Table - Now with dynamic height */}
          {selectedProjectId && (
            <Card>
              <CardContent className="p-0">
                <table className="w-full testcase-table">
                  <thead className="bg-gray-50">
                    <tr className="border-b border-gray-200">
                      {}
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        TEST CASE ID
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        DESCRIPTION
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        STEPS
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        TYPE
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        SEVERITY
                      </th>
                      {(can.testCase.edit || can.testCase.delete) && (
                        <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          ACTIONS
                        </th>
                      )}
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {paginatedTestCases.length === 0 ? (
                      <tr>
                        <td
                          colSpan={6}
                          className="px-6 py-8 text-center text-gray-500"
                        >
                          {searchResults !== null ||
                          clientFilteredResults !== null ? (
                            <div>
                              <p className="text-lg font-medium mb-2">
                                No test cases found
                              </p>
                              <p className="text-sm">
                                Try adjusting your search criteria or clear the
                                filters
                              </p>
                            </div>
                          ) : (
                            <div>
                              <p className="text-lg font-medium mb-2">
                                No test cases available
                              </p>
                              <p className="text-sm">
                                {selectedModuleId
                                  ? "No test cases found for the selected module. You can select a submodule for more specific results or add new test cases."
                                  : "Select a module to view test cases"}
                              </p>
                            </div>
                          )}
                        </td>
                      </tr>
                    ) : (
                      paginatedTestCases.map((testCase: TestCaseType) => (
                        <tr
                          key={testCase.id}
                          className="hover:bg-gray-50"
                        >
                          {}
                          <td
                            className="px-6 py-4 whitespace-nowrap text-sm font-medium text-blue-600 hover:text-blue-800 cursor-pointer"
                            onClick={() => handleViewTestCase(testCase)}
                            title="Click to view details"
                          >
                            {testCase ? formatTestCaseId(testCase) : "N/A"}
                          </td>
                          <td
                            className="px-6 py-4 text-sm text-gray-500 max-w-xs description-cell cursor-pointer hover:text-gray-900"
                            title={testCase.description}
                            onClick={() => handleViewTestCase(testCase)}
                          >
                            <div className="description-text">
                              {testCase.description}
                            </div>
                          </td>
                          <td className="px-6 py-4 text-sm text-gray-500">
                            <button
                              onClick={() => handleViewTestCase(testCase)}
                              className="text-blue-600 hover:text-blue-800 flex items-center space-x-1"
                              title="View Details"
                            >
                              <Eye className="w-4 h-4" />
                              <span>View</span>
                            </button>
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700 font-medium">
                            {testCase.type ||
                              testCase.defectTypeName ||
                              (testCase as any).defectType ||
                              (testCase.defectTypeId ? getTypeName(testCase.defectTypeId) : "") ||
                              "-"}
                          </td>
                          <td className="px-6 py-4 whitespace-nowrap">
                            {renderColoredSpan(
                              testCase.severity,
                              getSeverityColor(testCase.severity),
                            )}
                          </td>
                          {(can.testCase.edit || can.testCase.delete) && (
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                              <div className="flex space-x-2">
                                {can.testCase.edit && (
                                  <button
                                    
                                    onClick={() => {
                                      const subModuleId =
                                        (testCase as any).subModuleId ??
                                        testCase.subModule;

                                      
                                      let moduleId = (testCase as any).moduleId;
                                      let moduleName = testCase.moduleName;

                                      let subModuleName =
                                        testCase.subModuleName ||
                                        testCase.subModule ||
                                        "";

                                      // Search through projectModules to find which module contains this submodule
                                      for (const mod of projectModules) {
                                        const foundSub = mod.submodules.find(
                                          (sm: any) =>
                                            String(sm.id) ===
                                            String(subModuleId),
                                        );
                                        if (foundSub) {
                                          moduleId = mod.id;
                                          moduleName = mod.name;
                                          subModuleName =
                                            foundSub.name || subModuleName;
                                          break;
                                        }
                                      }

                                      // Fallback: if still not found, use selectedModuleId
                                      if (!moduleName && selectedModuleId) {
                                        const currentModule =
                                          projectModules.find(
                                            (m) => m.id === selectedModuleId,
                                          );
                                        if (currentModule) {
                                          moduleName = currentModule.name;
                                          moduleId = currentModule.id;
                                        }
                                      }

                                      const typeName = getTypeName(
                                        testCase.type ||
                                          testCase.defectTypeName,
                                      );
                                      const severityName = getSeverityName(
                                        testCase.severity ||
                                          testCase.severityName,
                                      );

                                      setFormData({
                                        module: moduleName,
                                        subModule: subModuleName,
                                        description: testCase.description || "",
                                        steps:
                                          testCase.detailsSteps ||
                                          testCase.steps ||
                                          "",
                                        type: typeName || testCase.type || "",
                                        severity:
                                          severityName ||
                                          testCase.severity ||
                                          "",
                                        projectId:
                                          testCase.projectId ||
                                          selectedProjectId,
                                        id: testCase.id,
                                        moduleId: moduleId,
                                        subModuleId: subModuleId,
                                      });
                                      setIsModalOpen(true);
                                    }}
                                    className="p-1 text-yellow-600 hover:text-yellow-800 hover:bg-yellow-50 rounded"
                                    title="Edit"
                                  >
                                    <Edit2 className="w-4 h-4" />
                                  </button>
                                )}
                                {can.testCase.delete && (
                                  <button
                                    onClick={() => {
                                      setPendingDeleteId(testCase.id);
                                      setPendingDeleteSubModuleId(
                                        testCase.subModuleId,
                                      );
                                      setConfirmOpen(true);
                                    }}
                                    className="p-1 text-red-600 hover:text-red-800 hover:bg-red-50 rounded"
                                    title="Delete"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                )}
                              </div>
                            </td>
                          )}
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
                {}
                <div className="flex items-center justify-between px-6 py-3 bg-gray-50 border-t border-gray-200">
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-700">
                      Rows per page:
                    </span>
                    <select
                      className="border border-gray-300 rounded px-2 py-1 text-sm"
                      value={rowsPerPage}
                      onChange={(e) => setRowsPerPage(Number(e.target.value))}
                    >
                      {[5, 10, 20, 50, 100].map((n) => (
                        <option key={n} value={n}>
                          {n}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="flex items-center space-x-2">
                    <button
                      className="px-2 py-1 rounded border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
                      onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                      disabled={currentPage === 1}
                      
                    >
                      Previous
                    </button>
                    <span className="text-sm text-gray-700">
                      Page {currentPage} of {totalPages}
                    </span>
                    <button
                      className="px-2 py-1 rounded border border-gray-300 bg-white text-gray-700 disabled:opacity-50"
                      onClick={() =>
                        setCurrentPage((p) => Math.min(totalPages, p + 1))
                      }
                      disabled={currentPage === totalPages}
                    >
                      Next
                    </button>
                  </div>
                  <span className="text-sm text-gray-500">
                    {totalRows === 0
                      ? "No test cases"
                      : `Showing ${(currentPage - 1) * rowsPerPage + 1}–${Math.min(currentPage * rowsPerPage, totalRows)} of ${totalRows}`}
                  </span>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {}
      {}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setFormData(defaultFormData);
          setCreateAlert({ isOpen: false, message: "" });
          setUpdateAlert({ isOpen: false, message: "" });
        }}
        // key={idx}
        // isOpen={modal.open}
        // onClose={() => {
        //   if (modals.length === 1) {
        //     setModals([{ ...modals[0], open: false }]);
        //     setCurrentModalIdx(0);
        //   } else {
        //     handleRemove(idx);
        //   }
        //   setCreateAlert({ isOpen: false, message: "" }); // Clear alert on close/cancel
        //   setUpdateAlert({ isOpen: false, message: "" });
        // }}
        title={isEditMode ? "Edit Test Case" : "Create New Test Case"}
        size="xl"
      >
        <form
          
          
          
          
          onSubmit={handleSubmitAll}
          className="space-y-4"
        >
          <div className="flex items-center mb-2">
            {}
          </div>
          <div className="border rounded-lg p-4 mb-2 relative">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Module
                </label>
                {isEditMode ? (
                  <div className="w-full px-3 py-2 rounded-lg bg-gray-100 text-gray-800 border border-gray-300">
                    {formData.module || "Unknown Module"}
                  </div>
                ) : (
                  <select
                    value={formData.moduleId || ""}
                    onChange={(e) => {
                      const modId = e.target.value;
                      const selectedMod = projectModules.find((m) => String(m.id) === String(modId));
                      setFormData((prev) => ({
                        ...prev,
                        moduleId: modId,
                        module: selectedMod?.name || "",
                        subModuleId: "",
                        subModule: "",
                      }));
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    required
                  >
                    <option value="">Select Module</option>
                    {projectModules.map((mod) => (
                      <option key={mod.id} value={mod.id}>
                        {mod.name}
                      </option>
                    ))}
                  </select>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Sub Module
                </label>
                {isEditMode ? (
                  <div className="w-full px-3 py-2 rounded-lg bg-gray-100 text-gray-800 border border-gray-300">
                    {formData.subModule || "-"}
                  </div>
                ) : (
                  <select
                    value={formData.subModuleId || ""}
                    onChange={(e) => {
                      const subId = e.target.value;
                      const selectedMod = projectModules.find((m) => String(m.id) === String(formData.moduleId));
                      const selectedSub = selectedMod?.submodules.find((s: any) => String(s.id) === String(subId));
                      setFormData((prev) => ({
                        ...prev,
                        subModuleId: subId,
                        subModule: selectedSub?.name || "",
                      }));
                    }}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    disabled={!formData.moduleId}
                    required
                  >
                    <option value="">Select Submodule</option>
                    {(projectModules.find((m) => String(m.id) === String(formData.moduleId))?.submodules || []).map((sm: any) => (
                      <option key={sm.id} value={sm.id}>
                        {sm.name}
                      </option>
                    ))}
                  </select>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Type
                </label>
                <select
                  
                  value={formData.type}
                  
                  
                  
                  onChange={(e) => handleInputChange("type", e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="">Select Type</option>
                  {/* {defectTypes.map((type) => (
                          <option key={type.id} value={type.name}>
                            {type.name}
                          </option>
                        ))} */}
                  {defectTypes.map((type) => (
                    <option key={type.id} value={type.name}>
                      {type.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Severity
                </label>
                <select
                  
                  
                  
                  
                  value={formData.severity}
                  onChange={(e) =>
                    handleInputChange("severity", e.target.value)
                  }
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  required
                >
                  <option value="">Select Severity</option>
                  {severities.map((sev) => (
                    <option key={sev.id} value={sev.name}>
                      {sev.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <p className="text-xs text-gray-500 mb-1">
                Description must contain letters and at least one number or
                special character
              </p>
              <textarea
                value={formData.description}
                onChange={(e) =>
                  handleInputChange("description", e.target.value)
                }
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                rows={1}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Test Steps
              </label>
              <p className="text-xs text-gray-500 mb-1">
                Steps must contain letters and at least one number or special
                character (e.g., "1. Step one", "Step 1!")
              </p>
              <textarea
                
                
                
                
                value={formData.steps}
                onChange={(e) => handleInputChange("steps", e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                rows={4}
                required
              />
            </div>
          </div>
          <div className="flex justify-between items-center pt-4">
            {}
            <div className="flex items-center space-x-3">
              {}
              <Button
                type="button"
                variant="secondary"
                
                
                
                
                
                
                
                
                onClick={() => {
                  setIsModalOpen(false);
                  setFormData(defaultFormData);
                }}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                
                
                
                
                
                
                
                
                disabled={
                  !formData.description ||
                  !formData.steps ||
                  !formData.module ||
                  !formData.subModule ||
                  !formData.type ||
                  !formData.severity
                }
              >
                {isEditMode ? "Update Test Case" : "Save Test Case"}
                {}
              </Button>
            </div>
          </div>
        </form>
      </Modal>
      {}
      {}

      {}
      <Modal
        isOpen={isViewStepsModalOpen}
        onClose={() => {
          setIsViewStepsModalOpen(false);
          setViewingTestCase(null);
        }}
        title={`Test Step - ${viewingTestCase ? formatTestCaseId(viewingTestCase) : "N/A"}`}
      >
        <div className="space-y-4">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-gray-700 whitespace-pre-wrap break-words">
              {viewingTestCase?.detailsSteps || viewingTestCase?.steps}
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
        title={`Test Case Details - ${viewingTestCase ? formatTestCaseId(viewingTestCase) : "N/A"}`}
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
                <h3 className="text-sm font-medium text-gray-500">Defect Type</h3>
                <p className="mt-1 text-sm font-medium text-gray-900">
                  {viewingTestCase.type ||
                    viewingTestCase.defectTypeName ||
                    (viewingTestCase as any).defectType ||
                    (viewingTestCase.defectTypeId ? getTypeName(viewingTestCase.defectTypeId) : "") ||
                    "-"}
                </p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">Severity</h3>
                <div className="mt-1">
                  {renderColoredSpan(
                    viewingTestCase.severity,
                    getSeverityColor(viewingTestCase.severity),
                  )}
                </div>
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
        isOpen={isDescriptionModalOpen}
        onClose={() => {
          setIsDescriptionModalOpen(false);
          setSelectedDescription("");
        }}
        title="Test Case Description"
      >
        <div className="space-y-4">
          <div className="bg-gray-50 rounded-lg p-4">
            <p className="text-gray-700 whitespace-pre-wrap">
              {selectedDescription}
            </p>
          </div>
          <div className="flex justify-end">
            <Button
              variant="secondary"
              onClick={() => {
                setIsDescriptionModalOpen(false);
                setSelectedDescription("");
              }}
            >
              Close
            </Button>
          </div>
        </div>
      </Modal>

      {/* Create Alert Modal */}
      <AlertModal
        isOpen={createAlert.isOpen}
        message={createAlert.message}
        onClose={() => {
          console.log("Closing create alert");
          setCreateAlert({ isOpen: false, message: "" });
        }}
      />
      {/* Update Alert Modal */}
      <AlertModal
        isOpen={updateAlert.isOpen}
        message={updateAlert.message}
        onClose={() => {
          console.log("Closing update alert");
          setUpdateAlert({ isOpen: false, message: "" });
        }}
      />
      {/* Delete Alert Modal */}
      <AlertModal
        isOpen={deleteAlert.isOpen}
        message={deleteAlert.message}
        onClose={() => {
          console.log("Closing delete alert");
          setDeleteAlert({ isOpen: false, message: "" });
        }}
      />
      {confirmOpen && (
        <div className="fixed inset-0 z-[60] flex justify-center items-start bg-black bg-opacity-40">
          <div
            className="mt-8 bg-[#444] text-white rounded-lg shadow-2xl min-w-[400px] max-w-[95vw]"
            style={{ boxShadow: "0 8px 24px rgba(0,0,0,0.25)" }}
          >
            <div className="px-6 pb-4 pt-5 text-base text-white">
              Are you sure you want to delete this test case?
            </div>
            <div className="px-6 pb-5 flex justify-end gap-3">
              <button
                className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold px-6 py-2 rounded mr-2"
                onClick={() => {
                  setConfirmOpen(false);
                  setPendingDeleteId(null);
                  setPendingDeleteSubModuleId(null);
                }}
                type="button"
              >
                Cancel
              </button>
              <button
                className="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-2 rounded"
                onClick={async () => {
                  if (pendingDeleteId) {
                    try {
                      await handleDeleteTestCase(
                        pendingDeleteId,
                        pendingDeleteSubModuleId || 0,
                      );
                      setDeleteAlert({
                        isOpen: true,
                        message: "Test case deleted successfully!",
                      });
                      
                      setTestCases((prev) => {
                        const updated = prev.filter(
                          (tc) =>
                            String(tc.testCaseId) !== String(pendingDeleteId) &&
                            String(tc.id) !== String(pendingDeleteId),
                        );
                        
                        const newTotalRows = updated.length;
                        const newTotalPages =
                          Math.ceil(newTotalRows / rowsPerPage) || 1;
                        if (currentPage > newTotalPages && currentPage > 1) {
                          setCurrentPage(currentPage - 1);
                        }
                        return updated;
                      });
                      setAllSubmoduleTestCases((prev) => {
                        const updated = { ...prev };
                        Object.keys(updated).forEach((submoduleId) => {
                          updated[submoduleId] = updated[submoduleId].filter(
                            (tc) =>
                              String(tc.testCaseId) !==
                                String(pendingDeleteId) &&
                              String(tc.id) !== String(pendingDeleteId),
                          );
                        });
                        return updated;
                      });
                      
                      refreshTestCases();
                      fetchAllSubmoduleTestCases();
                    } catch (error: any) {
                      setDeleteAlert({
                        isOpen: true,
                        message:
                          error?.response?.data?.message ||
                          error?.message ||
                          "Cannot delete test case: There are dependencies (e.g., allocated to a release or linked to a defect).",
                      });
                    } finally {
                      setConfirmOpen(false);
                      setPendingDeleteId(null);
                    }
                  }
                }}
                type="button"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
      {}
      {showQuickAdd && (
        <QuickAddTestCase
          selectedProjectId={selectedProjectId}
          onTestCaseAdded={() => {
            setShowQuickAdd(false);
            
           if (selectedProjectId) {
              if (selectedModuleId) {
                
                refreshTestCases();
              } else {
                
                fetchAllTestCasesForProject(selectedProjectId);
              }
            }
          }}
        />
      )}

      {isImporting && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="w-[380px] rounded-2xl bg-white p-8 shadow-2xl">
            <div className="flex flex-col items-center">
              <OrbitProgress
                color="#2563eb"
                size="medium"
                text=""
                textColor=""
              />
              <h2 className="mt-6 text-2xl font-bold text-gray-800">
                Importing Test Cases
              </h2>
              <p className="mt-2 text-center text-gray-500">
                Please wait while we parse and save your Excel sheet...
              </p>
              <div className="mt-6 w-full rounded-full bg-gray-200 h-2 overflow-hidden">
                <div className="h-full w-full bg-blue-600 animate-pulse rounded-full"></div>
              </div>
              <p className="mt-4 text-sm text-gray-400">
                This may take a few seconds.
              </p>
            </div>
          </div>
        </div>
      )}

      {isExporting && (
        <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="w-[380px] rounded-2xl bg-white p-8 shadow-2xl">
            <div className="flex flex-col items-center">
              <OrbitProgress
                color="#2563eb"
                size="medium"
                text=""
                textColor=""
              />
              <h2 className="mt-6 text-2xl font-bold text-gray-800">
                Exporting Test Cases
              </h2>
              <p className="mt-2 text-center text-gray-500">
                Please wait while we prepare and download your Excel sheet...
              </p>
              <div className="mt-6 w-full rounded-full bg-gray-200 h-2 overflow-hidden">
                <div className="h-full w-full bg-blue-600 animate-pulse rounded-full"></div>
              </div>
              <p className="mt-4 text-sm text-gray-400">
                This may take a few seconds.
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
