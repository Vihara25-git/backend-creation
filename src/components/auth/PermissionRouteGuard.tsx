import React from "react";
import { Navigate } from "react-router-dom";
import { usePermission } from "../../context/PermissionContext";

interface Props {
  children: React.ReactNode;
  permission?: boolean;
}

const PermissionRouteGuard: React.FC<Props> = ({ children, permission }) => {
  const { isLoading, permissionsReady, isAdmin } = usePermission();

  // Administrators have full access
  if (isAdmin) {
    return <>{children}</>;
  }

  if (isLoading || !permissionsReady) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="animate-spin rounded-full h-10 w-10 border-4 border-blue-600 border-t-transparent"></div>
      </div>
    );
  }

  if (permission === false) {
    return <Navigate to="/access-denied" replace />;
  }

  return <>{children}</>;
};

export default PermissionRouteGuard;