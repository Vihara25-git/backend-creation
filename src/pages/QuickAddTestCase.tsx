import React, { useState, useEffect } from "react";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { Toast } from "../components/ui/Toast";
import { useApp } from "../context/AppContext";
import { importTestCases } from "../api/importTestCase";
import { getSeverities } from "../api/severity";
import { getDefectTypes } from "../api/defectType";
import { getModulesByProjectId } from "../api/module/getModule";
import { getSubmodulesByModuleId } from "../api/submodule/submoduleget";
import { createTestCase } from "../api/testCase/createTestcase";
import { usePermission } from "../context/PermissionContext";


interface QuickAddTestCaseProps {
  selectedProjectId: string;
  onTestCaseAdded?: () => void;
  renderButton?: (props: { onClick: () => void; disabled: boolean }) => React.ReactNode;
}

const QuickAddTestCase: React.FC<QuickAddTestCaseProps> = ({ selectedProjectId, onTestCaseAdded, renderButton }) => {
  const { projects } = useApp();
  const {can} = usePermission();
  const [modal, setModal] = useState({
    open: false,
    formData: {
      moduleId: "",
      subModuleId: "",
      description: "",
      steps: "",
      type: "",
      severity: "",
    },
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const fileInputRef = React.useRef<HTMLInputElement>(null);
  const [severities, setSeverities] = useState<{ id: number; name: string; color: string }[]>([]);
  const [defectTypes, setDefectTypes] = useState<{ id: number; defectTypeName: string }[]>([]);
  const [subModules, setSubModules] = useState<any[]>([]);
  
  // Toast state
  const [toast, setToast] = useState<{ isOpen: boolean; message: string; type: 'success' | 'error' }>({ 
    isOpen: false, 
    message: '', 
    type: 'success' 
  });
  
  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ isOpen: true, message, type });
    setTimeout(() => {
      setToast({ isOpen: false, message: '', type: 'success' });
    }, 5000);
  };

  const [projectModules, setProjectModules] = useState<any[]>([]);

  const handleInputChange = (field: string, value: string) => {
    setModal((prev) => ({
      ...prev,
      formData: { ...prev.formData, [field]: value }
    }));
  };

  const handleImportExcel = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append("file", file);
    try {
      const response = await importTestCases(formData, selectedProjectId);
        if (response && response.data && Array.isArray(response.data)) {
          showToast("Import successful!", "success");
          if (onTestCaseAdded) onTestCaseAdded();
          window.dispatchEvent(
            new CustomEvent("testCaseCreated", {
              detail: { projectId: selectedProjectId },
            })
        );
      } else {
        showToast("Import succeeded but no data returned.", "error");
      }
    } catch (error: any) {
  showToast(
    error?.response?.data?.message ||
    error?.response?.data?.statusMessage ||
    error?.response?.data?.error ||
    "Failed to import test cases.",
    "error"
  );
}
  };

  const handleSubmit = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();

    const formData = modal.formData;

    const selectedSeverity = severities.find((sev) => sev.name === formData.severity);
    const selectedDefectType = defectTypes.find((dt) => dt.defectTypeName === formData.type);

    if (!selectedSeverity || !selectedDefectType) {
      showToast('Please fill all required fields before submitting.', "error");
      return;
    }

    const subModuleId = formData.subModuleId ? Number(formData.subModuleId) : null;
    if (!subModuleId) {
      showToast('Please select a submodule before submitting.', "error");
      return;
    }

    const payload = {
      description: formData.description,
      detailsSteps: formData.steps,
      severityId: selectedSeverity.id,
      defectTypeId: selectedDefectType.id,
      projectId: Number(selectedProjectId),
      moduleId: Number(formData.moduleId),
    };

    console.log('Quick Add - subModuleId:', subModuleId, 'payload:', payload);

    setIsSubmitting(true);

    try {
      const response = await createTestCase(subModuleId, payload);
      console.log("Create test case response:", response);
      
      
      if (response?.status === 'Created' || 
          response?.status === 'Success' || 
          response?.statusCode === 201 || 
          response?.statusCode === 200) {
        
        
        showToast(response?.statusMessage || 'Test case created successfully!', "success");
        
        
        setModal({
          open: false,
          formData: {
            moduleId: "",
            subModuleId: "",
            description: "",
            steps: "",
            type: "",
            severity: "",
          },
        });
        
        // Reset form state
        setSubModules([]);
        
        // Refresh parent component if callback exists
        if (onTestCaseAdded) {
          onTestCaseAdded();
        }
        
        window.dispatchEvent(
          new CustomEvent("testCaseCreated", {
            detail: { projectId: selectedProjectId },
          })
        );
      } else {
        
        showToast(response?.message || response?.statusMessage || 'Failed to create test case.', "error");
      }
    } catch (error: any) {
      console.error("Create test case error:", error);
      
      let errorMessage = 'Failed to create test case.';
      if (error?.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error?.response?.data?.statusMessage) {
        errorMessage = error.response.data.statusMessage;
      } else if (error?.message) {
        errorMessage = error.message;
      }
      showToast(errorMessage, "error");
    } finally {
      setIsSubmitting(false);
    }
  };

  const isFormValid = () => {
    return (
      modal.formData.moduleId &&
      modal.formData.subModuleId &&
      modal.formData.description &&
      modal.formData.steps &&
      modal.formData.type &&
      modal.formData.severity
    );
  };

  
  useEffect(() => {
    getSeverities(0, 100)
      .then(res => {
        console.log("Severities API response:", res);
        let severityData = [];
        if (res.data?.content && Array.isArray(res.data.content)) {
          severityData = res.data.content;
        } else if (res.data && Array.isArray(res.data)) {
          severityData = res.data;
        } else if (Array.isArray(res)) {
          severityData = res;
        }
        
        const mappedSeverities = severityData.map((sev: any) => ({
          id: sev.id,
          name: sev.name,
          color: sev.color
        }));
        console.log("Mapped severities:", mappedSeverities);
        setSeverities(mappedSeverities);
      })
      .catch((error) => {
        console.error("Error fetching severities:", error);
        setSeverities([]);
      });
  }, []);

  
  useEffect(() => {
    getDefectTypes(0, 100)
      .then(res => {
        console.log("Defect Types API response:", res);
        let defectTypeData = [];
        if (res.data?.content && Array.isArray(res.data.content)) {
          defectTypeData = res.data.content;
        } else if (res.data && Array.isArray(res.data)) {
          defectTypeData = res.data;
        } else if (Array.isArray(res)) {
          defectTypeData = res;
        }
        
        const mappedDefectTypes = defectTypeData.map((dt: any) => ({
          id: dt.id,
          defectTypeName: dt.name || dt.defectTypeName
        }));
        console.log("Mapped defect types:", mappedDefectTypes);
        setDefectTypes(mappedDefectTypes);
      })
      .catch((error) => {
        console.error("Error fetching defect types:", error);
        setDefectTypes([]);
      });
  }, []);

  useEffect(() => {
    if (modal.open && selectedProjectId) {
      fetchModules();
    }
  }, [modal.open, selectedProjectId]);

  const fetchModules = async () => {
    if (!selectedProjectId) {
      setProjectModules([]);
      return;
    }
    try {
      console.log("Fetching modules for project:", selectedProjectId);
      const res = await getModulesByProjectId(Number(selectedProjectId));
      console.log("Modules response:", res);
      
      let modules = [];
      if (Array.isArray(res)) {
        modules = res;
      } else if (res?.data && Array.isArray(res.data)) {
        modules = res.data;
      } else if (res?.content && Array.isArray(res.content)) {
        modules = res.content;
      } else if (res?.data?.content && Array.isArray(res.data.content)) {
        modules = res.data.content;
      }
      
      const mapped = modules.map((mod: any) => ({
        id: String(mod.id),
        name: mod.moduleName || mod.name,
        submodules: (mod.submodules || []).map((sm: any) => ({
          id: String(sm.id || sm.subModuleId),
          name: sm.getSubModuleName || sm.subModuleName || sm.name
        }))
      }));
      console.log("Mapped modules:", mapped);
      setProjectModules(mapped);
    } catch (error) {
      console.error("Error fetching modules:", error);
      setProjectModules([]);
    }
  };

  useEffect(() => {
    fetchModules();
  }, [selectedProjectId]);

  useEffect(() => {
    const selectedModuleId = modal.formData.moduleId;
    console.log("Selected module ID:", selectedModuleId);
    
    if (!selectedModuleId) {
      setSubModules([]);
      return;
    }
    
    const selectedModuleObj = projectModules.find(
      (m: any) => String(m.id) === String(selectedModuleId)
    );
    console.log("Selected module object:", selectedModuleObj);
    
    if (selectedModuleObj && selectedModuleObj.submodules && selectedModuleObj.submodules.length > 0) {
      console.log("Using submodules from module object:", selectedModuleObj.submodules);
      setSubModules(selectedModuleObj.submodules);
    } else if (selectedModuleObj) {
      const numericModuleId = Number(selectedModuleId);
      if (!isNaN(numericModuleId)) {
        console.log("Fetching submodules from API for module:", numericModuleId);
        getSubmodulesByModuleId(numericModuleId)
          .then(res => {
            console.log("Submodules API response:", res);
            let submodulesArray = [];
            if (res.data && Array.isArray(res.data)) {
              submodulesArray = res.data;
            } else if (res.data?.content && Array.isArray(res.data.content)) {
              submodulesArray = res.data.content;
            } else if (Array.isArray(res)) {
              submodulesArray = res;
            }
            
            const mapped = submodulesArray.map((sm: any) => ({
              id: String(sm.id || sm.subModuleId),
              name: sm.subModuleName || sm.getSubModuleName || sm.name
            }));
            console.log("Mapped submodules:", mapped);
            setSubModules(mapped);
          })
          .catch((error) => {
            console.error("Error fetching submodules:", error);
            setSubModules([]);
          });
      } else {
        setSubModules([]);
      }
    } else {
      setSubModules([]);
    }
  }, [modal.formData.moduleId, projectModules]);

  
  useEffect(() => {
    setModal({
      open: false,
      formData: {
        moduleId: "",
        subModuleId: "",
        description: "",
        steps: "",
        type: "",
        severity: "",
      },
    });
    setSubModules([]);
    setProjectModules([]);
  }, [selectedProjectId]);

  return (
    <>
      {/* Toast Notification */}
      <Toast
        isOpen={toast.isOpen}
        message={toast.message}
        type={toast.type}
        onClose={() => setToast({ ...toast, isOpen: false })}
      />
      
      <div>
        {can.testCase.create && (
          renderButton ? (
            renderButton({
              onClick: () => {
                setModal({
                  open: true,
                  formData: {
                    moduleId: "",
                    subModuleId: "",
                    description: "",
                    steps: "",
                    type: "",
                    severity: "",
                  },
                });
              },
              disabled: !selectedProjectId,
            })
          ) : (
            <div className="relative flex items-center w-44 h-12">
              <span className="absolute left-0 flex items-center justify-center w-12 h-12 rounded-lg bg-blue-500 shadow-md">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="lucide lucide-clipboard-check"
                  style={{ color: '#fff' }}
                >
                  <rect x="9" y="2" width="6" height="4" rx="1" />
                  <path d="M9 4H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-2" />
                  <path d="m9 14 2 2 4-4" stroke="#22c55e" />
                </svg>
              </span>
              <Button
                onClick={() => {
                  setModal({
                    open: true,
                    formData: {
                      moduleId: "",
                      subModuleId: "",
                      description: "",
                      steps: "",
                      type: "",
                      severity: "",
                    },
                  });
                }}
                className="pl-14 pr-4 py-1 bg-white rounded-xl shadow border border-gray-200 w-full h-12 flex items-center font-semibold text-gray-900 hover:shadow-lg hover:bg-gray-50 transition-all justify-start"
                disabled={!selectedProjectId}
                style={{ fontWeight: 500, borderStyle: 'solid' }}
              >
                <span className="text-base font-medium text-gray-900 whitespace-nowrap">Add Test Case</span>
              </Button>
            </div>
          )
        )}
        
        {modal.open && (
          <Modal
            isOpen={modal.open}
            onClose={() => {
              setModal({
                ...modal,
                open: false,
                formData: {
                  moduleId: "",
                  subModuleId: "",
                  description: "",
                  steps: "",
                  type: "",
                  severity: "",
                },
              });
            }}
            title={
              projects && projects.find(
                (p: { id: string }) => p.id === selectedProjectId
              )
                ? `Add New Test Case (${projects.find(
                  (p: { id: string }) => p.id === selectedProjectId
                )?.name})`
                : "Add New Test Case"
            }
            size="xl"
          >
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSubmit();
              }}
              className="space-y-4"
            >
              <div className="flex items-center mb-2">
                <button
                  type="button"
                  className="flex items-center px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded shadow mr-3"
                  onClick={() => fileInputRef.current?.click()}
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
                  Import from Excel/CSV
                </button>
                <input
                  type="file"
                  accept=".xlsx,.csv"
                  onChange={handleImportExcel}
                  ref={fileInputRef}
                  className="hidden"
                />
              </div>
              
              <div className="border rounded-lg p-4 mb-2 relative">
                <div className="grid grid-cols-2 gap-4">
                  {}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Module <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={modal.formData.moduleId}
                      onChange={(e) => {
                        handleInputChange("moduleId", e.target.value);
                        handleInputChange("subModuleId", "");
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      required
                      disabled={!selectedProjectId}
                    >
                      <option value="">Select Module</option>
                      {projectModules.map((module: { id: string; name: string }) => (
                        <option key={module.id} value={module.id}>
                          {module.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  {/* Submodule Dropdown */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Sub Module <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={modal.formData.subModuleId}
                      onChange={(e) => handleInputChange("subModuleId", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      disabled={!modal.formData.moduleId}
                      required
                    >
                      <option value="">
                        {!modal.formData.moduleId
                          ? "Select Module First"
                          : subModules.length === 0
                          ? "No submodules available"
                          : "Select Sub Module"}
                      </option>
                      {modal.formData.moduleId && subModules.map((submodule: any) => (
                        <option key={submodule.id} value={submodule.id}>
                          {submodule.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  
                  {}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Type <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={modal.formData.type}
                      onChange={(e) => handleInputChange("type", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      required
                    >
                      <option value="">Select Type</option>
                      {defectTypes.length === 0 ? (
                        <option value="" disabled>No defect types available</option>
                      ) : (
                        defectTypes.map((type) => (
                          <option key={type.id} value={type.defectTypeName}>
                            {type.defectTypeName}
                          </option>
                        ))
                      )}
                    </select>
                  </div>
                  
                  {/* Severity Dropdown - Fetched from Severity API */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Severity <span className="text-red-500">*</span>
                    </label>
                    <select
                      value={modal.formData.severity}
                      onChange={(e) => handleInputChange("severity", e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      required
                    >
                      <option value="">Select Severity</option>
                      {severities.length === 0 ? (
                        <option value="" disabled>No severities available</option>
                      ) : (
                        severities.map((severity) => (
                          <option key={severity.id} value={severity.name}>
                            {severity.name}
                          </option>
                        ))
                      )}
                    </select>
                  </div>
                </div>
                
                {/* Description Field */}
                <div className="mt-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Description <span className="text-red-500">*</span>
                  </label>
                  <p className="text-xs text-gray-500 mb-1">Description must contain letters and at least one number or special character</p>
                  <textarea
                    value={modal.formData.description}
                    onChange={(e) => handleInputChange("description", e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    rows={2}
                    required
                  />
                </div>
                
                {}
                <div className="mt-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Test Steps <span className="text-red-500">*</span>
                  </label>
                  <p className="text-xs text-gray-500 mb-1">Steps must contain letters and at least one number or special character (e.g., "1. Step one", "Step 1!")</p>
                  <textarea
                    value={modal.formData.steps}
                    onChange={(e) => handleInputChange("steps", e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    rows={4}
                    required
                  />
                </div>
              </div>
              
              {}
              <div className="flex justify-end items-center gap-3 pt-4">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setModal({
                      open: false,
                      formData: {
                        moduleId: "",
                        subModuleId: "",
                        description: "",
                        steps: "",
                        type: "",
                        severity: "",
                      },
                    });
                  }}
                  disabled={isSubmitting}
                >
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting || !isFormValid()}>
                  {isSubmitting ? "Creating..." : "Submit"}
                </Button>
              </div>
            </form>
          </Modal>
        )}
      </div>
    </>
  );
};

export default QuickAddTestCase;