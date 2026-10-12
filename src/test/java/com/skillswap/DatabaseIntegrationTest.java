package com.skillswap;

import com.skillswap.dao.ExchangeRequestDAO;
import com.skillswap.dao.SkillDAO;
import com.skillswap.dao.StudentDAO;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.Student;
import com.skillswap.service.ExchangeRequestService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseIntegrationTest {

    private static boolean dbAvailable = false;
    private static StudentService studentService;
    private static SkillService skillService;
    private static SkillMatchingService matchingService;
    private static ExchangeRequestService requestService;

    private static int studentAId = 0;
    private static int studentBId = 0;
    private static int skillTeachAId = 0;
    private static int skillLearnAId = 0;
    private static int skillTeachBId = 0;
    private static int skillLearnBId = 0;
    private static int exchangeRequestId = 0;

    @BeforeAll
    static void checkDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                dbAvailable = true;
                studentService = new StudentService();
                skillService = new SkillService();
                matchingService = new SkillMatchingService();
                requestService = new ExchangeRequestService();
                System.out.println("[DatabaseIntegrationTest] Connected to live MySQL database. Running full integration suite.");
            }
        } catch (SQLException e) {
            System.out.println("[DatabaseIntegrationTest] MySQL database is not accessible: " + e.getMessage());
            System.out.println("[DatabaseIntegrationTest] Live database integration tests will be skipped until DB_PASSWORD is configured.");
            dbAvailable = false;
        }
    }

    @Test
    @Order(1)
    @DisplayName("DB: Register students and test duplicate email rejection")
    void testRegisterStudentsAndDuplicateEmail() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String emailA = "test_arun_" + uniqueSuffix + "@skillswap.test";
        String emailB = "test_priya_" + uniqueSuffix + "@skillswap.test";

        Student sA = studentService.registerStudent("Test Arun", emailA, "Discord: @testarun");
        assertNotNull(sA);
        assertTrue(sA.getStudentId() > 0);
        studentAId = sA.getStudentId();

        Student sB = studentService.registerStudent("Test Priya", emailB, "Phone: 9999999999");
        assertNotNull(sB);
        assertTrue(sB.getStudentId() > 0);
        studentBId = sB.getStudentId();

        // Test duplicate email rejection
        assertThrows(IllegalArgumentException.class, () -> {
            studentService.registerStudent("Duplicate Arun", emailA, "other");
        });
    }

    @Test
    @Order(2)
    @DisplayName("DB: Add TEACH and LEARN skills and search skills")
    void testAddAndSearchSkills() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        // Student A: Teaches "Kotlin", Wants to Learn "Graphic Design"
        Skill teachA = skillService.addTeachSkill(studentAId, "Kotlin", "ADVANCED", "Modern Kotlin development");
        skillTeachAId = teachA.getSkillId();
        assertTrue(skillTeachAId > 0);

        Skill learnA = skillService.addLearnSkill(studentAId, "Graphic Design", "BEGINNER", "Photoshop and Figma");
        skillLearnAId = learnA.getSkillId();
        assertTrue(skillLearnAId > 0);

        // Student B: Teaches "Graphic Design", Wants to Learn "Kotlin"
        Skill teachB = skillService.addTeachSkill(studentBId, "Graphic Design", "INTERMEDIATE", "Vector art and layout");
        skillTeachBId = teachB.getSkillId();
        assertTrue(skillTeachBId > 0);

        Skill learnB = skillService.addLearnSkill(studentBId, "Kotlin", "BEGINNER", "Android app coding");
        skillLearnBId = learnB.getSkillId();
        assertTrue(skillLearnBId > 0);

        // Test search
        List<Skill> searchResults = skillService.searchSkillsByName("Kot");
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(s -> s.getSkillName().equalsIgnoreCase("Kotlin")));
    }

    @Test
    @Order(3)
    @DisplayName("DB: Smart Skill Matching identifies reciprocal pair")
    void testSmartSkillMatching() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        List<SkillMatch> matches = matchingService.findMatchesForStudent(studentAId);
        assertNotNull(matches);
        boolean foundB = matches.stream().anyMatch(m -> m.getPartnerStudentId() == studentBId);
        assertTrue(foundB, "Matching service should identify Student B as a mutual exchange partner for Student A");
    }

    @Test
    @Order(4)
    @DisplayName("DB: Send Exchange Request and verify PENDING status")
    void testSendExchangeRequest() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        ExchangeRequest req = requestService.createExchangeRequest(
                studentAId, studentBId, skillTeachAId, skillTeachBId
        );
        assertNotNull(req);
        assertTrue(req.getRequestId() > 0);
        assertEquals(ExchangeRequest.STATUS_PENDING, req.getStatus());
        exchangeRequestId = req.getRequestId();

        // Prevent duplicate active request
        assertThrows(IllegalStateException.class, () -> {
            requestService.createExchangeRequest(studentAId, studentBId, skillTeachAId, skillTeachBId);
        });

        // Prevent self exchange
        assertThrows(IllegalArgumentException.class, () -> {
            requestService.createExchangeRequest(studentAId, studentAId, skillTeachAId, skillTeachAId);
        });
    }

    @Test
    @Order(5)
    @DisplayName("DB: Accept request by receiver and verify status transition")
    void testAcceptRequest() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        // Only receiver (studentB) can accept
        assertThrows(IllegalArgumentException.class, () -> {
            requestService.acceptRequest(exchangeRequestId, studentAId); // Student A is requester, should fail
        });

        boolean accepted = requestService.acceptRequest(exchangeRequestId, studentBId);
        assertTrue(accepted);

        Optional<ExchangeRequest> updated = requestService.getRequestById(exchangeRequestId);
        assertTrue(updated.isPresent());
        assertEquals(ExchangeRequest.STATUS_ACCEPTED, updated.get().getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("DB: Mark accepted request as COMPLETED")
    void testCompleteRequest() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        boolean completed = requestService.completeRequest(exchangeRequestId, studentAId);
        assertTrue(completed);

        Optional<ExchangeRequest> updated = requestService.getRequestById(exchangeRequestId);
        assertTrue(updated.isPresent());
        assertEquals(ExchangeRequest.STATUS_COMPLETED, updated.get().getStatus());

        // Cannot cancel an already COMPLETED request
        assertThrows(IllegalStateException.class, () -> {
            requestService.cancelRequest(exchangeRequestId, studentAId);
        });
    }

    @Test
    @Order(7)
    @DisplayName("DB: Clean up test students and verify cascading delete")
    void testCleanUpAndCascadingDelete() throws SQLException {
        assumeTrue(dbAvailable, "MySQL is not accessible. Skipping live DB test.");

        if (studentAId > 0) {
            studentService.deleteStudent(studentAId);
            assertTrue(studentService.getStudentById(studentAId).isEmpty());
        }
        if (studentBId > 0) {
            studentService.deleteStudent(studentBId);
            assertTrue(studentService.getStudentById(studentBId).isEmpty());
        }
    }
}
