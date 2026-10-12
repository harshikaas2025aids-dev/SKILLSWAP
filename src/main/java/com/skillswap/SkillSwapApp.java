package com.skillswap;

import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Skill;
import com.skillswap.model.SkillMatch;
import com.skillswap.model.Student;
import com.skillswap.service.ExchangeRequestService;
import com.skillswap.service.SkillMatchingService;
import com.skillswap.service.SkillService;
import com.skillswap.service.StudentService;
import com.skillswap.util.InputUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Main console-driven application for SkillSwap – Student Skill Exchange.
 */
public class SkillSwapApp {

    private final StudentService studentService;
    private final SkillService skillService;
    private final SkillMatchingService matchingService;
    private final ExchangeRequestService requestService;

    public SkillSwapApp() {
        this.studentService = new StudentService();
        this.skillService = new SkillService();
        this.matchingService = new SkillMatchingService();
        this.requestService = new ExchangeRequestService();
    }

    public static void main(String[] args) {
        SkillSwapApp app = new SkillSwapApp();
        app.run();
    }

    public void run() {
        printWelcomeBanner();

        // Check database connection on startup
        boolean dbReady = DBConnection.testConnection();
        if (!dbReady) {
            System.out.println("\n[!] WARNING: Could not connect to the MySQL database.");
            System.out.println("    Current DB URL: " + DBConnection.getDbUrl());
            System.out.println("    Current DB User: " + DBConnection.getDbUser());
            System.out.println("    Tip: Set the DB_PASSWORD environment variable or configure MySQL.");
            boolean retry = InputUtil.readConfirmation("    Would you like to manually enter database credentials now?");
            if (retry) {
                configureDatabaseManually();
            }
        }

        boolean running = true;
        while (running) {
            if (!InputUtil.hasMoreInput()) {
                System.out.println("\nExiting SkillSwap. Goodbye!");
                break;
            }
            displayMenu();
            int choice = InputUtil.readInt("Enter your choice (0-19): ");
            System.out.println();

            try {
                switch (choice) {
                    case 1 -> registerStudent();
                    case 2 -> viewStudent();
                    case 3 -> viewAllStudents();
                    case 4 -> updateStudent();
                    case 5 -> deleteStudent();
                    case 6 -> addSkill();
                    case 7 -> viewStudentSkills();
                    case 8 -> searchSkills();
                    case 9 -> updateSkill();
                    case 10 -> deleteSkill();
                    case 11 -> findSmartSkillMatches();
                    case 12 -> sendExchangeRequest();
                    case 13 -> viewSentRequests();
                    case 14 -> viewReceivedRequests();
                    case 15 -> acceptRequest();
                    case 16 -> rejectRequest();
                    case 17 -> cancelRequest();
                    case 18 -> completeExchange();
                    case 19 -> viewRequestDetails();
                    case 0 -> {
                        System.out.println("Thank you for using SkillSwap! Goodbye.");
                        running = false;
                    }
                    default -> System.out.println("[!] Invalid choice. Please select an option between 0 and 19.");
                }
            } catch (SQLException e) {
                System.err.println(" [Database Error] " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(" [Validation Error] " + e.getMessage());
            } catch (Exception e) {
                System.err.println(" [Unexpected Error] " + e.getMessage());
            }

            if (running) {
                InputUtil.pressEnterToContinue();
            }
        }
    }

    private void configureDatabaseManually() {
        String url = InputUtil.readString("Enter JDBC URL [Press Enter for default: " + DBConnection.getDbUrl() + "]: ");
        if (url.isEmpty()) url = DBConnection.getDbUrl();

        String user = InputUtil.readString("Enter DB Username [default 'root']: ");
        if (user.isEmpty()) user = "root";

        String pass = InputUtil.readOptionalString("Enter DB Password: ");

        DBConnection.configure(url, user, pass);
        DBConnection.testConnection();
    }

    private void printWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println("       SKILLSWAP - STUDENT SKILL EXCHANGE PLATFORM (JAVA & MYSQL)               ");
        System.out.println("       Learn together. Teach each other. Zero course fees.                      ");
        System.out.println("================================================================================");
    }

