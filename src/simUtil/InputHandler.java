package simUtil;

import java.util.InputMismatchException;
import java.util.Scanner;

public class InputHandler {

    private final Scanner scanner;

    public InputHandler(Scanner scan) {
        scanner = scan;
    }

    public int getInt(String prompt, int minVal, int maxVal) {
        while (true) {
            System.out.print(prompt);

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice >= minVal && choice <= maxVal) {
                    return choice;
                }

                System.out.println("[ERROR] Please enter a number between " + minVal + " and " + maxVal);

            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public double getDouble(String prompt, double minVal, double maxVal) {
        while (true) {
            System.out.print(prompt);

            try {
                double choice = scanner.nextDouble();
                scanner.nextLine();

                if (choice >= minVal && choice <= maxVal) {
                    return choice;
                }

                System.out.println("[ERROR] Please enter a number between " + minVal + " and " + maxVal);

            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public String getString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("[ERROR] Input cannot be empty.");
        }
    }

    public String getString(String prompt, int minLength, int maxLength) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("[ERROR] Input cannot be empty.");
            } else if (input.length() < minLength) {
                System.out.println("[ERROR] Input must be at least " + minLength + " characters.");
            } else if (input.length() > maxLength) {
                System.out.println("[ERROR] Input must be at most " + maxLength + " characters.");
            } else {
                return input;
            }
        }
    }


    public void display(String title, String[] options) {
        System.out.println("\n=== " + title + " ===");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + "-" + options[i]);
        }
    }


    public void message(String prompt) {
        System.out.printf("--{ %s }--%n", prompt);
        System.out.print("Press [ENTER] to continue...");
        scanner.nextLine();
    }

}
