package com.skillswap;

import com.skillswap.service.SkillMatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SkillMatchingServiceTest {

    @Test
    @DisplayName("Exact reciprocal match should be recognized")
    void testReciprocalMatchExact() {
        // Arun: teaches Java, wants UI Design
        // Priya: teaches UI Design, wants Java
        boolean isMatch = SkillMatchingService.isReciprocalMatch("Java", "UI Design", "UI Design", "Java");
        assertTrue(isMatch, "Arun and Priya should form a mutual reciprocal skill match");
    }

    @Test
    @DisplayName("Case-insensitive and whitespace-padded skill names should match")
    void testReciprocalMatchCaseAndTrimInsensitive() {
        // Different casing and extra whitespaces
        boolean isMatch = SkillMatchingService.isReciprocalMatch("  jAvA  ", "  ui design ", "UI DESIGN", "java");
        assertTrue(isMatch, "Case-insensitive and trimmed names must match");
    }

    @Test
    @DisplayName("One-way interest should NOT form a reciprocal match")
    void testOneWayNonMatch() {
        // Arun teaches Java, wants UI Design
        // Sneha teaches UI Design, but wants Python (not Java)
        boolean isMatch = SkillMatchingService.isReciprocalMatch("Java", "UI Design", "UI Design", "Python");
        assertFalse(isMatch, "One-way match must not be recognized as a mutual reciprocal exchange");
    }

    @Test
    @DisplayName("Null skill parameters should return false safely")
    void testNullSkillNames() {
        assertFalse(SkillMatchingService.isReciprocalMatch(null, "Java", "Java", "Python"));
        assertFalse(SkillMatchingService.isReciprocalMatch("Java", null, "Java", "Python"));
        assertFalse(SkillMatchingService.isReciprocalMatch("Java", "Python", null, "Python"));
        assertFalse(SkillMatchingService.isReciprocalMatch("Java", "Python", "Java", null));
    }
}