    private void displayMenu() {
        System.out.println("\n===== SKILLSWAP – STUDENT SKILL EXCHANGE =====");
        System.out.println(" 1. Register Student");
        System.out.println(" 2. View Student");
        System.out.println(" 3. View All Students");
        System.out.println(" 4. Update Student");
        System.out.println(" 5. Delete Student");
        System.out.println(" 6. Add Skill");
        System.out.println(" 7. View Student Skills");
        System.out.println(" 8. Search Skills");
        System.out.println(" 9. Update Skill");
        System.out.println("10. Delete Skill");
        System.out.println("11. Find Smart Skill Matches");
        System.out.println("12. Send Exchange Request");
        System.out.println("13. View Sent Requests");
        System.out.println("14. View Received Requests");
        System.out.println("15. Accept Request");
        System.out.println("16. Reject Request");
        System.out.println("17. Cancel Request");
        System.out.println("18. Complete Exchange");
        System.out.println("19. View Request Details");
        System.out.println(" 0. Exit");
        System.out.println("==============================================");
    }

    // -------------------------------------------------------------------------
    // Student Handlers
    // -------------------------------------------------------------------------

    private void registerStudent() throws SQLException {
        System.out.println("--- Register New Student ---");
        String name = InputUtil.readString("Enter Student Full Name: ");
        String email = InputUtil.readString("Enter Student Email Address: ");
        String contact = InputUtil.readOptionalString("Enter Contact Info (phone / discord / linkedin): ");

        Student registered = studentService.registerStudent(name, email, contact);
        System.out.println("\n[SUCCESS] Student registered successfully!");
        System.out.printf("  Assigned Student ID : %d%n", registered.getStudentId());
        System.out.printf("  Name                : %s%n", registered.getStudentName());
        System.out.printf("  Email               : %s%n", registered.getEmail());
        System.out.printf("  Contact Info        : %s%n", registered.getContactInfo() != null ? registered.getContactInfo() : "N/A");
    }

    private void viewStudent() throws SQLException {
        System.out.println("--- View Student Details ---");
        int id = InputUtil.readInt("Enter Student ID: ");
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            System.out.println("[!] No student found with ID: " + id);
            return;
        }
        Student s = studentOpt.get();
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("| ID: %-5d | Name: %-25s | Email: %-30s |%n", s.getStudentId(), s.getStudentName(), s.getEmail());
        System.out.printf("| Contact Info: %-62s |%n", (s.getContactInfo() != null ? s.getContactInfo() : "None"));
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private void viewAllStudents() throws SQLException {
        System.out.println("--- All Registered Students ---");
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
            return;
        }

