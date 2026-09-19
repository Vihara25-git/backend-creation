import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from "react";
import { useAuth } from "./AuthContext";
import apiClient from "../lib/api";
import AuthService from "../services/authService";

interface ProjectInfo {
  projectId: number;
  projectName: string;
  role: string;
}

interface PermissionContextType {
  can: any;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permList: string[]) => boolean;
  allPermissions: string[];
  isLoading: boolean;
  permissionsReady: boolean;
  projects: ProjectInfo[];
  selectedProjectId: number | null;
  switchProject: (projectId: number | null) => Promise<void>;
  refreshPermissions: () => Promise<void>;
  isAdmin: boolean;
}

const PermissionContext = createContext<PermissionContextType | undefined>(undefined);

export const usePermission = () => {
  const context = useContext(PermissionContext);
  if (!context) throw new Error("usePermission must be used within PermissionProvider");
  return context;
};

const MODULE_MAPPINGS: Record<string, string[]> = {
  employee: ["EMPLOYEE"],
  bench: ["BENCH", "PROJECT_ALLOCATION"],
  project: ["PROJECT"],
  testCase: ["TEST_CASE"],
  release: ["RELEASE"],
  defect: ["DEFECT"],
  designation: ["DESIGNATION"],
  role: ["ROLE"],
  permission: ["PERMISSION", "ROLE_PERMISSION", "EMPLOYEE_PERMISSION"],
  defectType: ["DEFECT_TYPE"],
  releaseType: ["RELEASE_TYPE"],
  severity: ["SEVERITY"],
  priority: ["PRIORITY"],
  statusType: ["STATUS"],
  workflow: ["WORKFLOW"],
  emailConfig: ["EMAIL_CONFIG"],
  pointSetup: ["POINT_SETUP"],
  roleEmailRecipient: ["ROLE_EMAIL_RECIPIENT"],
  employeeEmailRecipient: ["EMPLOYEE_EMAIL_RECIPIENT"],
  emailTemplate: ["EMAIL_TEMPLATE"],
  module: ["MODULE"],
  subModule: ["SUB_MODULE"],
  projectAllocation: ["PROJECT_ALLOCATION"],
  defectComment: ["DEFECT_COMMENT"],
};

const ACTION_MAPPINGS: Record<string, string[]> = {
  view: ["READ"],
  read: ["READ"],
  list: ["READ"],
  create: ["CREATE", "ASSIGN"],
  add: ["CREATE", "ASSIGN"],
  edit: ["UPDATE", "EXTEND", "STATUS_UPDATE", "STATUS_CHANGE"],
  update: ["UPDATE", "EXTEND", "STATUS_UPDATE", "STATUS_CHANGE"],
  delete: ["DELETE", "DEALLOCATE"],
  remove: ["DELETE", "DEALLOCATE"],
  assign: ["ASSIGN", "CREATE"],
  extend: ["EXTEND", "UPDATE"],
  deallocate: ["DEALLOCATE", "DELETE"],
  statusUpdate: ["STATUS_UPDATE", "STATUS_CHANGE", "UPDATE"],
  statusChange: ["STATUS_CHANGE", "STATUS_UPDATE", "UPDATE"],
};

const createModuleProxy = (moduleName: string, permissionsSet: Set<string>, isAdmin: boolean) => {
  return new Proxy({}, {
    get: (_target, actionProp: string | symbol) => {
      if (typeof actionProp !== "string") return false;
      if (actionProp === "then") return undefined;

      // Administrator has full access
      if (isAdmin) return true;

      const targetModules = MODULE_MAPPINGS[moduleName] || [
        moduleName.replace(/([a-z])([A-Z])/g, "$1_$2").toUpperCase()
      ];
      const targetActions = ACTION_MAPPINGS[actionProp] || [
        actionProp.replace(/([a-z])([A-Z])/g, "$1_$2").toUpperCase()
      ];

      for (const mod of targetModules) {
        for (const act of targetActions) {
          if (permissionsSet.has(`${mod}_${act}`)) {
            return true;
          }
        }
      }
      return false;
    }
  });
};

const createCanProxy = (permissionsSet: Set<string>, isAdmin: boolean) => {
  return new Proxy({}, {
    get: (_target, moduleProp: string | symbol) => {
      if (typeof moduleProp !== "string") return false;
      if (moduleProp === "then") return undefined;

      return createModuleProxy(moduleProp, permissionsSet, isAdmin);
    }
  });
};

