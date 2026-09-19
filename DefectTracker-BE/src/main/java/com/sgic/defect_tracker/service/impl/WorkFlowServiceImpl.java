package com.sgic.defect_tracker.service.impl;

import com.sgic.defect_tracker.dtos.request.WorkFlowRequestDTO;
import com.sgic.defect_tracker.dtos.response.StatusTypeResponseDTO;
import com.sgic.defect_tracker.dtos.response.WorkFlowResponseDTO;
import com.sgic.defect_tracker.entities.StatusType;
import com.sgic.defect_tracker.entities.WorkFlow;
import com.sgic.defect_tracker.mapper.StatusTypeMapper;
import com.sgic.defect_tracker.mapper.WorkFlowMapper;
import com.sgic.defect_tracker.repositories.DefectRepository;
import com.sgic.defect_tracker.repositories.StatusTypeRepository;
import com.sgic.defect_tracker.repositories.WorkflowRepository;
import com.sgic.defect_tracker.service.WorkFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    @Autowired
    private WorkflowRepository workflowrepository;

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private WorkFlowMapper workflowMapper;

    @Autowired
    private StatusTypeRepository statusTypeRepository;

    @Autowired
    private StatusTypeMapper statusTypeMapper;

    private boolean isStatusTypeUsedByDefect(Long statusTypeId) {
        return defectRepository.existsByStatusType_StatusTypeId(statusTypeId);
    }

    @Override
    public List<WorkFlowResponseDTO> createWorkFlow(
            List<WorkFlowRequestDTO> requestDTOs) {

        // Get all existing workflows from database
        List<WorkFlow> existingWorkflows =
                workflowrepository.findAll();

        // Convert frontend request into a list of pairs
        List<String> requestedConnections = requestDTOs.stream()
                .map(dto ->
                        dto.getFromStatusId() + "-" + dto.getToStatusId()
                )
                .collect(Collectors.toList());

        // Delete workflows that are no longer present in the request
        List<WorkFlow> workflowsToDelete = existingWorkflows.stream()
                .filter(workflow -> {
                    String connection =
                            workflow.getStatusTypeId1() + "-"
                                    + workflow.getStatusTypeId2();

                    return !requestedConnections.contains(connection);
                })
                .collect(Collectors.toList());

// Check whether workflows to be deleted are used by defects
        for (WorkFlow workflow : workflowsToDelete) {

            if (isStatusTypeUsedByDefect(workflow.getStatusTypeId1())
                    || isStatusTypeUsedByDefect(workflow.getStatusTypeId2())) {

                throw new RuntimeException(
                        "Cannot update workflows because status type "
                                + "is currently used by a defect."
                );
            }
        }

        if (!workflowsToDelete.isEmpty()) {
            workflowrepository.deleteAll(workflowsToDelete);
        }

        // Find existing connections
        List<String> existingConnections = existingWorkflows.stream()
                .map(workflow ->
                        workflow.getStatusTypeId1() + "-"
                                + workflow.getStatusTypeId2()
                )
                .collect(Collectors.toList());

        // Create only NEW workflows
        List<WorkFlow> workflowsToSave = requestDTOs.stream()
                .filter(dto -> {
                    String connection =
                            dto.getFromStatusId() + "-"
                                    + dto.getToStatusId();

                    return !existingConnections.contains(connection);
                })
                .map(workflowMapper::toEntity)
                .collect(Collectors.toList());

        // Save only new connections
        if (!workflowsToSave.isEmpty()) {
            workflowrepository.saveAll(workflowsToSave);
        }

        // Return current workflow state from database
        List<WorkFlow> currentWorkflows =
                workflowrepository.findAll();

        return currentWorkflows.stream()
                .map(workflowMapper::toDto)
                .collect(Collectors.toList());
    }





    @Override
    public void deleteAllWorkFlows() {

        List<WorkFlow> workflows = workflowrepository.findAll();

        for (WorkFlow workflow : workflows) {

            if (isStatusTypeUsedByDefect(workflow.getStatusTypeId1())
                    || isStatusTypeUsedByDefect(workflow.getStatusTypeId2())) {

                throw new RuntimeException(
                        "Cannot delete workflows because status type "
                                + "is currently used by a defect."
                );
            }
        }

        workflowrepository.deleteAll();
    }
    @Override
    public List<WorkFlowResponseDTO> getWorkFlowsByStatusTypeId1(
            Long statusTypeId1) {

        List<WorkFlow> workflows =
                workflowrepository.findByStatusTypeId1(statusTypeId1);

        if (workflows == null || workflows.isEmpty()) {
            return Collections.emptyList();
        }

        return workflows.stream()
                .map(workflowMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkFlowResponseDTO> getAllWorkFlows(){

        List<WorkFlow> workflows = workflowrepository.findAll();

        return workflows.stream()
                .map(workflowMapper::toDto)
                .collect(Collectors.toList());
    }

    // for identify the first node
    @Override
    public Long getStartingStatusTypeId() {

        List<WorkFlow> workflows =
                workflowrepository.findAll();

        if (workflows == null || workflows.isEmpty()) {
            return null;
        }

        // All status IDs that have outgoing connections
        List<Long> statusTypeId1List = workflows.stream()
                .map(WorkFlow::getStatusTypeId1)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // All status IDs that have incoming connections
        List<Long> statusTypeId2List = workflows.stream()
                .map(WorkFlow::getStatusTypeId2)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Starting node = statusTypeId1 which is never a statusTypeId2
        return statusTypeId1List.stream()
                .filter(id -> !statusTypeId2List.contains(id))
                .findFirst()
                .orElse(statusTypeId1List.isEmpty() ? null : statusTypeId1List.get(0));
    }

    @Override
    public List<StatusTypeResponseDTO> getWorkflowStatusSequence() {
        List<WorkFlow> workflows = workflowrepository.findAll();
        if (workflows == null || workflows.isEmpty()) {
            return statusTypeRepository.findAll().stream()
                    .map(statusTypeMapper::toResponse)
                    .collect(Collectors.toList());
        }

        // Build adjacency map: fromId -> list of toIds
        Map<Long, List<Long>> adjacency = new LinkedHashMap<>();
        Set<Long> allNodeIds = new LinkedHashSet<>();

        for (WorkFlow wf : workflows) {
            Long fromId = wf.getStatusTypeId1();
            Long toId = wf.getStatusTypeId2();
            if (fromId != null && toId != null) {
                adjacency.computeIfAbsent(fromId, k -> new ArrayList<>()).add(toId);
                allNodeIds.add(fromId);
                allNodeIds.add(toId);
            }
        }

        // Find starting node
        Long startNode = getStartingStatusTypeId();
        List<Long> orderedIds = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        if (startNode != null && allNodeIds.contains(startNode)) {
            Queue<Long> queue = new LinkedList<>();
            queue.add(startNode);
            visited.add(startNode);
            orderedIds.add(startNode);

            while (!queue.isEmpty()) {
                Long current = queue.poll();
                List<Long> nextNodes = adjacency.getOrDefault(current, Collections.emptyList());
                for (Long next : nextNodes) {
                    if (!visited.contains(next)) {
                        visited.add(next);
                        orderedIds.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        // Add any remaining nodes in the workflow that weren't reached
        for (Long nodeId : allNodeIds) {
            if (!visited.contains(nodeId)) {
                visited.add(nodeId);
                orderedIds.add(nodeId);
            }
        }

        // Fetch entities for ordered IDs
        Map<Long, StatusType> statusMap = statusTypeRepository.findAllById(orderedIds).stream()
                .collect(Collectors.toMap(StatusType::getStatusTypeId, s -> s));

        List<StatusTypeResponseDTO> result = new ArrayList<>();
        for (Long id : orderedIds) {
            StatusType st = statusMap.get(id);
            if (st != null) {
                result.add(statusTypeMapper.toResponse(st));
            }
        }

        return result;
    }
}