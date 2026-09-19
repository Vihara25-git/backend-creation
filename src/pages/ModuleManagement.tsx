import React, { useState, useEffect } from "react";
import { Toast } from "../components/ui/Toast";
import {
  roleTypeBasedRoleFetch,
  roleTypeBasedRoleIdFetch,
} from "../utils/roleTypeBasedRoleFetch";
import getAllSubDevwithName from "../api/submodule/getAllSubmoduleAllocatedDevsWithName";
import {
  PlusCircle,
  Edit2,
  Trash2,
  ChevronLeft,
  Users,
  Search
} from "lucide-react";

import { Card, CardContent } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Input } from "../components/ui/Input";
import { Modal } from "../components/ui/Modal";
import { useApp } from "../context/AppContext";
import { useParams, useNavigate } from "react-router-dom";
import { ProjectSelector } from "../components/ui/ProjectSelector";
import { createModule as createModuleApi } from "../api/module/createModule";
import { updateModule as updateModuleApi } from "../api/module/updateModule";
import { deleteModule as deleteModuleApi } from "../api/module/deleteModule";
import { deleteSubmodule as deleteSubmoduleApi } from "../api/module/deleteSubmodule";
import { updateSubmodule as updateSubmoduleApi } from "../api/module/updateSubmodule";
import {
  getModulesByProjectId,
  Modules as ApiModule,
} from "../api/module/getModule";
import { createSubmodule } from "../api/module/createModule";
import AlertModal from "../components/ui/AlertModal";
import { getDevelopersByModuleId } from "../api/module/getModuleDevelopers";
import { getBulkSuboduleAllocation } from "../api/submodule/getBulkSuboduleAllocation";
import {
  getUsersByAllocation,
  getUsersBySubmoduleAllocation,
  UserByAllocation,
} from "../api/module/getUsersByAllocation";
import {
  deallocateDeveloperFromModule,
  deallocateModuleLeaderWithAllocateModuleId,
  reassignDeveloperWithAllocateModuleId,
  reassignSubmoduleDeveloperWithAllocateModuleId,
} from "../api/module/deallocateDevelopers";
import { getDefectsByProjectId } from "../api/defect/filterDefectByProject";
import apiClient from "../lib/api";
import { getSubmodulesByModule } from "../api/submodule/getSubmodulesByModule";
import {
  allocateProjectEmployeeToSubModule,
  deAllocateProjectEmployeeFromSubModule,
  type SubModuleDevAllocation,
} from "../api/subModuleDevAlloc";
import { allocateModuleLeader } from "../api/module/allocateModuleLeader";
import { useAccessibleProjects } from "../api/useAccessibleProjects";
import { usePermission } from "../context/PermissionContext";

type ModuleAssignment = {
  moduleId: string;
  submoduleId?: string;
  employeeIds: string[];
};


