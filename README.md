# SkillSwap – Student Skill Exchange Platform

SkillSwap is a student skill exchange platform built with **Java 17**, **JDBC**, **MySQL 8+**, and **Maven**. It connects students who want to trade their skills directly—without spending money on commercial courses.

For example:
* **Arun** can teach *Java* and wants to learn *UI Design*.
* **Priya** can teach *UI Design* and wants to learn *Java*.
* The platform's **Smart Skill Matching** engine detects this mutual synergy and connects them for a reciprocal peer-to-peer exchange.

---

## 1. Technology Stack

* **Language**: Java 17+ (LTS)
* **Build & Dependency Management**: Apache Maven 3.9+
* **Database**: MySQL 8.0+
* **Driver**: MySQL Connector/J (`com.mysql:mysql-connector-j:8.3.0`)
* **Data Access**: Core JDBC with `PreparedStatement` and connection transactions
* **User Interface**: Interactive Console-driven Menu (ANSI/ASCII formatted)
* **Testing**: JUnit 5 Jupiter (`org.junit.jupiter:junit-jupiter:5.10.2`)
* **IDE**: Visual Studio Code / IntelliJ IDEA / Eclipse

> **Note**: This application contains zero Spring Boot, React, Node.js, or MongoDB dependencies. All persistent data lives in the actual MySQL relational database.

---

## 2. Project Folder Structure

```
SkillSwap/
├── pom.xml
├── README.md
├── .gitignore
├── database/
│   └── skillswap.sql
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── skillswap/
    │               ├── DBConnection.java
    │               ├── SkillSwapApp.java
    │               ├── model/
    │               │   ├── Student.java
    │               │   ├── Skill.java
    │               │   ├── SkillMatch.java
    │               │   └── ExchangeRequest.java
    │               ├── dao/
    │               │   ├── StudentDAO.java
    │               │   ├── SkillDAO.java
    │               │   └── ExchangeRequestDAO.java
    │               ├── service/
    │               │   ├── StudentService.java
    │               │   ├── SkillService.java
    │               │   ├── SkillMatchingService.java
    │               │   └── ExchangeRequestService.java
    │               └── util/
    │                   └── InputUtil.java
    └── test/
        └── java/
            └── com/
                └── skillswap/
                    ├── StudentServiceTest.java
                    ├── SkillServiceTest.java
                    ├── SkillMatchingServiceTest.java
                    ├── ExchangeRequestServiceTest.java
                    └── DatabaseIntegrationTest.java
```

---

## 3. Prerequisites & Installation

Ensure the following tools are installed on your machine:
1. **Java Development Kit (JDK) 17 or higher**:
   Verify with:
   ```powershell
   java -version
   ```
2. **Apache Maven 3.8+**:
   Verify with:
   ```powershell
   mvn -version
   ```
3. **MySQL Server 8.0+**:
   Ensure the MySQL service is running:
   ```powershell
   Get-Service -Name *mysql*
   ```

---

## 4. Database Setup & Initialization

### Step 4.1: Import the Database Schema
Open your terminal (PowerShell or Command Prompt) and execute the SQL initialization script `database/skillswap.sql`:

```powershell
# Using MySQL Command Line Client:
mysql -u root -p < "database\skillswap.sql"
```

Or execute it interactively:
```powershell
mysql -u root -p
```
Once inside the MySQL shell:
```sql
SOURCE database/skillswap.sql;
SHOW TABLES;
```

This creates the `skillswap` database, the three tables (`students`, `skills`, `skill_exchange_requests`), all indexes, foreign keys, and seed sample data for Arun, Priya, Sneha, and Rahul.

---

## 5. Database Configuration on Windows

The application dynamically reads database credentials from environment variables, preventing hardcoded credentials:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:mysql://localhost:3306/skillswap` | JDBC connection string |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | *(Empty / User supplied)* | Database password |

### Setting Environment Variables on Windows:

#### Option A: Current PowerShell Session
```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
$env:DB_URL="jdbc:mysql://localhost:3306/skillswap"
```

#### Option B: Current Command Prompt (CMD)
```cmd
set DB_USER=root
set DB_PASSWORD=your_mysql_password
set DB_URL=jdbc:mysql://localhost:3306/skillswap
```

#### Option C: Permanent System Environment Variables
1. Press `Win + R`, type `sysdm.cpl`, and press Enter.
2. Navigate to **Advanced** > **Environment Variables**.
3. Under **User variables**, click **New**:
   * Name: `DB_PASSWORD`, Value: `your_mysql_password`
   * Name: `DB_USER`, Value: `root`
   * Name: `DB_URL`, Value: `jdbc:mysql://localhost:3306/skillswap`
4. Click **OK** and restart your terminal.

#### Option D: Interactive Fallback
If `DB_PASSWORD` is not set in your environment, the application will detect the missing connection upon startup and prompt you to enter the credentials directly in the console.