const checkIsUserAdmin = (u: any): boolean => {
  if (!u) return false;
  return (
    u.userType === "ADMIN" ||
    (Array.isArray(u.roles) && (u.roles.includes("ROLE_ADMIN") || u.roles.includes("ADMIN")))
  );
};

export const PermissionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();

  const [permissions, setPermissions] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [permissionsReady, setPermissionsReady] = useState(false);
  const [projects, setProjects] = useState<ProjectInfo[]>([]);
  const [isAdmin, setIsAdmin] = useState<boolean>(() => {
    const currentUser = AuthService.getCurrentUser();
    return checkIsUserAdmin(currentUser);
  });

  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(() => {
    const saved = localStorage.getItem("selectedProjectId");
    return saved ? parseInt(saved) : null;
  });

  const fetchPermissions = useCallback(async (projId: number | null) => {
    try {
      setIsLoading(true);
      const res = await apiClient.get("/api/v1/user/me/permissions");
      const data = res.data?.data || res.data;
      let permList: string[] = Array.isArray(data?.permissions)
        ? data.permissions
        : Array.isArray(data)
        ? data
        : [];
      let isAdm = Boolean(data?.admin);

      if (projId) {
        try {
          const projRes = await apiClient.get(`/api/v1/user/me/projects/${projId}/permissions`);
          const projData = projRes.data?.data || projRes.data;
          const projPerms: string[] = Array.isArray(projData?.permissions)
            ? projData.permissions
            : [];
          permList = Array.from(new Set([...permList, ...projPerms]));
          if (projData?.admin !== undefined) {
            isAdm = isAdm || Boolean(projData.admin);
          }
        } catch {
          // ignore project permissions fetch error
        }
      }

      setPermissions(permList);
      setIsAdmin(isAdm);
    } catch (error) {
      console.error("Failed to fetch user permissions:", error);
      const currentUser = AuthService.getCurrentUser();
      if (checkIsUserAdmin(currentUser)) {
        setIsAdmin(true);
      } else {
        setPermissions([]);
        setIsAdmin(false);
      }
    } finally {
      setIsLoading(false);
      setPermissionsReady(true);
    }
  }, []);

  const fetchProjects = useCallback(async () => {
    try {
      const res = await apiClient.get("/api/v1/project");
      const data = res.data?.data || res.data;
      const list = Array.isArray(data) ? data : [];
      const projectList = list.map((p: any) => ({
        projectId: Number(p.id || p.projectId),
        projectName: p.name || p.projectName || "Project",
        role: "Project Member",
      }));
      setProjects(projectList);
      return projectList;
    } catch {
      setProjects([]);
      return [];
    }
  }, []);

  const switchProject = useCallback(
    async (projectId: number | null) => {
      if (projectId) {
        localStorage.setItem("selectedProjectId", String(projectId));
      } else {
        localStorage.removeItem("selectedProjectId");
      }
      setSelectedProjectId(projectId);
      await fetchPermissions(projectId);
    },
    [fetchPermissions]
  );

  const refreshPermissions = useCallback(async () => {
    await Promise.all([fetchPermissions(selectedProjectId), fetchProjects()]);
  }, [fetchPermissions, fetchProjects, selectedProjectId]);

  useEffect(() => {
    if (user) {
      fetchProjects();
      fetchPermissions(selectedProjectId);
    } else {
      setPermissions([]);
      setIsAdmin(false);
      setPermissionsReady(true);
      setIsLoading(false);
    }
  }, [user, selectedProjectId, fetchProjects, fetchPermissions]);

  const permissionsSet = useMemo(
    () => new Set(permissions.map(p => p.toUpperCase())),
    [permissions]
  );

  const can = useMemo(
    () => createCanProxy(permissionsSet, isAdmin),
    [permissionsSet, isAdmin]
  );

  const hasPermission = useCallback(
    (permission: string): boolean => {
      if (isAdmin) return true;
      return permissionsSet.has(permission) || permissionsSet.has(permission.toUpperCase());
    },
    [isAdmin, permissionsSet]
  );

  const hasAnyPermission = useCallback(
    (permList: string[]): boolean => {
      if (isAdmin) return true;
      return permList.some(p => hasPermission(p));
    },
    [isAdmin, permissionsSet, hasPermission]
  );

  return (
    <PermissionContext.Provider
      value={{
        can,
        hasPermission,
        hasAnyPermission,
        allPermissions: permissions,
        isLoading,
        permissionsReady,
        projects,
        selectedProjectId,
        switchProject,
        refreshPermissions,
        isAdmin,
      }}
    >
      {children}
    </PermissionContext.Provider>
  );
};