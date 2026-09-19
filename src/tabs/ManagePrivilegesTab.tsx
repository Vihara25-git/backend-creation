import React, { useState, useEffect, useMemo } from "react";
import { Button } from "../components/ui/Button";
import { Input } from "../components/ui/Input";
import { Modal } from "../components/ui/Modal";
import { Card, CardContent } from "../components/ui/Card";
import { Badge } from "../components/ui/Badge";
import { Toast } from "../components/ui/Toast";
import {
  Plus,
  Edit2,
  Trash2,
  Search,
  Key,
  Shield,
  Loader2,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  Filter,
} from "lucide-react";
import {
  getAllPrivilegeTemplates,
  createPrivilegeTemplate,
  updatePrivilegeTemplate,
  deletePrivilegeTemplate,
  PrivilegeTemplateItem,
} from "../api/Privileges";
import { isAxiosError } from "axios";

interface ManagePrivilegesTabProps {
  onPrivilegeChanged?: () => void;
}

const COMMON_ACTIONS = [
  "READ",
  "CREATE",
  "UPDATE",
  "DELETE",
  "ASSIGN",
  "DEALLOCATE",
  "EXTEND",
  "STATUS_CHANGE",
  "STATUS_UPDATE",
  "ASSIGN_DEVELOPER",
  "READ_STATUS_HISTORY",
  "CHANGE",
  "EXPORT",
];

