import React, { useState, useCallback, useEffect } from "react";
import ReactFlow, {
  Node,
  Edge,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
  Connection,
  Panel,
  MarkerType,
  EdgeTypes,
  Handle,
  Position,
} from "reactflow";
import "reactflow/dist/style.css";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";

import {
  RotateCcw,
  Maximize2,
  LayoutGrid,
  LayoutList,
  Save,
  Trash2,
  ChevronLeft,
} from "lucide-react";
import { useApp } from "../context/AppContext";
import {
  saveWorkflow,
  getAllWorkflows,
  deleteWorkflows,
} from "../api/workflow";
import { getAllDefectStatuses } from "../api/defectStatus";


const CustomEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
}: any) => {
  const offset = 30;
  const midX = (sourceX + targetX) / 2;
  const midY = (sourceY + targetY) / 2;

  const edgePath = `M ${sourceX} ${sourceY} 
                    C ${sourceX + offset} ${sourceY},
                      ${midX} ${midY},
                      ${targetX} ${targetY}`;

  return (
    <path
      id={id}
      style={{
        ...style,
        strokeWidth: 2,
        stroke: "#94a3b8",
      }}
      className="react-flow__edge-path"
      d={edgePath}
      markerEnd={markerEnd}
    />
  );
};


const edgeTypes: EdgeTypes = {
  custom: CustomEdge,
};


const nodeTypes = {
  default: ({ data }: { data: any }) => (
    <div className="px-4 py-2 shadow-md rounded-md bg-white border-2 border-gray-200 relative">
      <Handle
        type="source"
        position={Position.Bottom}
        className="w-3 h-3 bg-blue-500 border-2 border-white"
      />
      <Handle
        type="target"
        position={Position.Top}
        className="w-3 h-3 bg-blue-500 border-2 border-white"
      />
      <div className="flex items-center">
        <div
          className="rounded-full w-3 h-3 mr-2"
          style={{ backgroundColor: data.color }}
        />
        <div className="font-medium">{data.label}</div>
      </div>
    </div>
  ),
};


const initialNodes: Node[] = [];
const initialEdges: Edge[] = [];

