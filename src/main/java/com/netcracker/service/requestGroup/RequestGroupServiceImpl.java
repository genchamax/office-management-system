package com.netcracker.service.requestGroup;

import com.netcracker.exception.CurrentUserNotPresentException;
import com.netcracker.exception.IllegalAccessException;
import com.netcracker.exception.IncorrectStatusException;
import com.netcracker.exception.ResourceNotFoundException;
import com.netcracker.exception.requestGroup.RequestGroupAlreadyExist;
import com.netcracker.model.dto.RequestGroupDTO;
import com.netcracker.model.entity.*;
import com.netcracker.repository.common.Pageable;
import com.netcracker.repository.data.interfaces.*;
import com.netcracker.util.enums.role.RoleEnum;
import com.netcracker.util.enums.status.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.netcracker.util.MessageConstant.*;

@Service
public class RequestGroupServiceImpl implements RequestGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestGroupServiceImpl.class);
    private static final Locale LOCALE = LocaleContextHolder.getLocale();


    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RequestGroupRepository requestGroupRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<RequestGroup> getRequestGroupByAuthorId(Long authorId, Pageable pageable) {
        return requestGroupRepository.findRequestGroupByAuthorId(authorId, pageable);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<RequestGroup> getRequestGroupByNamePart(String namePart, Long authorId) {
        String regex = generateRegexByNamePart(namePart);
        return requestGroupRepository.findRequestGroupByNameRegex(regex, authorId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_OFFICE MANAGER', 'ROLE_ADMINISTRATOR')")
    public Optional<RequestGroup> saveRequestGroup(RequestGroup requestGroup) throws RequestGroupAlreadyExist {
        if (requestGroupRepository
                .findRequestGroupByNameAndAuthor(requestGroup.getName(), requestGroup.getAuthor().getId()).isPresent()) {
            LOGGER.error(messageSource
                    .getMessage(REQUEST_GROUP_ALREADY_EXIST, new Object[]{requestGroup.getName()}, LOCALE));

            throw new RequestGroupAlreadyExist(messageSource
                    .getMessage(REQUEST_GROUP_ALREADY_EXIST, new Object[]{requestGroup.getName()}, LOCALE));
        }

        return requestGroupRepository.save(requestGroup);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_OFFICE MANAGER', 'ROLE_ADMINISTRATOR')")
    public Optional<RequestGroup> saveRequestGroup(RequestGroupDTO requestGroupDTO, Principal principal) throws CurrentUserNotPresentException, RequestGroupAlreadyExist {
        RequestGroup requestGroup = requestGroupDTO.toRequestGroup();

        String currentUserEmail = principal.getName();
        if (currentUserEmail.isEmpty()) {
            LOGGER.error("Current user not present");
            throw new CurrentUserNotPresentException(messageSource
                    .getMessage(USER_ERROR_NOT_PRESENT, null, LOCALE));
        }
        Optional<Person> currentUser = personRepository.findPersonByEmail(currentUserEmail);
        if (!currentUser.isPresent()) {
            LOGGER.error("Can't fetch information about current user");
            throw new CurrentUserNotPresentException(messageSource
                    .getMessage(USER_WITH_EMAIL_NOT_PRESENT, new Object[]{currentUserEmail}, LOCALE));
        } else
            requestGroup.setAuthor(currentUser.get());

        return saveRequestGroup(requestGroup);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_OFFICE MANAGER', 'ROLE_ADMINISTRATOR')")
    public Optional<RequestGroup> updateRequestGroup(RequestGroupDTO requestGroupDTO, Principal principal) throws ResourceNotFoundException, IllegalAccessException, RequestGroupAlreadyExist {
        RequestGroup requestGroup = getRequestGroupById(requestGroupDTO.getId());

        if (requestGroupDTO.getName() != null) {
            requestGroup.setName(requestGroupDTO.getName());
        }

        if (!isAccessLegal(requestGroup, principal))
            throw new IllegalAccessException(messageSource
                    .getMessage(REQUEST_GROUP_ILLEGAL_ACCESS, null, LOCALE));

        return saveRequestGroup(requestGroup);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_OFFICE MANAGER', 'ROLE_ADMINISTRATOR')")
    public void removeRequestGroup(Integer requestGroupId) throws ResourceNotFoundException {

        RequestGroup requestGroup = getRequestGroupById(requestGroupId);

        List<Request> requests = requestRepository.findRequestsByRequestGroupId(requestGroup.getId());
        requests.forEach(request -> requestRepository.removeRequestFromRequestGroup(request.getId()));

        requestGroupRepository.delete(requestGroupId);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public int getRequestGroupCountByAuthor(Long authorId) {
        return requestGroupRepository.countRequestGroupByAuthor(authorId);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('ROLE_OFFICE MANAGER', 'ROLE_ADMINISTRATOR')")
    public void setRequestGroupStatus(Integer requestGroupId, Integer statusId, Principal principal)
            throws ResourceNotFoundException, IncorrectStatusException, IllegalAccessException {

        RequestGroup requestGroup = getRequestGroupById(requestGroupId);

        if (!isAccessLegal(requestGroup, principal))
            throw new IllegalAccessException(messageSource
                    .getMessage(REQUEST_GROUP_ILLEGAL_ACCESS, null, LOCALE));

        Optional<Status> statusOptional = statusRepository.findOne(statusId);

        if (!statusOptional.isPresent()) {
            LOGGER.error("Status with id {} not found", statusId);
            throw new ResourceNotFoundException(messageSource
                    .getMessage(STATUS_ERROR, new Object[]{statusId}, LOCALE));
        }

        Status status = statusOptional.get();
        String statusName = status.getName();

        if (statusName.equalsIgnoreCase(StatusEnum.FREE.getName()) ||
                statusName.equalsIgnoreCase(StatusEnum.CANCELED.getName())) {
            LOGGER.error("Can't set status {} for group", statusName);
            throw new IncorrectStatusException(messageSource.getMessage(STATUS_ERROR_NOT_AVAILABLE, null, LOCALE),
                    messageSource.getMessage(STATUS_ERROR_NOT_AVAILABLE_FOR_GROUP, new Object[]{statusName}, LOCALE));
        }

        List<Request> requestsOfRequestGroup = requestRepository.findRequestsByRequestGroupId(requestGroupId);

        Integer canceledStatusId = statusRepository.findStatusByName(StatusEnum.CANCELED.getName()).get().getId();

        for (Request request :
                requestsOfRequestGroup) {

            Integer currentStatusId = request.getStatus().getId();

            if (!currentStatusId.equals(canceledStatusId)) {
                request.setStatus(status);
                requestRepository.save(request);
            } else
                LOGGER.trace("Request {} already canceled", request.getId());
        }

    }

    /**
     * Get request group
     *
     * @param requestGroupId
     * @return request group
     * @throws ResourceNotFoundException
     */
    public RequestGroup getRequestGroupById(Integer requestGroupId) throws ResourceNotFoundException {
        Optional<RequestGroup> requestGroupOptional = requestGroupRepository.findOne(requestGroupId);

        if (!requestGroupOptional.isPresent()) {
            LOGGER.error("Request group with id {} does not exist", requestGroupOptional);
            throw new ResourceNotFoundException(messageSource
                    .getMessage(REQUEST_ERROR_NOT_EXIST, new Object[]{requestGroupId}, LOCALE));
        }

        return requestGroupOptional.get();
    }

    private String generateRegexByNamePart(String namePart) {
        String regex = new StringBuilder()
                .append(".*")
                .append(namePart)
                .append(".*")
                .toString();

        return regex;
    }

    private boolean isAccessLegal(RequestGroup requestGroup, Principal principal) throws CurrentUserNotPresentException, IllegalAccessException {

        Optional<Person> currentUser = personRepository.findPersonByEmail(principal.getName());
        if (!currentUser.isPresent()) {
            LOGGER.warn("Current user not present");
            throw new CurrentUserNotPresentException(messageSource
                    .getMessage(USER_ERROR_NOT_PRESENT, null, LOCALE));
        } else if (!currentUser.get().getId().equals(requestGroup.getAuthor().getId())) {
            Optional<Role> adminRole = roleRepository.findRoleByName(RoleEnum.ADMINISTRATOR.getName());
            if (!currentUser.get().getRole().getId().equals(adminRole.get().getId())) {
                LOGGER.error("Access only for author or administrator");
                return false;
            }
        }

        return true;
    }
}