export const ModuleManagement: React.FC = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const {
    employees,
    setSelectedProjectId: setGlobalProjectId,
    selectedProjectId,
  } = useApp();

  const { projects, switchProject } = useAccessibleProjects();

  const { can } = usePermission();

  const [isAddModuleModalOpen, setIsAddModuleModalOpen] = useState(false);
  const [isCreatingModule, setIsCreatingModule] = useState(false);
  const [isAssignmentModalOpen, setIsAssignmentModalOpen] = useState(false);
  const [isEditModuleModalOpen, setIsEditModuleModalOpen] = useState(false);
  const [isBulkAssignmentModalOpen, setIsBulkAssignmentModalOpen] =
    useState(false);
  const [selectedModuleForAssignment, setSelectedModuleForAssignment] =
    useState<any>(null);
  const [selectedSubmoduleForAssignment, setSelectedSubmoduleForAssignment] =
    useState<any>(null);
  const [editingModule, setEditingModule] = useState<any>(null);
  const [selectedItems, setSelectedItems] = useState<
    Array<{
      type: "module" | "submodule";
      moduleId: string;
      submoduleId?: string;
    }>
  >([]);
  const [modulesByProjectId, setModulesByProjectId] = useState<
    ApiModule[] | null
  >(null);
  const [isLoading, setIsLoading] = useState(false);

  const [moduleForm, setModuleForm] = useState({
    name: "",
  });
  const [moduleSearch, setModuleSearch] = useState("");

  // Input refs for auto-focus when modals open
  const addModuleInputRef = React.useRef<HTMLInputElement>(null);
  const editModuleInputRef = React.useRef<HTMLInputElement>(null);
  const submoduleInputRef = React.useRef<HTMLInputElement>(null);

  const [assignmentForm, setAssignmentForm] = useState<ModuleAssignment>({
    moduleId: "",
    employeeIds: [],
  });

  const [isAddSubmoduleModalOpen, setIsAddSubmoduleModalOpen] = useState(false);
  const [submoduleForm, setSubmoduleForm] = useState({ name: "" });
  const [currentModuleIdForSubmodule, setCurrentModuleIdForSubmodule] =
    useState<string | null>(null);
  const [isEditingSubmodule, setIsEditingSubmodule] = useState(false);
  const [editingSubmoduleId, setEditingSubmoduleId] = useState<string | null>(
    null,
  );
  const [isUpdatingModule, setIsUpdatingModule] = useState(false);
  const [isCreatingSubmodule, setIsCreatingSubmodule] = useState(false);
  const [isUpdatingSubmodule, setIsUpdatingSubmodule] = useState(false);

  // State for project-allocated employees with roles
  const [developersWithRoles, setDevelopersWithRoles] = useState<Array<{
    userWithRole: string;
    employeeName: string;
    roleName: string;
    roleType?: string;
    designationName?: string;
    projectAllocationId: number;
    userId: number;
    roleId?: number;
  }>>([]);

  const [developerRoleNames, setDeveloperRoleNames] = useState<string[]>([]);
  const [developerRoleIds, setDeveloperRoleIds] = useState<number[]>([]);
  const [moduleLeaderRoleNames, setModuleLeaderRoleNames] = useState<string[]>([]);
  const [moduleLeaderRoleIds, setModuleLeaderRoleIds] = useState<number[]>([]);

  const normalizeRoleName = (value: unknown): string =>
    String(value || "")
      .trim()
      .toUpperCase()
      .replace(/[\s-]+/g, "_");

  const getRoleFromUserWithRole = (userWithRole: string): string => {
    const parts = userWithRole.split("-");
    return normalizeRoleName(parts.slice(1).join("-"));
  };

  const isQaRole = (
    roleName?: string,
    roleType?: string,
    designationName?: string,
    roleId?: number | string,
  ): boolean => {
    const numRoleId = roleId !== undefined && roleId !== null ? Number(roleId) : null;
    const rName = (roleName || "").trim().toUpperCase();
    const rType = (roleType || "").trim().toUpperCase();
    const dName = (designationName || "").trim().toUpperCase();

    // Explicitly reject Project Manager
    if (numRoleId === 5 || rType === "PROJECT_MANAGER" || rName.includes("PROJECT MANAGER")) {
      return false;
    }

    // Explicitly reject Developer roles
    if (
      numRoleId === 2 ||
      numRoleId === 4 ||
      rType === "DEVELOPER" ||
      rType === "SENIOR_DEVELOPER" ||
      rType === "JUNIOR_DEVELOPER" ||
      rType === "DEV_LEAD" ||
      rName === "DEVELOPER" ||
      rName === "SENIOR DEVELOPER"
    ) {
      return false;
    }

    // Role ID matches QA Lead (1) or QA Engineer (3)
    if (numRoleId === 1 || numRoleId === 3) {
      return true;
    }

    // Role Type matches QA_LEAD or QA_ENGINEER
    if (rType === "QA_LEAD" || rType === "QA_ENGINEER") {
      return true;
    }

    // Role Name matches QA Lead or QA Engineer
    if (
      rName === "QA LEAD" ||
      rName === "QA ENGINEER" ||
      rName === "QA_LEAD" ||
      rName === "QA_ENGINEER" ||
      rName.includes("QA LEAD") ||
      rName.includes("QA ENGINEER")
    ) {
      return true;
    }

    // General QA keyword match in roleName
    if (rName.includes("QA") || rName.includes("QUALITY ASSURANCE")) {
      return true;
    }

    // Fallback to designation only if role info is absent
    if (!rName && !rType && !numRoleId) {
      if (
        dName.includes("QA") ||
        dName.includes("QUALITY ASSURANCE") ||
        dName.includes("TEST")
      ) {
        return true;
      }
    }

    return false;
  };

  const isDeveloperRole = (
    roleName?: string,
    roleType?: string,
    designationName?: string,
    roleId?: number | string,
  ): boolean => {
    // Cannot be QA role or Project Manager
    if (isQaRole(roleName, roleType, designationName, roleId)) {
      return false;
    }

    const numRoleId = roleId !== undefined && roleId !== null ? Number(roleId) : null;
    const rName = (roleName || "").trim().toUpperCase();
    const rType = (roleType || "").trim().toUpperCase();
    const dName = (designationName || "").trim().toUpperCase();

    // Reject Project Manager
    if (numRoleId === 5 || rType === "PROJECT_MANAGER" || rName.includes("PROJECT MANAGER")) {
      return false;
    }

    // Developer Role IDs: 2 (Developer), 4 (Senior developer)
    if (numRoleId === 2 || numRoleId === 4) {
      return true;
    }

    // Developer Role Types
    if (
      rType === "DEVELOPER" ||
      rType === "SENIOR_DEVELOPER" ||
      rType === "JUNIOR_DEVELOPER" ||
      rType === "DEV_LEAD"
    ) {
      return true;
    }

    // Developer Role Names
    if (
      rName.includes("DEVELOPER") ||
      rName.includes("DEV") ||
      rName.includes("SOFTWARE") ||
      rName.includes("PROGRAMMER")
    ) {
      return true;
    }

    // Fallback to designation only if role info is absent
    if (!rName && !rType && !numRoleId) {
      if (
        dName.includes("DEVELOPER") ||
        dName.includes("DEV") ||
        dName.includes("SOFTWARE") ||
        dName.includes("PROGRAMMER")
      ) {
        return true;
      }
    }

    return false;
  };

  const getRoleTypedDevelopers = () => {
    return developersWithRoles.filter((dev) =>
      isDeveloperRole(dev.roleName, dev.roleType, dev.designationName, dev.roleId)
    );
  };

  const getRoleTypedModuleLeaders = () => {
    return developersWithRoles.filter((dev) =>
      isQaRole(dev.roleName, dev.roleType, dev.designationName, dev.roleId)
    );
  };

  
  const [
    selectedDeveloperProjectAllocationId,
    setSelectedDeveloperProjectAllocationId,
  ] = useState<number | null>(null);

  
  const [
    selectedModuleDeveloperProjectAllocationId,
    setSelectedModuleDeveloperProjectAllocationId,
  ] = useState<number | null>(null);

  const [alertOpen, setAlertOpen] = useState(false);
  const [alertMessage, setAlertMessage] = useState("");
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);

  // New state for developers assigned to modules/submodules
  const [moduleDevelopers, setModuleDevelopers] = useState<
    Record<string, any[]>
  >({});
  const [moduleDevelopersLoading, setModuleDevelopersLoading] = useState<
    Record<string, boolean>
  >({});
  const [moduleDevelopersError, setModuleDevelopersError] = useState<
    Record<string, string>
  >({});
  console.log(projectId, "projectId from params in module management page");

  
  const [submoduleAllocations, setSubmoduleAllocations] = useState<
    Record<string, { [submoduleId: string]: string[] }>
  >({});
  const [submoduleAllocatedDevs, setSubmoduleAllocatedDevs] = useState<
    Record<string, SubModuleDevAllocation[]>
  >({});

  
  const [moduleAllocatedUsers, setModuleAllocatedUsers] = useState<
    UserByAllocation[]
  >([]);
  const [moduleAllocatedUsersLoading, setModuleAllocatedUsersLoading] =
    useState(false);
  const [moduleAllocatedUsersError, setModuleAllocatedUsersError] = useState<
    string | null
  >(null);

  const [
    pendingDeleteModuleIdForSubmodule,
    setPendingDeleteModuleIdForSubmodule,
  ] = useState<string | null>(null);
  

  
  
  
  
  
  
  
  

  useEffect(() => {
    if (projectId) {
      setGlobalProjectId(projectId);
    }
  }, [projectId, setGlobalProjectId]);

  useEffect(() => {
    if (selectedProjectId) {
      fetchModules();
      fetchDevelopersWithRoles();
    }
  }, [selectedProjectId]);

  

  const [projectAllocatedEmployeeIds, setProjectAllocatedEmployeeIds] = useState<Set<string>>(new Set());
  const [hasLoadedProjectAllocatedEmployees, setHasLoadedProjectAllocatedEmployees] = useState(false);
  const fetchDevelopersWithRoles = async () => {
    if (!selectedProjectId) return;
    try {
      const res = await apiClient.get(`/api/v1/bench-allocation/${selectedProjectId}/project`);
      const resData = res.data?.data || res.data;
      const projectAllocations = Array.isArray(resData) ? resData : [];
      const mapped = projectAllocations.map((alloc: any) => {
        const employeeId = alloc.empId || alloc.employeeId;
        const employeeName = alloc.employeeName || alloc.userFullName || `${alloc.firstName || ''} ${alloc.lastName || ''}`.trim() || 'Employee';
        const roleName = alloc.roleName || alloc.role?.roleName || alloc.role?.name || '';
        const roleType = alloc.roleType || alloc.role?.roleType || alloc.role?.type || '';
        const designationName = alloc.designationName || alloc.designation?.designationName || '';
        const roleId = alloc.roleId !== undefined && alloc.roleId !== null
          ? Number(alloc.roleId)
          : (alloc.role?.roleId !== undefined ? Number(alloc.role?.roleId) : undefined);
        const projectAllocationId = alloc.benchAllocationId ?? alloc.id;

        return {
          userWithRole: `${employeeName} - ${roleName || 'Employee'}`,
          employeeName,
          roleName,
          roleType,
          designationName,
          projectAllocationId,
          userId: employeeId,
          roleId,
        };
      });
      setDevelopersWithRoles(mapped);
      setProjectAllocatedEmployeeIds(
        new Set(mapped.map((dev: any) => String(dev.userId)))
      );
      setHasLoadedProjectAllocatedEmployees(true);
    } catch (error) {
      console.error("Error fetching project allocated employees:", error);
      setProjectAllocatedEmployeeIds(new Set());
      setHasLoadedProjectAllocatedEmployees(false);
    }
  };

  const removeInvalidSubmoduleAllocatedDevs = async (
    submoduleId: string | number,
    allocatedDevs: SubModuleDevAllocation[]
  ) => {
    if (!hasLoadedProjectAllocatedEmployees) {
      return allocatedDevs;
    }

    const validDevs: SubModuleDevAllocation[] = [];
    const invalidDevs: SubModuleDevAllocation[] = [];

    allocatedDevs.forEach((allocation) => {
      const employeeId = allocation.employeeId;

      if (projectAllocatedEmployeeIds.has(String(employeeId))) {
        validDevs.push(allocation);
      } else {
        invalidDevs.push(allocation);
      }
    });

    if (invalidDevs.length === 0) {
      return validDevs;
    }

    await Promise.allSettled(
      invalidDevs.map((dev) =>
        deAllocateProjectEmployeeFromSubModule(
          Number(submoduleId),
          Number(dev.employeeId)
        )
      )
    );

    return validDevs;
  };

  const fetchSubmoduleAllocatedDevs = async (submoduleId: string | number) => {
    const numericSubmoduleId = Number(submoduleId);

    if (!numericSubmoduleId || Number.isNaN(numericSubmoduleId)) {
      return [];
    }

    try {
      const response = await getAllSubDevwithName(numericSubmoduleId);
      const allocatedDevs = response.data || [];

      setSubmoduleAllocatedDevs((prev) => ({
        ...prev,
        [String(submoduleId)]: allocatedDevs,
      }));

      return allocatedDevs;
    } catch (error) {
      const apiError = error as any;
      const errorCode = apiError?.response?.data?.errorCode;
      const message =
        apiError?.response?.data?.message || apiError?.message || "";

      if (
        errorCode !== "40003" &&
        !message.toLowerCase().includes("couldn't find devs")
      ) {
        console.error("Error fetching submodule allocated developers:", error);
      }

      setSubmoduleAllocatedDevs((prev) => ({
        ...prev,
        [String(submoduleId)]: [],
      }));
      return [];
    }
  };

  
  const fetchModuleAllocatedUsers = async (moduleId: string) => {
    if (!selectedProjectId || !moduleId) return;

    setModuleAllocatedUsersLoading(true);
    setModuleAllocatedUsersError(null);

    try {
      const users = await getUsersByAllocation(
        Number(selectedProjectId),
        Number(moduleId),
      );
      console.log("Module allocated users response:", users);
      setModuleAllocatedUsers(users);
    } catch (error) {
      console.error("Error fetching module allocated users:", error);
      setModuleAllocatedUsersError(
        "Failed to fetch allocated users for this module",
      );
      setModuleAllocatedUsers([]);
    } finally {
      setModuleAllocatedUsersLoading(false);
    }
  };

  
  const fetchAllSelectedModulesAllocatedUsers = async () => {
    const moduleItems = selectedItems.filter((item) => item.type === "module");

    if (!selectedProjectId || moduleItems.length === 0) {
      setModuleAllocatedUsers([]);
      setModuleAllocatedUsersError(null);
      setModuleAllocatedUsersLoading(false);
      return;
    }

    console.log(
      "Fetching common allocated users for all selected modules:",
      moduleItems,
    );

    setModuleAllocatedUsersLoading(true);
    setModuleAllocatedUsersError(null);

    try {
      const allModuleUsers: UserByAllocation[][] = [];
      const errors: string[] = [];

      
      for (const item of moduleItems) {
        try {
          const users = await getUsersByAllocation(
            Number(selectedProjectId),
            Number(item.moduleId),
          );
          allModuleUsers.push(users);
        } catch (error) {
          console.error(
            `Error fetching users for module ${item.moduleId}:`,
            error,
          );
          errors.push(`Failed to load users for module ${item.moduleId}`);
          allModuleUsers.push([]); 
        }
      }

      let commonUsers: UserByAllocation[] = [];

      if (allModuleUsers.length === 1) {
        
        commonUsers = allModuleUsers[0] || [];
      } else if (allModuleUsers.length > 1) {
        
        const firstModuleUsers = allModuleUsers[0] || [];

        commonUsers = firstModuleUsers.filter((user) => {
          
          return allModuleUsers
            .slice(1)
            .every((moduleUsers) =>
              moduleUsers.some((otherUser) => otherUser.userId === user.userId),
            );
        });
      }

      console.log(
        "Users per module:",
        allModuleUsers.map((users) => users.length),
      );
      console.log("Common users found:", commonUsers.length);

      setModuleAllocatedUsers(commonUsers);

      if (errors.length > 0) {
        setModuleAllocatedUsersError(errors.join("; "));
      } else {
        setModuleAllocatedUsersError(null);
      }
    } catch (error) {
      console.error(
        "Error fetching common allocated users for selected modules:",
        error,
      );
      setModuleAllocatedUsers([]);
      setModuleAllocatedUsersError(
        error instanceof Error
          ? error.message
          : "Failed to load allocated users for selected modules",
      );
    } finally {
      setModuleAllocatedUsersLoading(false);
    }
  };

  
  const fetchModuleAllocatedUsersForReassignment = async (moduleId: string) => {
    if (!selectedProjectId || !moduleId) {
      console.log(
        "Missing parameters for fetchModuleAllocatedUsersForReassignment:",
        { selectedProjectId, moduleId },
      );
      return;
    }

    console.log("Fetching module allocated users for reassignment:", {
      selectedProjectId,
      moduleId,
    });

    setModuleAllocatedUsersForReassignmentLoading(true);
    setModuleAllocatedUsersForReassignmentError(null);

    try {
      const users = await getUsersByAllocation(
        Number(selectedProjectId),
        Number(moduleId),
      );
      console.log("Module allocated users for reassignment response:", users);
      console.log("Number of users found:", users.length);
      setModuleAllocatedUsersForReassignment(users);
      setModuleAllocatedUsersForReassignmentError(null);
    } catch (error) {
      console.error(
        "Error fetching module allocated users for reassignment:",
        error,
      );
      console.error("Error details:", {
        selectedProjectId,
        moduleId,
        error: error instanceof Error ? error.message : error,
      });
      setModuleAllocatedUsersForReassignment([]);
      setModuleAllocatedUsersForReassignmentError(
        error instanceof Error
          ? error.message
          : "Failed to load allocated users for this module",
      );
    } finally {
      setModuleAllocatedUsersForReassignmentLoading(false);
    }
  };

  
  const fetchSubmoduleAllocatedUsersForReassignment = async (
    moduleId: string,
    submoduleId: string,
  ) => {
    if (!selectedProjectId || !moduleId || !submoduleId) {
      console.log(
        "Missing parameters for fetchSubmoduleAllocatedUsersForReassignment:",
        { selectedProjectId, moduleId, submoduleId },
      );
      return;
    }

    console.log("Fetching submodule allocated users for reassignment:", {
      selectedProjectId,
      moduleId,
      submoduleId,
    });

    setSubmoduleAllocatedUsersForReassignmentLoading(true);
    setSubmoduleAllocatedUsersForReassignmentError(null);

    try {
      const users = await getUsersBySubmoduleAllocation(
        Number(selectedProjectId),
        Number(moduleId),
        Number(submoduleId),
      );
      console.log(
        "Submodule allocated users for reassignment response:",
        users,
      );
      console.log("Number of users found:", users.length);
      setSubmoduleAllocatedUsersForReassignment(users);
      setSubmoduleAllocatedUsersForReassignmentError(null);
    } catch (error) {
      console.error(
        "Error fetching submodule allocated users for reassignment:",
        error,
      );
      console.error("Error details:", {
        selectedProjectId,
        moduleId,
        submoduleId,
        error: error instanceof Error ? error.message : error,
      });
      setSubmoduleAllocatedUsersForReassignment([]);
      setSubmoduleAllocatedUsersForReassignmentError(
        error instanceof Error
          ? error.message
          : "Failed to load allocated users for this submodule",
      );
    } finally {
      setSubmoduleAllocatedUsersForReassignmentLoading(false);
    }
  };

  
  const fetchSubmoduleAllocatedUsersForDeallocation = async (
    moduleId: string,
    submoduleId: string,
  ) => {
    if (!selectedProjectId || !moduleId || !submoduleId) {
      console.log(
        "Missing parameters for fetchSubmoduleAllocatedUsersForDeallocation:",
        { selectedProjectId, moduleId, submoduleId },
      );
      return;
    }

    console.log("Fetching submodule allocated users for deallocation:", {
      selectedProjectId,
      moduleId,
      submoduleId,
    });

    setSubmoduleAllocatedUsersForDeallocationLoading(true);
    setSubmoduleAllocatedUsersForDeallocationError(null);

    try {
      const users = await getUsersBySubmoduleAllocation(
        Number(selectedProjectId),
        Number(moduleId),
        Number(submoduleId),
      );
      console.log(
        "Submodule allocated users for deallocation response:",
        users,
      );
      console.log("Number of users found:", users.length);
      setSubmoduleAllocatedUsersForDeallocation(users);
      setSubmoduleAllocatedUsersForDeallocationError(null);
    } catch (error) {
      console.error(
        "Error fetching submodule allocated users for deallocation:",
        error,
      );
      console.error("Error details:", {
        selectedProjectId,
        moduleId,
        submoduleId,
        error: error instanceof Error ? error.message : error,
      });
      setSubmoduleAllocatedUsersForDeallocation([]);
      setSubmoduleAllocatedUsersForDeallocationError(
        error instanceof Error
          ? error.message
          : "Failed to load allocated users for this submodule",
      );
    } finally {
      setSubmoduleAllocatedUsersForDeallocationLoading(false);
    }
  };

  
  const fetchAllSelectedSubmodulesAllocatedUsers = async () => {
    const submoduleItems = selectedItems.filter(
      (item) => item.type === "submodule",
    );

    if (!selectedProjectId || submoduleItems.length === 0) {
      setSubmoduleAllocatedUsersForDeallocation([]);
      setSubmoduleAllocatedUsersForDeallocationError(null);
      setSubmoduleAllocatedUsersForDeallocationLoading(false);
      return;
    }

    console.log(
      "Fetching common allocated users for all selected submodules:",
      submoduleItems,
    );

    setSubmoduleAllocatedUsersForDeallocationLoading(true);
    setSubmoduleAllocatedUsersForDeallocationError(null);

    try {
      const allSubmoduleUsers: UserByAllocation[][] = [];
      const errors: string[] = [];

      
      for (const item of submoduleItems) {
        if (item.submoduleId) {
          try {
            const users = await getUsersBySubmoduleAllocation(
              Number(selectedProjectId),
              Number(item.moduleId),
              Number(item.submoduleId),
            );
            allSubmoduleUsers.push(users);
          } catch (error) {
            console.error(
              `Error fetching users for submodule ${item.submoduleId}:`,
              error,
            );
            errors.push(
              `Failed to load users for submodule ${item.submoduleId}`,
            );
            allSubmoduleUsers.push([]); 
          }
        }
      }

      let commonUsers: UserByAllocation[] = [];

      if (allSubmoduleUsers.length === 1) {
        
        commonUsers = allSubmoduleUsers[0] || [];
      } else if (allSubmoduleUsers.length > 1) {
        
        const firstSubmoduleUsers = allSubmoduleUsers[0] || [];

        commonUsers = firstSubmoduleUsers.filter((user) => {
          
          return allSubmoduleUsers
            .slice(1)
            .every((submoduleUsers) =>
              submoduleUsers.some(
                (otherUser) => otherUser.userId === user.userId,
              ),
            );
        });
      }

      console.log(
        "Users per submodule:",
        allSubmoduleUsers.map((users) => users.length),
      );
      console.log("Common users found:", commonUsers.length);

      setSubmoduleAllocatedUsersForDeallocation(commonUsers);

      if (errors.length > 0) {
        setSubmoduleAllocatedUsersForDeallocationError(errors.join("; "));
      } else {
        setSubmoduleAllocatedUsersForDeallocationError(null);
      }
    } catch (error) {
      console.error(
        "Error fetching common allocated users for selected submodules:",
        error,
      );
      setSubmoduleAllocatedUsersForDeallocation([]);
      setSubmoduleAllocatedUsersForDeallocationError(
        error instanceof Error
          ? error.message
          : "Failed to load allocated users for selected submodules",
      );
    } finally {
      setSubmoduleAllocatedUsersForDeallocationLoading(false);
    }
  };
  const handleAddModule = async () => {
    if (moduleForm.name.trim() && selectedProjectId) {
      const payload = {
        name: moduleForm.name.trim(),
        projectId: Number(selectedProjectId),
      };

      setIsCreatingModule(true);

      try {
        const response = await createModuleApi(payload);
        console.log("Create module response:", response);

        if (response.status === "Created") {
          await fetchModules();
          setToastMessage("Module created successfully!");
          setShowToast(true);
          setModuleForm({ name: "" });
          setIsAddModuleModalOpen(false);

          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          const isSuccess =
            response.status === "Created" || response.success === true;
          if (isSuccess) {
            await fetchModules();
            setToastMessage("Module created successfully!");
            setShowToast(true);
            setModuleForm({ name: "" });
            setIsAddModuleModalOpen(false);
          } else {
            const errorMessage =
              response.message || response.error || "Failed to create module";
            setToastMessage(errorMessage);
            setShowToast(true);
          }
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } catch (error: any) {
        console.error("Error creating module:", error);
        let errorMessage = "Failed to add module. Please try again.";

        if (error.response?.data?.message) {
          errorMessage = error.response.data.message;
        } else if (error.response?.data?.error) {
          errorMessage = error.response.data.error;
        } else if (error.message) {
          errorMessage = error.message;
        }

        setToastMessage(errorMessage);
        setShowToast(true);

        setTimeout(() => {
          setShowToast(false);
          setToastMessage(null);
        }, 5000);
      } finally {
        setIsCreatingModule(false);
      }
    }
  };
  const handleModuleAssignment = (module: ApiModule, submodule?: any) => {
    setSelectedModuleForAssignment(module);
    setSelectedSubmoduleForAssignment(submodule || null);
    setAssignmentForm({
      moduleId: module.id.toString(),
      submoduleId: submodule?.id,
      employeeIds: submodule
        ? submodule.assignedDevs || []
        : module.assignedDevs || [],
    });
    setIsAssignmentModalOpen(true);
  };

  
  const handleSaveAssignment = () => {
    if (!selectedProjectId || !assignmentForm.moduleId) return;
    const module = modulesByProjectId?.find(
      (m) => m.id.toString() === assignmentForm.moduleId,
    );
    if (!module) return;

    
    setIsAssignmentModalOpen(false);
  };

  const handleEditModule = (module: ApiModule) => {
    setEditingModule(module);
    setModuleForm({
      name: module.name || "",
    });
    setIsEditModuleModalOpen(true);
  };

  const handleUpdateModule = async () => {
    if (moduleForm.name.trim() && editingModule && selectedProjectId) {
      setIsUpdatingModule(true);
      try {
        const response = await updateModuleApi(
          Number(selectedProjectId),
          Number(editingModule.id),
          { name: moduleForm.name },
        );
        if (response.success && response.module) {
          await fetchModules();
          setToastMessage("Module updated successfully!");
          setShowToast(true);
          setTimeout(() => {
            setModuleForm({ name: "" });
            setEditingModule(null);
            setIsEditModuleModalOpen(false);
          }, 200);

          // Auto-close notification after 5 seconds
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          // Fallback: refetch modules if update did not succeed
          await fetchModules();
          setToastMessage("Module updated successfully!");
          setShowToast(true);
          setTimeout(() => {
            setModuleForm({ name: "" });
            setEditingModule(null);
            setIsEditModuleModalOpen(false);
          }, 200);

          // Auto-close notification after 5 seconds
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } catch (error: any) {
        console.error("Error updating module:", error);

        if (error.response?.data?.message) {
          // Use the exact message from API response
          setToastMessage(error.response.data.message);
          setShowToast(true);

          // Auto-close notification after 5 seconds
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          // Generic error
          setToastMessage("Failed to update module. Please try again.");
          setShowToast(true);

          // Auto-close notification after 5 seconds
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } finally {
        setIsUpdatingModule(false);
      }
    }
  };

  // Save or update submodule (used by button click and Enter key)
  const handleSaveSubmodule = async () => {
    if (!submoduleForm.name.trim() || !currentModuleIdForSubmodule) return;

    if (isEditingSubmodule && editingSubmoduleId) {
      // Edit mode: update submodule name via API
      setIsUpdatingSubmodule(true);
      try {
        const response = await updateSubmoduleApi(
          Number(editingSubmoduleId),
          Number(currentModuleIdForSubmodule),
          { subModuleName: submoduleForm.name },
        );

        if (response.status === "success" || response.success) {
          await fetchModules();
          setIsAddSubmoduleModalOpen(false);
          setIsEditingSubmodule(false);
          setEditingSubmoduleId(null);
          setToastMessage(
            response.message || "Submodule updated successfully!",
          );
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          const serverMessage = response.message;
          setToastMessage(
            serverMessage || "Failed to update submodule. Please try again.",
          );
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } catch (error: any) {
        const backendMessage = error.response?.data?.message || error.message;
        if (backendMessage) {
          setToastMessage(backendMessage);
        } else {
          setToastMessage("Failed to update submodule. Please try again.");
        }
        setShowToast(true);
        setTimeout(() => {
          setShowToast(false);
          setToastMessage(null);
        }, 5000);
      } finally {
        setIsUpdatingSubmodule(false);
      }
    } else {
      
      setIsCreatingSubmodule(true);
      try {
        const response = await createSubmodule({
          subModuleName: submoduleForm.name,
          moduleId: Number(currentModuleIdForSubmodule),
        });
        if (response.status === "success") {
          await fetchModules();
          setIsAddSubmoduleModalOpen(false);
          setIsEditingSubmodule(false);
          setEditingSubmoduleId(null);
          setToastMessage("Submodule added successfully!");
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          const serverMessage = response.message;
          setToastMessage(
            serverMessage || "Failed to add submodule. Please try again.",
          );
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } catch (error: any) {
        if (error.response?.data?.message) {
          setToastMessage(error.response.data.message);
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        } else {
          setToastMessage("Failed to add submodule. Please try again.");
          setShowToast(true);
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000);
        }
      } finally {
        setIsCreatingSubmodule(false);
      }
    }
  };

  
  const hasModuleAllocatedDevelopers = (moduleId: string): boolean => {
    const directModuleDevs = (moduleDevelopers[moduleId] || []).filter(
      (d) => d.subModuleId == null,
    );
    return directModuleDevs.length > 0;
  };

  
  const hasSubmoduleAllocatedDevelopers = (
    moduleId: string,
    submoduleId: string,
  ): boolean => {
    const submoduleUsers =
      (submoduleAllocations[moduleId] &&
        submoduleAllocations[moduleId][submoduleId]) ||
      [];
    return submoduleUsers.length > 0;
  };

  
  const hasModuleChildren = (moduleId: string): boolean => {
    const module = modulesByProjectId?.find(
      (m) => m.id.toString() === moduleId,
    );
    return !!(
      module?.submodules &&
      Array.isArray(module.submodules) &&
      module.submodules.length > 0
    );
  };

  
  const checkModuleUsedInDefects = async (
    moduleId: string,
  ): Promise<boolean> => {
    if (!selectedProjectId) return false;
    try {
      const defects = await getDefectsByProjectId(selectedProjectId, 0, 1000000);
      const module = modulesByProjectId?.find(
        (m) => m.id.toString() === moduleId,
      );
      const moduleName = module?.name;

      if (!moduleName) return false;

      
      return defects.some((defect) => defect.module_name === moduleName);
    } catch (error) {
      console.error("Error checking module usage in defects:", error);
      return false;
    }
  };

  
  const checkSubmoduleUsedInDefects = async (
    moduleId: string,
    submoduleId: string,
  ): Promise<boolean> => {
    if (!selectedProjectId) return false;
    try {
      const defects = await getDefectsByProjectId(selectedProjectId, 0, 1000000);
      const module = modulesByProjectId?.find(
        (m) => m.id.toString() === moduleId,
      );
      const submodule = module?.submodules?.find(
        (s: any) => s.id.toString() === submoduleId,
      );
      const submoduleName =
        submodule?.getSubModuleName ||
        submodule?.name ||
        submodule?.name ||
        submodule?.submoduleName ||
        submodule?.subModuleId;

      if (!submoduleName) return false;

      
      return defects.some((defect) => defect.sub_module_name === submoduleName);
    } catch (error) {
      console.error("Error checking submodule usage in defects:", error);
      return false;
    }
  };

  

  const handleDeleteSubmoduleClick = async (
    moduleId: string,
    submoduleId: string,
  ) => {
    console.log("1. Received moduleId:", moduleId);
    console.log("2. Received submoduleId:", submoduleId);

    if (hasSubmoduleAllocatedDevelopers(moduleId, submoduleId)) {
      setToastMessage("Cannot delete submodule: It has allocated developers.");
      setShowToast(true);
    } else {
      const isUsedInDefects = await checkSubmoduleUsedInDefects(
        moduleId,
        submoduleId,
      );
      if (isUsedInDefects) {
        setToastMessage(
          "Cannot delete submodule: It is being used in defects.",
        );
        setShowToast(true);
      } else {
        console.log("3. Setting state - moduleId:", moduleId);
        console.log("4. Setting state - submoduleId:", submoduleId);

        setPendingDeleteModuleIdForSubmodule(moduleId);
        setPendingDeleteSubmoduleId(submoduleId);
        setConfirmOpen(true);
      }
    }
  };

  const handleDeleteModule = async (moduleId: string) => {
    if (!selectedProjectId) return;

    try {
      const response = await deleteModuleApi(
        Number(selectedProjectId),
        Number(moduleId),
      );

      
      await fetchModules();
      setToastMessage("Module deleted successfully!");
      setShowToast(true);

      setTimeout(() => {
        setShowToast(false);
        setToastMessage(null);
      }, 5000);
    } catch (error: any) {
      console.error("Error deleting module:", error);

      
      const errorMessage =
        error.response?.data?.message ||
        error.message ||
        "Failed to delete module";
      
      setToastMessage("Module Already Linked - Cannot Delete");
      setShowToast(true);

      setTimeout(() => {
        setShowToast(false);
        setToastMessage(null);
      }, 5000);
    }
  };

  const handleBulkAssignment = () => {
    fetchDevelopersWithRoles();
    if (selectedItems.length > 0) {
      setAssignmentForm({
        moduleId: "",
        employeeIds: [],
      });
      setSelectedDeveloperProjectAllocationId(null); // Reset on open
      setSelectedModuleDeveloperProjectAllocationId(null); // Reset on open
      setSelectedDevelopersForDeallocation(null); // Reset deallocation selections
      setSelectedDevelopersForDeallocationBulk([]); // Reset bulk deallocation selections
      setSelectedModuleDevelopersForDeallocationBulk([]); // Reset module bulk deallocation selections
      setSelectedDevelopersForReassignment({
        oldDeveloperId: null,
        newDeveloperId: null,
      }); // Reset reassignment selections

      // Reset submodule reassignment data
      setSubmoduleAllocatedUsersForReassignment([]);
      setSubmoduleAllocatedUsersForReassignmentError(null);
      setSubmoduleAllocatedUsersForReassignmentLoading(false);

      // Reset submodule deallocation data
      setSubmoduleAllocatedUsersForDeallocation([]);
      setSubmoduleAllocatedUsersForDeallocationError(null);
      setSubmoduleAllocatedUsersForDeallocationLoading(false);

      // Don't pre-select developers - ensure buttons start disabled until user makes selections
      setSelectedDeveloperProjectAllocationIds([]);
      setSelectedModuleDeveloperProjectAllocationId(null);

      setIsBulkAssignmentModalOpen(true);
    }
  };

  // Enhanced developer management handlers
  const handleDeallocateDevelopers = async () => {
    if (
      selectedModuleDevelopersForDeallocationBulk.length === 0 &&
      selectedDevelopersForDeallocationBulk.length === 0
    ) {
      setToastMessage("Please select developers to deallocate.");
      setShowToast(true);
      return;
    }

    setIsDeallocating(true);
    try {
      let successCount = 0;
      let errorCount = 0;

      for (const item of selectedItems) {
        try {
          if (item.type === "module" && selectedModuleDevelopersForDeallocationBulk.length > 0) {
            try {
              const moduleUsers = await getUsersByAllocation(
                Number(selectedProjectId),
                Number(item.moduleId),
              );

              for (const developerId of selectedModuleDevelopersForDeallocationBulk) {
                try {
                  await deallocateDeveloperFromModule(
                    Number(selectedProjectId),
                    Number(item.moduleId),
                    developerId,
                  );
                  successCount++;
                } catch (error) {
                  console.error(
                    `Module deallocation error for developer ${developerId} from module ${item.moduleId}:`,
                    error,
                  );
                  errorCount++;
                }
              }
            } catch (error) {
              console.error(
                `Error fetching module allocation data for module ${item.moduleId}:`,
                error,
              );
              for (const developerId of selectedModuleDevelopersForDeallocationBulk) {
                try {
                  await deallocateDeveloperFromModule(
                    Number(selectedProjectId),
                    Number(item.moduleId),
                    developerId,
                  );
                  successCount++;
                } catch (legacyError) {
                  errorCount++;
                }
              }
            }
          } else if (item.type === "submodule" && item.submoduleId && selectedDevelopersForDeallocationBulk.length > 0) {
            const subModuleId = Number(item.submoduleId);

            for (const developerId of selectedDevelopersForDeallocationBulk) {
              try {
                await deAllocateProjectEmployeeFromSubModule(
                  subModuleId,
                  Number(developerId),
                );
                successCount++;
              } catch (error) {
                console.error(
                  `Submodule deallocation error for developer ${developerId} from submodule ${item.submoduleId}:`,
                  error,
                );
                errorCount++;
              }
            }
          }
        } catch (error) {
          console.error("Deallocation error:", error);
          errorCount++;
        }
      }

      if (successCount > 0) {
        setToastMessage(
          `Successfully deallocated ${successCount} developer(s).${errorCount > 0 ? ` ${errorCount} operation(s) failed.` : ""}`,
        );
        setShowToast(true);
        setSelectedModuleDevelopersForDeallocationBulk([]);
        setSelectedDevelopersForDeallocationBulk([]);
        setActiveSubmoduleSection(null);

        await fetchModules();
        await fetchDevelopersWithRoles();

        if (selectedItems.length > 0) {
          const moduleItems = selectedItems.filter(
            (item) => item.type === "module",
          );
          const submoduleItems = selectedItems.filter(
            (item) => item.type === "submodule",
          );

          if (moduleItems.length > 0) {
            await fetchAllSelectedModulesAllocatedUsers();
            for (const item of moduleItems) {
              const devs = await getDevelopersByModuleId(
                Number(selectedProjectId),
                Number(item.moduleId),
              );
              setModuleDevelopers((prev) => ({
                ...prev,
                [item.moduleId]: devs,
              }));
            }
          }
          if (submoduleItems.length > 0) {
            await fetchAllSelectedSubmodulesAllocatedUsers();
            await Promise.all(
              submoduleItems.map((item) =>
                item.submoduleId
                  ? fetchSubmoduleAllocatedDevs(item.submoduleId)
                  : Promise.resolve([]),
              ),
            );
          }
        }

        setTimeout(() => {
          setShowToast(false);
          setToastMessage(null);
        }, 5000);
      } else {
        setToastMessage("Failed to deallocate developers. Please try again.");
        setShowToast(true);
      }
    } catch (error) {
      setToastMessage("An error occurred during deallocation.");
      setShowToast(true);
    } finally {
      setIsDeallocating(false);
      setSelectedDevelopersForDeallocation(null);
      setSelectedDevelopersForDeallocationBulk([]);
      setSelectedModuleDevelopersForDeallocationBulk([]);
    }
  };

  const handleReassignDevelopers = async () => {
    if (
      !selectedProjectId ||
      !selectedDevelopersForReassignment.oldDeveloperId ||
      !selectedDevelopersForReassignment.newDeveloperId
    ) {
      setToastMessage(
        "Please select both old and new developers for reassignment.",
      );
      setShowToast(true);
      return;
    }

    setIsDeallocating(true);
    try {
      let successCount = 0;
      let errorCount = 0;

      for (const item of selectedItems) {
        try {
          if (item.type === "module") {
            
            const selectedOldUser = moduleAllocatedUsersForReassignment.find(
              (user) =>
                user.userId ===
                selectedDevelopersForReassignment.oldDeveloperId,
            );

            if (selectedOldUser && selectedOldUser.allocateModuleId) {
              
              await reassignDeveloperWithAllocateModuleId(
                selectedOldUser.allocateModuleId,
                selectedDevelopersForReassignment.newDeveloperId,
              );
              successCount++;
            } else {
              console.error(
                "allocateModuleId not found for selected old developer",
              );
              errorCount++;
            }
          } else if (item.type === "submodule" && item.submoduleId) {
            
            console.log("Processing submodule reassignment:", {
              submoduleId: item.submoduleId,
              oldDeveloperId: selectedDevelopersForReassignment.oldDeveloperId,
              newDeveloperId: selectedDevelopersForReassignment.newDeveloperId,
              availableUsers: submoduleAllocatedUsersForReassignment,
            });

            const selectedOldUser = submoduleAllocatedUsersForReassignment.find(
              (user) =>
                user.userId ===
                selectedDevelopersForReassignment.oldDeveloperId,
            );

            if (selectedOldUser && selectedOldUser.allocationId) {
              console.log("Found old user with allocationId:", selectedOldUser);
              
              await reassignSubmoduleDeveloperWithAllocateModuleId(
                selectedOldUser.allocationId,
                selectedDevelopersForReassignment.newDeveloperId,
              );
              successCount++;
            } else {
              console.error(
                "Cannot reassign submodule developer - allocationId not found:",
                {
                  selectedOldUser,
                  oldDeveloperId:
                    selectedDevelopersForReassignment.oldDeveloperId,
                  submoduleAllocatedUsers:
                    submoduleAllocatedUsersForReassignment,
                  hasAllocationId: selectedOldUser?.allocationId,
                },
              );

              
              const errorMsg = !selectedOldUser
                ? "Selected developer not found in submodule allocations"
                : "Allocation ID missing for selected developer";
              console.error(`Submodule reassignment failed: ${errorMsg}`);
              errorCount++;
            }
          }
        } catch (error) {
          console.error("Reassignment error:", error);
          errorCount++;
        }
      }

      if (successCount > 0) {
        setToastMessage(
          `Successfully reassigned ${successCount} developer(s).${errorCount > 0 ? ` ${errorCount} operation(s) failed.` : ""}`,
        );
        setShowToast(true);

        
        const moduleItem = selectedItems.find((item) => item.type === "module");
        if (moduleItem && moduleItem.moduleId) {
          fetchModuleAllocatedUsersForReassignment(moduleItem.moduleId);
        }

        
        const submoduleItem = selectedItems.find(
          (item) => item.type === "submodule",
        );
        if (
          submoduleItem &&
          submoduleItem.moduleId &&
          submoduleItem.submoduleId
        ) {
          fetchSubmoduleAllocatedUsersForReassignment(
            submoduleItem.moduleId,
            submoduleItem.submoduleId,
          );
        }

        await fetchModules();
      } else {
        setToastMessage("Failed to reassign developers. Please try again.");
        setShowToast(true);
      }
    } catch (error) {
      setToastMessage("An error occurred during reassignment.");
      setShowToast(true);
    } finally {
      setIsDeallocating(false);
      setSelectedDevelopersForReassignment({
        oldDeveloperId: null,
        newDeveloperId: null,
      });
      setIsBulkAssignmentModalOpen(false);
      setSelectedItems([]);
    }
  };

  
  const handleSaveBulkAssignment = async () => {
    setIsAllocating(true);

    try {
      if (onlyModulesSelected) {
        if (!selectedModuleDeveloperProjectAllocationId) {
          setToastMessage("Please select a QA Lead or QA Engineer for module allocation.");
          setShowToast(true);
          return;
        }
        let didAllocate = false;
        for (const item of selectedItems) {
          if (item.type === "module") {
            
            const directModuleDevs = (
              moduleDevelopers[item.moduleId] || []
            ).filter((d) => d.subModuleId == null);
            if (directModuleDevs.length > 0) {
              const module = modulesByProjectId?.find(
                (m) => m.id.toString() === item.moduleId,
              );
              const moduleName = module?.name || "Unknown Module";
              setToastMessage(
                `Module leader already allocated for ${moduleName}`,
              );
              setShowToast(true);
              setTimeout(() => setShowToast(false), 3000);
              return;
            }

            
            const alreadyAssigned = directModuleDevs.some(
              (d) =>
                d.projectAllocationId ===
                selectedModuleDeveloperProjectAllocationId,
            );
            if (alreadyAssigned) {
              continue;
            }

            try {
              
              const selectedDeveloper = developersWithRoles.find(
                (d) =>
                  d.projectAllocationId ===
                  selectedModuleDeveloperProjectAllocationId,
              );
              if (!selectedDeveloper) {
                setAlertMessage(
                  "Selected developer not found. Please try again.",
                );
                setAlertOpen(true);
                return;
              }

              await allocateModuleLeader({
                projectId: Number(selectedProjectId),
                moduleId: Number(item.moduleId),
                userId: selectedDeveloper.userId,
              });
              didAllocate = true;
            } catch (error: any) {
              if (error.response?.data?.message) {
                setToastMessage(error.response.data.message);
              } else {
                setToastMessage(
                  "Failed to allocate module leader. Please try again.",
                );
              }
              setShowToast(true);
              return;
            }
          }
        }
        if (didAllocate) {
          setToastMessage("Module leader allocated successfully!");
          setShowToast(true);
          
          if (selectedItems.length > 0) {
            const moduleItems = selectedItems.filter(
              (item) => item.type === "module",
            );
            if (moduleItems.length > 0) {
              await fetchAllSelectedModulesAllocatedUsers();
              for (const item of moduleItems) {
                const devs = await getDevelopersByModuleId(
                  Number(selectedProjectId),
                  Number(item.moduleId),
                );
                setModuleDevelopers((prev) => ({
                  ...prev,
                  [item.moduleId]: devs,
                }));
              }
            }
          }

          
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000); 

          
          
          
          
          
          
          
          
          
          
          
          
          
          
          
          
          




        } else {
          setToastMessage("Already allocated.");
          setShowToast(true);
        }
        
        setSelectedModuleDeveloperProjectAllocationId(null);
        
        fetchModules();
        return;
      }
      
      if (onlySubmodulesSelected) {
        if (
          !selectedDeveloperProjectAllocationIds ||
          selectedDeveloperProjectAllocationIds.length === 0
        ) {
          setToastMessage(
            "Please select at least one developer for submodule allocation.",
          );
          setShowToast(true);
          return;
        }

        const submoduleAllocationResults: {
          success: number;
          alreadyAllocated: string[];
        } = { success: 0, alreadyAllocated: [] };

        for (const item of selectedItems) {
          if (item.type === "submodule" && item.submoduleId) {
            
            const existingSubmoduleDeveloperIds = (
              submoduleAllocatedDevs[item.submoduleId] || []
            ).map((allocation) => String(allocation.employeeId));
            const module = modulesByProjectId?.find(
              (m) => m.id.toString() === item.moduleId,
            );
            const submodule = module?.submodules?.find(
              (s) => s.id.toString() === item.submoduleId,
            );
            const submoduleName =
              submodule?.getSubModuleName ||
              submodule?.name ||
              submodule?.name ||
              submodule?.submoduleName ||
              submodule?.subModuleId ||
              "Unknown Submodule";

            
            const alreadyAllocatedUserNames: string[] = [];
            const newEmployeeIds: number[] = [];
            for (const projectAllocationId of selectedDeveloperProjectAllocationIds) {
              const dev = developersWithRoles.find(
                (d) => d.projectAllocationId === projectAllocationId,
              );
              const employeeId = dev?.userId;
              const selectedUserName = dev
                ? dev.userWithRole.split("-")[0].trim()
                : projectAllocationId.toString();
              const isAlreadyAllocated = employeeId
                ? existingSubmoduleDeveloperIds.includes(String(employeeId))
                : false;
              if (isAlreadyAllocated) {
                alreadyAllocatedUserNames.push(selectedUserName);
              } else if (employeeId) {
                newEmployeeIds.push(employeeId);
              }
            }

            if (
              alreadyAllocatedUserNames.length ===
              selectedDeveloperProjectAllocationIds.length
            ) {
              setToastMessage(
                `Already allocated ${alreadyAllocatedUserNames.join(", ")} for ${submoduleName}`,
              );
              setShowToast(true);
              setTimeout(() => setShowToast(false), 3000);
              
              continue; 
            }

            if (newEmployeeIds.length > 0) {
              try {
                for (const employeeId of newEmployeeIds) {
                  await allocateProjectEmployeeToSubModule(
                    Number(item.submoduleId),
                    employeeId,
                  );
                  submoduleAllocationResults.success++;
                }
                await fetchSubmoduleAllocatedDevs(item.submoduleId);
              } catch (error) {
                console.error(
                  `Submodule allocation error for ${submoduleName}:`,
                  error,
                );
              }
            }
          }
        }
        
        if (submoduleAllocationResults.success > 0) {
          setToastMessage("Allocations successfully");
          setShowToast(true);
          
          if (selectedItems.length > 0) {
            const submoduleItems = selectedItems.filter(
              (item) => item.type === "submodule",
            );
            if (submoduleItems.length > 0) {
              await fetchAllSelectedSubmodulesAllocatedUsers();
              await Promise.all(
                submoduleItems.map((item) =>
                  item.submoduleId
                    ? fetchSubmoduleAllocatedDevs(item.submoduleId)
                    : Promise.resolve([]),
                ),
              );
            }
          }

          
          setTimeout(() => {
            setShowToast(false);
            setToastMessage(null);
          }, 5000); 

          
          setSelectedDeveloperProjectAllocationIds([]);

          
          
          
          
          
          
          
          
          
          
          
          
          
          
          
          
          
        }
        fetchModules();
        return;
      }
    } finally {
      setIsAllocating(false);
    }
  };

  
  const hasModuleSelection = () => {
    return selectedItems.some((item) => item.type === "module");
  };

  const hasSubmoduleSelection = () => {
    return selectedItems.some((item) => item.type === "submodule");
  };

  
  const isModuleAlreadyAllocated = () => {
    if (!hasModuleSelection()) return false;

    return selectedItems.some((item) => {
      if (item.type === "module") {
        const directModuleDevs = (moduleDevelopers[item.moduleId] || []).filter(
          (d) => d.subModuleId == null,
        );
        return directModuleDevs.length > 0 || (moduleAllocatedUsers && moduleAllocatedUsers.length > 0);
      }
      return false;
    });
  };

  
  const isSubmoduleAlreadyAllocated = () => {
    
    
    return false;
  };

  
  const hasAllocatedDevelopersForDeallocation = () => {
    let hasModuleAlloc = false;
    let hasSubmoduleAlloc = false;

    if (selectedItems.some((item) => item.type === "module")) {
      hasModuleAlloc = !!(moduleAllocatedUsers && moduleAllocatedUsers.length > 0);
    }
    if (selectedItems.some((item) => item.type === "submodule")) {
      hasSubmoduleAlloc = !!(
        selectedSubmoduleAllocatedDevelopers.length > 0 ||
        (submoduleAllocatedUsersForDeallocation &&
          submoduleAllocatedUsersForDeallocation.length > 0)
      );
    }
    return hasModuleAlloc || hasSubmoduleAlloc;
  };

  const isItemDisabled = (
    type: "module" | "submodule",
    moduleId: string,
    submoduleId?: string,
  ) => {
    
    if (selectedItems.length === 0) return false;

    
    if (hasModuleSelection() && type === "submodule") return true;

    
    if (hasSubmoduleSelection() && type === "module") return true;

    return false;
  };

  const handleSelectItem = (
    type: "module" | "submodule",
    moduleId: string,
    checked: boolean,
    submoduleId?: string,
  ) => {
    if (checked) {
      if (type === "module") {
        
        setSelectedItems((prev) => [
          ...prev.filter((item) => item.type === "module"),
          { type: "module" as const, moduleId, submoduleId: undefined },
        ]);
      } else {
        
        setSelectedItems((prev) => [
          ...prev.filter((item) => item.type === "submodule"),
          { type, moduleId, submoduleId },
        ]);
      }
    } else {
      if (type === "module") {
        
        setSelectedItems((prev) =>
          prev.filter(
            (item) => !(item.type === "module" && item.moduleId === moduleId),
          ),
        );
      } else {
        
        setSelectedItems((prev) =>
          prev.filter(
            (item) =>
              !(
                item.type === type &&
                item.moduleId === moduleId &&
                item.submoduleId === submoduleId
              ),
          ),
        );
      }
    }
  };

  const handleSelectAllModules = (checked: boolean) => {
    if (checked) {
      
      const allItems = (modulesByProjectId || []).flatMap((module) => [
        {
          type: "module" as const,
          moduleId: module.id.toString(),
          submoduleId: undefined,
        },
        ...(module.submodules || []).map((sub) => ({
          type: "submodule" as const,
          moduleId: module.id.toString(),
          submoduleId: sub.id.toString(),
        })),
      ]);
      setSelectedItems(allItems);
    } else {
      setSelectedItems([]);
    }
  };

  const isItemSelected = (
    type: "module" | "submodule",
    moduleId: string,
    submoduleId?: string,
  ) => {
    return selectedItems.some(
      (item) =>
        item.type === type &&
        item.moduleId === moduleId &&
        item.submoduleId === submoduleId,
    );
  };

  const isAllModulesSelected = () => {
    return (
      (modulesByProjectId || []).length > 0 &&
      (modulesByProjectId || []).every((module) =>
        selectedItems.some(
          (item) =>
            item.type === "module" && item.moduleId === module.id.toString(),
        ),
      )
    );
  };

  
  const handleProjectSelect = (id: string, projectData?: any) => {
    console.log("Project selected:", id, projectData);
    switchProject(Number(id));
    setGlobalProjectId(id); 
    
    setModulesByProjectId(null);
    setSelectedItems([]);
    navigate(`/projects/${id}/project-management/module-management`);
  };

  
  console.log("Selected Project ID:", selectedProjectId);

  const project = projects.find(
    (p) => String(p.id) === String(selectedProjectId),
  );

  

  
  
  const fetchModules = async () => {
    if (!selectedProjectId) return;
    setIsLoading(true);
    try {
      const response = await getModulesByProjectId(Number(selectedProjectId));
      if (response.data) {
        const modulesWithSubmodules = await Promise.all(
          response.data.map(async (module) => {
            try {
              
              
              
              const submodulesRes = await getSubmodulesByModule(module.id);

              return {
                ...module,
                submodules: submodulesRes.data || [],
              };
            } catch (err) {
              console.error(
                `Error fetching submodules for module ${module.id}:`,
                err,
              );
              return { ...module, submodules: [] };
            }
          }),
        );
        setModulesByProjectId(modulesWithSubmodules);
      }
    } catch (error) {
      console.error("Error fetching modules:", error);
      setModulesByProjectId([]);
    } finally {
      setIsLoading(false);
    }
  };


  const filteredModules = (modulesByProjectId || []).filter((module) =>
    module.name
      ?.toLowerCase()
      .includes(moduleSearch.toLowerCase())
  );

  useEffect(() => {
    if (selectedProjectId) {
      fetchModules();
      fetchDevelopersWithRoles();
    }
  }, [selectedProjectId]);

  useEffect(() => {
    fetchModules();
  }, [selectedProjectId]);

  useEffect(() => {
    if (modulesByProjectId && selectedProjectId) {
      modulesByProjectId.forEach(async (module) => {
        try {
          
          const projectId = Number(selectedProjectId);
          const moduleId = Number(module.id);

          if (!projectId || !moduleId || isNaN(projectId) || isNaN(moduleId)) {
            console.warn(
              "Skipping getDevelopersByModuleId due to invalid IDs:",
              {
                projectId,
                moduleId,
                selectedProjectId,
                moduleObjectId: module.id, 
              },
            );
            setModuleDevelopers((prev) => ({ ...prev, [module.id]: [] }));
            setModuleDevelopersError((prev) => ({
              ...prev,
              [module.id]: "Invalid module ID",
            }));
            return;
          }

          
          setModuleDevelopersLoading((prev) => ({
            ...prev,
            [module.id]: true,
          }));
          setModuleDevelopersError((prev) => ({ ...prev, [module.id]: "" }));

          // Use selectedProjectId (or projectId) and module.id for the new API
          const devs = await getDevelopersByModuleId(projectId, moduleId);
          console.log("Module developers for module:", moduleId, devs);

          // Ensure devs is an array and has the expected structure
          let processedDevs = devs;
          if (Array.isArray(devs)) {
            processedDevs = devs.map((dev: any) => {
              // Ensure each developer object has the necessary fields
              return {
                ...dev,
                userName:
                  dev.userName ||
                  dev.userFullName ||
                  dev.name ||
                  (dev.firstName && dev.lastName
                    ? `${dev.firstName} ${dev.lastName}`
                    : "Unknown User"),
                subModuleId:
                  dev.subModuleId ||
                  dev.submoduleId ||
                  dev.submodule_id ||
                  null,
              };
            });
          } else {
            console.warn("Module developers response is not an array:", devs);
            processedDevs = [];
          }

          console.log("Processed module developers:", processedDevs);

          
          if (processedDevs.length === 0) {
            console.log("No module developers found for module:", moduleId);
          }

          setModuleDevelopers((prev) => ({
            ...prev,
            [module.id]: processedDevs,
          }));
          setModuleDevelopersLoading((prev) => ({
            ...prev,
            [module.id]: false,
          }));
        } catch (e) {
          console.error("Error fetching developers for module:", module.id, e);
          setModuleDevelopers((prev) => ({ ...prev, [module.id]: [] }));
          setModuleDevelopersLoading((prev) => ({
            ...prev,
            [module.id]: false,
          }));
          setModuleDevelopersError((prev) => ({
            ...prev,
            [module.id]: e instanceof Error ? e.message : "Unknown error",
          }));
        }
      });
    }
  }, [modulesByProjectId, selectedProjectId]);

  
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingDeleteSubmoduleId, setPendingDeleteSubmoduleId] = useState<
    string | null
  >(null);

  
  const [confirmModuleOpen, setConfirmModuleOpen] = useState(false);
  const [pendingDeleteModuleId, setPendingDeleteModuleId] = useState<
    string | null
  >(null);

  
  const [
    selectedDeveloperProjectAllocationIds,
    setSelectedDeveloperProjectAllocationIds,
  ] = useState<number[]>([]);

  
  const [activeTab, setActiveTab] = useState<
    "allocate" | "deallocate" | "reassign"
  >("allocate");
  const [
    selectedDevelopersForDeallocation,
    setSelectedDevelopersForDeallocation,
  ] = useState<number | null>(null);
  const [
    selectedDevelopersForDeallocationBulk,
    setSelectedDevelopersForDeallocationBulk,
  ] = useState<number[]>([]);
  const [
    selectedModuleDevelopersForDeallocationBulk,
    setSelectedModuleDevelopersForDeallocationBulk,
  ] = useState<number[]>([]);
  const [
    selectedDevelopersForReassignment,
    setSelectedDevelopersForReassignment,
  ] = useState<{
    oldDeveloperId: number | null;
    newDeveloperId: number | null;
  }>({ oldDeveloperId: null, newDeveloperId: null });
  const [activeSubmoduleSection, setActiveSubmoduleSection] = useState<
    "allocate" | "deallocate" | null
  >(null);
  const [isAllocating, setIsAllocating] = useState(false);
  const [isDeallocating, setIsDeallocating] = useState(false);

  
  const [
    moduleAllocatedUsersForReassignment,
    setModuleAllocatedUsersForReassignment,
  ] = useState<UserByAllocation[]>([]);
  const [
    moduleAllocatedUsersForReassignmentLoading,
    setModuleAllocatedUsersForReassignmentLoading,
  ] = useState(false);
  const [
    moduleAllocatedUsersForReassignmentError,
    setModuleAllocatedUsersForReassignmentError,
  ] = useState<string | null>(null);

  
  const [
    submoduleAllocatedUsersForReassignment,
    setSubmoduleAllocatedUsersForReassignment,
  ] = useState<UserByAllocation[]>([]);
  const [
    submoduleAllocatedUsersForReassignmentLoading,
    setSubmoduleAllocatedUsersForReassignmentLoading,
  ] = useState(false);
  const [
    submoduleAllocatedUsersForReassignmentError,
    setSubmoduleAllocatedUsersForReassignmentError,
  ] = useState<string | null>(null);

  
  const [
    submoduleAllocatedUsersForDeallocation,
    setSubmoduleAllocatedUsersForDeallocation,
  ] = useState<UserByAllocation[]>([]);
  const [
    submoduleAllocatedUsersForDeallocationLoading,
    setSubmoduleAllocatedUsersForDeallocationLoading,
  ] = useState(false);
  const [
    submoduleAllocatedUsersForDeallocationError,
    setSubmoduleAllocatedUsersForDeallocationError,
  ] = useState<string | null>(null);

  const onlyModulesSelected =
    selectedItems.length > 0 &&
    selectedItems.every((item) => item.type === "module");
  const onlySubmodulesSelected =
    selectedItems.length > 0 &&
    selectedItems.every((item) => item.type === "submodule");
  const mixedSelection =
    selectedItems.some((item) => item.type === "module") &&
    selectedItems.some((item) => item.type === "submodule");
  const selectedSubmoduleAllocatedDevelopers = (() => {
    const submoduleItems = selectedItems.filter(
      (item) => item.type === "submodule" && item.submoduleId,
    );
    if (submoduleItems.length === 0) return [];

    const allocationsBySubmodule = submoduleItems.map(
      (item) => submoduleAllocatedDevs[item.submoduleId!.toString()] || [],
    );

    const commonAllocations =
      allocationsBySubmodule.length === 1
        ? allocationsBySubmodule[0]
        : (allocationsBySubmodule[0] || []).filter((allocation) =>
          allocationsBySubmodule
            .slice(1)
            .every((submoduleAllocationsForItem) =>
              submoduleAllocationsForItem.some(
                (otherAllocation) =>
                  String(otherAllocation.employeeId) ===
                  String(allocation.employeeId),
              ),
            ),
        );

    return commonAllocations.map((allocation) => {
      const developer = developersWithRoles.find(
        (dev) => String(dev.userId) === String(allocation.employeeId),
      );
      const employee = employees.find(
        (emp) => String(emp.id) === String(allocation.employeeId),
      );
      const [developerName, developerRole] =
        developer?.userWithRole?.split("-") || [];
      const displayName =
        allocation.employeeName ||
        developerName?.trim() ||
        (employee ? `${employee.firstName} ${employee.lastName}` : "") ||
        `Employee ${allocation.employeeId}`;
      const role =
        developerRole?.trim() || employee?.designation || "Developer";

      return {
        userId: Number(allocation.employeeId),
        userName: displayName,
        userRole: role,
        userWithRole: `${displayName}-${role}`,
        allocationId: Number(allocation.id),
        subModuleId: Number(allocation.submoduleId),
      };
    });
  })();

  

  useEffect(() => {
    const fetchDeveloperRoles = async () => {
      try {
        const roleNameGroups = await Promise.all([
          roleTypeBasedRoleFetch("DEV_LEAD"),
          roleTypeBasedRoleFetch("SENIOR_DEVELOPER"),
          roleTypeBasedRoleFetch("DEVELOPER"),
          roleTypeBasedRoleFetch("JUNIOR_DEVELOPER"),
        ]);

        const roleIdGroups = await Promise.all([
          roleTypeBasedRoleIdFetch("DEV_LEAD"),
          roleTypeBasedRoleIdFetch("SENIOR_DEVELOPER"),
          roleTypeBasedRoleIdFetch("DEVELOPER"),
          roleTypeBasedRoleIdFetch("JUNIOR_DEVELOPER"),
        ]);

        setDeveloperRoleNames(roleNameGroups.flat());
        setDeveloperRoleIds(roleIdGroups.flat());
      } catch (error) {
        console.error("Error fetching developer roles:", error);
        setDeveloperRoleNames([]);
        setDeveloperRoleIds([]);
      }
    };



    fetchDeveloperRoles();
  }, []);




  useEffect(() => {
    const fetchModuleLeaderRoles = async () => {
      try {
        const roleNameGroups = await Promise.all([
          roleTypeBasedRoleFetch("QA_LEAD"),
          roleTypeBasedRoleFetch("QA_ENGINEER"),
        ]);

        const roleIdGroups = await Promise.all([
          roleTypeBasedRoleIdFetch("QA_LEAD"),
          roleTypeBasedRoleIdFetch("QA_ENGINEER"),
        ]);

        setModuleLeaderRoleNames(roleNameGroups.flat());
        setModuleLeaderRoleIds(roleIdGroups.flat());
      } catch (error) {
        console.error("Error fetching module leader roles:", error);
        setModuleLeaderRoleNames([]);
        setModuleLeaderRoleIds([]);
      }
    };

    fetchModuleLeaderRoles();
  }, []);
  useEffect(() => {
    const fetchAllSubmoduleAllocations = async () => {
      if (!modulesByProjectId || !selectedProjectId) return;
      const allocations: Record<string, { [submoduleId: string]: string[] }> =
        {};
      for (const module of modulesByProjectId) {
        allocations[module.id] = {};
        if (Array.isArray(module.submodules)) {
          for (const sub of module.submodules) {
            try {
              
              const projectId = Number(selectedProjectId);
              const moduleId = Number(module.id);
              const submoduleId = Number(sub.id);

              if (
                !projectId ||
                !moduleId ||
                !submoduleId ||
                isNaN(projectId) ||
                isNaN(moduleId) ||
                isNaN(submoduleId)
              ) {
                console.warn(
                  "Skipping getBulkSuboduleAllocation due to invalid IDs:",
                  {
                    projectId,
                    moduleId,
                    submoduleId,
                    selectedProjectId,
                    moduleObjectId: module.id, 
                    submoduleObjectId: sub.id, 
                  },
                );
                allocations[module.id][sub.id] = [];
                continue;
              }

              await fetchSubmoduleAllocatedDevs(submoduleId);

              const data = await getBulkSuboduleAllocation(
                projectId,
                moduleId,
                submoduleId,
              );
              console.log(
                "Submodule allocation data for:",
                { moduleId, submoduleId },
                data,
              );

              
              let userNames: string[] = [];
              if (Array.isArray(data)) {
                userNames = [
                  ...new Set(
                    data.map((d: any) => {
                      console.log("Processing user data:", d);
                      
                      const userName =
                        d.userName ||
                        d.userFullName ||
                        d.name ||
                        (d.firstName && d.lastName
                          ? `${d.firstName} ${d.lastName}`
                          : null) ||
                        d.employeeName ||
                        d.fullName ||
                        "Unknown User";
                      console.log("Extracted userName:", userName);
                      return userName;
                    }),
                  ),
                ];
              } else if (data && typeof data === "object") {
                
                console.log("Data is object, checking for user fields:", data);
                const userName =
                  data.userName ||
                  data.userFullName ||
                  data.name ||
                  (data.firstName && data.lastName
                    ? `${data.firstName} ${data.lastName}`
                    : null) ||
                  data.employeeName ||
                  data.fullName ||
                  "Unknown User";
                if (userName !== "Unknown User") {
                  userNames = [userName];
                }
              }
              console.log("Final extracted user names:", userNames);
              allocations[module.id][sub.id] = userNames;
            } catch (error) {
              console.error(
                "Error fetching submodule allocations for:",
                { moduleId: module.id, submoduleId: sub.id },
                error,
              );
              allocations[module.id][sub.id] = [];
            }
          }
        }
      }
      setSubmoduleAllocations(allocations);
    };
    fetchAllSubmoduleAllocations();
  }, [modulesByProjectId, selectedProjectId]);

  
  
  
  
  
  
  
  

  
  useEffect(() => {
    if (isBulkAssignmentModalOpen && selectedItems.length > 0) {
      const moduleItems = selectedItems.filter(
        (item) => item.type === "module",
      );
      const submoduleItems = selectedItems.filter(
        (item) => item.type === "submodule",
      );

      if (moduleItems.length > 0) {
        
        fetchAllSelectedModulesAllocatedUsers();
      } else if (submoduleItems.length > 0) {
        
        fetchAllSelectedSubmodulesAllocatedUsers();
        submoduleItems.forEach((item) => {
          if (item.submoduleId) {
            fetchSubmoduleAllocatedDevs(item.submoduleId);
          }
        });
      }
    }
  }, [selectedItems, isBulkAssignmentModalOpen]);

  
  useEffect(() => {
    console.log("Active submodule section changed:", activeSubmoduleSection);
    console.log(
      "Allocation selections:",
      selectedDeveloperProjectAllocationIds,
    );
    console.log(
      "Deallocation selections:",
      selectedDevelopersForDeallocationBulk,
    );
  }, [
    activeSubmoduleSection,
    selectedDeveloperProjectAllocationIds,
    selectedDevelopersForDeallocationBulk,
  ]);

  
  useEffect(() => {
    console.log("Checking if both sections are empty:", {
      allocationLength: selectedDeveloperProjectAllocationIds.length,
      deallocationLength: selectedDevelopersForDeallocationBulk.length,
      currentActiveSection: activeSubmoduleSection,
    });

    if (
      selectedDevelopersForDeallocationBulk.length === 0 &&
      selectedDeveloperProjectAllocationIds.length === 0
    ) {
      console.log(
        "Both sections empty, resetting activeSubmoduleSection to null",
      );
      setActiveSubmoduleSection(null);
    }
  }, [
    selectedDevelopersForDeallocationBulk,
    selectedDeveloperProjectAllocationIds,
  ]);

  
  useEffect(() => {
    if (
      isBulkAssignmentModalOpen &&
      developersWithRoles.length > 0 &&
      activeTab === "allocate"
    ) {
      if (onlySubmodulesSelected) {
        
        
        setSelectedDeveloperProjectAllocationIds([]);
      } else if (onlyModulesSelected) {
        
        
        setSelectedModuleDeveloperProjectAllocationId(null);
      }
    }
  }, [
    isBulkAssignmentModalOpen,
    developersWithRoles,
    activeTab,
    selectedItems,
    submoduleAllocations,
    moduleDevelopers,
    onlySubmodulesSelected,
    onlyModulesSelected,
  ]);

  
  useEffect(() => {
    if (isAddModuleModalOpen) {
      
      setTimeout(() => addModuleInputRef.current?.focus(), 0);
    }
  }, [isAddModuleModalOpen]);

  useEffect(() => {
    if (isEditModuleModalOpen) {
      setTimeout(() => editModuleInputRef.current?.focus(), 0);
    }
  }, [isEditModuleModalOpen]);

  useEffect(() => {
    if (isAddSubmoduleModalOpen) {
      setTimeout(() => submoduleInputRef.current?.focus(), 0);
    }
  }, [isAddSubmoduleModalOpen]);

  useEffect(() => {
    if (isBulkAssignmentModalOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }

    return () => {
      document.body.style.overflow = "unset";
    };
  }, [isBulkAssignmentModalOpen]);

  
  useEffect(() => {
    const moduleItems = selectedItems.filter((item) => item.type === "module");
    if (moduleItems.length > 0) {
      fetchAllSelectedModulesAllocatedUsers();
    } else {
      
      setModuleAllocatedUsers([]);
      setModuleAllocatedUsersError(null);
    }
  }, [selectedItems, selectedProjectId]);

  
  useEffect(() => {
    const moduleItem = selectedItems.find((item) => item.type === "module");
    if (activeTab === "reassign" && moduleItem && moduleItem.moduleId) {
      fetchModuleAllocatedUsersForReassignment(moduleItem.moduleId);
    } else {
      
      setModuleAllocatedUsersForReassignment([]);
      setModuleAllocatedUsersForReassignmentError(null);
      setModuleAllocatedUsersForReassignmentLoading(false);
    }
  }, [activeTab, selectedItems, selectedProjectId]);

  
  useEffect(() => {
    const submoduleItem = selectedItems.find(
      (item) => item.type === "submodule",
    );
    if (
      activeTab === "reassign" &&
      submoduleItem &&
      submoduleItem.moduleId &&
      submoduleItem.submoduleId
    ) {
      fetchSubmoduleAllocatedUsersForReassignment(
        submoduleItem.moduleId,
        submoduleItem.submoduleId,
      );
    } else {
      
      setSubmoduleAllocatedUsersForReassignment([]);
      setSubmoduleAllocatedUsersForReassignmentError(null);
      setSubmoduleAllocatedUsersForReassignmentLoading(false);
    }
  }, [activeTab, selectedItems, selectedProjectId]);

  
  useEffect(() => {
    const submoduleItems = selectedItems.filter(
      (item) => item.type === "submodule",
    );
    if (activeTab === "deallocate" && submoduleItems.length > 0) {
      fetchAllSelectedSubmodulesAllocatedUsers();
    } else {
      
      setSubmoduleAllocatedUsersForDeallocation([]);
      setSubmoduleAllocatedUsersForDeallocationError(null);
      setSubmoduleAllocatedUsersForDeallocationLoading(false);
    }
  }, [activeTab, selectedItems, selectedProjectId]);

  
  useEffect(() => {
    if (!selectedProjectId) return;

    const buttonsElement = document.getElementById("action-buttons");
    const sentinelElement = document.getElementById("sticky-sentinel");

    if (!buttonsElement || !sentinelElement) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          
          buttonsElement.style.position = "relative";
          buttonsElement.style.top = "auto";
          buttonsElement.style.left = "auto";
          buttonsElement.style.right = "auto";
          buttonsElement.style.zIndex = "auto";
          buttonsElement.style.width = "auto";
          buttonsElement.style.boxShadow = "none";

          
          const buttons = buttonsElement.querySelectorAll("button");
          buttons.forEach((button) => {
            button.style.backgroundColor = "";
            button.style.color = "";
          });
        } else {
          // Sentinel is not visible, make buttons fixed
          buttonsElement.style.position = "fixed";
          buttonsElement.style.top = "80px";
          buttonsElement.style.left = "320px"; 
          buttonsElement.style.right = "16px";
          buttonsElement.style.zIndex = "45";
          buttonsElement.style.width = "auto";
          buttonsElement.style.boxShadow =
            "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)";
        }
      },
      {
        root: null,
        threshold: 0,
        rootMargin: "-64px 0px 0px 0px", 
      },
    );

    observer.observe(sentinelElement);

    return () => {
      observer.disconnect();
    };
  }, [selectedProjectId]);

  return (
    <div
      className="max-w-6xl mx-auto"
      style={{ overflow: isBulkAssignmentModalOpen ? "hidden" : "auto" }}
    >
      <AlertModal
        isOpen={alertOpen}
        message={alertMessage}
        onClose={() => setAlertOpen(false)}
      />
      {}
      <Toast
        isOpen={!!showToast && !!toastMessage}
        message={toastMessage || ""}
        type={
          toastMessage &&
            /(fail|cannot|error|unable|invalid|not allowed|already|exists|violate|constraint|duplicate|no changes found)/i.test(
              toastMessage,
            )
            ? "error"
            : "success"
        }
        duration={5000}
        onClose={() => {
          setShowToast(false);
          setToastMessage(null);
        }}
      />
      {!selectedProjectId ? (
        <div className="p-8 text-center text-gray-500">
          Please select a project to manage modules.
        </div>
      ) : (
        <>
          {}
          <div className="flex-none p-6 pb-4">
            <div className="flex justify-between items-center mb-4">
              <div className="space-y-1">
                <h1 className="text-2xl font-bold text-gray-900">
                  Module Management
                </h1>
                <p className="text-sm text-gray-500">
                  {selectedProjectId && project
                    ? `Project: ${project.name || project.projectName}`
                    : "Select a project to begin"}
                </p>
              </div>
              {}
              <div className="flex-shrink-0">
                <Button
                  variant="secondary"
                  onClick={() =>
                    navigate(`/projects/${projectId}/project-management`)
                  }
                  className="flex items-center"
                >
                  <ChevronLeft className="w-5 h-5 mr-2" /> Back
                </Button>
              </div>
            </div>
            {}
            <ProjectSelector
              projects={projects.map((p) => ({
                ...p,
                name: p.name || p.projectName,
              }))}
              selectedProjectId={selectedProjectId}
              onSelect={handleProjectSelect}
            />
          </div>

          {}
          <div id="sticky-sentinel" style={{ height: "1px" }}></div>
          {}
          {}
          <div className="mt-4 relative w-full md:w-96 ml-6">

            <Search
              className="absolute left-3 top-3 text-gray-400"
              size={18}
            />

            <Input
              type="text"
              placeholder="Search module..."
              value={moduleSearch}
              onChange={(e) => setModuleSearch(e.target.value)}
              className="pl-10"
            />

          </div>
          <div
            id="action-buttons"
            className="bg-white border-b border-gray-200 shadow-sm mx-6 mb-6 mt-2 rounded-md"
          >
            <div className="max-w-6xl mx-auto px-6 py-4">
              <div className="flex justify-between items-center">
                <div className="flex space-x-6">
                  {can.module.create && (
                    <Button
                      onClick={() => setIsAddModuleModalOpen(true)}
                      className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700"
                    >
                      <PlusCircle className="w-4 h-4" />

                      <span>Add Module</span>
                    </Button>
                  )}
                  {}
                  {selectedItems.length > 0 && (
                    <Button
                      onClick={() => {
                        handleBulkAssignment();
                        setActiveTab("allocate");
                      }}
                      className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700"
                    >
                      <Users className="w-4 h-4" />
                      <span>
                        Manage Allocation ({selectedItems.length} selected)
                      </span>
                    </Button>
                  )}
                  {}
                </div>
                {selectedItems.length > 0 && (
                  <Button
                    variant="secondary"
                    onClick={() => setSelectedItems([])}
                    className="text-sm"
                  >
                    Clear Selection
                  </Button>
                )}
              </div>
            </div>
          </div>

          {}
          <div className="max-w-6xl mx-auto px-6 pb-6">
            {}
            {isLoading && (
              <div className="text-center py-8">
                <div className="text-gray-500">Loading modules...</div>
              </div>
            )}

            {}
            {!isLoading && (
              <>
                {console.log(
                  "Modules state:",
                  modulesByProjectId,
                  "Length:",
                  modulesByProjectId?.length,
                )}
                {modulesByProjectId &&
                  Array.isArray(modulesByProjectId) &&
                  modulesByProjectId.length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 items-stretch">
                    {(filteredModules || []).map((module) => {
                      

                      const moduleDevs = (moduleDevelopers[module.id] && moduleDevelopers[module.id].length > 0)
                        ? moduleDevelopers[module.id]
                        : (module.assignedDev
                          ? [{ userName: module.assignedDev.userName, userId: module.assignedDev.userId }]
                          : []);
                      console.log(
                        "Module developers for module",
                        module.id,
                        ":",
                        moduleDevs,
                      );
                      return (
                        <Card
                          key={module.id}
                          className={`hover:shadow-lg transition-shadow flex flex-col h-full group ${isItemSelected("module", module.id.toString()) ? "ring-2 ring-blue-500 bg-blue-50" : ""}`}
                        >
                          <CardContent className="p-6 flex flex-col h-full">
                            <div className="flex justify-between items-start mb-4">
                              <div className="flex items-center space-x-3">
                                <input
                                  type="checkbox"
                                  checked={isItemSelected(
                                    "module",
                                    module.id.toString(),
                                  )}
                                  onChange={(e) =>
                                    handleSelectItem(
                                      "module",
                                      module.id.toString(),
                                      e.target.checked,
                                    )
                                  }
                                  disabled={isItemDisabled(
                                    "module",
                                    module.id.toString(),
                                  )}
                                  className={`rounded border-gray-300 text-blue-600 focus:ring-blue-500 transition-opacity duration-200 ${isItemDisabled(
                                    "module",
                                    module.id.toString(),
                                  )
                                    ? "opacity-30 cursor-not-allowed"
                                    : isItemSelected(
                                      "module",
                                      module.id.toString(),
                                    )
                                      ? "opacity-100 cursor-pointer"
                                      : "opacity-0 group-hover:opacity-100 cursor-pointer"
                                    }`}
                                />

                                <div>
                                  <div className="flex items-center">
                                    <h3 className="text-lg font-semibold text-gray-900">
                                      {module.name}
                                    </h3>
                                    {isItemSelected(
                                      "module",
                                      module.id.toString(),
                                    ) && (
                                        <div className="ml-2 w-2 h-2 bg-blue-500 rounded-full"></div>
                                      )}
                                  </div>
                                  {}
                                  {moduleDevelopersLoading[module.id] ? (
                                    <div className="mt-2 p-2 bg-gray-100 rounded-md border border-gray-200">
                                      <span className="text-xs text-gray-500">
                                        Loading...
                                      </span>
                                    </div>
                                  ) : moduleDevelopersError[module.id] ? (
                                    <div className="mt-2 p-2 bg-red-50 rounded-md border border-red-200">
                                      <span className="text-xs text-red-500">
                                        Error:{" "}
                                        {moduleDevelopersError[module.id]}
                                      </span>
                                    </div>
                                  ) : moduleDevs.length > 0 ? (
                                    <div className="mt-2">
                                      <div className="flex flex-wrap gap-2">
                                        {moduleDevs.map((d, index) => {
                                          
                                          const userName =
                                            d.userName ||
                                            d.userFullName ||
                                            d.name ||
                                            (d.firstName && d.lastName
                                              ? `${d.firstName} ${d.lastName}`
                                              : "Unknown User");
                                          return (
                                            <div
                                              key={index}
                                              className="px-3 py-1 bg-blue-100 rounded-md border border-blue-300 text-xs text-blue-700 font-medium flex items-center gap-1"
                                            >
                                              <Users className="w-3 h-3" />
                                              {userName}
                                            </div>
                                          );
                                        })}
                                      </div>
                                    </div>
                                  ) : (
                                    <div className="mt-2 p-2 bg-gray-50 rounded-md border border-gray-200">
                                      <div className="flex items-center justify-between">
                                        <span className="text-xs text-gray-500 flex-1">
                                          No module leader assigned
                                        </span>
                                        <button
                                          type="button"
                                          className={`text-xs font-medium ml-2 whitespace-nowrap ${isItemDisabled(
                                            "module",
                                            module.id.toString(),
                                          )
                                            ? "text-gray-400 cursor-not-allowed"
                                            : "text-blue-600 hover:text-blue-800"
                                            }`}
                                          onClick={() => {
                                            if (
                                              !isItemDisabled(
                                                "module",
                                                module.id.toString(),
                                              )
                                            ) {
                                              setSelectedItems([
                                                {
                                                  type: "module",
                                                  moduleId:
                                                    module.id.toString(),
                                                },
                                              ]);
                                              handleBulkAssignment();
                                              setActiveTab("allocate");
                                            }
                                          }}
                                          disabled={isItemDisabled(
                                            "module",
                                            module.id.toString(),
                                          )}
                                        >
                                          Assign Leader
                                        </button>
                                      </div>
                                    </div>
                                  )}
                                </div>
                              </div>
                              <div className="flex space-x-2">
                                {can.module.edit && (
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => handleEditModule(module)}
                                    className="p-1"
                                    title="Edit Module"
                                  >
                                    <Edit2 className="w-4 h-4" />
                                  </Button>
                                )}
                                {can.module.delete && (
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => {
                                      setPendingDeleteModuleId(
                                        module.id.toString(),
                                      );
                                      setConfirmModuleOpen(true);
                                    }}
                                    className="p-1 text-red-600 hover:text-red-800"
                                    title="Delete Module"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </Button>
                                )}
                              </div>
                            </div>
                            {}
                            {can.subModule.view &&
                              <div className="flex-1">
                                <h4 className="text-sm font-medium text-gray-700 mb-2">
                                  Submodules
                                </h4>
                                {Array.isArray(module.submodules) &&
                                  module.submodules.length > 0 ? (
                                  <ul className="list-inside space-y-1">
                                    {module.submodules.map((sub: any) => {
                                      const submoduleName =
                                        sub.getSubModuleName ||
                                        sub.subModuleName ||
                                        sub.name ||
                                        sub.submoduleName ||
                                        sub.subModule ||
                                        "Unknown";
                                      
                                      const allocatedSubmoduleDevs =
                                        submoduleAllocatedDevs[
                                        sub.id.toString()
                                        ] || [];
                                      const submoduleUserNames =
                                        allocatedSubmoduleDevs.length > 0
                                          ? allocatedSubmoduleDevs.map(
                                            (allocation) => {
                                              if (allocation.employeeName) {
                                                return allocation.employeeName;
                                              }
                                              const developer =
                                                developersWithRoles.find(
                                                  (dev) =>
                                                    String(dev.userId) ===
                                                    String(
                                                      allocation.employeeId,
                                                    ),
                                                );
                                              const employee = employees.find(
                                                (emp) =>
                                                  String(emp.id) ===
                                                  String(allocation.employeeId),
                                              );
                                              return (
                                                developer?.userWithRole
                                                  ?.split("-")[0]
                                                  ?.trim() ||
                                                (employee
                                                  ? `${employee.firstName} ${employee.lastName}`
                                                  : null) ||
                                                `Employee ${allocation.employeeId}`
                                              );
                                            },
                                          )
                                          : (submoduleAllocations[module.id] &&
                                            submoduleAllocations[module.id][
                                            sub.id
                                            ]) ||
                                          [];
                                      console.log(
                                        "Submodule user names for",
                                        sub.id,
                                        ":",
                                        submoduleUserNames,
                                      );
                                      return (
                                        <li
                                          key={sub.id}
                                          className={`text-gray-800 text-sm group ${isItemSelected("submodule", module.id.toString(), sub.id.toString()) ? "bg-blue-50 rounded px-2 py-1" : ""}`}
                                        >
                                          <div className="flex items-center justify-between mb-2">
                                            <div className="flex items-center">
                                              <input
                                                type="checkbox"
                                                checked={isItemSelected(
                                                  "submodule",
                                                  module.id.toString(),
                                                  sub.id.toString(),
                                                )}
                                                onChange={(e) =>
                                                  handleSelectItem(
                                                    "submodule",
                                                    module.id.toString(),
                                                    e.target.checked,
                                                    sub.id.toString(),
                                                  )
                                                }
                                                disabled={isItemDisabled(
                                                  "submodule",
                                                  module.id.toString(),
                                                  sub.id.toString(),
                                                )}
                                                className={`rounded border-gray-300 text-blue-600 focus:ring-blue-500 mr-2 transition-opacity duration-200 ${isItemDisabled(
                                                  "submodule",
                                                  module.id.toString(),
                                                  sub.id.toString(),
                                                )
                                                  ? "opacity-30 cursor-not-allowed"
                                                  : isItemSelected(
                                                    "submodule",
                                                    module.id.toString(),
                                                    sub.id.toString(),
                                                  )
                                                    ? "opacity-100 cursor-pointer"
                                                    : "opacity-0 group-hover:opacity-100 cursor-pointer"
                                                  }`}
                                              />

                                              <span className="font-medium">
                                                {submoduleName}
                                              </span>
                                              {isItemSelected(
                                                "submodule",
                                                module.id.toString(),
                                                sub.id.toString(),
                                              ) && (
                                                  <div className="ml-2 w-2 h-2 bg-blue-500 rounded-full"></div>
                                                )}
                                            </div>
                                            <span className="flex items-center space-x-2 opacity-80 group-hover:opacity-100">
                                              {can.subModule.edit &&
                                                <button
                                                  type="button"
                                                  className="p-1 hover:text-blue-600"
                                                  title="Edit Submodule"
                                                  onClick={() => {
                                                    setCurrentModuleIdForSubmodule(
                                                      module.id.toString(),
                                                    );
                                                    setIsAddSubmoduleModalOpen(
                                                      true,
                                                    );
                                                    setSubmoduleForm({
                                                      name: submoduleName,
                                                    });
                                                    setIsEditingSubmodule(true);
                                                    setEditingSubmoduleId(
                                                      sub.id.toString(),
                                                    );
                                                  }}
                                                >
                                                  <Edit2 className="w-4 h-4" />
                                                </button>}
                                              {can.subModule.delete &&
                                                <button
                                                  type="button"
                                                  className="p-1 hover:text-red-600"
                                                  title="Delete Submodule"
                                                  onClick={() =>
                                                    handleDeleteSubmoduleClick(
                                                      module.id.toString(),
                                                      sub.id.toString(),
                                                    )
                                                  }
                                                >
                                                  <Trash2 className="w-4 h-4" />
                                                </button>
                                              }
                                            </span>
                                          </div>
                                          {}
                                          {submoduleUserNames.length > 0 ? (
                                            <div className="ml-8 mt-2">
                                              <div className="flex flex-wrap gap-2">
                                                {submoduleUserNames.map(
                                                  (name, index) => {
                                                    
                                                    const userName =
                                                      name || "Unknown User";
                                                    return (
                                                      <div
                                                        key={index}
                                                        className="px-3 py-1 bg-blue-100 rounded-md border border-blue-300 text-xs text-blue-700 font-medium flex items-center gap-1"
                                                      >
                                                        <Users className="w-3 h-3" />
                                                        {userName}
                                                      </div>
                                                    );
                                                  },
                                                )}
                                              </div>
                                            </div>
                                          ) : (
                                            <div className="ml-8 p-2 bg-gray-50 rounded-md border border-gray-200">
                                              <div className="flex items-center justify-between">
                                                <span className="text-xs text-gray-500 flex-1">
                                                  No developers assigned
                                                </span>
                                                <button
                                                  type="button"
                                                  className={`text-xs font-medium ml-2 whitespace-nowrap ${isItemDisabled(
                                                    "submodule",
                                                    module.id.toString(),
                                                    sub.id.toString(),
                                                  )
                                                    ? "text-gray-400 cursor-not-allowed"
                                                    : "text-blue-600 hover:text-blue-800"
                                                    }`}
                                                  onClick={() => {
                                                    if (
                                                      !isItemDisabled(
                                                        "submodule",
                                                        module.id.toString(),
                                                        sub.id.toString(),
                                                      )
                                                    ) {
                                                      setSelectedItems([
                                                        {
                                                          type: "submodule",
                                                          moduleId:
                                                            module.id.toString(),
                                                          submoduleId:
                                                            sub.id.toString(),
                                                        },
                                                      ]);
                                                      handleBulkAssignment();
                                                      setActiveTab("allocate");
                                                    }
                                                  }}
                                                  disabled={isItemDisabled(
                                                    "submodule",
                                                    module.id.toString(),
                                                    sub.id.toString(),
                                                  )}
                                                >
                                                  Assign Developer
                                                </button>
                                              </div>
                                            </div>
                                          )}
                                        </li>
                                      );
                                    })}
                                  </ul>
                                ) : (
                                  <div className="italic text-gray-400 text-sm">
                                    No Submodules
                                  </div>
                                )}
                              </div>}

                            <div className="flex justify-end mt-4">
                              {can.subModule.create &&
                                <Button
                                  size="sm"
                                  variant="primary"
                                  onClick={() => {
                                    setCurrentModuleIdForSubmodule(
                                      module.id.toString(),
                                    );
                                    setIsAddSubmoduleModalOpen(true);
                                    setSubmoduleForm({ name: "" });
                                    setIsEditingSubmodule(false);
                                    setEditingSubmoduleId(null);
                                  }}
                                >
                                  <PlusCircle className="w-4 h-4 mr-1" /> Add
                                  Submodule
                                </Button>}
                            </div>
                          </CardContent>
                        </Card>
                      );
                    })}
                  </div>

                ) : (
                  <Card>

                    <CardContent className="p-12 text-center">

                      <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Edit2 className="w-8 h-8 text-gray-400" />

                      </div>

                      <h3 className="text-lg font-medium text-gray-900 mb-2">
                        No modules found
                      </h3>
                      <p className="text-gray-500 mb-4">
                        This project doesn't have any modules yet. Create your
                        first module to get started.
                      </p>


                      {can.module.create && (

                        <Button
                          onClick={() => setIsAddModuleModalOpen(true)}
                          icon={PlusCircle}
                        >
                          Add Module
                        </Button>

                      )}


                    </CardContent>

                  </Card>

                )}
              </>
            )}

          </div>
        </>
      )}


      {}
      <Modal
        isOpen={isAddModuleModalOpen}
        onClose={() => setIsAddModuleModalOpen(false)}
        title="Add New Module"
        size="xl"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Name
            </label>
            <Input
              ref={addModuleInputRef}
              value={moduleForm.name}
              onChange={(e) =>
                setModuleForm((prev) => ({ ...prev, name: e.target.value }))
              }
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleAddModule();
                }
              }}
              placeholder="Enter module name"
              disabled={isCreatingModule}
            />
            {moduleForm.name.trim() &&
              modulesByProjectId?.find(
                (module) =>
                  module.name.toLowerCase() ===
                  moduleForm.name.trim().toLowerCase(),
              ) && (
                <p className="text-sm text-red-600 mt-1">
                  A module with this name already exists in the project.
                </p>
              )}
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsAddModuleModalOpen(false)}
              disabled={isCreatingModule}
            >
              Cancel
            </Button>
            <Button
              onClick={handleAddModule}
              disabled={isCreatingModule || !moduleForm.name.trim()}
            >
              {isCreatingModule ? "Creating..." : "Add Module"}
            </Button>
          </div>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={isEditModuleModalOpen}
        onClose={() => {
          setIsEditModuleModalOpen(false);
          setEditingModule(null);
          setModuleForm({ name: "" });
        }}
        title="Edit Module"
        size="xl"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Name
            </label>
            <Input
              ref={editModuleInputRef}
              value={moduleForm.name}
              onChange={(e) =>
                setModuleForm((prev) => ({ ...prev, name: e.target.value }))
              }
              onKeyDown={(e) => {
                if (
                  e.key === "Enter" &&
                  moduleForm.name.trim() &&
                  !isUpdatingModule
                ) {
                  e.preventDefault();
                  handleUpdateModule();
                }
              }}
              placeholder="Enter module name"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => {
                setIsEditModuleModalOpen(false);
                setEditingModule(null);
                setModuleForm({ name: "" });
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleUpdateModule}
              disabled={isUpdatingModule || !moduleForm.name.trim()}
            >
              {isUpdatingModule ? "Updating..." : "Update Module"}
            </Button>
          </div>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={isBulkAssignmentModalOpen}
        onClose={() => {
          setIsBulkAssignmentModalOpen(false);
          setSelectedItems([]);
          setSelectedDeveloperProjectAllocationId(null);
          setSelectedModuleDeveloperProjectAllocationId(null);
          setSelectedDeveloperProjectAllocationIds([]);
          setSelectedDevelopersForDeallocation(null);
          setSelectedDevelopersForDeallocationBulk([]);
          setSelectedModuleDevelopersForDeallocationBulk([]);
          setSelectedDevelopersForReassignment({
            oldDeveloperId: null,
            newDeveloperId: null,
          });
          setActiveSubmoduleSection(null);
          setActiveTab("allocate");
        }}
        title={`Developer Management`}
        size="full"
      >
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 max-h-[95vh] overflow-y-auto">
          {}
          <div className="lg:col-span-1">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Selected Modules and Submodules
            </label>
            <div className="max-h-[50vh] overflow-y-auto p-3 bg-gray-50 rounded text-sm mb-4">
              {}
              {(() => {
                const moduleMap: Record<
                  string,
                  { module: any; selected: boolean; submodules: Set<string> }
                > = {};
                selectedItems.forEach((item) => {
                  if (!moduleMap[item.moduleId]) {
                    const module = modulesByProjectId?.find(
                      (m) => m.id.toString() === item.moduleId,
                    );
                    moduleMap[item.moduleId] = {
                      module,
                      selected: false,
                      submodules: new Set<string>(),
                    };
                  }
                  if (item.type === "module") {
                    moduleMap[item.moduleId].selected = true;
                  } else if (item.type === "submodule" && item.submoduleId) {
                    moduleMap[item.moduleId].submodules.add(item.submoduleId);
                  }
                });
                return Object.values(moduleMap).map(
                  ({ module, selected, submodules }, idx) => {
                    if (!module) return null;
                    const allSubmodules = module.submodules || [];
                    return (
                      <div
                        key={module.id}
                        className="mb-3 border border-gray-200 rounded-lg p-3 bg-white"
                      >
                        {}
                        <div className="flex items-center gap-2 mb-2">
                          <div
                            className={`w-3 h-3 rounded-full flex-shrink-0 ${selected ? "bg-blue-500" : "bg-gray-300"}`}
                          ></div>
                          <div className="font-semibold text-gray-900 flex items-center gap-2">
                            <span className="text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full font-medium">
                              MODULE
                            </span>
                            {module.name}
                          </div>
                          {selected && (
                            <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full font-medium ml-auto">
                              SELECTED
                            </span>
                          )}
                        </div>

                        {}
                        {(selected || submodules.size > 0) && (
                          <div className="ml-5 border-l-2 border-gray-200 pl-3">
                            <div className="text-xs text-gray-600 mb-2 font-medium">
                              Submodules:
                            </div>
                            <ul className="space-y-1">
                              {selected
                                ? allSubmodules.map((sub: any) => {
                                  const submoduleName =
                                    sub.getSubModuleName ||
                                    sub.subModuleName ||
                                    sub.name ||
                                    sub.submoduleName ||
                                    sub.subModule ||
                                    "Unknown";
                                  return (
                                    <li
                                      key={sub.id}
                                      className="flex items-center gap-2 text-sm"
                                    >
                                      <div className="w-2 h-2 rounded-full bg-blue-300 flex-shrink-0"></div>
                                      <span className="text-xs bg-purple-100 text-purple-800 px-2 py-0.5 rounded-full font-medium">
                                        SUB
                                      </span>
                                      <span className="text-gray-700">
                                        {submoduleName}
                                      </span>
                                      <span className="text-xs bg-green-100 text-green-800 px-2 py-0.5 rounded-full font-medium ml-auto">
                                        SELECTED
                                      </span>
                                    </li>
                                  );
                                })
                                : Array.from(submodules).map((subId) => {
                                  const sub = allSubmodules.find(
                                    (s: any) => s.id.toString() === subId,
                                  );
                                  if (!sub) return null;
                                  const submoduleName =
                                    sub.getSubModuleName ||
                                    sub.subModuleName ||
                                    sub.name ||
                                    sub.submoduleName ||
                                    sub.subModule ||
                                    "Unknown";
                                  return (
                                    <li
                                      key={sub.id}
                                      className="flex items-center gap-2 text-sm"
                                    >
                                      <div className="w-2 h-2 rounded-full bg-purple-500 flex-shrink-0"></div>
                                      <span className="text-xs bg-purple-100 text-purple-800 px-2 py-0.5 rounded-full font-medium">
                                        SUB
                                      </span>
                                      <span className="text-gray-700">
                                        {submoduleName}
                                      </span>
                                      <span className="text-xs bg-green-100 text-green-800 px-2 py-0.5 rounded-full font-medium ml-auto">
                                        SELECTED
                                      </span>
                                    </li>
                                  );
                                })}
                            </ul>
                          </div>
                        )}
                      </div>
                    );
                  },
                );
              })()}
            </div>

            {}
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-md">
              <p className="text-sm text-blue-800">
                <strong>Available Actions:</strong>
                <br />• <strong>Allocate:</strong>{" "}
                {isModuleAlreadyAllocated()
                  ? "Not available - already allocated"
                  : `${selectedDeveloperProjectAllocationIds.length + (selectedModuleDeveloperProjectAllocationId ? 1 : 0)} developer(s) selected for allocation`}
                <br />• <strong>Deallocate:</strong>{" "}
                {(selectedModuleDevelopersForDeallocationBulk.length +
                  selectedDevelopersForDeallocationBulk.length)}{" "}
                developer(s) selected for deallocation
              </p>
            </div>
          </div>

          {}
          <div className="lg:col-span-1">
            {onlyModulesSelected && (
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Select QA Lead / QA Engineer for Module Allocation
                </label>
                <div className="max-h-60 overflow-y-auto space-y-2">
                  {getRoleTypedModuleLeaders().length > 0 ? (
                    getRoleTypedModuleLeaders()
                      .filter(dev => {
                        const name = dev.employeeName || (dev.userWithRole.includes(" - ") ? dev.userWithRole.split(" - ")[0] : dev.userWithRole.split("-")[0]);
                        const trimmedName = name.trim();
                        return !selectedItems.some((item) => {
                          if (item.type === "module") {
                            const moduleAllocatedUserNames =
                              moduleAllocatedUsers.map((user) => {
                                const allocatedName = user.userName || user.userWithRole.split("-")[0];
                                return allocatedName.trim();
                              });
                            return moduleAllocatedUserNames.includes(
                              trimmedName,
                            );
                          }
                          return false;
                        });
                      })
                      .map((dev, idx) => {
                        const name = dev.employeeName || (dev.userWithRole.includes(" - ") ? dev.userWithRole.split(" - ")[0] : dev.userWithRole.split("-")[0]);
                        const role = dev.roleName || (dev.userWithRole.includes(" - ") ? dev.userWithRole.split(" - ")[1] : dev.userWithRole.split("-")[1]);
                        return (
                          <div
                            key={idx}
                            className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded cursor-pointer"
                            onClick={() =>
                              setSelectedModuleDeveloperProjectAllocationId(
                                dev.projectAllocationId,
                              )
                            }
                          >
                            <input
                              type="radio"
                              name="module-assign-developer"
                              checked={
                                selectedModuleDeveloperProjectAllocationId ===
                                dev.projectAllocationId
                              }
                              onChange={() =>
                                setSelectedModuleDeveloperProjectAllocationId(
                                  dev.projectAllocationId,
                                )
                              }
                              disabled={isModuleAlreadyAllocated()}
                              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
                            />
                            <div className="flex-1">
                              <div className="text-sm font-semibold text-gray-900">
                                {name.trim()}
                              </div>
                              {role && (
                                <div className="text-xs text-gray-500">
                                  {role.trim()}
                                </div>
                              )}
                            </div>
                          </div>
                        );
                      })
                  ) : (
                    <div className="text-gray-400 text-sm">
                      No QA Lead or QA Engineer found allocated to this project.
                    </div>
                  )}
                </div>
                {isModuleAlreadyAllocated() && (
                  <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-md">
                    <div className="text-sm text-blue-800">
                      <strong>Module Already Allocated:</strong> The selected
                      module(s) already have module leaders assigned. Use the
                      deallocate option to remove existing assignments before
                      allocating new ones.
                    </div>
                  </div>
                )}
              </div>
            )}
            {onlySubmodulesSelected && (
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Select Developers for Submodule Allocation
                </label>

                {}
                {(() => {
                  const availableDevelopers = getRoleTypedDevelopers().filter(dev => {
                    return !selectedSubmoduleAllocatedDevelopers.some(
                      (allocatedDev) => allocatedDev.userId === dev.userId
                    );
                  });

                  return availableDevelopers.length > 0 ? (
                    <div className="mb-3 p-2 bg-gray-50 rounded border">
                      <label className="flex items-center space-x-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={
                            selectedDeveloperProjectAllocationIds.length ===
                            availableDevelopers.length
                          }
                          onChange={(e) => {
                            if (e.target.checked) {
                              
                              setSelectedDeveloperProjectAllocationIds(
                                availableDevelopers.map(
                                  (dev) => dev.projectAllocationId,
                                ),
                              );
                              setActiveSubmoduleSection("allocate");
                              setSelectedDevelopersForDeallocationBulk([]);
                            } else {
                              
                              setSelectedDeveloperProjectAllocationIds([]);
                              setActiveSubmoduleSection(null);
                            }
                          }}
                          className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span className="text-sm font-medium text-gray-700">
                          Select All Available Developers
                        </span>
                      </label>
                    </div>
                  ) : null;
                })()}

                <div className={`max-h-60 overflow-y-auto space-y-2 ${activeSubmoduleSection === 'deallocate' && selectedDevelopersForDeallocationBulk.length > 0 ? 'opacity-50 pointer-events-none' : ''
                  }`}>
                  {getRoleTypedDevelopers().length > 0 ? (
                    getRoleTypedDevelopers()
                      .filter(dev => {
                        
                        return !selectedSubmoduleAllocatedDevelopers.some(
                          (allocatedDev) => allocatedDev.userId === dev.userId,
                        );
                      })
                      .map((dev, idx) => {
                        const name = dev.employeeName || (dev.userWithRole.includes(" - ") ? dev.userWithRole.split(" - ")[0] : dev.userWithRole.split("-")[0]);
                        const role = dev.roleName || (dev.userWithRole.includes(" - ") ? dev.userWithRole.split(" - ")[1] : dev.userWithRole.split("-")[1]);
                        return (
                          <div
                            key={idx}
                            className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded cursor-pointer"
                            onClick={() => {
                              const isCurrentlySelected =
                                selectedDeveloperProjectAllocationIds.includes(
                                  dev.projectAllocationId,
                                );
                              const newSelection = isCurrentlySelected
                                ? selectedDeveloperProjectAllocationIds.filter(
                                  (id) => id !== dev.projectAllocationId,
                                )
                                : [
                                  ...selectedDeveloperProjectAllocationIds,
                                  dev.projectAllocationId,
                                ];

                              setSelectedDeveloperProjectAllocationIds(
                                newSelection,
                              );
                              if (newSelection.length > 0) {
                                setActiveSubmoduleSection("allocate");
                                setSelectedDevelopersForDeallocationBulk([]);
                              } else {
                                setActiveSubmoduleSection(null);
                                setSelectedDevelopersForDeallocationBulk([]);
                              }
                            }}
                          >
                            <input
                              type="checkbox"
                              name="submodule-assign-developer"
                              checked={selectedDeveloperProjectAllocationIds.includes(
                                dev.projectAllocationId,
                              )}
                              onChange={() => {
                                const isCurrentlySelected =
                                  selectedDeveloperProjectAllocationIds.includes(
                                    dev.projectAllocationId,
                                  );
                                const newSelection = isCurrentlySelected
                                  ? selectedDeveloperProjectAllocationIds.filter(
                                    (id) => id !== dev.projectAllocationId,
                                  )
                                  : [
                                    ...selectedDeveloperProjectAllocationIds,
                                    dev.projectAllocationId,
                                  ];

                                console.log("Allocation checkbox changed:", {
                                  devName: dev.userWithRole.split("-")[0],
                                  isCurrentlySelected,
                                  newSelection,
                                  newSelectionLength: newSelection.length,
                                  currentActiveSection: activeSubmoduleSection,
                                });

                                setSelectedDeveloperProjectAllocationIds(
                                  newSelection,
                                );

                                
                                if (newSelection.length > 0) {
                                  setActiveSubmoduleSection("allocate");
                                  setSelectedDevelopersForDeallocationBulk([]);
                                } else {
                                  
                                  setActiveSubmoduleSection(null);
                                  setSelectedDevelopersForDeallocationBulk([]);
                                }
                              }}
                              disabled={
                                activeSubmoduleSection === "deallocate" &&
                                selectedDevelopersForDeallocationBulk.length > 0
                              }
                              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500 disabled:opacity-50"
                            />
                            <div className="flex-1">
                              <div className="text-sm font-semibold text-gray-900">
                                {name.trim()}
                              </div>
                              {role && (
                                <div className="text-xs text-gray-500">
                                  {role.trim()}
                                </div>
                              )}
                            </div>
                          </div>
                        );
                      })
                  ) : (
                    <div className="text-gray-400 text-sm p-2">
                      No Developers found allocated to this project.
                    </div>
                  )}
                </div>
              </div>
            )}
            {mixedSelection && (
              <div className="mb-4 text-red-600 font-medium">
                Please select either modules or submodules, not both, for
                allocation.
              </div>
            )}
          </div>

          {}
          <div className="lg:col-span-1">
            {}
            {selectedItems.some((item) => item.type === "module") && (
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  {selectedItems.filter((item) => item.type === "module")
                    .length > 1
                    ? "Common Developers Across Selected Modules"
                    : "Allocated Developers for Selected Module"}
                </label>

                {}
                {moduleAllocatedUsers.length > 0 ? (
                  <div className="mb-3 p-2 bg-gray-50 rounded border">
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={
                          selectedModuleDevelopersForDeallocationBulk.length ===
                          moduleAllocatedUsers.length
                        }
                        onChange={(e) => {
                          if (e.target.checked) {
                            
                            setSelectedModuleDevelopersForDeallocationBulk(
                              moduleAllocatedUsers.map((dev) => dev.userId),
                            );
                          } else {
                            
                            setSelectedModuleDevelopersForDeallocationBulk([]);
                          }
                        }}
                        className="rounded border-gray-300 text-red-600 focus:ring-red-500"
                      />
                      <span className="text-sm font-medium text-gray-700">
                        {selectedItems.filter((item) => item.type === "module")
                          .length > 1
                          ? "Select All Common Developers"
                          : "Select All Allocated Developers"}
                      </span>
                    </label>
                  </div>
                ) : null}

                <div className="max-h-60 overflow-y-auto space-y-2">
                  {moduleAllocatedUsersLoading ? (
                    <div className="text-sm text-gray-500 p-2">
                      Loading allocated users...
                    </div>
                  ) : moduleAllocatedUsersError ? (
                    <div className="text-sm text-red-500 p-2">
                      {moduleAllocatedUsersError}
                    </div>
                  ) : moduleAllocatedUsers.length > 0 ? (
                    moduleAllocatedUsers.map((user, idx) => {
                      const [name, role] = user.userWithRole.split("-");
                      return (
                        <div
                          key={idx}
                          className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded"
                        >
                          <input
                            type="checkbox"
                            name="deallocate-module-developer"
                            checked={selectedModuleDevelopersForDeallocationBulk.includes(
                              user.userId,
                            )}
                            onChange={() => {
                              const isCurrentlySelected =
                                selectedModuleDevelopersForDeallocationBulk.includes(
                                  user.userId,
                                );
                              const newSelection = isCurrentlySelected
                                ? selectedModuleDevelopersForDeallocationBulk.filter(
                                  (id) => id !== user.userId,
                                )
                                : [
                                  ...selectedModuleDevelopersForDeallocationBulk,
                                  user.userId,
                                ];

                              setSelectedModuleDevelopersForDeallocationBulk(
                                newSelection,
                              );
                            }}
                            className="rounded border-gray-300 text-red-600 focus:ring-red-500"
                          />
                          <div className="flex-1">
                            <div className="text-sm font-semibold text-gray-900">
                              {name.trim()}
                            </div>
                            {role &&
                              role.trim().toLowerCase() !== "developer" && (
                                <div className="text-xs text-gray-500">
                                  {role.trim()}
                                </div>
                              )}
                          </div>
                        </div>
                      );
                    })
                  ) : (
                    <div className="text-gray-400 text-sm">
                      {selectedItems.filter((item) => item.type === "module")
                        .length > 1
                        ? "No common developers found across selected modules."
                        : "No developers allocated to this module."}
                    </div>
                  )}
                </div>
              </div>
            )}

            {}
            {selectedItems.some((item) => item.type === "submodule") && (
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  {selectedItems.filter((item) => item.type === "submodule")
                    .length > 1
                    ? "Common Developers Across Selected Submodules"
                    : "Allocated Developers for Selected Submodule"}
                </label>

                {}
                {selectedSubmoduleAllocatedDevelopers.length > 0 ? (
                  <div className="mb-3 p-2 bg-gray-50 rounded border">
                    <label className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={
                          selectedDevelopersForDeallocationBulk.length ===
                          selectedSubmoduleAllocatedDevelopers.length
                        }
                        onChange={(e) => {
                          if (e.target.checked) {
                            
                            setSelectedDevelopersForDeallocationBulk(
                              selectedSubmoduleAllocatedDevelopers.map(
                                (dev) => dev.userId,
                              ),
                            );
                            setActiveSubmoduleSection("deallocate");
                            setSelectedDeveloperProjectAllocationIds([]);
                          } else {
                            
                            setSelectedDevelopersForDeallocationBulk([]);
                            setActiveSubmoduleSection(null);
                          }
                        }}
                        className="rounded border-gray-300 text-red-600 focus:ring-red-500"
                      />
                      <span className="text-sm font-medium text-gray-700">
                        {selectedItems.filter(
                          (item) => item.type === "submodule",
                        ).length > 1
                          ? "Select All Common Developers"
                          : "Select All Allocated Developers"}
                      </span>
                    </label>
                  </div>
                ) : null}

                <div
                  className={`max-h-60 overflow-y-auto space-y-2 ${activeSubmoduleSection === "allocate" &&
                    selectedDeveloperProjectAllocationIds.length > 0
                    ? "opacity-50 pointer-events-none"
                    : ""
                    }`}
                >
                  {selectedItems.some((item) => item.type === "submodule") ? (
                    submoduleAllocatedUsersForDeallocationLoading ? (
                      <div className="text-sm text-gray-500 p-2">
                        Loading allocated developers...
                      </div>
                    ) : submoduleAllocatedUsersForDeallocationError ? (
                      <div className="text-sm text-red-500 p-2">
                        <div>
                          Error loading developers:{" "}
                          {submoduleAllocatedUsersForDeallocationError}
                        </div>
                        <div className="text-xs mt-1">
                          Check console for details.
                        </div>
                      </div>
                    ) : selectedSubmoduleAllocatedDevelopers.length > 0 ? (
                      selectedSubmoduleAllocatedDevelopers.map((dev, idx) => {
                        const [name, role] = dev.userWithRole.split("-");
                        return (
                          <div
                            key={idx}
                            className="flex items-center space-x-3 p-2 hover:bg-gray-50 rounded"
                          >
                            <input
                              type="checkbox"
                              name="deallocate-submodule-developer"
                              checked={selectedDevelopersForDeallocationBulk.includes(
                                dev.userId,
                              )}
                              onChange={() => {
                                const isCurrentlySelected =
                                  selectedDevelopersForDeallocationBulk.includes(
                                    dev.userId,
                                  );
                                const newSelection = isCurrentlySelected
                                  ? selectedDevelopersForDeallocationBulk.filter(
                                    (id) => id !== dev.userId,
                                  )
                                  : [
                                    ...selectedDevelopersForDeallocationBulk,
                                    dev.userId,
                                  ];

                                console.log("Deallocation checkbox changed:", {
                                  devName: dev.userWithRole.split("-")[0],
                                  isCurrentlySelected,
                                  newSelection,
                                  newSelectionLength: newSelection.length,
                                  currentActiveSection: activeSubmoduleSection,
                                });

                                setSelectedDevelopersForDeallocationBulk(
                                  newSelection,
                                );

                                
                                if (newSelection.length > 0) {
                                  setActiveSubmoduleSection("deallocate");
                                  setSelectedDeveloperProjectAllocationIds([]);
                                } else {
                                  
                                  setActiveSubmoduleSection(null);
                                  setSelectedDeveloperProjectAllocationIds([]);
                                }
                              }}
                              disabled={
                                activeSubmoduleSection === "allocate" &&
                                selectedDeveloperProjectAllocationIds.length > 0
                              }
                              className="rounded border-gray-300 text-red-600 focus:ring-red-500 disabled:opacity-50"
                            />
                            <div className="flex-1">
                              <div className="text-sm font-semibold text-gray-900">
                                {name.trim()}
                              </div>
                              {role &&
                                role.trim().toLowerCase() !== "developer" && (
                                  <div className="text-xs text-gray-500">
                                    {role.trim()}
                                  </div>
                                )}
                              {}
                            </div>
                          </div>
                        );
                      })
                    ) : (
                      <div className="text-gray-400 text-sm">
                        <div>
                          {selectedItems.filter(
                            (item) => item.type === "submodule",
                          ).length > 1
                            ? "No common developers found across selected submodules."
                            : "No developers allocated to selected submodule."}
                        </div>
                        <div className="text-xs mt-1">
                          {selectedItems.filter(
                            (item) => item.type === "submodule",
                          ).length > 1
                            ? "The selected submodules don't share any common developers."
                            : "Selected submodule may not have any allocated developers."}
                        </div>
                      </div>
                    )
                  ) : (
                    <div className="text-gray-400 text-sm">
                      Please select submodules to see allocated developers.
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-end space-x-3 pt-4">
          <Button
            variant="secondary"
            onClick={() => {
              setIsBulkAssignmentModalOpen(false);
              setSelectedItems([]);
              setSelectedDeveloperProjectAllocationId(null);
              setSelectedModuleDeveloperProjectAllocationId(null);
              setSelectedDeveloperProjectAllocationIds([]);
              setSelectedDevelopersForDeallocation(null);
              setSelectedDevelopersForDeallocationBulk([]);
              setSelectedModuleDevelopersForDeallocationBulk([]);
              setSelectedDevelopersForReassignment({
                oldDeveloperId: null,
                newDeveloperId: null,
              });
              setActiveSubmoduleSection(null);
              setActiveTab("allocate");
            }}
          >
            Close
          </Button>
          <Button
            onClick={handleSaveBulkAssignment}
            disabled={
              selectedItems.length === 0 ||
              mixedSelection ||
              isAllocating ||
              (onlyModulesSelected && isModuleAlreadyAllocated()) ||
              (onlyModulesSelected &&
                !selectedModuleDeveloperProjectAllocationId) ||
              (onlySubmodulesSelected &&
                selectedDeveloperProjectAllocationIds.length === 0)
            }
            className="bg-blue-600 hover:bg-blue-700"
          >
            {isAllocating ? "Processing..." : "Allocate Developers"}
          </Button>
          <Button
            onClick={handleDeallocateDevelopers}
            disabled={
              selectedItems.length === 0 ||
              isDeallocating ||
              !hasAllocatedDevelopersForDeallocation() ||
              (selectedModuleDevelopersForDeallocationBulk.length === 0 &&
                selectedDevelopersForDeallocationBulk.length === 0)
            }
            className="bg-blue-600 hover:bg-blue-700"
          >
            {isDeallocating ? "Processing..." : "Deallocate Developers"}
          </Button>
        </div>
      </Modal>

      {}
      <Modal
        isOpen={isAddSubmoduleModalOpen}
        onClose={() => {
          setIsAddSubmoduleModalOpen(false);
          setIsEditingSubmodule(false);
          setEditingSubmoduleId(null);
        }}
        title="Add Submodule"
        size="md"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Submodule Name
            </label>
            <Input
              ref={submoduleInputRef}
              value={submoduleForm.name}
              onChange={(e) => setSubmoduleForm({ name: e.target.value })}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  handleSaveSubmodule();
                }
              }}
              placeholder="Enter submodule name"
              disabled={isCreatingSubmodule || isUpdatingSubmodule}
            />
          </div>
          <div className="flex justify-end space-x-3 pt-4">
            <Button
              variant="secondary"
              onClick={() => setIsAddSubmoduleModalOpen(false)}
              disabled={isCreatingSubmodule || isUpdatingSubmodule}
            >
              Cancel
            </Button>
            <Button
              disabled={
                !submoduleForm.name.trim() ||
                isCreatingSubmodule ||
                isUpdatingSubmodule
              }
              onClick={handleSaveSubmodule}
            >
              {isEditingSubmodule
                ? isUpdatingSubmodule
                  ? "Updating..."
                  : "Update Submodule"
                : isCreatingSubmodule
                  ? "Creating..."
                  : "Add Submodule"}
            </Button>
          </div>
        </div>
      </Modal>

      {}
      {confirmOpen && (
        <div className="fixed inset-0 z-[60] flex justify-center items-start bg-black bg-opacity-40">
          <div
            className="mt-8 bg-[#444] text-white rounded-lg shadow-2xl min-w-[400px] max-w-[95vw]"
            style={{ boxShadow: "0 8px 24px rgba(0,0,0,0.25)" }}
          >
            <div className="px-6 pb-4 pt-5 text-base text-white">
              Are you sure you want to delete this submodule?
            </div>
            <div className="px-6 pb-5 flex justify-end gap-3">
              <button
                className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold px-6 py-2 rounded mr-2"
                onClick={() => {
                  setConfirmOpen(false);
                  setPendingDeleteSubmoduleId(null);
                }}
                type="button"
              >
                Cancel
              </button>
              <button
                className="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-2 rounded"
                onClick={async () => {
                  console.log(
                    "Delete confirm - ModuleId:",
                    pendingDeleteModuleIdForSubmodule,
                  );
                  console.log(
                    "Delete confirm - SubmoduleId:",
                    pendingDeleteSubmoduleId,
                  );

                  if (
                    pendingDeleteSubmoduleId &&
                    pendingDeleteModuleIdForSubmodule
                  ) {
                    try {
                      const response = await deleteSubmoduleApi(
                        Number(pendingDeleteSubmoduleId),
                        Number(pendingDeleteModuleIdForSubmodule),
                      );
                      console.log("Delete response:", response);

                      if (response.status === "success" || response.success) {
                        await fetchModules();
                        setToastMessage("Submodule deleted successfully!");
                        setConfirmOpen(false);
                        setPendingDeleteSubmoduleId(null);
                        setPendingDeleteModuleIdForSubmodule(null);
                      } else {
                        setToastMessage(
                          response.message || "Failed to delete submodule",
                        );
                      }
                      setShowToast(true);
                    } catch (error: any) {
                      console.error("Delete error:", error);

                      const backendMessage =
                        error.response?.data?.message ||
                        error.response?.data?.statusMessage ||
                        error.response?.data?.error ||
                        error.message ||
                        "Failed to delete submodule";
                      setConfirmOpen(false);
                      setPendingDeleteSubmoduleId(null);
                      setPendingDeleteModuleIdForSubmodule(null);
                      setToastMessage(backendMessage);
                      setShowToast(true);
                    } finally {
                      setTimeout(() => {
                        setShowToast(false);
                        setToastMessage(null);
                      }, 5000);
                    }
                  } else {
                    console.error("Missing IDs in dialog:", {
                      submoduleId: pendingDeleteSubmoduleId,
                      moduleId: pendingDeleteModuleIdForSubmodule,
                    });
                    setToastMessage("Error: Missing submodule information");
                    setShowToast(true);
                    setConfirmOpen(false);
                    setPendingDeleteSubmoduleId(null);
                    setPendingDeleteModuleIdForSubmodule(null);
                  }
                }}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {}
      {confirmModuleOpen && (
        <div className="fixed inset-0 z-[60] flex justify-center items-start bg-black bg-opacity-40">
          <div
            className="mt-8 bg-[#444] text-white rounded-lg shadow-2xl min-w-[400px] max-w-[95vw]"
            style={{ boxShadow: "0 8px 24px rgba(0,0,0,0.25)" }}
          >
            <div className="px-6 pb-4 pt-5 text-base text-white">
              Are you sure you want to delete this module? This will also delete
              all submodules.
            </div>
            <div className="px-6 pb-5 flex justify-end gap-3">
              <button
                className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold px-6 py-2 rounded mr-2"
                onClick={() => {
                  setConfirmModuleOpen(false);
                  setPendingDeleteModuleId(null);
                }}
                type="button"
              >
                Cancel
              </button>
              <button
                className="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-2 rounded"
                onClick={async () => {
                  if (pendingDeleteModuleId) {
                    await handleDeleteModule(pendingDeleteModuleId);
                    setConfirmModuleOpen(false);
                    setPendingDeleteModuleId(null);
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
    </div>
  );
};
