import apiClient from "../../lib/api";

export const updateDefectById = async (
  defectId: string | number,
  payload: FormData | any
) => {
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
    briefDescription: defectData.description || defectData.title || defectData.briefDescription || 'Defect description',
    steps: defectData.steps || defectData.stepsToRecreation || 'Steps',
    projectId: defectData.projectId ? Number(defectData.projectId) : undefined,
    moduleId: defectData.moduleId ? Number(defectData.moduleId) : undefined,
    subModuleId: defectData.subModuleId ? Number(defectData.subModuleId) : undefined,
    defectTypeId: Number(defectData.defectTypeId || defectData.typeId || 1),
    severityId: defectData.severityId ? Number(defectData.severityId) : undefined,
    priorityId: defectData.priorityId ? Number(defectData.priorityId) : undefined,
    statusTypeId: defectData.statusTypeId || defectData.statusId || defectData.defectStatusId ? Number(defectData.statusTypeId || defectData.statusId || defectData.defectStatusId) : undefined,
    releaseIds: defectData.releaseId ? [Number(defectData.releaseId)] : (defectData.releaseIds ? defectData.releaseIds.map(Number) : undefined),
    assignToId: (defectData.assignedTo || defectData.assigntoId || defectData.assignToId) ? Number(defectData.assignedTo || defectData.assigntoId || defectData.assignToId) : undefined,
    testCaseId: defectData.testCaseId ? Number(defectData.testCaseId) : null,
    testCaseRequired: Boolean(defectData.testCaseRequired),
    removeAttachment: Boolean(defectData.removeAttachment),
    enterBy: defectData.enterBy || 'Admin',
  };

  const formData = new FormData();
  formData.append('defect', new Blob([JSON.stringify(defectDto)], { type: 'application/json' }));
  if (imageFile instanceof File || (imageFile instanceof Blob && imageFile.size > 0)) {
    formData.append('attachmentImage', imageFile);
  }

  const response = await apiClient.patch(`/api/v1/defect/put/${defectId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

  return {
    status: 200,
    statusCode: 200,
    data: {
      status: 'Success',
      statusCode: 200,
      message: response.data?.statusMessage || 'Defect updated successfully',
      data: response.data?.data || response.data,
    },
  };
};

export const updateDefect = updateDefectById;