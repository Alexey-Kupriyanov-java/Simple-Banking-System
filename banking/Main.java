package banking;

import java.util.Scanner;

public class Main {
    private static final int BIN = 400_000;
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Storage storage = new DBStorage(args[1]);
        String number;
        String pin;

        boolean exit = false;
        do {
            displayMainMenu();
            int userInput = Integer.parseInt(scanner.nextLine());
            switch (userInput) {
                case 0:
                    System.out.println("Bye!");
                    exit = true;
                    break;
                case 1:
                    number = storage.create(BIN);
                    pin = storage.getPin(number);
                    System.out.println("\nYour card has been created");
                    System.out.println("Your card number:");
                    System.out.println(number);
                    System.out.println("Your card PIN:");
                    System.out.println(pin + "\n");
                    break;
                case 2:
                    System.out.println("\nEnter your card number:");
                    number = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    pin = scanner.nextLine();

                    if (!storage.logIn(number, pin)) {
                        System.out.println("\nWrong card number or PIN!\n");
                        break;
                    }
                    System.out.println("\nYou have successfully logged in!\n");
                    boolean logout = false;
                    do {
                        displayAccountMenu();
                        userInput = Integer.parseInt(scanner.nextLine());
                        switch (userInput) {
                            case 0:
                                System.out.println("Bye!");
                                exit = true;
                                break;
                            case 1:
                                System.out.println("\nBalance: " + storage.getBalance(number) + "\n");
                                break;
                            case 2:
                                System.out.println("Enter income:");
                                int income = Integer.parseInt(scanner.nextLine());
                                storage.addIncome(number, income);
                                System.out.println("Income was added!");
                                break;
                            case 3:
                                System.out.println("Transfer");
                                System.out.println("Enter card number:");
                                String toNumber = scanner.nextLine();
                                int money;
                                if (!storage.checkValidNumber(toNumber)) {
                                    System.out.println("Probably you made a mistake in the card number. Please try again!");
                                    break;
                                }
                                if (!storage.consist(toNumber)) {
                                    System.out.println("Such a card does not exist");
                                    break;
                                }
                                System.out.println("Enter how much money you want to transfer:");
                                money = Integer.parseInt(scanner.nextLine());
                                if (money > storage.getBalance(number)) {
                                    System.out.println("Not enough money!");
                                    break;
                                }
                                storage.transfer(number, toNumber, money);
                                break;
                            case 4:
                                storage.closeAccount(number);
                                System.out.println("\nThe account has been closed!");
                                break;
                            case 5:
                                System.out.println("\nYou have successfully logged out!\n");
                                logout = true;
                                break;
                        }
                    } while (!(logout || exit));
            }
        } while (!exit);

    }

    private static void displayMainMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    private static void displayAccountMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Balance");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }
}