export const ManagePrivilegesTab: React.FC<ManagePrivilegesTabProps> = ({
  onPrivilegeChanged,
}) => {
  const [templates, setTemplates] = useState<PrivilegeTemplateItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [moduleFilter, setModuleFilter] = useState("ALL");

  // Modals
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const [editingTemplate, setEditingTemplate] = useState<PrivilegeTemplateItem | null>(null);
  const [deletingTemplate, setDeletingTemplate] = useState<PrivilegeTemplateItem | null>(null);

  const [formData, setFormData] = useState({
    type: "",
    subType: "READ",
    description: "",
  });

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  // Toast
  const [toast, setToast] = useState<{
    isOpen: boolean;
    message: string;
    type: "success" | "error";
  }>({
    isOpen: false,
    message: "",
    type: "success",
  });

  const showToast = (message: string, type: "success" | "error" = "success") => {
    setToast({ isOpen: true, message, type });
  };

  const fetchTemplates = async () => {
    setLoading(true);
    try {
      const data = await getAllPrivilegeTemplates();
      setTemplates(data);
    } catch (error) {
      console.error("Error fetching privilege templates:", error);
      showToast("Failed to fetch privileges", "error");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  // Distinct modules for filter and suggestions
  const availableModules = useMemo(() => {
    const set = new Set<string>();
    templates.forEach((t) => {
      if (t.type) set.add(t.type);
    });
    return Array.from(set).sort();
  }, [templates]);

  // Filtered templates
  const filteredTemplates = useMemo(() => {
    return templates.filter((t) => {
      const search = searchTerm.toLowerCase().trim();
      const matchesSearch =
        !search ||
        t.type.toLowerCase().includes(search) ||
        t.subType.toLowerCase().includes(search) ||
        t.description.toLowerCase().includes(search);

      const matchesModule =
        moduleFilter === "ALL" || t.type.toLowerCase() === moduleFilter.toLowerCase();

      return matchesSearch && matchesModule;
    });
  }, [templates, searchTerm, moduleFilter]);

  // Paginated templates
  const totalPages = Math.max(1, Math.ceil(filteredTemplates.length / pageSize));
  const paginatedTemplates = useMemo(() => {
    const start = (currentPage - 1) * pageSize;
    return filteredTemplates.slice(start, start + pageSize);
  }, [filteredTemplates, currentPage, pageSize]);

  // Reset page when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, moduleFilter]);

  const resetForm = () => {
    setFormData({
      type: "",
      subType: "READ",
      description: "",
    });
  };

  const validateForm = () => {
    if (!formData.type.trim()) {
      return { isValid: false, message: "Module/Type name cannot be empty." };
    }
    if (!formData.subType.trim()) {
      return { isValid: false, message: "Action/Sub-Type cannot be empty." };
    }
    return { isValid: true, message: "" };
  };

  const handleCreate = async () => {
    const v = validateForm();
    if (!v.isValid) {
      showToast(v.message, "error");
      return;
    }

    setSaving(true);
    try {
      await createPrivilegeTemplate({
        type: formData.type.trim(),
        subType: formData.subType.trim().toUpperCase(),
        description: formData.description.trim(),
      });

      showToast("Privilege created successfully", "success");
      setIsCreateModalOpen(false);
      resetForm();
      await fetchTemplates();
      onPrivilegeChanged?.();
    } catch (error: any) {
      console.error("Error creating privilege:", error);
      const msg =
        error.response?.data?.message ||
        error.response?.data?.statusMessage ||
        (isAxiosError(error) ? error.message : "Failed to create privilege");
      showToast(msg, "error");
    } finally {
      setSaving(false);
    }
  };

  const handleOpenEdit = (template: PrivilegeTemplateItem) => {
    setEditingTemplate(template);
    setFormData({
      type: template.type,
      subType: template.subType,
      description: template.description || "",
    });
    setIsEditModalOpen(true);
  };

  const handleUpdate = async () => {
    if (!editingTemplate) return;

    const v = validateForm();
    if (!v.isValid) {
      showToast(v.message, "error");
      return;
    }

    setSaving(true);
    try {
      await updatePrivilegeTemplate(editingTemplate.id, {
        type: formData.type.trim(),
        subType: formData.subType.trim().toUpperCase(),
        description: formData.description.trim(),
      });

      showToast("Privilege updated successfully", "success");
      setIsEditModalOpen(false);
      setEditingTemplate(null);
      resetForm();
      await fetchTemplates();
      onPrivilegeChanged?.();
    } catch (error: any) {
      console.error("Error updating privilege:", error);
      const msg =
        error.response?.data?.message ||
        error.response?.data?.statusMessage ||
        (isAxiosError(error) ? error.message : "Failed to update privilege");
      showToast(msg, "error");
    } finally {
      setSaving(false);
    }
  };

  const handleOpenDelete = (template: PrivilegeTemplateItem) => {
    setDeletingTemplate(template);
    setIsDeleteModalOpen(true);
  };

  const handleDelete = async () => {
    if (!deletingTemplate) return;

    setSaving(true);
    try {
      await deletePrivilegeTemplate(deletingTemplate.id);
      showToast("Privilege deleted successfully", "success");
      setIsDeleteModalOpen(false);
      setDeletingTemplate(null);
      await fetchTemplates();
      onPrivilegeChanged?.();
    } catch (error: any) {
      console.error("Error deleting privilege:", error);
      const msg =
        error.response?.data?.message ||
        error.response?.data?.statusMessage ||
        (isAxiosError(error) ? error.message : "Failed to delete privilege");
      showToast(msg, "error");
    } finally {
      setSaving(false);
    }
  };

  const getActionBadge = (action: string) => {
    const act = action.toUpperCase();
    if (act === "READ") {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
          READ
        </span>
      );
    }
    if (act === "CREATE") {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
          CREATE
        </span>
      );
    }
    if (act === "UPDATE") {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800">
          UPDATE
        </span>
      );
    }
    if (act === "DELETE") {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
          DELETE
        </span>
      );
    }
    if (act.includes("ASSIGN")) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
          {act}
        </span>
      );
    }
    if (act.includes("STATUS")) {
      return (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-800">
          {act}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
        {act}
      </span>
    );
  };

  return (
    <div className="space-y-6">
      <Toast
        isOpen={toast.isOpen}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ ...toast, isOpen: false })}
      />

      {/* Action Header Card */}
      <Card className="shadow-sm border-gray-200">
        <CardContent className="p-6">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div className="flex flex-col sm:flex-row gap-3 flex-1 w-full sm:w-auto">
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <input
                  type="text"
                  placeholder="Search by module, action, description..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white shadow-sm"
                />
              </div>

              <div className="relative sm:w-60">
                <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                <select
                  value={moduleFilter}
                  onChange={(e) => setModuleFilter(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white shadow-sm"
                >
                  <option value="ALL">All Modules ({availableModules.length})</option>
                  {availableModules.map((m) => (
                    <option key={m} value={m}>
                      {m}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <Button
              onClick={() => {
                resetForm();
                setIsCreateModalOpen(true);
              }}
              className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white shadow-sm px-4 py-2 rounded-lg"
            >
              <Plus className="w-4 h-4" />
              <span>Add Privilege</span>
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Privileges Table Card */}
      <Card className="shadow-lg border-gray-200 overflow-hidden">
        <CardContent className="p-0">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    ID
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Module / Type
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Action / Sub-Type
                  </th>
                  <th className="px-6 py-3.5 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Description
                  </th>
                  <th className="px-6 py-3.5 text-right text-xs font-semibold text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {loading ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center">
                      <Loader2 className="w-8 h-8 animate-spin text-blue-500 mx-auto mb-2" />
                      <p className="text-gray-500 text-sm">Loading privileges...</p>
                    </td>
                  </tr>
                ) : paginatedTemplates.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-6 py-12 text-center">
                      <Key className="w-12 h-12 text-gray-300 mx-auto mb-3" />
                      <p className="text-gray-600 font-medium">No privileges found</p>
                      <p className="text-gray-400 text-sm mt-1">
                        Try adjusting your search or filter, or click "Add Privilege" to create one.
                      </p>
                    </td>
                  </tr>
                ) : (
                  paginatedTemplates.map((t) => (
                    <tr key={t.id} className="hover:bg-blue-50/40 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-500">
                        #{t.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <Shield className="w-4 h-4 text-blue-500 mr-2 flex-shrink-0" />
                          <span className="text-sm font-semibold text-gray-900">{t.type}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        {getActionBadge(t.subType)}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-600 max-w-md">
                        {t.description || <span className="text-gray-400 italic">No description</span>}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex items-center justify-end space-x-2">
                          <button
                            onClick={() => handleOpenEdit(t)}
                            className="p-1.5 text-blue-600 hover:text-blue-800 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Edit Privilege"
                          >
                            <Edit2 className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => handleOpenDelete(t)}
                            className="p-1.5 text-red-600 hover:text-red-800 hover:bg-red-50 rounded-lg transition-colors"
                            title="Delete Privilege"
                          >
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex flex-col sm:flex-row justify-between items-center gap-3">
            <div className="text-sm text-gray-500">
              Showing{" "}
              <span className="font-medium text-gray-700">
                {filteredTemplates.length === 0
                  ? 0
                  : (currentPage - 1) * pageSize + 1}
              </span>{" "}
              to{" "}
              <span className="font-medium text-gray-700">
                {Math.min(currentPage * pageSize, filteredTemplates.length)}
              </span>{" "}
              of <span className="font-medium text-gray-700">{filteredTemplates.length}</span> privileges
            </div>

            <div className="flex items-center space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage === 1 || loading}
                className="flex items-center"
              >
                <ChevronLeft className="w-4 h-4 mr-1" />
                Previous
              </Button>

              <div className="text-sm font-medium text-gray-700 px-3">
                Page {currentPage} of {totalPages}
              </div>

              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                disabled={currentPage >= totalPages || loading}
                className="flex items-center"
              >
                Next
                <ChevronRight className="w-4 h-4 ml-1" />
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* CREATE PRIVILEGE MODAL */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => {
          if (!saving) setIsCreateModalOpen(false);
        }}
        title="Add New Privilege"
        size="md"
      >
        <div className="space-y-4 pt-2">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Name / Type <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              list="module-datalist"
              placeholder="e.g. Defect, Release, Report"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
            <datalist id="module-datalist">
              {availableModules.map((m) => (
                <option key={m} value={m} />
              ))}
            </datalist>
            <p className="text-xs text-gray-400 mt-1">
              Select an existing module or type a new module name
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Action / Sub-Type <span className="text-red-500">*</span>
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                list="action-datalist"
                placeholder="e.g. READ, CREATE, UPDATE, DELETE, EXPORT"
                value={formData.subType}
                onChange={(e) =>
                  setFormData({ ...formData, subType: e.target.value.toUpperCase() })
                }
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
              <select
                onChange={(e) => {
                  if (e.target.value) {
                    setFormData({ ...formData, subType: e.target.value });
                  }
                }}
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50"
              >
                <option value="">Common Actions</option>
                {COMMON_ACTIONS.map((act) => (
                  <option key={act} value={act}>
                    {act}
                  </option>
                ))}
              </select>
            </div>
            <datalist id="action-datalist">
              {COMMON_ACTIONS.map((act) => (
                <option key={act} value={act} />
              ))}
            </datalist>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              rows={3}
              placeholder="e.g. Allows viewing defect list and defect details"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-100">
            <Button
              variant="outline"
              onClick={() => setIsCreateModalOpen(false)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button
              onClick={handleCreate}
              disabled={saving}
              className="bg-blue-600 hover:bg-blue-700 text-white flex items-center"
            >
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {saving ? "Creating..." : "Create Privilege"}
            </Button>
          </div>
        </div>
      </Modal>

      {/* EDIT PRIVILEGE MODAL */}
      <Modal
        isOpen={isEditModalOpen}
        onClose={() => {
          if (!saving) setIsEditModalOpen(false);
        }}
        title="Edit Privilege"
        size="md"
      >
        <div className="space-y-4 pt-2">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Module Name / Type <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              list="module-datalist-edit"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
            <datalist id="module-datalist-edit">
              {availableModules.map((m) => (
                <option key={m} value={m} />
              ))}
            </datalist>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Action / Sub-Type <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.subType}
              onChange={(e) =>
                setFormData({ ...formData, subType: e.target.value.toUpperCase() })
              }
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-100">
            <Button
              variant="outline"
              onClick={() => setIsEditModalOpen(false)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button
              onClick={handleUpdate}
              disabled={saving}
              className="bg-blue-600 hover:bg-blue-700 text-white flex items-center"
            >
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {saving ? "Saving..." : "Save Changes"}
            </Button>
          </div>
        </div>
      </Modal>

      {/* DELETE PRIVILEGE CONFIRMATION MODAL */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => {
          if (!saving) setIsDeleteModalOpen(false);
        }}
        title="Delete Privilege"
        size="sm"
      >
        <div className="space-y-4 pt-2">
          <div className="flex items-start space-x-3 text-red-600 bg-red-50 p-3 rounded-lg">
            <AlertTriangle className="w-6 h-6 flex-shrink-0 mt-0.5" />
            <div className="text-sm text-red-800">
              <p className="font-semibold">Warning: Irreversible action</p>
              <p className="mt-1">
                Deleting this privilege will permanently remove it from the catalog and immediately revoke it from all roles and employees who have it assigned.
              </p>
            </div>
          </div>

          {deletingTemplate && (
            <div className="p-3 bg-gray-50 rounded-lg text-sm space-y-1">
              <div>
                <span className="text-gray-500">Module: </span>
                <span className="font-semibold text-gray-800">{deletingTemplate.type}</span>
              </div>
              <div>
                <span className="text-gray-500">Action: </span>
                <span className="font-mono font-semibold text-gray-800">
                  {deletingTemplate.subType}
                </span>
              </div>
              {deletingTemplate.description && (
                <div>
                  <span className="text-gray-500">Description: </span>
                  <span className="text-gray-700">{deletingTemplate.description}</span>
                </div>
              )}
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-100">
            <Button
              variant="outline"
              onClick={() => setIsDeleteModalOpen(false)}
              disabled={saving}
            >
              Cancel
            </Button>
            <Button
              onClick={handleDelete}
              disabled={saving}
              className="bg-red-600 hover:bg-red-700 text-white flex items-center"
            >
              {saving && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {saving ? "Deleting..." : "Delete Privilege"}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
