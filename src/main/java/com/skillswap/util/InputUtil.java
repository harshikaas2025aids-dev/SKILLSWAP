package com.skillswap.util;

import java.util.Scanner;

/**
 * Robust console input utility that handles all parsing, prompts, and validations
 * without throwing unhandled exceptions or crashing the application.
 */
public class InputUtil {

    private static Scanner scanner = new Scanner(System.in);

    /**
     * Allows setting a custom Scanner (useful for testing or automated input simulation).
     */
    public static void setScanner(Scanner customScanner) {
        if (customScanner != null) {
            scanner = customScanner;
        }
    }

    /**
     * Reads a non-empty string from the console. Reprompts if input is blank.
     */
    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return "";
                }
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    return line;
                }
                System.out.println("  [!] Input cannot be empty. Please try again.");
            } catch (Exception e) {
                System.out.println("  [!] Error reading input. Please try again.");
            }
        }
    }

    /**
     * Reads a line of text which can optionally be empty.
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        try {
            if (!scanner.hasNextLine()) {
                return "";
            }
            return scanner.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Checks if there is more input available from the scanner.
     */
    public static boolean hasMoreInput() {
        try {
            return scanner.hasNextLine();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reads an integer from the console. Returns 0 (Exit) if input stream reaches EOF.
     */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return 0; // Graceful exit on EOF
                }
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please enter a valid integer.");
            } catch (Exception e) {
                return 0;
            }
        }
    }

    /**
     * Reads an integer within an inclusive range [min, max].
     */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int val = readInt(prompt);
            if (val >= min && val <= max) {
                return val;
            }
            System.out.printf("  [!] Please enter a number between %d and %d.%n", min, max);
        }
    }

    /**
     * Displays a selection prompt for predefined options.
     */
    public static String readChoice(String prompt, String... validOptions) {
        while (true) {
            System.out.print(prompt);
            try {
                if (!scanner.hasNextLine()) {
                    return validOptions[0];
                }
                String line = scanner.nextLine().trim().toUpperCase();
                for (String option : validOptions) {
                    if (option.equalsIgnoreCase(line)) {
                        return option.toUpperCase();
                    }
                }
                System.out.print("  [!] Invalid selection. Valid options are: [");
                for (int i = 0; i < validOptions.length; i++) {
                    System.out.print(validOptions[i] + (i < validOptions.length - 1 ? ", " : ""));
                }
                System.out.println("]. Please try again.");
            } catch (Exception e) {
                System.out.println("  [!] Error reading input. Please try again.");
            }
        }
    }

    /**
     * Prompts for confirmation (Y/N).
     */
    public static boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            try {
                if (!scanner.hasNextLine()) {
                    return false;
                }
                String line = scanner.nextLine().trim().toUpperCase();
                if (line.equals("Y") || line.equals("YES")) {
                    return true;
                }
                if (line.equals("N") || line.equals("NO")) {
                    return false;
                }
                System.out.println("  [!] Please enter Y or N.");
            } catch (Exception e) {
                return false;
            }
        }
    }

    /**
     * Pauses and waits for the user to press Enter.
     */
    public static void pressEnterToContinue() {
        System.out.print("\nPress [Enter] to continue...");
        try {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        } catch (Exception ignored) {
        }
    }
}