        System.out.printf("%-6s | %-24s | %-30s | %-20s%n", "ID", "Name", "Email", "Contact Info");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Student s : students) {
            System.out.printf("%-6d | %-24s | %-30s | %-20s%n",
                    s.getStudentId(),
                    s.getStudentName(),
                    s.getEmail(),
                    (s.getContactInfo() != null ? s.getContactInfo() : "-"));
        }
    }

    private void updateStudent() throws SQLException {
        System.out.println("--- Update Student Details ---");
        int id = InputUtil.readInt("Enter Student ID to update: ");
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            System.out.println("[!] Student with ID " + id + " not found.");
            return;
        }
        Student current = studentOpt.get();
        System.out.println("Current Name: " + current.getStudentName());
        System.out.println("Current Email: " + current.getEmail());
        System.out.println("Current Contact: " + current.getContactInfo());

        String newName = InputUtil.readString("Enter New Name: ");
        String newEmail = InputUtil.readString("Enter New Email: ");
        String newContact = InputUtil.readOptionalString("Enter New Contact Info: ");

        boolean updated = studentService.updateStudent(id, newName, newEmail, newContact);
        if (updated) {
            System.out.println("[SUCCESS] Student #" + id + " details updated successfully.");
        } else {
            System.out.println("[!] Update failed.");
        }
    }

    private void deleteStudent() throws SQLException {
        System.out.println("--- Delete Student ---");
        int id = InputUtil.readInt("Enter Student ID to delete: ");
        Optional<Student> studentOpt = studentService.getStudentById(id);
        if (studentOpt.isEmpty()) {
            System.out.println("[!] Student with ID " + id + " not found.");
            return;
        }

        System.out.println("WARNING: Deleting student '" + studentOpt.get().getStudentName() + 
                "' will also remove their registered skills and exchange requests.");
        boolean confirm = InputUtil.readConfirmation("Are you sure you want to delete this student?");
        if (confirm) {
            boolean deleted = studentService.deleteStudent(id);
            if (deleted) {
                System.out.println("[SUCCESS] Student #" + id + " and associated records deleted successfully.");
            } else {
                System.out.println("[!] Failed to delete student.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // -------------------------------------------------------------------------
    // Skill Handlers
    // -------------------------------------------------------------------------

    private void addSkill() throws SQLException {
        System.out.println("--- Add Student Skill ---");
        int studentId = InputUtil.readInt("Enter Student ID: ");
        Optional<Student> studentOpt = studentService.getStudentById(studentId);
        if (studentOpt.isEmpty()) {
            System.out.println("[!] Student with ID " + studentId + " does not exist.");
            return;
        }

        System.out.println("Student: " + studentOpt.get().getStudentName());
        String skillType = InputUtil.readChoice("Select Skill Type (TEACH / LEARN): ", "TEACH", "LEARN");
        String skillName = InputUtil.readString("Enter Skill Name (e.g. Java, UI Design, Python): ");
        String proficiency = InputUtil.readChoice("Select Proficiency Level (BEGINNER / INTERMEDIATE / ADVANCED): ", 
                "BEGINNER", "INTERMEDIATE", "ADVANCED");
        String description = InputUtil.readOptionalString("Enter Brief Description: ");

        Skill created = skillService.addSkill(studentId, skillName, skillType, proficiency, description);
        System.out.println("\n[SUCCESS] Skill registered successfully!");
        System.out.printf("  Skill ID    : %d%n", created.getSkillId());
        System.out.printf("  Skill Name  : %s%n", created.getSkillName());
        System.out.printf("  Type        : %s%n", created.getSkillType());
        System.out.printf("  Proficiency : %s%n", created.getProficiencyLevel());
    }

    private void viewStudentSkills() throws SQLException {
        System.out.println("--- View Student Skills ---");
        int studentId = InputUtil.readInt("Enter Student ID: ");
        List<Skill> skills = skillService.getSkillsByStudentId(studentId);
        if (skills.isEmpty()) {
            System.out.println("No skills registered for Student #" + studentId);
            return;
        }

        System.out.printf("%-8s | %-20s | %-8s | %-14s | %-25s%n", "Skill ID", "Skill Name", "Type", "Proficiency", "Description");
        System.out.println("----------------------------------------------------------------------------------------");
        for (Skill sk : skills) {
            System.out.printf("%-8d | %-20s | %-8s | %-14s | %-25s%n",
                    sk.getSkillId(), sk.getSkillName(), sk.getSkillType(), sk.getProficiencyLevel(),
                    (sk.getDescription() != null ? sk.getDescription() : "-"));
        }
    }

    private void searchSkills() throws SQLException {
        System.out.println("--- Search and Filter Skills ---");
        System.out.println("1. Search by Skill Name Keyword");
        System.out.println("2. Filter by Type (TEACH / LEARN)");
        System.out.println("3. Filter by Proficiency Level");
        System.out.println("4. View All Skills");
        int option = InputUtil.readIntInRange("Choose filter option (1-4): ", 1, 4);

        List<Skill> results;
        switch (option) {
            case 1 -> {
                String kw = InputUtil.readString("Enter skill keyword to search: ");
                results = skillService.searchSkillsByName(kw);
            }
            case 2 -> {
                String type = InputUtil.readChoice("Select Type (TEACH / LEARN): ", "TEACH", "LEARN");
                results = skillService.filterSkillsByType(type);
            }
            case 3 -> {
                String prof = InputUtil.readChoice("Select Proficiency (BEGINNER / INTERMEDIATE / ADVANCED): ", 
                        "BEGINNER", "INTERMEDIATE", "ADVANCED");
                results = skillService.filterSkillsByProficiency(prof);
            }
            default -> results = skillService.getAllSkills();
        }

        if (results.isEmpty()) {
            System.out.println("No skills matched your search criteria.");
            return;
        }

        System.out.printf("%-8s | %-18s | %-6s | %-12s | %-18s | %-20s%n", 
                "Skill ID", "Skill Name", "Type", "Proficiency", "Student Name", "Student Email");
        System.out.println("------------------------------------------------------------------------------------------------");
        for (Skill s : results) {
            System.out.printf("%-8d | %-18s | %-6s | %-12s | %-18s | %-20s%n",
                    s.getSkillId(), s.getSkillName(), s.getSkillType(), s.getProficiencyLevel(),
                    s.getStudentName(), s.getStudentEmail());
        }
    }

    private void updateSkill() throws SQLException {
        System.out.println("--- Update Skill ---");
        int skillId = InputUtil.readInt("Enter Skill ID: ");
        int studentId = InputUtil.readInt("Enter Student ID (Ownership Check): ");

        Optional<Skill> opt = skillService.getSkillById(skillId);
        if (opt.isEmpty()) {
            System.out.println("[!] Skill #" + skillId + " not found.");
            return;
        }

        Skill s = opt.get();
        System.out.printf("Current: %s [%s | %s]%n", s.getSkillName(), s.getSkillType(), s.getProficiencyLevel());

        String newName = InputUtil.readString("Enter New Skill Name: ");
        String newType = InputUtil.readChoice("Enter New Type (TEACH / LEARN): ", "TEACH", "LEARN");
        String newProf = InputUtil.readChoice("Enter New Proficiency (BEGINNER / INTERMEDIATE / ADVANCED): ",
                "BEGINNER", "INTERMEDIATE", "ADVANCED");
        String newDesc = InputUtil.readOptionalString("Enter New Description: ");

        boolean ok = skillService.updateSkill(skillId, studentId, newName, newType, newProf, newDesc);
        if (ok) {
            System.out.println("[SUCCESS] Skill #" + skillId + " updated successfully.");
        } else {
            System.out.println("[!] Skill update failed.");
        }
    }

    private void deleteSkill() throws SQLException {
        System.out.println("--- Delete Skill ---");
        int skillId = InputUtil.readInt("Enter Skill ID: ");
        int studentId = InputUtil.readInt("Enter Student ID: ");

        boolean confirm = InputUtil.readConfirmation("Are you sure you want to delete Skill #" + skillId + "?");
        if (confirm) {
            boolean ok = skillService.deleteSkill(skillId, studentId);
            if (ok) {
                System.out.println("[SUCCESS] Skill #" + skillId + " deleted successfully.");
            } else {
                System.out.println("[!] Deletion failed.");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Smart Matching Handler
    // -------------------------------------------------------------------------

    private void findSmartSkillMatches() throws SQLException {
        System.out.println("--- Smart Skill Matching ---");
        System.out.println("1. Find Mutual Matches for a Specific Student");
        System.out.println("2. View All Mutual Matches Platform-Wide");
        int mode = InputUtil.readIntInRange("Choose mode (1 or 2): ", 1, 2);

        List<SkillMatch> matches;
        int activeStudentId = 0;

        if (mode == 1) {
            activeStudentId = InputUtil.readInt("Enter Student ID: ");
            matches = matchingService.findMatchesForStudent(activeStudentId);
        } else {
            matches = matchingService.findAllMatches();
        }

        if (matches.isEmpty()) {
            System.out.println("No reciprocal skill matches found at this time.");
            System.out.println("Tip: Add more TEACH and LEARN skills to unlock matches!");
            return;
        }

        System.out.println("\n>>> Found " + matches.size() + " Mutual Skill Exchange Match(es)! <<<");
        System.out.println("==========================================================================================");
        int index = 1;
        for (SkillMatch m : matches) {
            System.out.printf("[%d] Match Partner: %s (Student ID: %d | Email: %s)%n",
                    index++, m.getPartnerStudentName(), m.getPartnerStudentId(), m.getPartnerEmail());
            System.out.printf("    - You Teach (They want)  : %s [%s] (Your Skill ID: %d)%n",
                    m.getCurrentOfferedSkillName(), m.getCurrentOfferedProficiency(), m.getCurrentOfferedSkillId());
            System.out.printf("    - They Teach (You want)  : %s [%s] (Their Skill ID: %d)%n",
                    m.getPartnerOfferedSkillName(), m.getPartnerOfferedProficiency(), m.getPartnerOfferedSkillId());
            System.out.println("------------------------------------------------------------------------------------------");
        }

        if (mode == 1) {
            boolean send = InputUtil.readConfirmation("Would you like to send an exchange request to one of these matches?");
            if (send) {
                int matchChoice = InputUtil.readIntInRange("Enter the Match number (1-" + matches.size() + "): ", 1, matches.size());
                SkillMatch selected = matches.get(matchChoice - 1);
                ExchangeRequest req = requestService.createExchangeRequest(
                        selected.getCurrentStudentId(),
                        selected.getPartnerStudentId(),
                        selected.getCurrentOfferedSkillId(),
                        selected.getPartnerOfferedSkillId()
                );
                System.out.println("\n[SUCCESS] Exchange Request #" + req.getRequestId() + " sent successfully with status PENDING!");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Exchange Request Handlers
    // -------------------------------------------------------------------------

    private void sendExchangeRequest() throws SQLException {
        System.out.println("--- Send New Exchange Request ---");
        int requesterId = InputUtil.readInt("Enter Requester Student ID (Your ID): ");
        int receiverId = InputUtil.readInt("Enter Receiver Student ID (Partner ID): ");
        int offeredSkillId = InputUtil.readInt("Enter Offered Skill ID (Your TEACH skill): ");
        int wantedSkillId = InputUtil.readInt("Enter Wanted Skill ID (Partner's TEACH skill): ");

        ExchangeRequest req = requestService.createExchangeRequest(requesterId, receiverId, offeredSkillId, wantedSkillId);
        System.out.println("\n[SUCCESS] Exchange Request created successfully!");
        System.out.printf("  Request ID : %d%n", req.getRequestId());
        System.out.printf("  Status     : %s%n", req.getStatus());
        System.out.println("  The receiver can now view and accept your request.");
    }

    private void viewSentRequests() throws SQLException {
        System.out.println("--- Sent Exchange Requests ---");
        int studentId = InputUtil.readInt("Enter Your Student ID: ");
        List<ExchangeRequest> list = requestService.getSentRequests(studentId);
        if (list.isEmpty()) {
            System.out.println("You have not sent any exchange requests.");
            return;
        }

        System.out.printf("%-6s | %-18s | %-16s | %-16s | %-10s | %-16s%n",
                "Req ID", "Sent To", "Offered Skill", "Wanted Skill", "Status", "Date");
        System.out.println("--------------------------------------------------------------------------------------------");
        for (ExchangeRequest r : list) {
            System.out.printf("%-6d | %-18s | %-16s | %-16s | %-10s | %-16s%n",
                    r.getRequestId(),
                    r.getReceiverName() + " (#" + r.getReceiverId() + ")",
                    r.getOfferedSkillName(),
                    r.getWantedSkillName(),
                    r.getStatus(),
                    r.getRequestDate().toString().substring(0, 16));
        }
    }

    private void viewReceivedRequests() throws SQLException {
        System.out.println("--- Received Exchange Requests ---");
        int studentId = InputUtil.readInt("Enter Your Student ID: ");
        List<ExchangeRequest> list = requestService.getReceivedRequests(studentId);
        if (list.isEmpty()) {
            System.out.println("You have not received any exchange requests.");
            return;
        }

        System.out.printf("%-6s | %-18s | %-16s | %-16s | %-10s | %-16s%n",
                "Req ID", "Received From", "They Offer", "They Want", "Status", "Date");
        System.out.println("--------------------------------------------------------------------------------------------");
        for (ExchangeRequest r : list) {
            System.out.printf("%-6d | %-18s | %-16s | %-16s | %-10s | %-16s%n",
                    r.getRequestId(),
                    r.getRequesterName() + " (#" + r.getRequesterId() + ")",
                    r.getOfferedSkillName(),
                    r.getWantedSkillName(),
                    r.getStatus(),
                    r.getRequestDate().toString().substring(0, 16));
        }
    }

    private void acceptRequest() throws SQLException {
        System.out.println("--- Accept Exchange Request ---");
        int requestId = InputUtil.readInt("Enter Request ID to accept: ");
        int receiverId = InputUtil.readInt("Enter Your Student ID (Receiver Authorization): ");

        boolean ok = requestService.acceptRequest(requestId, receiverId);
        if (ok) {
            System.out.println("[SUCCESS] Request #" + requestId + " ACCEPTED! Both students can now coordinate learning.");
        } else {
            System.out.println("[!] Failed to accept request.");
        }
    }

    private void rejectRequest() throws SQLException {
        System.out.println("--- Reject Exchange Request ---");
        int requestId = InputUtil.readInt("Enter Request ID to reject: ");
        int receiverId = InputUtil.readInt("Enter Your Student ID (Receiver Authorization): ");

        boolean ok = requestService.rejectRequest(requestId, receiverId);
        if (ok) {
            System.out.println("[SUCCESS] Request #" + requestId + " REJECTED.");
        } else {
            System.out.println("[!] Failed to reject request.");
        }
    }

    private void cancelRequest() throws SQLException {
        System.out.println("--- Cancel Exchange Request ---");
        int requestId = InputUtil.readInt("Enter Request ID to cancel: ");
        int studentId = InputUtil.readInt("Enter Your Student ID (Must be participant): ");

        boolean ok = requestService.cancelRequest(requestId, studentId);
        if (ok) {
            System.out.println("[SUCCESS] Request #" + requestId + " CANCELLED.");
        } else {
            System.out.println("[!] Failed to cancel request.");
        }
    }

    private void completeExchange() throws SQLException {
        System.out.println("--- Mark Exchange as Completed ---");
        int requestId = InputUtil.readInt("Enter Request ID to complete: ");
        int studentId = InputUtil.readInt("Enter Your Student ID (Must be participant): ");

        boolean ok = requestService.completeRequest(requestId, studentId);
        if (ok) {
            System.out.println("[SUCCESS] Exchange Request #" + requestId + " marked as COMPLETED! Great job learning together!");
        } else {
            System.out.println("[!] Failed to complete exchange.");
        }
    }

    private void viewRequestDetails() throws SQLException {
        System.out.println("--- View Request Details ---");
        int requestId = InputUtil.readInt("Enter Request ID: ");
        Optional<ExchangeRequest> opt = requestService.getRequestById(requestId);
        if (opt.isEmpty()) {
            System.out.println("[!] Request #" + requestId + " not found.");
            return;
        }

        ExchangeRequest r = opt.get();
        System.out.println("+==============================================================================+");
        System.out.printf("| EXCHANGE REQUEST #%-58d |%n", r.getRequestId());
        System.out.println("+==============================================================================+");
        System.out.printf("| Status: %-20s | Created: %-32s |%n", r.getStatus(), r.getRequestDate());
        System.out.println("|------------------------------------------------------------------------------|");
        System.out.printf("| Requester: %-25s (ID: %-4d | Email: %-24s) |%n", 
                r.getRequesterName(), r.getRequesterId(), r.getRequesterEmail());
        System.out.printf("| Receiver : %-25s (ID: %-4d | Email: %-24s) |%n", 
                r.getReceiverName(), r.getReceiverId(), r.getReceiverEmail());
        System.out.println("|------------------------------------------------------------------------------|");
        System.out.printf("| Offered Skill: %-20s (Level: %-12s | ID: %-5d)           |%n",
                r.getOfferedSkillName(), r.getOfferedSkillProficiency(), r.getOfferedSkillId());
        System.out.printf("| Wanted Skill : %-20s (Level: %-12s | ID: %-5d)           |%n",
                r.getWantedSkillName(), r.getWantedSkillProficiency(), r.getWantedSkillId());
        System.out.println("+==============================================================================+");
    }
}
