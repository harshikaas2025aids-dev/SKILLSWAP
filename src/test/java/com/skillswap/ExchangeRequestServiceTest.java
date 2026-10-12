package com.skillswap;

import com.skillswap.model.ExchangeRequest;
import com.skillswap.service.ExchangeRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeRequestServiceTest {

    @Test
    @DisplayName("Valid transitions from PENDING should succeed")
    void testValidTransitionsFromPending() {
        assertTrue(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_PENDING, ExchangeRequest.STATUS_ACCEPTED));
        assertTrue(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_PENDING, ExchangeRequest.STATUS_REJECTED));
        assertTrue(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_PENDING, ExchangeRequest.STATUS_CANCELLED));
    }

    @Test
    @DisplayName("Valid transitions from ACCEPTED should succeed")
    void testValidTransitionsFromAccepted() {
        assertTrue(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_ACCEPTED, ExchangeRequest.STATUS_COMPLETED));
        assertTrue(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_ACCEPTED, ExchangeRequest.STATUS_CANCELLED));
    }

    @Test
    @DisplayName("Invalid transitions from PENDING should be rejected")
    void testInvalidTransitionsFromPending() {
        // Cannot directly complete without accepting first
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_PENDING, ExchangeRequest.STATUS_COMPLETED));
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_PENDING, "UNKNOWN_STATUS"));
    }

    @Test
    @DisplayName("Terminal states cannot transition to any new status")
    void testTerminalStatesCannotTransition() {
        // REJECTED
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_REJECTED, ExchangeRequest.STATUS_ACCEPTED));
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_REJECTED, ExchangeRequest.STATUS_COMPLETED));

        // CANCELLED
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_CANCELLED, ExchangeRequest.STATUS_ACCEPTED));
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_CANCELLED, ExchangeRequest.STATUS_COMPLETED));

        // COMPLETED
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_COMPLETED, ExchangeRequest.STATUS_ACCEPTED));
        assertFalse(ExchangeRequestService.isValidTransition(ExchangeRequest.STATUS_COMPLETED, ExchangeRequest.STATUS_CANCELLED));
    }

    @Test
    @DisplayName("ExchangeRequest model initializes with PENDING status")
    void testExchangeRequestDefaultStatus() {
        ExchangeRequest req = new ExchangeRequest(1, 2, 10, 20);
        assertEquals(ExchangeRequest.STATUS_PENDING, req.getStatus());
        assertEquals(1, req.getRequesterId());
        assertEquals(2, req.getReceiverId());
    }
}
