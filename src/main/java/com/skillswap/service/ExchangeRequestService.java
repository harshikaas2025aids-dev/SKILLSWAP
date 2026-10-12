package com.skillswap.service;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Skill;
import com.skillswap.model.Student;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer managing skill exchange requests, workflow transitions, and validations.
 */
public class ExchangeRequestService {

    private final ExchangeRequestDAO requestDAO;
    private final StudentDAO studentDAO;
    private final SkillDAO skillDAO;

    public ExchangeRequestService() {
        this.requestDAO = new ExchangeRequestDAO();
        this.studentDAO = new StudentDAO();
        this.skillDAO = new SkillDAO();
    }

    public ExchangeRequestService(ExchangeRequestDAO requestDAO, StudentDAO studentDAO, SkillDAO skillDAO) {
        this.requestDAO = requestDAO;
        this.studentDAO = studentDAO;
        this.skillDAO = skillDAO;
    }

    /**
     * Checks if a transition between exchange request statuses is permitted.
     */
    public static boolean isValidTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) return false;
        String cur = currentStatus.toUpperCase().trim();
        String tgt = targetStatus.toUpperCase().trim();

        if (ExchangeRequest.STATUS_PENDING.equals(cur)) {
            return ExchangeRequest.STATUS_ACCEPTED.equals(tgt)
                || ExchangeRequest.STATUS_REJECTED.equals(tgt)
                || ExchangeRequest.STATUS_CANCELLED.equals(tgt);
        }
        if (ExchangeRequest.STATUS_ACCEPTED.equals(cur)) {
            return ExchangeRequest.STATUS_COMPLETED.equals(tgt)
                || ExchangeRequest.STATUS_CANCELLED.equals(tgt);
        }
        // Terminal states: REJECTED, COMPLETED, CANCELLED
        return false;
    }

    /**
     * Creates and submits a new skill exchange request.
     */
    public ExchangeRequest createExchangeRequest(int requesterId, int receiverId, 
                                                int offeredSkillId, int wantedSkillId) throws SQLException {
        // 1. Prevent self-exchange
        if (requesterId == receiverId) {
            throw new IllegalArgumentException("Cannot send exchange request to yourself. Requester and receiver must be different students.");
        }

        // 2. Validate students exist
        Optional<Student> requesterOpt = studentDAO.findById(requesterId);
        if (requesterOpt.isEmpty()) {
            throw new IllegalArgumentException("Requester with Student ID " + requesterId + " does not exist.");
        }
        Optional<Student> receiverOpt = studentDAO.findById(receiverId);
        if (receiverOpt.isEmpty()) {
            throw new IllegalArgumentException("Receiver with Student ID " + receiverId + " does not exist.");
        }

        // 3. Validate offered skill ownership and type
        Optional<Skill> offeredOpt = skillDAO.findById(offeredSkillId);
        if (offeredOpt.isEmpty()) {
            throw new IllegalArgumentException("Offered skill with ID " + offeredSkillId + " does not exist.");
        }
        Skill offeredSkill = offeredOpt.get();
        if (offeredSkill.getStudentId() != requesterId) {
            throw new IllegalArgumentException("Ownership validation failed: Offered skill #" + offeredSkillId + 
                " does not belong to requester #" + requesterId);
        }
        if (!Skill.TYPE_TEACH.equalsIgnoreCase(offeredSkill.getSkillType())) {
            throw new IllegalArgumentException("Skill type error: Offered skill must be a TEACH skill, but was " + offeredSkill.getSkillType());
        }

        // 4. Validate wanted skill ownership and type
        Optional<Skill> wantedOpt = skillDAO.findById(wantedSkillId);
        if (wantedOpt.isEmpty()) {
            throw new IllegalArgumentException("Wanted skill with ID " + wantedSkillId + " does not exist.");
        }
        Skill wantedSkill = wantedOpt.get();
        if (wantedSkill.getStudentId() != receiverId) {
            throw new IllegalArgumentException("Ownership validation failed: Wanted skill #" + wantedSkillId + 
                " does not belong to receiver #" + receiverId);
        }
        if (!Skill.TYPE_TEACH.equalsIgnoreCase(wantedSkill.getSkillType())) {
            throw new IllegalArgumentException("Skill type error: Wanted skill must be a TEACH skill of the receiver, but was " + wantedSkill.getSkillType());
        }

        // 5. Check both students' LEARN preferences to confirm reciprocity alignment
        List<Skill> requesterSkills = skillDAO.findByStudentId(requesterId);
        boolean requesterWantsThis = requesterSkills.stream()
                .anyMatch(s -> Skill.TYPE_LEARN.equalsIgnoreCase(s.getSkillType()) 
                        && s.getSkillName().trim().equalsIgnoreCase(wantedSkill.getSkillName().trim()));

        List<Skill> receiverSkills = skillDAO.findByStudentId(receiverId);
        boolean receiverWantsThis = receiverSkills.stream()
                .anyMatch(s -> Skill.TYPE_LEARN.equalsIgnoreCase(s.getSkillType()) 
                        && s.getSkillName().trim().equalsIgnoreCase(offeredSkill.getSkillName().trim()));

        if (!requesterWantsThis) {
            System.out.println("  [Notice] Requester #" + requesterId + " does not have an explicit LEARN record for '" + wantedSkill.getSkillName() + "'.");
        }
        if (!receiverWantsThis) {
            System.out.println("  [Notice] Receiver #" + receiverId + " does not have an explicit LEARN record for '" + offeredSkill.getSkillName() + "'.");
        }

        // 6. Check for duplicate active requests
        if (requestDAO.hasActiveRequest(requesterId, receiverId, offeredSkillId, wantedSkillId)) {
            throw new IllegalStateException("An active exchange request (PENDING or ACCEPTED) already exists for this exact skill exchange.");
        }

        // 7. Persist exchange request
        ExchangeRequest request = new ExchangeRequest(requesterId, receiverId, offeredSkillId, wantedSkillId);
        int generatedId = requestDAO.create(request);
        request.setRequestId(generatedId);
        return request;
    }

    /**
     * Retrieves an exchange request by its primary key.
     */
    public Optional<ExchangeRequest> getRequestById(int requestId) throws SQLException {
        if (requestId <= 0) {
            throw new IllegalArgumentException("Request ID must be a positive integer.");
        }
        return requestDAO.findById(requestId);
    }

    /**
     * Lists requests initiated by a specific student.
     */
    public List<ExchangeRequest> getSentRequests(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        return requestDAO.findByRequesterId(studentId);
    }

    /**
     * Lists requests received by a specific student.
     */
    public List<ExchangeRequest> getReceivedRequests(int studentId) throws SQLException {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        return requestDAO.findByReceiverId(studentId);
    }

    /**
     * Accepts a PENDING request by its receiver.
     */
    public boolean acceptRequest(int requestId, int actingStudentId) throws SQLException {
        ExchangeRequest request = fetchAndValidateRequest(requestId);

        if (request.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Authorization error: Only the receiver (Student #" + 
                request.getReceiverId() + ") can accept this request.");
        }

        if (!isValidTransition(request.getStatus(), ExchangeRequest.STATUS_ACCEPTED)) {
            throw new IllegalStateException("Cannot accept request #" + requestId + 
                " because its current status is " + request.getStatus() + " (Must be PENDING).");
        }

        return requestDAO.updateStatus(requestId, ExchangeRequest.STATUS_ACCEPTED);
    }

    /**
     * Rejects a PENDING request by its receiver.
     */
    public boolean rejectRequest(int requestId, int actingStudentId) throws SQLException {
        ExchangeRequest request = fetchAndValidateRequest(requestId);

        if (request.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Authorization error: Only the receiver (Student #" + 
                request.getReceiverId() + ") can reject this request.");
        }

        if (!isValidTransition(request.getStatus(), ExchangeRequest.STATUS_REJECTED)) {
            throw new IllegalStateException("Cannot reject request #" + requestId + 
                " because its current status is " + request.getStatus() + " (Must be PENDING).");
        }

        return requestDAO.updateStatus(requestId, ExchangeRequest.STATUS_REJECTED);
    }

    /**
     * Cancels an eligible PENDING or ACCEPTED request by either participant.
     */
    public boolean cancelRequest(int requestId, int actingStudentId) throws SQLException {
        ExchangeRequest request = fetchAndValidateRequest(requestId);

        if (request.getRequesterId() != actingStudentId && request.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Authorization error: Only participants (Student #" + 
                request.getRequesterId() + " or #" + request.getReceiverId() + ") can cancel this request.");
        }

        if (!isValidTransition(request.getStatus(), ExchangeRequest.STATUS_CANCELLED)) {
            throw new IllegalStateException("Cannot cancel request #" + requestId + 
                " because its current status is " + request.getStatus() + " (Must be PENDING or ACCEPTED).");
        }

        return requestDAO.updateStatus(requestId, ExchangeRequest.STATUS_CANCELLED);
    }

    /**
     * Marks an ACCEPTED request as COMPLETED by either participant.
     */
    public boolean completeRequest(int requestId, int actingStudentId) throws SQLException {
        ExchangeRequest request = fetchAndValidateRequest(requestId);

        if (request.getRequesterId() != actingStudentId && request.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Authorization error: Only participants (Student #" + 
                request.getRequesterId() + " or #" + request.getReceiverId() + ") can mark this exchange as completed.");
        }

        if (!isValidTransition(request.getStatus(), ExchangeRequest.STATUS_COMPLETED)) {
            throw new IllegalStateException("Cannot complete request #" + requestId + 
                " because its current status is " + request.getStatus() + " (Must be ACCEPTED).");
        }

        return requestDAO.updateStatus(requestId, ExchangeRequest.STATUS_COMPLETED);
    }

    private ExchangeRequest fetchAndValidateRequest(int requestId) throws SQLException {
        if (requestId <= 0) {
            throw new IllegalArgumentException("Request ID must be a positive integer.");
        }
        Optional<ExchangeRequest> opt = requestDAO.findById(requestId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Exchange request #" + requestId + " not found.");
        }
        return opt.get();
    }
}