const StatusWorkflow: React.FC = () => {
  const { statusTypes, setStatusTypes } = useApp();
  const [reactFlowInstance, setReactFlowInstance] = useState<any>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [showModal, setShowModal] = useState(false);
  const [editingNode, setEditingNode] = useState<Node | null>(null);

  const [isVertical, setIsVertical] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [saveMessage, setSaveMessage] = useState<{
    type: "success" | "error";
    text: string;
  } | null>(null);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [existingWorkflows, setExistingWorkflows] = useState<any[]>([]);
  const [isLoadingWorkflows, setIsLoadingWorkflows] = useState(false);

  
  useEffect(() => {
    setIsInitialized(true);
  }, []);

  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = "move";
  }, []);

  const onDrop = useCallback(
    (event: React.DragEvent) => {
      event.preventDefault();

      const rawData = event.dataTransfer.getData("application/reactflow");
      if (!rawData) return;

      let statusObj: any = null;
      try {
        statusObj = JSON.parse(rawData);
      } catch {
        statusObj = statusTypes && statusTypes.find((s) => s.name === rawData);
      }

      if (!statusObj) {
        statusObj = statusTypes && statusTypes.find((s) => s.name === rawData);
      }
      if (!statusObj) return;

      const position = reactFlowInstance.screenToFlowPosition({
        x: event.clientX,
        y: event.clientY,
      });

      const nodeId = `status-${statusObj.id}`;
      // Prevent duplicate status nodes on the canvas
      const existing = nodes.find(
        (n) => n.id === nodeId || n.data?.statusId === Number(statusObj.id),
      );
      if (existing) {
        return;
      }

      const newNode: Node = {
        id: nodeId,
        type: "default",
        position,
        data: {
          statusId: Number(statusObj.id),
          label: statusObj.name,
          color: statusObj.color || "#94a3b8",
        },
      };

      setNodes((nds: Node[]) => nds.concat(newNode));
    },
    [reactFlowInstance, setNodes, statusTypes, nodes],
  );

  const onNodeClick = useCallback((event: React.MouseEvent, node: Node) => {
    setEditingNode(node);
    setShowModal(true);
  }, []);

  const handleDeleteNode = useCallback(
    (nodeId: string) => {
      setNodes((nds: Node[]) => nds.filter((node: Node) => node.id !== nodeId));
      setEdges((eds: Edge[]) =>
        eds.filter(
          (edge: Edge) => edge.source !== nodeId && edge.target !== nodeId,
        ),
      );
    },
    [setNodes, setEdges],
  );

  const handleLayout = useCallback(() => {
    if (!reactFlowInstance) return;

    const currentNodes = reactFlowInstance.getNodes();
    const currentEdges = reactFlowInstance.getEdges();

    
    const incomingConnections = new Map<string, string[]>();
    const outgoingConnections = new Map<string, string[]>();

    
    currentEdges.forEach((edge) => {
      if (!outgoingConnections.has(edge.source)) {
        outgoingConnections.set(edge.source, []);
      }
      outgoingConnections.get(edge.source)!.push(edge.target);

      if (!incomingConnections.has(edge.target)) {
        incomingConnections.set(edge.target, []);
      }
      incomingConnections.get(edge.target)!.push(edge.source);
    });

    
    const initialNodes = currentNodes.filter(
      (node) =>
        !incomingConnections.has(node.id) ||
        incomingConnections.get(node.id)!.length === 0,
    );

    
    const levels = new Map<string, number>();
    const positioned = new Set<string>();

    
    initialNodes.forEach((node) => {
      levels.set(node.id, 0);
      positioned.add(node.id);
    });

    
    let currentLevel = 0;
    let hasChanges = true;

    while (hasChanges && currentLevel < 10) {
      hasChanges = false;

      currentNodes.forEach((node) => {
        if (positioned.has(node.id)) return;

        const incoming = incomingConnections.get(node.id) || [];
        const incomingLevels = incoming
          .filter((id) => positioned.has(id))
          .map((id) => levels.get(id)!);

        if (incomingLevels.length > 0) {
          const maxIncomingLevel = Math.max(...incomingLevels);
          levels.set(node.id, maxIncomingLevel + 1);
          positioned.add(node.id);
          hasChanges = true;
        }
      });

      currentLevel++;
    }

    
    currentNodes.forEach((node) => {
      if (!positioned.has(node.id)) {
        levels.set(node.id, currentLevel);
      }
    });

    
    const levelGroups = new Map<number, Node[]>();
    currentNodes.forEach((node) => {
      const level = levels.get(node.id) || 0;
      if (!levelGroups.has(level)) {
        levelGroups.set(level, []);
      }
      levelGroups.get(level)!.push(node);
    });

    
    const levelSpacing = isVertical ? 200 : 300;
    const nodeSpacing = isVertical ? 150 : 120;
    const startX = 200;
    const startY = 100;

    const newNodes = currentNodes.map((node) => {
      const level = levels.get(node.id) || 0;
      const levelNodes = levelGroups.get(level) || [];
      const indexInLevel = levelNodes.findIndex((n) => n.id === node.id);

      const totalWidth = (levelNodes.length - 1) * nodeSpacing;
      const startXForLevel = startX - totalWidth / 2;

      const x = isVertical
        ? startXForLevel + indexInLevel * nodeSpacing
        : startX + level * levelSpacing;
      const y = isVertical
        ? startY + level * levelSpacing
        : startY + indexInLevel * nodeSpacing;

      return {
        ...node,
        position: { x, y },
      };
    });

    setNodes(newNodes);
    setIsVertical(!isVertical);

    
    setTimeout(() => {
      reactFlowInstance.fitView({ padding: 0.2 });
    }, 100);
  }, [reactFlowInstance, isVertical, setNodes]);

  const onConnect = useCallback(
    (params: Connection) => {
      const newEdge: Edge = {
        id: `e${params.source}-${params.target}`,
        source: params.source!,
        target: params.target!,
        type: "custom",
        animated: true,
        markerEnd: {
          type: MarkerType.ArrowClosed,
          width: 20,
          height: 20,
          color: "#94a3b8",
        },
      };
      setEdges((eds: Edge[]) => addEdge(newEdge, eds));
    },
    [setEdges],
  );

  
  useEffect(() => {
    setNodes((nds) =>
      nds.map((node) => {
        const status =
          statusTypes && statusTypes.find((s) => s.name === node.data.label);
        if (status) {
          return {
            ...node,
            data: {
              ...node.data,
              color: status.color,
            },
          };
        }
        return node;
      }),
    );
  }, [statusTypes, setNodes]);

  
  const convertWorkflowToApiFormat = useCallback(() => {
    const apiNodes = nodes.map((node) => {
      const statusId =
        node.data?.statusId ||
        statusTypes.find((s) => s.name === node.data?.label)?.id;

      return {
        id: Number(statusId),
        positionX: Math.round(node.position.x),
        positionY: Math.round(node.position.y),
      };
    });

    const connections = edges.map((edge) => {
      const sourceNode = nodes.find((node) => node.id === edge.source);
      const targetNode = nodes.find((node) => node.id === edge.target);

      const sourceId =
        sourceNode?.data?.statusId ||
        statusTypes.find((status) => status.name === sourceNode?.data?.label)?.id;

      const targetId =
        targetNode?.data?.statusId ||
        statusTypes.find((status) => status.name === targetNode?.data?.label)?.id;

      return {
        fromStatusId: Number(sourceId),
        toStatusId: Number(targetId),
      };
    });

    return {
      nodes: apiNodes,
      connections,
    };
  }, [nodes, edges, statusTypes]);

  const convertApiWorkflowToVisual = useCallback(
    (workflow: any) => {
      const newNodes: Node[] = [];
      const newEdges: Edge[] = [];
      const addedStatuses = new Map<number, boolean>();

      // Add nodes from positions
      if (Array.isArray(workflow.positions)) {
        workflow.positions.forEach((pos: any) => {
          if (!addedStatuses.has(pos.id)) {
            addedStatuses.set(pos.id, true);
            newNodes.push({
              id: `status-${pos.id}`,
              type: "default",
              position: {
                x: pos.positionX ?? 0,
                y: pos.positionY ?? 0,
              },
              data: {
                statusId: pos.id,
                label: pos.name,
                color: pos.color,
              },
            });
          }
        });
      }

      (workflow.transitions || []).forEach((transition: any) => {
        const fromStatus = transition.fromStatus;
        const toStatus = transition.toStatus;

        if (fromStatus && !addedStatuses.has(fromStatus.id)) {
          addedStatuses.set(fromStatus.id, true);
          newNodes.push({
            id: `status-${fromStatus.id}`,
            type: "default",
            position: {
              x: fromStatus.positionX ?? 0,
              y: fromStatus.positionY ?? 0,
            },
            data: {
              statusId: fromStatus.id,
              label: fromStatus.name,
              color: fromStatus.color,
            },
          });
        }

        if (toStatus && !addedStatuses.has(toStatus.id)) {
          addedStatuses.set(toStatus.id, true);
          newNodes.push({
            id: `status-${toStatus.id}`,
            type: "default",
            position: {
              x: toStatus.positionX ?? 0,
              y: toStatus.positionY ?? 0,
            },
            data: {
              statusId: toStatus.id,
              label: toStatus.name,
              color: toStatus.color,
            },
          });
        }

        if (fromStatus && toStatus) {
          newEdges.push({
            id: `e${fromStatus.id}-${toStatus.id}-${transition.id}`,
            source: `status-${fromStatus.id}`,
            target: `status-${toStatus.id}`,
            type: "custom",
            animated: true,
            markerEnd: {
              type: MarkerType.ArrowClosed,
              width: 20,
              height: 20,
              color: "#94a3b8",
            },
          });
        }
      });

      setNodes(newNodes);
      setEdges(newEdges);

      setTimeout(() => {
        if (reactFlowInstance) {
          reactFlowInstance.fitView({ padding: 0.2 });
        }
      }, 100);
    },
    [setNodes, setEdges, reactFlowInstance],
  );

  const loadExistingWorkflows = useCallback(async () => {
    try {
      setIsLoadingWorkflows(true);
      const response = await getAllWorkflows();

      if (
        (response.data && Array.isArray(response.data) && response.data.length > 0) ||
        (response.positions && Array.isArray(response.positions) && response.positions.length > 0)
      ) {
        const workflowData = {
          id: 1,
          name: "Current Workflow",
          transitions: response.data || [],
          positions: response.positions || [],
        };
        setExistingWorkflows([workflowData]);
        convertApiWorkflowToVisual(workflowData);
      } else {
        setExistingWorkflows([]);
        setNodes([]);
        setEdges([]);
      }
    } catch (error) {
      console.error("Failed to load existing workflows:", error);
      setExistingWorkflows([]);
    } finally {
      setIsLoadingWorkflows(false);
    }
  }, [convertApiWorkflowToVisual, setNodes, setEdges]);

  const handleSaveWorkflow = useCallback(async () => {
    try {
      setIsSaving(true);
      setSaveMessage(null);

      const workflowData = convertWorkflowToApiFormat();

      if (workflowData.connections.length === 0) {
        setSaveMessage({
          type: "error",
          text: "No workflow transitions to save. Please create connections between status nodes.",
        });
        return;
      }

      await saveWorkflow(workflowData);
      setSaveMessage({ type: "success", text: "Workflow saved successfully!" });
      await loadExistingWorkflows();
      window.dispatchEvent(new CustomEvent("refreshDefectStatuses"));
      setTimeout(() => setSaveMessage(null), 3000);
    } catch (error: any) {
      console.error("Failed to save workflow:", error);

      const backendMessage =
        error.response?.data?.message ||
        error.response?.data?.statusMessage ||
        error.message ||
        "Failed to save workflow. Please try again.";

      setSaveMessage({
        type: "error",
        text: backendMessage,
      });
    } finally {
      setIsSaving(false);
    }
  }, [convertWorkflowToApiFormat, loadExistingWorkflows]);

  
  useEffect(() => {
    if (statusTypes.length > 0 && isInitialized) {
      loadExistingWorkflows();
    }
  }, [statusTypes, isInitialized, loadExistingWorkflows]);

  
  const handleRefreshStatuses = useCallback(async () => {
    try {
      setIsRefreshing(true);
      setSaveMessage(null); 

      
      const response = await getAllDefectStatuses();

      
      const apiStatusTypes = response.content.map((status) => ({
        id: String(status.id),
        name: status.name,
        color: status.color,
      }));

      
      
      
      setStatusTypes(apiStatusTypes);

      
      setSaveMessage({
        type: "success",
        text: `Refreshed ${apiStatusTypes.length} defect statuses from API`,
      });

      
      setTimeout(() => setSaveMessage(null), 3000);
    } catch (error) {
      console.error("Failed to refresh statuses:", error);
      setSaveMessage({
        type: "error",
        text: "Failed to refresh statuses. Please try again.",
      });
    } finally {
      setIsRefreshing(false);
    }
  }, [setStatusTypes]);

    useEffect(() => {
    handleRefreshStatuses();
  }, [handleRefreshStatuses]);

  return (
    <div className="h-screen flex">
      {}
      <div className="w-64 bg-white border-r border-gray-200 p-4 flex flex-col">
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">
                Defect Statuses
              </h2>
              <p className="text-xs text-gray-500">
                {statusTypes.length} statuses available
              </p>
            </div>
            <div className="flex space-x-1">
              <Button
                variant="ghost"
                size="sm"
                icon={RotateCcw}
                onClick={handleRefreshStatuses}
                disabled={isRefreshing}
                title="Refresh statuses from API"
              />
              <Button
                variant="ghost"
                size="sm"
                icon={isVertical ? LayoutGrid : LayoutList}
                onClick={() => setIsVertical(!isVertical)}
                title="Toggle layout"
              />
            </div>
          </div>

          <div className="text-xs text-gray-600 bg-blue-50 p-2 rounded-md">
            <p className="font-medium mb-1">How to create workflow:</p>
            <p>1. Drag any status from the list below to the canvas</p>
            <p>2. Add more statuses as needed</p>
            <p>3. Connect statuses by dragging from one node to another</p>
            <p>4. Use the auto-arrange button (↻) to organize the layout</p>
            <p>5. Click "Save Workflow" to save your changes</p>
            <p className="mt-2 text-blue-600"></p>
          </div>

          <div className="space-y-2">
            {statusTypes.length === 0 ? (
              <div className="p-4 text-center text-gray-500">
                <div className="animate-spin w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full mx-auto mb-2"></div>
                <p className="text-sm">Loading defect statuses...</p>
              </div>
            ) : (
              statusTypes.map((status) => (
                <div
                  key={status.name}
                  className="p-2 border border-gray-200 rounded-md cursor-move hover:bg-gray-50 transition-colors"
                  draggable
                  onDragStart={(event) => {
                    event.dataTransfer.setData(
                      "application/reactflow",
                      JSON.stringify(status),
                    );
                    event.dataTransfer.effectAllowed = "move";
                  }}
                >
                  <div className="flex items-center">
                    <div
                      className="w-3 h-3 rounded-full mr-2"
                      style={{ backgroundColor: status.color }}
                    />
                    <span className="text-sm font-medium">{status.name}</span>
                    <span className="text-xs text-gray-400 ml-auto">
                      ID: {status.id}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {}
        {isLoadingWorkflows ? (
          <div className="border-t pt-4 mt-4">
            <div className="flex items-center justify-center p-4">
              <div className="animate-spin w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full mr-2"></div>
              <span className="text-sm text-gray-600">
                Loading existing workflows...
              </span>
            </div>
          </div>
        ) : existingWorkflows.length > 0 ? (
          <div className="border-t pt-4 mt-4">
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-sm font-medium text-gray-900">
                Existing Workflow
              </h3>
              <Button
                variant="ghost"
                size="sm"
                icon={RotateCcw}
                onClick={loadExistingWorkflows}
                disabled={isLoadingWorkflows}
                className="text-xs px-1 py-1 h-auto"
                title="Refresh workflows"
              />
            </div>
            <div className="space-y-2">
              {existingWorkflows.map((workflow, index) => (
                <div
                  key={workflow.id || index}
                  className="p-3 bg-green-50 border border-green-200 rounded-md text-xs"
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-medium text-green-800">
                      {workflow.name || "Saved Workflow"}
                    </span>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="text-xs px-2 py-1 h-auto text-green-600 hover:text-green-700"
                      onClick={() => convertApiWorkflowToVisual(workflow)}
                    >
                      Load
                    </Button>
                  </div>
                  <div className="text-green-700 mb-1">
                    {workflow.transitions.length} transitions found
                  </div>
                  <div className="text-green-600 text-xs">
                    Click "Load" to visualize this workflow
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="border-t pt-4 mt-4">
            <div className="p-3 bg-gray-50 rounded-md text-xs text-center text-gray-600">
              No existing workflows found
            </div>
          </div>
        )}

        <div className="mt-auto space-y-2">
          {}
          {nodes.length > 0 && (
            <Button
              variant="ghost"
              className="w-full text-red-600 hover:text-red-700 hover:bg-red-50"
              onClick={async () => {
                try {
                  await deleteWorkflows();
                  setNodes([]);
                  setEdges([]);
                  setExistingWorkflows([]);
                  setSaveMessage({
                    type: "success",
                    text: "Workflow deleted successfully!",
                  });
                  setTimeout(() => setSaveMessage(null), 3000);
                } catch (delErr: any) {
                  setSaveMessage({
                    type: "error",
                    text:
                      delErr.response?.data?.message ||
                      delErr.message ||
                      "Failed to delete workflow",
                  });
                }
              }}
            >
              Clear Workflow
            </Button>
          )}

          {}
          {edges.length > 0 && (
            <div className="text-xs bg-gray-50 p-2 rounded-md">
              <p className="font-medium mb-1">Workflow Preview:</p>
              <div className="max-h-20 overflow-y-auto">
                {edges.map((edge, index) => {
                  const sourceNode = nodes.find((n) => n.id === edge.source);
                  const targetNode = nodes.find((n) => n.id === edge.target);
                  const sourceStatus = statusTypes.find(
                    (s) => s.name === sourceNode?.data.label,
                  );
                  const targetStatus = statusTypes.find(
                    (s) => s.name === targetNode?.data.label,
                  );
                  return (
                    <div key={index} className="text-gray-600">
                      {sourceNode?.data.label} (ID:{sourceStatus?.id}) →{" "}
                      {targetNode?.data.label} (ID:{targetStatus?.id})
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {}
          {saveMessage && (
            <div
              className={`p-2 rounded-md text-sm ${
                saveMessage.type === "success"
                  ? "bg-green-100 text-green-800 border border-green-200"
                  : "bg-red-100 text-red-800 border border-red-200"
              }`}
            >
              {saveMessage.text}
            </div>
          )}

          <Button
            variant="secondary"
            className="w-full"
            icon={ChevronLeft}
            onClick={() => window.history.back()}
          >
            Go Back
          </Button>
          <Button
            variant="secondary"
            className="w-full"
            icon={Save}
            onClick={handleSaveWorkflow}
            disabled={isSaving || nodes.length === 0}
          >
            {isSaving ? "Saving..." : "Save Workflow"}
          </Button>
        </div>
      </div>

      {}
      <div className="flex-1">
        {isInitialized && (
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onInit={setReactFlowInstance}
            onDrop={onDrop}
            onDragOver={onDragOver}
            onNodeClick={onNodeClick}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            fitView
            defaultEdgeOptions={{
              type: "custom",
              animated: true,
              markerEnd: {
                type: MarkerType.ArrowClosed,
                width: 20,
                height: 20,
                color: "#94a3b8",
              },
            }}
            connectionRadius={20}
            snapToGrid={true}
            snapGrid={[15, 15]}
          >
            <Background />
            <Controls />

            {}
            {nodes.length === 0 && !isLoadingWorkflows && (
              <Panel
                position="top-center"
                className="pointer-events-none mt-20"
              >
                <div className="bg-white/90 backdrop-blur-sm p-6 rounded-lg border border-gray-200 text-center shadow-lg">
                  <h3 className="text-lg font-medium text-gray-900 mb-2">
                    Create Your Workflow
                  </h3>
                  <p className="text-gray-600 mb-4">
                    {existingWorkflows.length > 0
                      ? "Load an existing workflow from the sidebar or drag status items to create a new one"
                      : "Drag status items from the sidebar to start building your workflow"}
                  </p>
                  <div className="text-sm text-gray-500 mb-2">
                    {statusTypes.length > 0 ? (
                      <>
                        Available statuses:{" "}
                        {statusTypes.map((s) => s.name).join(", ")}
                      </>
                    ) : (
                      "Loading statuses from API..."
                    )}
                  </div>
                  <div className="text-xs text-blue-600">
                    ✨ All statuses and workflows are loaded dynamically from
                    the API
                  </div>
                </div>
              </Panel>
            )}

            <Panel position="top-right" className="space-x-2">
              <Button
                variant="secondary"
                size="sm"
                icon={RotateCcw}
                onClick={handleLayout}
                title="Auto-arrange workflow layout"
              />
              <Button
                variant="secondary"
                size="sm"
                icon={Maximize2}
                onClick={() => reactFlowInstance?.fitView({ padding: 0.2 })}
                title="Fit workflow to view"
              />
            </Panel>
          </ReactFlow>
        )}
      </div>

      {}
      <Modal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditingNode(null);
        }}
        title="Delete Node"
      >
        <div className="space-y-4">
          <p className="text-gray-600">
            Are you sure you want to delete the node "{editingNode?.data.label}
            "? This action cannot be undone.
          </p>
          <div className="flex justify-end space-x-2">
            <Button
              variant="secondary"
              onClick={() => {
                setShowModal(false);
                setEditingNode(null);
              }}
            >
              Cancel
            </Button>
            <Button
              variant="secondary"
              icon={Trash2}
              onClick={() => {
                if (editingNode) {
                  handleDeleteNode(editingNode.id);
                }
                setShowModal(false);
              }}
            >
              Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default StatusWorkflow;