---

## 6. How to Open the Project in VS Code

1. Launch **Visual Studio Code**.
2. Go to **File** > **Open Folder...** and select the `SkillSwap` root directory.
3. Install recommended extensions:
   * **Extension Pack for Java** (Microsoft)
   * **Maven for Java** (Microsoft)
   * **MySQL** (cweijan or Oracle)
4. VS Code will automatically detect the Maven project and configure the classpath.

---

## 7. How to Build & Run the Application

### Compile the project:
```powershell
mvn clean compile
```

### Run all tests:
```powershell
mvn test
```

### Run the console application:
```powershell
mvn exec:java
```

Or run via Java CLI directly:
```powershell
java -cp "target/classes;target/dependency/*" com.skillswap.SkillSwapApp
```

---

## 8. Database Tables & Architecture

### 1. `STUDENTS`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `student_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique student identifier |
| `student_name` | VARCHAR(100) | NOT NULL, CHECK non-blank | Student full name |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | Student contact email |
| `contact_info` | VARCHAR(255) | NULLABLE | Phone, LinkedIn, or Discord handle |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Registration timestamp |

### 2. `SKILLS`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `skill_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique skill identifier |
| `student_id` | INT | FK to `STUDENTS(student_id)` ON DELETE CASCADE | Owner of the skill |
| `skill_name` | VARCHAR(100) | NOT NULL | Name of skill (e.g. Java, Python) |
| `skill_type` | ENUM | `'TEACH'`, `'LEARN'` | Offer or demand |
| `proficiency_level`| ENUM | `'BEGINNER'`, `'INTERMEDIATE'`, `'ADVANCED'` | Proficiency rank |
| `description` | TEXT | NULLABLE | Skill bio or learning goals |

### 3. `SKILL_EXCHANGE_REQUESTS`
| Column | Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `request_id` | INT | PRIMARY KEY, AUTO_INCREMENT | Unique request identifier |
| `requester_id` | INT | FK to `STUDENTS(student_id)` | Student initiating the swap |
| `receiver_id` | INT | FK to `STUDENTS(student_id)` | Student receiving the swap |
| `offered_skill_id`| INT | FK to `SKILLS(skill_id)` ON DELETE RESTRICT | TEACH skill offered by requester |
| `wanted_skill_id` | INT | FK to `SKILLS(skill_id)` ON DELETE RESTRICT | TEACH skill requested from receiver |
| `request_date` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Submission timestamp |
| `status` | ENUM | `'PENDING'`, `'ACCEPTED'`, `'REJECTED'`, `'COMPLETED'`, `'CANCELLED'` | Current request state |

---

## 9. Implemented Features & Menu Guide

The application features an interactive, menu-driven interface:

```
===== SKILLSWAP – STUDENT SKILL EXCHANGE =====
 1. Register Student
 2. View Student
 3. View All Students
 4. Update Student
 5. Delete Student
 6. Add Skill
 7. View Student Skills
 8. Search Skills
 9. Update Skill
10. Delete Skill
11. Find Smart Skill Matches
12. Send Exchange Request
13. View Sent Requests
14. View Received Requests
15. Accept Request
16. Reject Request
17. Cancel Request
18. Complete Exchange
19. View Request Details
 0. Exit
==============================================
```

### Detailed Functional Walkthrough:
* **Student Management (Options 1–5)**:
  * Register new students with automatic auto-increment ID display.
  * Strict validation for non-blank names, email formats (`student@domain.ext`), and duplicate email prevention.
  * Transactional cascading delete: when a student is removed, their skills and exchange requests are cleaned up safely.
* **Skill Management (Options 6–10)**:
  * Register TEACH or LEARN skills with BEGINNER, INTERMEDIATE, or ADVANCED levels.
  * Ownership verification: users can only modify or delete their own skills.
  * Referential integrity guard: a skill cannot be deleted if active or completed exchange requests reference it.
  * Full search by name substring, filter by TEACH/LEARN, or filter by proficiency level.
* **Smart Skill Matching (Option 11)**:
  * Identifies genuine two-way matches:
    * Current student wants to learn what partner teaches.
    * Current student teaches what partner wants to learn.
  * Case-insensitive, whitespace-trimmed comparison.
  * Excludes the current student from their own matches.
  * Displays partner details, offered and wanted skill names, and proficiency levels.
  * Provides a 1-click option to instantly dispatch an exchange request to the matched partner.
* **Exchange Request Workflow (Options 12–19)**:
  * Finite state machine for request lifecycles:
    * `PENDING` -> `ACCEPTED` (by receiver only)
    * `PENDING` -> `REJECTED` (by receiver only)
    * `PENDING` / `ACCEPTED` -> `CANCELLED` (by either participant)
    * `ACCEPTED` -> `COMPLETED` (by either participant)
  * Prevents users from sending requests to themselves.
  * Validates that offered skill belongs to requester and wanted skill belongs to receiver.
  * Prevents duplicate active requests for the same skill pair.

