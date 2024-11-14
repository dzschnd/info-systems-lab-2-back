package org.lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.lab.model.AdminRequest;
import org.lab.model.RequestStatus;
import org.lab.model.Role;
import org.lab.model.User;
import org.lab.repository.AdminRequestRepository;
import org.lab.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class AdminRequestService {

    @Inject
    private AdminRequestRepository adminRequestRepository;

    @Inject
    private UserRepository userRepository;

    public AdminRequest createRequest(User user) {
        AdminRequest request = new AdminRequest();
        request.setUser(user);
        request.setStatus(RequestStatus.PENDING);
        request.setRequestDate(LocalDateTime.now());
        return adminRequestRepository.save(request);
    }

    public List<AdminRequest> getPendingRequests() {
        return adminRequestRepository.findAllByStatus(RequestStatus.PENDING);
    }

    public void approveRequest(Integer requestId) {
        AdminRequest request = adminRequestRepository.findById(requestId);
        if (request == null) {
            throw new RuntimeException("Request not found");
        }
        request.setStatus(RequestStatus.APPROVED);
        User user = request.getUser();
        user.setRole(Role.ADMIN);
        userRepository.update(user);
        adminRequestRepository.update(request);
    }

    public void rejectRequest(Integer requestId) {
        AdminRequest request = adminRequestRepository.findById(requestId);
        if (request == null) {
            throw new RuntimeException("Request not found");
        }
        request.setStatus(RequestStatus.REJECTED);
        adminRequestRepository.update(request);
    }
}
