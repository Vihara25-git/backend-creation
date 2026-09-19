import apiClient from '../lib/api';

export interface DefectStatus {
  color: string;
  name: string;
  statusName?: string;
  id: number;
  type: string;
  statusType?: string;
}

export interface DefectStatusData {
  content: DefectStatus[];
  totalPages: number;
}

export interface DefectStatusResponse {
  status: string;
  statusMessage: string;
  data: DefectStatusData;
  statusCode: number;
}

export interface CreateDefectStatusRequest {
  name: string;
  color: string;
  type: string;
}

export interface UpdateDefectStatusRequest {
  name: string;
  color: string;
  type: string;
}

export const getAllDefectStatuses = async (
  _page: number = 0,
  _pageSize: number = 100,
  sortByWorkflow: boolean = true
): Promise<DefectStatusData> => {
  try {
    const [response, workflowSeqRes] = await Promise.all([
      apiClient.get(`/api/v1/status-type?page=${_page}&size=${_pageSize}`),
      sortByWorkflow
        ? apiClient.get('/api/v1/status/workflow/sequence').catch(() => null)
        : Promise.resolve(null),
    ]);
    const resData = response.data?.data;
    const items = Array.isArray(resData) ? resData : (resData?.content || []);

    let mapped = items.map((s: any) => ({
      id: s.statusTypeId || s.id,
      name: s.statusName || s.name || '',
      statusName: s.statusName || s.name || '',
      color: s.colorCode || s.color || '#000000',
      type: s.statusType || s.type || 'OPEN',
      statusType: s.statusType || s.type || 'OPEN',
    }));

    if (sortByWorkflow) {
      let seqIds: number[] = [];
      if (workflowSeqRes?.data?.data && Array.isArray(workflowSeqRes.data.data) && workflowSeqRes.data.data.length > 0) {
        seqIds = workflowSeqRes.data.data.map((s: any) => Number(s.statusTypeId || s.id));
      } else {
        try {
          const [wfRes, startRes] = await Promise.all([
            apiClient.get('/api/v1/status/workflow').catch(() => ({ data: { data: [] } })),
            apiClient.get('/api/v1/status/workflow/start').catch(() => ({ data: { data: null } })),
          ]);
          const transitions = Array.isArray(wfRes.data?.data) ? wfRes.data.data : (Array.isArray(wfRes.data) ? wfRes.data : []);
          if (transitions.length > 0) {
            const adj = new Map<number, number[]>();
            const allNodeIds = new Set<number>();
            transitions.forEach((t: any) => {
              const fromId = Number(t.statusTypeId1 || t.fromStatusId);
              const toId = Number(t.statusTypeId2 || t.toStatusId);
              if (fromId && toId) {
                if (!adj.has(fromId)) adj.set(fromId, []);
                adj.get(fromId)!.push(toId);
                allNodeIds.add(fromId);
                allNodeIds.add(toId);
              }
            });
            const startId = Number(startRes?.data?.data || startRes?.data);
            const visited = new Set<number>();
            const queue: number[] = [];
            if (startId && allNodeIds.has(startId)) {
              queue.push(startId);
              visited.add(startId);
              seqIds.push(startId);
            } else if (allNodeIds.size > 0) {
              const first = Array.from(allNodeIds)[0];
              queue.push(first);
              visited.add(first);
              seqIds.push(first);
            }
            while (queue.length > 0) {
              const curr = queue.shift()!;
              const nexts = adj.get(curr) || [];
              for (const n of nexts) {
                if (!visited.has(n)) {
                  visited.add(n);
                  seqIds.push(n);
                  queue.push(n);
                }
              }
            }
            allNodeIds.forEach(id => {
              if (!visited.has(id)) {
                visited.add(id);
                seqIds.push(id);
              }
            });
          }
        } catch {}
      }

      if (seqIds.length > 0) {
        mapped.sort((a, b) => {
          const idxA = seqIds.indexOf(Number(a.id));
          const idxB = seqIds.indexOf(Number(b.id));
          if (idxA !== -1 && idxB !== -1) return idxA - idxB;
          if (idxA !== -1) return -1;
          if (idxB !== -1) return 1;
          return Number(a.id) - Number(b.id);
        });
      }
    }

    return {
      content: mapped,
      totalPages: resData?.totalPages ?? 1,
    };
  } catch (err: any) {
    if (err.response?.status === 404) {
      return {
        content: [],
        totalPages: 1,
      };
    }
    throw err;
  }
};

export const createDefectStatus = async (
  statusData: CreateDefectStatusRequest
): Promise<DefectStatusResponse> => {
  const response = await apiClient.post('/api/v1/status-type', {
    statusName: statusData.name,
    statusType: statusData.type,
    colorCode: statusData.color,
  });
  const resData = response.data?.data || response.data;
  const createdItem: DefectStatus = {
    id: resData?.statusTypeId || resData?.id,
    name: resData?.statusName || statusData.name,
    statusName: resData?.statusName || statusData.name,
    color: resData?.colorCode || statusData.color,
    type: resData?.statusType || statusData.type,
    statusType: resData?.statusType || statusData.type,
  };
  return {
    status: 'success',
    statusMessage: response.data?.statusMessage || 'Status created successfully',
    statusCode: response.status || 200,
    data: {
      content: [createdItem],
      totalPages: 1,
    },
  };
};

export const updateDefectStatus = async (
  id: number,
  statusData: UpdateDefectStatusRequest
): Promise<DefectStatusResponse> => {
  const response = await apiClient.put(`/api/v1/status-type/${id}`, {
    statusName: statusData.name,
    statusType: statusData.type,
    colorCode: statusData.color,
  });
  const resData = response.data?.data || response.data;
  const updatedItem: DefectStatus = {
    id: resData?.statusTypeId || resData?.id || id,
    name: resData?.statusName || statusData.name,
    statusName: resData?.statusName || statusData.name,
    color: resData?.colorCode || statusData.color,
    type: resData?.statusType || statusData.type,
    statusType: resData?.statusType || statusData.type,
  };
  return {
    status: 'success',
    statusMessage: response.data?.statusMessage || 'Status updated successfully',
    statusCode: response.status || 200,
    data: {
      content: [updatedItem],
      totalPages: 1,
    },
  };
};

export const deleteDefectStatus = async (id: number): Promise<DefectStatusResponse> => {
  const response = await apiClient.delete(`/api/v1/status-type/${id}`);
  return {
    status: 'success',
    statusMessage: response.data?.statusMessage || 'Status deleted successfully',
    statusCode: response.status || 200,
    data: {
      content: [],
      totalPages: 1,
    },
  };
};
