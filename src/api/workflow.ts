import apiClient from '../lib/api';
import { getAllDefectStatuses } from './defectStatus';

interface WorkflowNodeRequest {
  id: number;
  positionX: number;
  positionY: number;
}

interface WorkflowConnectionRequest {
  fromStatusId: number;
  toStatusId: number;
}

export interface SaveWorkflowRequest {
  nodes: WorkflowNodeRequest[];
  connections: WorkflowConnectionRequest[];
}

export interface SaveWorkflowResponse {
  status: string;
  statusMessage: string;
  data?: any;
  statusCode: number;
}

interface StatusInfo {
  id: number;
  name: string;
  color: string;
  positionX?: number;
  positionY?: number;
}

interface WorkflowTransitionResponse {
  id: number;
  fromStatus: StatusInfo;
  toStatus: StatusInfo;
}

export interface GetAllWorkflowsResponse {
  status: string;
  statusMessage: string;
  data: WorkflowTransitionResponse[];
  positions?: StatusInfo[];
  statusCode: number;
}

export interface NextStatusResponse {
  status: string;
  statusMessage: string;
  data: StatusInfo[];
  statusCode: number;
}

export const getAllWorkflows = async (): Promise<GetAllWorkflowsResponse> => {
  try {
    const [transitionsRes, positionsRes, statusesData] = await Promise.all([
      apiClient.get('/api/v1/status/workflow').catch(() => ({ data: { data: [] } })),
      apiClient.get('/api/v1/status/workflow-position').catch(() => ({ data: { data: [] } })),
      getAllDefectStatuses(0, 100).catch(() => ({ content: [] })),
    ]);

    const transitions = Array.isArray(transitionsRes.data?.data)
      ? transitionsRes.data.data
      : (Array.isArray(transitionsRes.data) ? transitionsRes.data : []);

    const positions = Array.isArray(positionsRes.data?.data)
      ? positionsRes.data.data
      : (Array.isArray(positionsRes.data) ? positionsRes.data : []);

    const statuses = statusesData.content || [];

    const nodePosMap = new Map<number, { x: number; y: number }>();
    const positionsList: StatusInfo[] = [];

    positions.forEach((p: any) => {
      const stId = Number(p.statusTypeId || p.id);
      const x = Number(p.positionX || 0);
      const y = Number(p.positionY || 0);
      nodePosMap.set(stId, { x, y });

      const statusObj = statuses.find(s => Number(s.id) === stId);
      positionsList.push({
        id: stId,
        name: statusObj?.name || statusObj?.statusName || `Status ${stId}`,
        color: statusObj?.color || '#3B82F6',
        positionX: x,
        positionY: y,
      });
    });

    const formattedTransitions: WorkflowTransitionResponse[] = transitions.map((t: any, idx: number) => {
      const fromId = Number(t.statusTypeId1 || t.fromStatusId);
      const toId = Number(t.statusTypeId2 || t.toStatusId);

      const fromStatusObj = statuses.find(s => Number(s.id) === fromId);
      const toStatusObj = statuses.find(s => Number(s.id) === toId);

      const fromPos = nodePosMap.get(fromId);
      const toPos = nodePosMap.get(toId);

      return {
        id: t.workflowId || t.id || idx + 1,
        fromStatus: {
          id: fromId,
          name: fromStatusObj?.name || fromStatusObj?.statusName || `Status ${fromId}`,
          color: fromStatusObj?.color || '#3B82F6',
          positionX: fromPos?.x,
          positionY: fromPos?.y,
        },
        toStatus: {
          id: toId,
          name: toStatusObj?.name || toStatusObj?.statusName || `Status ${toId}`,
          color: toStatusObj?.color || '#10B981',
          positionX: toPos?.x,
          positionY: toPos?.y,
        },
      };
    });

    return {
      status: 'success',
      statusMessage: 'Workflows fetched successfully',
      statusCode: 200,
      data: formattedTransitions,
      positions: positionsList,
    };
  } catch (err: any) {
    return {
      status: 'success',
      statusMessage: 'Workflows fetched',
      statusCode: 200,
      data: [],
      positions: [],
    };
  }
};

export const deleteWorkflows = async (): Promise<void> => {
  await apiClient.delete('/api/v1/status/workflow/deleteAll');
  try {
    await apiClient.delete('/api/v1/status/workflow-position');
  } catch {
    // Ignore position deletion error if any
  }
};

export const saveWorkflow = async (workflowData: SaveWorkflowRequest): Promise<SaveWorkflowResponse> => {
  try {
    const payload = workflowData.connections.map(c => ({
      fromStatusId: Number(c.fromStatusId),
      toStatusId: Number(c.toStatusId),
    }));

    const response = await apiClient.post('/api/v1/status/workflow', payload);

    // Save positions
    if (workflowData.nodes && workflowData.nodes.length > 0) {
      try {
        await apiClient.delete('/api/v1/status/workflow-position').catch(() => {});
        const posPayload = workflowData.nodes.map(n => ({
          statusTypeId: Number(n.id),
          positionX: Number(n.positionX),
          positionY: Number(n.positionY),
        }));
        await apiClient.post('/api/v1/status/workflow-position', posPayload);
      } catch (posErr) {
        console.warn('Failed to save workflow positions', posErr);
      }
    }

    return {
      status: 'success',
      statusMessage: 'Workflow saved successfully',
      statusCode: response.status || 200,
      data: response.data?.data || response.data,
    };
  } catch (err: any) {
    throw new Error(err.response?.data?.message || err.message || 'Failed to save workflow');
  }
};