---

## 10. End-to-End Walkthrough: Arun & Priya Exchanging Java & UI Design

Here is an example exchange session between Arun and Priya:

### Step 1: Register Arun & Priya
1. Select Option `1` (Register Student):
   * Name: `Arun Kumar`
   * Email: `arun@college.edu`
   * Contact: `Discord: @arunk`
   * Result: Student ID `1` assigned.
2. Select Option `1` again:
   * Name: `Priya Sharma`
   * Email: `priya@college.edu`
   * Contact: `Phone: 9876543210`
   * Result: Student ID `2` assigned.

### Step 2: Add Skills
1. Option `6` (Add Skill) for Arun:
   * Student ID: `1`
   * Type: `TEACH`
   * Name: `Java`
   * Level: `ADVANCED`
   * Description: `Core Java & Collections`
2. Option `6` (Add Skill) for Arun:
   * Student ID: `1`
   * Type: `LEARN`
   * Name: `UI Design`
   * Level: `BEGINNER`
3. Option `6` (Add Skill) for Priya:
   * Student ID: `2`
   * Type: `TEACH`
   * Name: `UI Design`
   * Level: `ADVANCED`
   * Description: `Figma & Design Systems`
4. Option `6` (Add Skill) for Priya:
   * Student ID: `2`
   * Type: `LEARN`
   * Name: `Java`
   * Level: `BEGINNER`

### Step 3: Run Smart Match
1. Select Option `11` (Find Smart Skill Matches):
   * Select Option `1` (Specific Student).
   * Enter Student ID: `1` (Arun).
2. Console Output:
   ```
   >>> Found 1 Mutual Skill Exchange Match(es)! <<<
   [1] Match Partner: Priya Sharma (Student ID: 2 | Email: priya@college.edu)
       - You Teach (They want)  : Java [ADVANCED] (Your Skill ID: 1)
       - They Teach (You want)  : UI Design [ADVANCED] (Their Skill ID: 3)
   ```
3. Prompt: `Would you like to send an exchange request to this partner? (Y/N): Y`
   * Result: `[SUCCESS] Exchange Request #1 sent successfully with status PENDING!`

### Step 4: Priya Accepts the Request
1. Select Option `14` (View Received Requests) with Student ID `2` (Priya).
   * Request #1 displayed from Arun offering Java for UI Design.
2. Select Option `15` (Accept Request):
   * Enter Request ID: `1`
   * Enter Your Student ID: `2`
   * Result: `[SUCCESS] Request #1 ACCEPTED! Both students can now coordinate learning.`

### Step 5: Complete the Exchange
1. Once both students finish teaching each other, select Option `18` (Complete Exchange):
   * Request ID: `1`
   * Your Student ID: `1` or `2`
   * Result: `[SUCCESS] Exchange Request #1 marked as COMPLETED!`

---

## 11. Common Errors & Troubleshooting

### 1. `Access denied for user 'root'@'localhost'`
* **Cause**: Incorrect or missing MySQL root password.
* **Fix**: Set `$env:DB_PASSWORD="your_password"` in PowerShell before launching `mvn exec:java`, or supply it at the interactive startup prompt.

### 2. `Table 'skillswap.students' doesn't exist`
* **Cause**: Database initialization script has not been executed yet.
* **Fix**: Run `mysql -u root -p < database/skillswap.sql` to generate all tables and views.

### 3. `Communications link failure / Connection refused`
* **Cause**: MySQL Server service is stopped or running on a non-standard port.
* **Fix**: Start the service in PowerShell: `Start-Service MySQL80` or verify port 3306 in `my.ini`.

### 4. `A student with email '...' is already registered`
* **Cause**: Duplicate email restriction violation.
* **Fix**: Use a unique email address for each student.

### 5. `Cannot delete skill: It is currently referenced in one or more exchange requests`
* **Cause**: Referential integrity protection.
* **Fix**: A skill involved in an active or historical exchange cannot be deleted directly without cancelling/removing the associated requests first.

---

## 12. Security & Best Practices

1. **SQL Injection Protection**: 100% of SQL statements with parameters utilize JDBC `PreparedStatement`.
2. **Credential Safety**: Database passwords are never printed to stdout/stderr or hardcoded into the source code repository.
3. **Transaction Management**: Multi-step operations (such as cascading student deletions) are wrapped in explicit database transactions (`setAutoCommit(false)` / `commit()` / `rollback()`).
4. **Resource Management**: All JDBC connections, statements, and result sets are managed with `try-with-resources` to prevent resource leaks.
5. **Note on Console Identification**: Entering a student ID in console menus serves as a simulation demo; for production web applications, implement token-based session authentication (e.g., JWT).
