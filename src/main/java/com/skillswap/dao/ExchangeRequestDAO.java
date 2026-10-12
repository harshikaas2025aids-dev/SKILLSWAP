package com.skillswap.dao;

import com.skillswap.DBConnection;
import com.skillswap.model.ExchangeRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for SKILL_EXCHANGE_REQUESTS using JDBC PreparedStatement.
 */
public class ExchangeRequestDAO {

    private static final String BASE_JOIN_QUERY = 
        "SELECT r.request_id, r.requester_id, r.receiver_id, r.offered_skill_id, r.wanted_skill_id, " +
        "       r.request_date, r.status, " +
        "       s_req.student_name AS requester_name, s_req.email AS requester_email, " +
        "       s_rec.student_name AS receiver_name, s_rec.email AS receiver_email, " +
        "       sk_off.skill_name AS offered_skill_name, sk_off.proficiency_level AS offered_proficiency, " +
        "       sk_wnt.skill_name AS wanted_skill_name, sk_wnt.proficiency_level AS wanted_proficiency " +
        "FROM skill_exchange_requests r " +
        "JOIN students s_req ON r.requester_id = s_req.student_id " +
        "JOIN students s_rec ON r.receiver_id = s_rec.student_id " +
        "JOIN skills sk_off ON r.offered_skill_id = sk_off.skill_id " +
        "JOIN skills sk_wnt ON r.wanted_skill_id = sk_wnt.skill_id ";

    /**
     * Inserts a new exchange request and returns the generated request_id.
     */
    public int create(ExchangeRequest request) throws SQLException {
        String sql = "INSERT INTO skill_exchange_requests " +
                     "(requester_id, receiver_id, offered_skill_id, wanted_skill_id, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, request.getRequesterId());
            ps.setInt(2, request.getReceiverId());
            ps.setInt(3, request.getOfferedSkillId());
            ps.setInt(4, request.getWantedSkillId());
            ps.setString(5, request.getStatus() != null ? request.getStatus().trim().toUpperCase() : ExchangeRequest.STATUS_PENDING);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating exchange request failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    request.setRequestId(id);
                    return id;
                } else {
                    throw new SQLException("Creating exchange request failed, no generated ID obtained.");
                }
            }
        }
    }

    /**
     * Finds an exchange request by ID with all joined details.
     */
    public Optional<ExchangeRequest> findById(int requestId) throws SQLException {
        String sql = BASE_JOIN_QUERY + "WHERE r.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRequest(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all requests sent by a specific student.
     */
    public List<ExchangeRequest> findByRequesterId(int requesterId) throws SQLException {
        List<ExchangeRequest> list = new ArrayList<>();
        String sql = BASE_JOIN_QUERY + "WHERE r.requester_id = ? ORDER BY r.request_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds all requests received by a specific student.
     */
    public List<ExchangeRequest> findByReceiverId(int receiverId) throws SQLException {
        List<ExchangeRequest> list = new ArrayList<>();
        String sql = BASE_JOIN_QUERY + "WHERE r.receiver_id = ? ORDER BY r.request_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRequest(rs));
                }
            }
        }
        return list;
    }

    /**
     * Updates the status of an exchange request.
     */
    public boolean updateStatus(int requestId, String newStatus) throws SQLException {
        String sql = "UPDATE skill_exchange_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus.trim().toUpperCase());
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks if an active (PENDING or ACCEPTED) request already exists between the two students for these skills.
     */
    public boolean hasActiveRequest(int requesterId, int receiverId, int offeredSkillId, int wantedSkillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skill_exchange_requests " +
                     "WHERE requester_id = ? AND receiver_id = ? " +
                     "  AND offered_skill_id = ? AND wanted_skill_id = ? " +
                     "  AND status IN ('PENDING', 'ACCEPTED')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requesterId);
            ps.setInt(2, receiverId);
            ps.setInt(3, offeredSkillId);
            ps.setInt(4, wantedSkillId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private ExchangeRequest mapRowToRequest(ResultSet rs) throws SQLException {
        ExchangeRequest req = new ExchangeRequest(
            rs.getInt("request_id"),
            rs.getInt("requester_id"),
            rs.getInt("receiver_id"),
            rs.getInt("offered_skill_id"),
            rs.getInt("wanted_skill_id"),
            rs.getTimestamp("request_date"),
            rs.getString("status")
        );
        req.setRequesterName(rs.getString("requester_name"));
        req.setRequesterEmail(rs.getString("requester_email"));
        req.setReceiverName(rs.getString("receiver_name"));
        req.setReceiverEmail(rs.getString("receiver_email"));
        req.setOfferedSkillName(rs.getString("offered_skill_name"));
        req.setOfferedSkillProficiency(rs.getString("offered_proficiency"));
        req.setWantedSkillName(rs.getString("wanted_skill_name"));
        req.setWantedSkillProficiency(rs.getString("wanted_proficiency"));
        return req;
    }
}
