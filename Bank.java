import java.util.Scanner;

/**
 * Bank.java
 * Author: Jarod Ruschill
 * Version: 0.7
 * This program acts like the bank interface
 * It will take the user options and allow them to login or sign up
 *  Once logged in the user will see a menu of the options they can select
 * From there, the options are all implemented except for option G which will just say it is a WIP
 *
 */
public class Bank {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        char option;
        //do while to get user choice on whether to login sign up or quit and keep looping till they input correct value
        do {
            option = bankMenu();
            switch (option){
                case 'A':
                    // initiate login sequence
                    Customer c = login();
                    if(c != null){
                        System.out.println(c.getAccountDetails());
                        char choice ='a';
                        //H is to exit
                        while (choice != 'H') {
                            choice = postLoginMenu();
                            handleAccountOptions(choice, c);
                        }
                        System.out.println("You are now logged out!");

                    } else {
                        System.out.println("I am sorry but your login failed!\nPlease try again!");
                    }
                    break;
                case 'B':
                    // sign up time
                    signUp();
                    break;
                case 'C':
                    System.out.println("Goodbye!~.");
                    break;
                    //this should never happen lol ( its "you should not be here backwards")
                default:
                    System.out.println("ereh eb ton dluohs uoy");
            }
//            if (option == 'A') {
//                //
//            } else if (option == 'B') {
//                signUp();
//            }
        } while (option != 'C');
       // System.out.println("Goodbye!~.");
    }

    /**
     * Bank Menu has been given to you
     * @return character that is the user choice.
     */
    public static char bankMenu() {
        System.out.println("~~ Welcome to Tony Stark's Bank of Marvel Universe ~~\n");
        char option;
        do {
            System.out.println("Please choose 1 of the following options: ");
            System.out.println("A. Login");
            System.out.println("B. Sign up");
            System.out.println("C. Exit");
            option = scanner.next().toUpperCase().charAt(0);
            if (option < 'A' || option > 'C') {
                System.out.println("\nYou have entered an invalid option.");
            }
        } while (option < 'A' || option > 'C');
        return option;
    }

    /**
     * This menu will be displayed to the user on successful login.
     * @return the user choice
     */
    public static char postLoginMenu() {
        // modeled after bankMenu one
        char choice;
        do {
            //all your different choices
            System.out.println("Please choose 1 of the following options: ");
            System.out.println("A. Balance");
            System.out.println("B. Account Number");
            System.out.println("C. View Transaction Report");
            System.out.println("D. Deposit");
            System.out.println("E. Withdraw");
            System.out.println("F. Transfer");
            System.out.println("G is a work in progress");
            System.out.println("H. to quit");
            choice = scanner.next().toUpperCase().charAt(0);
            if (choice < 'A' || choice > 'H') {
                System.out.println("\nYou have entered an invalid option.");
            }
        } while (choice < 'A' || choice > 'H');

        return choice;
    }

    /**
     * Login the customer. Make sure to assign this customer object to a variable so that it can be reused.
     * @return the Customer Object after asking for username and password
     */
    public static Customer login() {
        // login time!
        System.out.println("Enter your username:");
        String uName = scanner.next();
        System.out.println("Now enter your password:");
        String passWord = scanner.next();
        return BankHelper.login(uName,passWord);
    }

    /**
     * Sign up for the customer by asking them proper details.
     * Refer to customer class and sample run for this.
     */
    public static void signUp() {
        //This gets all the info required to create an account
        System.out.println("Congrats! on opening your account!");
        System.out.println("First we need your first name");
        String fName = scanner.next();
        System.out.println("Now your last name");
        String lName = scanner.next();
        System.out.println("Now your user ID");
        String id = scanner.next();
        System.out.println("Please enter a password ");
        System.out.println("No spaces, You need one uppercase letter, one lowercase letter\nA number and a symbol");
        String password = scanner.next();
        System.out.println("Your account balance");
        double bal = scanner.nextDouble();
        System.out.println("Now enter your account type (Please type either Checking or Saving)");
        String aType = scanner.next();
        Customer customer = new Customer(fName, lName,id, password,bal,aType);
    }

    /**
     * Once the user selects an option from postLoginMenu, use this method to properly
     * navigate through the choices and print appropriate results.
     * @param option -> User Selected option from postLoginMenu
     * @param customer -> The Customer Object received after successful Login
     */
    public static void handleAccountOptions(char option, Customer customer) {
        //switch because I think it looks cleaner
        switch (option){
            //The give me my balance option
            case 'A':
                System.out.printf("This account's balance is : $%.2f\n", customer.accountBalance);
                break;
                //This just gives  you the account number
            case 'B':
                System.out.println("Your account number is : "+ customer.getAccountId());
                break;
                //Let's the customer see their transaction history
            case 'C' :
                BankHelper.viewTransactionHistory(customer);
                break;
                //Let's the user make a deposit
            case 'D' :
                System.out.println("Please enter the amount you are depositing");
                double mon= scanner.nextDouble();
                while (mon<0){
                    System.out.println("If you are taking money out please enter 0 and then select the withdraw option");
                    mon = scanner.nextDouble();
                }
                customer.depositMoney(mon);
                break;
                // withdraw money - interesting note is that the user can enter their withdraw as a negative or
            // positive and it will still do what it's supposed to
            case 'E' :
                System.out.println("Please enter the amount you are withdrawing");
                double w= scanner.nextDouble();
                // obvious lol
                while (w > customer.accountBalance){
                    System.out.println("You cannot withdraw more money than you have");
                    w = scanner.nextDouble();
                }
                customer.withdrawMoney(w);
                break;
                //Transfer time
            case 'F':
                System.out.println("Please enter the amount you are transferring");
                double amo = scanner.nextDouble();
                //again pretty self explanatory
                while (amo > customer.accountBalance){
                    System.out.println("You cannot transfer more money than you have!");
                    amo = scanner.nextDouble();
                }
                System.out.println("Please enter the other account's ID");
                String accName = scanner.next();
                customer.transferMoney(amo, accName);
                break;
            case 'G' :
                System.out.println("The programmer is still working on this ");
                break;
        }
    }

    /**
     * Option G for those extra achievers.
     */
    public static void optionG() {
        System.out.println("This is Extra Credit.");

    }

}
