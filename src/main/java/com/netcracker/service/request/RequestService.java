package com.netcracker.service.request;

import com.netcracker.exception.*;
import com.netcracker.exception.IllegalAccessException;
import com.netcracker.exception.request.RequestNotAssignedException;
import com.netcracker.model.dto.Page;
import com.netcracker.model.entity.ChangeGroup;
import com.netcracker.model.entity.Request;
import com.netcracker.model.entity.Status;
import com.netcracker.repository.common.Pageable;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestService {
    Optional<Request> getRequestById(Long id);

    Optional<Request> saveSubRequest(Request subRequest, String email) throws CannotCreateSubRequestException;

    Optional<Request> saveRequest(Request request, String email) throws CannotCreateRequestException, CannotCreateSubRequestException;

    int addToRequestGroup(Long requestId, Integer groupId, Principal principal) throws ResourceNotFoundException, IncorrectStatusException, IllegalAccessException, RequestNotAssignedException;

    int removeFromRequestGroup(Long requestId, Principal principal) throws ResourceNotFoundException, IllegalAccessException;

    Optional<Request> updateRequest(Request request, Long requestId, Principal principal) throws ResourceNotFoundException, IllegalAccessException;

    Optional<Request> updateRequestPriority(Long requestId, String priority, String authorName);

    List<Request> getAllSubRequest(Long parentId) throws ResourceNotFoundException;

    void deleteRequestById(Long id, Principal principal) throws CannotDeleteRequestException, ResourceNotFoundException;

    int changeRequestStatus(Request request, Status status, String authorName);

    boolean assignRequest(Long requestId, Long personId, Principal principal) throws CannotAssignRequestException;

    List<Request> getAvailableRequestList(Integer priorityId, Pageable pageable);

    Page<Request> getAvailableRequestList(Integer priorityId, Pageable pageable, Integer temporary);

    List<Request> getAllRequestByEmployee(String employeeEmail, Pageable pageable);

    Long getCountFree(Integer priorityId);

    Long getCountAllRequestByEmployee(String employeeEmail);

    Set<ChangeGroup> getRequestHistory(Long requestId, String period, Pageable pageable);

    List<Request> getRequestsByRequestGroup(Integer requestGroupId);

    List<Request> getRequestsByRequestGroup(Integer requestGroupId, Pageable pageable);

    void fill(Request request);

    void checkRequestsForExpiry();
}