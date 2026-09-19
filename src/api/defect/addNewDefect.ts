import apiClient from "../../lib/api";

export interface DefectCreate {
  description: string;
  steps: string;
  projectId?: number;
  severityId: number;
  priorityId: number;
  defectStatusId?: number;
  statusId?: number;
  typeId?: number;
  defectTypeId?: number;
  reOpenCount?: number;
  attachment?: string | null;
  assignbyId?: number | null;
  assignedById?: number | null;
  assigntoId?: number;
  assignedTo?: number;
  modulesId?: number;
  moduleId?: number;
  subModuleId?: number | null;
  releasesId?: number | null;
  releaseId?: number | null;
  testCaseRequired?: boolean;
  stepsToRecreation?: string;
  testCaseId?: number | null;
}

export interface DefectCreateProps {
  message: string;
  data: any;
  status: string;
  statusCode: number;
}

export const addDefects = async (
  payload: DefectCreate | FormData
): Promise<DefectCreateProps> => {
  let defectData: any = {};
  let imageFile: any = null;

  if (payload instanceof FormData) {
    const dataPart = payload.get("data");
    if (dataPart instanceof Blob) {
      try {
        const text = await dataPart.text();
        defectData = JSON.parse(text);
      } catch {
        defectData = {};
      }
    } else if (typeof dataPart === "string") {
      try {
        defectData = JSON.parse(dataPart);
      } catch {
        defectData = {};
      }
    } else {
      payload.forEach((val, key) => {
        if (key === 'attachmentImage' || key === 'file') {
          imageFile = val;
        } else {
          defectData[key] = val;
        }
      });
    }
    const filePart = payload.get("attachmentImage") || payload.get("file") || payload.get("attachmentFile");
    if (filePart instanceof File || filePart instanceof Blob) {
      if (filePart.size > 0) imageFile = filePart;
    }
  } else {
    defectData = payload;
  }

  const defectDto: any = {
    briefDescription: defectData.description || defectData.briefDescription || 'Defect description',
    steps: defectData.steps || defectData.stepsToRecreation || 'Steps to reproduce',
    projectId: defectData.projectId ? Number(defectData.projectId) : 1,
    moduleId: defectData.moduleId || defectData.modulesId ? Number(defectData.moduleId || defectData.modulesId) : 1,
    subModuleId: defectData.subModuleId ? Number(defectData.subModuleId) : 1,
    defectTypeId: Number(defectData.defectTypeId || defectData.typeId || 1),
    severityId: Number(defectData.severityId || 1),
    priorityId: Number(defectData.priorityId || 1),
    statusTypeId: Number(defectData.defectStatusId || defectData.statusId || defectData.statusTypeId || 1),
    releaseIds: defectData.releaseId ? [Number(defectData.releaseId)] : (defectData.releaseIds ? defectData.releaseIds.map(Number) : [1]),
    assignToId: (defectData.assigntoId || defectData.assignedTo || defectData.assignToId) ? Number(defectData.assigntoId || defectData.assignedTo || defectData.assignToId) : null,
    testCaseId: defectData.testCaseId ? Number(defectData.testCaseId) : null,
    testCaseRequired: Boolean(defectData.testCaseRequired ?? defectData.isAddTestCase),
    isAddTestCase: Boolean(defectData.testCaseRequired ?? defectData.isAddTestCase),
    enterBy: defectData.enterBy || defectData.assignbyId || defectData.enteredBy || defectData.assignedByName || 'Admin SGIC',
  };

  const formData = new FormData();
  formData.append('defect', new Blob([JSON.stringify(defectDto)], { type: 'application/json' }));
  if (imageFile instanceof File || (imageFile instanceof Blob && imageFile.size > 0)) {
    formData.append('attachmentImage', imageFile);
  }

  const response = await apiClient.post('/api/v1/defect/save', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

  return {
    status: 'Created',
    statusCode: response.status || 201,
    message: response.data?.statusMessage || 'Defect created successfully',
    data: [response.data?.data || response.data],
  };
};