export const getNextStatuses = async (
  fromStatusId: number
): Promise<NextStatusResponse> => {
  try {
    const [transitionsRes, allWorkflowsRes, statusesData] = await Promise.all([
      apiClient.get(`/api/v1/status/workflow/${fromStatusId}`).catch(() => ({ data: { data: [] } })),
      apiClient.get('/api/v1/status/workflow').catch(() => ({ data: { data: [] } })),
      getAllDefectStatuses(0, 100).catch(() => ({ content: [] })),
    ]);

    const transitions = Array.isArray(transitionsRes.data?.data)
      ? transitionsRes.data.data
      : (Array.isArray(transitionsRes.data) ? transitionsRes.data : []);

    const allWorkflows = Array.isArray(allWorkflowsRes.data?.data)
      ? allWorkflowsRes.data.data
      : (Array.isArray(allWorkflowsRes.data) ? allWorkflowsRes.data : []);

    const statuses = statusesData.content || [];

    const nextStatusIds = transitions
      .map((t: any) => Number(t.statusTypeId2 || t.toStatusId))
      .filter((id: number) => !isNaN(id) && id > 0);

    let allowedStatuses: any[] = [];
    if (allWorkflows.length > 0) {
      allowedStatuses = statuses.filter(s => nextStatusIds.includes(Number(s.id)));
    } else {
      allowedStatuses = statuses.filter(s => Number(s.id) !== Number(fromStatusId));
    }

    return {
      status: 'success',
      statusMessage: 'Next statuses fetched',
      statusCode: 200,
      data: allowedStatuses.map(s => ({
        id: s.id,
        name: s.name || s.statusName || '',
        statusName: s.name || s.statusName || '',
        color: s.color || s.colorCode || '#3B82F6',
        colorCode: s.color || s.colorCode || '#3B82F6',
        toStatus: {
          id: s.id,
          name: s.name || s.statusName || '',
          color: s.color || s.colorCode || '#3B82F6',
        },
      })),
    };
  } catch (err: any) {
    return {
      status: 'error',
      statusMessage: err.message || 'Failed to fetch next statuses',
      statusCode: 500,
      data: [],
    };
  }
};

export const getWorkflowStatusSequence = async (): Promise<StatusInfo[]> => {
  try {
    const seqRes = await apiClient.get('/api/v1/status/workflow/sequence').catch(() => null);
    if (seqRes?.data?.data && Array.isArray(seqRes.data.data) && seqRes.data.data.length > 0) {
      return seqRes.data.data.map((s: any) => ({
        id: s.statusTypeId || s.id,
        name: s.statusName || s.name || '',
        color: s.colorCode || s.color || '#3B82F6',
      }));
    }

    const [transitionsRes, startRes, statusesData] = await Promise.all([
      apiClient.get('/api/v1/status/workflow').catch(() => ({ data: { data: [] } })),
      apiClient.get('/api/v1/status/workflow/start').catch(() => ({ data: { data: null } })),
      getAllDefectStatuses(0, 100).catch(() => ({ content: [] })),
    ]);

    const transitions = Array.isArray(transitionsRes.data?.data)
      ? transitionsRes.data.data
      : (Array.isArray(transitionsRes.data) ? transitionsRes.data : []);
    const statuses = statusesData.content || [];

    if (!transitions || transitions.length === 0) {
      return statuses.map(s => ({
        id: s.id,
        name: s.name || s.statusName || '',
        color: s.color || s.colorCode || '#3B82F6',
      }));
    }

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
    const orderedIds: number[] = [];
    const visited = new Set<number>();

    const queue: number[] = [];
    if (startId && allNodeIds.has(startId)) {
      queue.push(startId);
      visited.add(startId);
      orderedIds.push(startId);
    } else if (allNodeIds.size > 0) {
      const first = Array.from(allNodeIds)[0];
      queue.push(first);
      visited.add(first);
      orderedIds.push(first);
    }

    while (queue.length > 0) {
      const curr = queue.shift()!;
      const nexts = adj.get(curr) || [];
      for (const n of nexts) {
        if (!visited.has(n)) {
          visited.add(n);
          orderedIds.push(n);
          queue.push(n);
        }
      }
    }

    allNodeIds.forEach(id => {
      if (!visited.has(id)) {
        visited.add(id);
        orderedIds.push(id);
      }
    });

    return orderedIds.map(id => {
      const st = statuses.find(s => Number(s.id) === id);
      return {
        id,
        name: st?.name || st?.statusName || `Status ${id}`,
        color: st?.color || st?.colorCode || '#3B82F6',
      };
    });
  } catch (e) {
    return [];
  }
};
