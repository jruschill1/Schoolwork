import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * BankHelper.java
 * This class will be used in order to carry out certain interactions between the Bank and the Customer.
 * This class can do the following things:
 *    - Login Customer
 *    - View Transaction History of a customer account
 *    - Deposit Money to a customer's account
 *    - Withdraw Money from a customer's account
 *    - Transfer Money between 2 customer accounts
 *    - Add User Information in local files
 *    - Create customer's account files
 *    - Check if the password entered is valid or not
 *    - Check if the userId is valid or not
 *    - Check if accountId is unique or not
 *    - Generate a new accountId
 *
 * There are also some internal helper methods that are private and documented on their declaration.
 * @version 1.0
 * @author Devansh Desai
 */
public class BankHelper {

    // Static constants for consistency.
    private static final String DEPOSIT = "Deposit";
    private static final String WITHDRAW = "Withdraw";

    /**
     * Login for Customer. This Method finds the customer Info based on username and password provided.
     * It then returns the Customer back to the caller. If no user exists, it will return null.
     * @param userName -> username of the customer
     * @param password -> password corresponding to that user
     * @return [Customer] if it was successful login. Null otherwise.
     */
    public static Customer login(String userName, String password) {
        // getting unique accountId to find the account files for the user.
        String accountId = getLoginAccountId(userName, password);
        try {
            if (accountId != null) {
                //Reading the account files.
                Stream<String> lines = Files.lines(Paths.get( accountId + "_account_details.txt"));
                List<String> data = lines.collect(Collectors.toList());

                if (data.size() > 0) {
                    // Parsing proper data from files to construct the Customer Object.
                    String name = data.get(0);
                    String firstName = name.substring(0, name.indexOf(" "));
                    String lastName = name.substring(name.indexOf(" ") + 1);
                    double accountBalance = Double.parseDouble(data.get(1));
                    String accountType = data.get(2);

                    return new Customer(firstName, lastName, userName, password, accountBalance, accountType, accountId);
                }
            }
            return null;
        } catch (IOException exception) {
            System.out.println("A weird Random Error has occured");
            System.out.println("The Login File Does not Exist.");
            return null;
        }
    }

    /**
     * View Transaction History of the customer.
     * This method will print the transaction history of the customer as is.
     * @param customer -> customer info through which we will find transaction details
     */
    public static void viewTransactionHistory(Customer customer) {
        try {
            // Read the transation Details
            Path transactionDetailsPath = Paths.get(customer.getAccountId() + "_transaction_details.txt");
            Stream<String> lines = Files.lines(transactionDetailsPath);

            // Print for each line
            lines.forEach(System.out::println);
        } catch (IOException exception) {
            // This will only occur if someone's smart mind will decide to delete some text files :/
            System.out.println("Someone has hacked and deleted files. Call IT to fix this issue.");
        }
    }

    /**
     * Deposit Money to a customer account
     * @param customerId -> customerId
     * @param amount -> amount to be deposited
     * @param isTransfer -> is it only deposit or a transfer
     * @param otherAccountId -> other customer's account id if required
     * @return true if deposit was successful, false otherwise
     */
    public static boolean depositMoney(String customerId, double amount, boolean isTransfer, String otherAccountId) {
        try {
            // Open Account Details for the customer.
            Path accountDetailsPath = Paths.get(customerId + "_account_details.txt");
            Stream<String> lines = Files.lines(accountDetailsPath);

            List<String> data = lines.collect(Collectors.toList());
            if (data.size() > 0) {
                // Once we find the customer, parse proper data
                String name = data.get(0);
                String firstName = name.substring(0, name.indexOf(" "));
                String lastName = name.substring(name.indexOf(" ") + 1);
                double accountBalance = Double.parseDouble(data.get(1));
                String accountType = data.get(2);
                accountBalance += amount; // DEPOSITING the amount
                String content = formatAccountProfile(firstName, lastName, accountBalance, accountType);

                // Rewriting the updated contents to the file.
                Files.write(accountDetailsPath, content.getBytes(), StandardOpenOption.CREATE);

                // Adding Transaction for the customer
                addTransaction(customerId, amount, DEPOSIT, isTransfer, otherAccountId, accountBalance);
                return true;
            } else {
                System.out.println("Some Error occurred during Deposit");
                return false;
            }
        } catch (IOException exception) {
            System.out.println("Someone has hacked. Call IT to fix this issue.");
            return false;
        }
    }

    /**
     * Withdraw Money from a customer account
     * @param customerId -> customerId
     * @param amount -> amount to be withdraw
     * @param isTransfer -> is it only deposit or a transfer
     * @param otherAccountId -> other customer's account id if required
     * @return true if withdraw was successful, false otherwise
     */
    public static boolean withdrawMoney(String customerId, double amount, boolean isTransfer, String otherAccountId) {
        try {
            // Open Account Details for the customer.
            Path accountDetailsPath = Paths.get(customerId + "_account_details.txt");
            Stream<String> lines = Files.lines(accountDetailsPath);

            List<String> data = lines.collect(Collectors.toList());
            if (data.size() > 0) {
                // Once we find the customer, parse proper data
                String name = data.get(0);
                String firstName = name.substring(0, name.indexOf(" "));
                String lastName = name.substring(name.indexOf(" ") + 1);
                double accountBalance = Double.parseDouble(data.get(1));
                String accountType = data.get(2);
                accountBalance -= amount; // WITHDRAWING the amount
                String content = formatAccountProfile(firstName, lastName, accountBalance, accountType);

                // Rewriting the updated contents to the file.
                Files.write(accountDetailsPath, content.getBytes(), StandardOpenOption.CREATE);

                // Adding Transaction for the customer
                addTransaction(customerId, amount, WITHDRAW, isTransfer, otherAccountId, accountBalance);
                return true;
            } else {
                System.out.println("Some Error occurred during Withdraw");
                return false;
            }
        } catch (IOException exception) {
            System.out.println("Someone has hacked. Call IT to fix this issue.");
            return false;
        }
    }

    /**
     * Transfer money from one account to another
     * @param customerId -> customer id
     * @param amount -> amount that needs to be transferred
     * @param anotherAccountId -> account id of another account
     * @return true if transfer was successful, false otherwise
     */
    public static boolean transferMoney(String customerId, double amount, String anotherAccountId) {
        // Check if deposit was successful
        boolean deposit = depositMoney(anotherAccountId, amount, true, customerId);
        boolean withdraw = false;

        // Go with the withdrawal ONLY If deposit was successful
        if (deposit) { withdraw = withdrawMoney(customerId, amount, true, anotherAccountId); }

        // Return the successes of both.
        return deposit && withdraw;
    }

    /**
     * Adding A new User to Login Text file
     * @param userName -> username of the user
     * @param password -> password of the user
     * @param accountId -> accountId of the user
     * @return true if user was successfully added, false otherwise
     */
    public static boolean addUserToUserLoginInfo(String userName, String password, String accountId)  {
        // Making Sure that Username and Password match the requirements.
        if (!checkUserIdUnique(userName)) {
            System.out.println("Username is not unique. Please try a different Username.");
            return false;
        }
        if (!checkPassword(password)) {
            System.out.println("Password Does not stand the requirement. Please make sure that password countains a lowercase, uppercase, symbol and number. (NO SPACES)");
            return false;
        }
        try {
            // Format the String in a certain format to add it to the userLoginInfo text file.
            String content = "\r" + userName + " " + password + " " + accountId;
            Files.write(Paths.get("userLoginInfo.txt"), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            // Create a new file for the login info if none exists.
            return createFileWithUser(userName, password, accountId);
        }
        return true;
    }

    /**
     * Create Account files for the given customer
     * @param customer -> Customer data whose files needs to be created.
     */
    public static void createAccountFiles(Customer customer) {
        System.out.println("Creating Account Files now ... ");
        try {
            //Creating Account Files
            Path accountDetailsPath = Paths.get(customer.getAccountId() + "_account_details.txt");
            Files.write(accountDetailsPath, formatAccountProfile(
                    customer.firstName,
                    customer.lastName,
                    customer.accountBalance,
                    customer.accountType
            ).getBytes(), StandardOpenOption.CREATE);

            //Path and proper date format for transaction history.
            Path transactionDetailsPath = Paths.get(customer.getAccountId() + "_transaction_details.txt");
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            // Adding First Transaction and Creating Transaction File
            String firstTransaction = customer.accountType + " Account " + "(" + customer.getAccountId() + ") Created on " + formatter.format(date)
                    + ". Initial Balance: " + customer.accountBalance;

            Files.write(transactionDetailsPath, firstTransaction.getBytes(), StandardOpenOption.CREATE);

        } catch (IOException exception1) {
            System.out.println("A weird Random Error has occured while creating account profile files");
            exception1.printStackTrace();
        }
    }

    /**
     * Validate password
     * @param password -> password string
     * @return true if it matches requirements, false otherwise
     */
    public static boolean checkPassword(String password) {
        //Password regex. Must contain one of 0-9, a-z, A-Z and the @#$%^&-+=(). Password length 8-20
        String regex = "^(?=.*[0-9])" +
                "(?=.*[a-z])" +
                "(?=.*[A-Z])" +
                "(?=.*[@#$%^&-+=()])(?=\\S+$).{8,20}$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        // Returns true if the password matches the given regex
        return matcher.matches();
    }

    /**
     * Validate username
     * @param username -> username string
     * @return true if username is unique
     */
    public static boolean checkUserIdUnique(String username) {
        List<String> userNames = new ArrayList<>();
        try {
            // Get all the userIds and add them to a list
            Stream<String> lines = Files.lines(Paths.get( "userLoginInfo.txt"));
            lines.forEach(s -> userNames.add(getUserNameFromLoginInfo(s)));
        }
        catch (IOException e) {
            return true;
        }
        // Check if the userId exists in the list
        return !userNames.contains(username) && !username.contains(" ");
    }

    /**
     * Validate accountId
     * @param accountID -> accountId string
     * @return true if accountId is unique, false otherwise.
     */
    public static boolean checkAccountIdUnique(String accountID) {
        List<String> accountIds = new ArrayList<>();
        try {
            // Get all the accountIds and add them to a list
            Stream<String> lines = Files.lines(Paths.get( "userLoginInfo.txt"));
            lines.forEach(s -> accountIds.add(getAccountIdFromLoginInfo(s)));
        }
        catch (IOException e) {
            return true;
        }
        // Check if the accountId exists in the list
        return !accountIds.contains(accountID);
    }

    /**
     * Generate a random 10 digit accountID
     */
    public static String generateAccountID() {
        String id;
        boolean isUniqueId;
        // Keep finding accountId until we find unique
        do {
            // Generate 10 digit random account ID
            id = ((long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L) + "";
            isUniqueId = checkAccountIdUnique(id);
        } while (!isUniqueId);
        return id;
    }

    /**
     * Add Transaction Details in the Customer's Log file.
     * @param customerId -> accountId of the customer
     * @param amount -> transaction amount
     * @param type -> type of transaction Deposit / Withdraw
     * @param isTranfer -> boolean to determine whether transaction is a transfer or not
     * @param otherAccountId -> accountId of other customer, null if not not applicable
     * @param newBalance -> new current balance.
     */
    private static void addTransaction(String customerId, double amount, String type, boolean isTranfer, String otherAccountId, double newBalance) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            // Formatting the string
            String content = "\r(" + formatter.format(date) + "): ";

            // Enhanced Switch statement. (Only available in the recent Java versions)
            switch (type) {
                case DEPOSIT -> {
                    if (isTranfer) {
                        content += "Transfer Received  $" + amount + " from Account #" + otherAccountId;
                    } else {
                        content += "Deposit  $" + amount;
                    }
                    content += " | New Balance: $" + newBalance;
                }
                case WITHDRAW -> {
                    if (isTranfer) {
                        content += "Transfer Initiated $" + amount + "  to  Account #" + otherAccountId;
                    } else {
                        content += "Withdraw $" + amount;
                    }
                    content += " | New Balance: $" + newBalance;
                }
                default -> content += "GOD MODE is activated to come here.";
            }

            //Adding the Transaction to the file.
            Path transactionDetailsPath = Paths.get(customerId + "_transaction_details.txt");
            Files.write(transactionDetailsPath, content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            System.out.println("Some error writing transaction to history.");
        }
    }

    /**
     * Get AccountId based on user credentials.
     * @param userName -> username string
     * @param password -> password string
     * @return accountId if one exists, null otherwise
     */
    private static String getLoginAccountId(String userName, String password) {
        try {
            String combination = userName + " " + password;
            Stream<String> lines = Files.lines(Paths.get( "userLoginInfo.txt"));

            // Filtering students based on the combination of user credentials
            List<String> users = lines.filter(s -> s.contains(combination)).collect(Collectors.toList());

            // Return the first found user with the combination or return null
            if (users.size() > 0) {
                return getAccountIdFromLoginInfo(users.get(0));
            } else {
                return null;
            }
        } catch (IOException exception1) {
            System.out.println("No users Exist in the files. Please add users.");
            return null;
        }
    }

    /**
     * Create the userLoginInfo file if it does not exist
     * @param userName -> username string
     * @param password -> password string
     * @param accountId -> accountId string
     * @return true if file was successfully created, false if it was unsuccessful
     */
    private static boolean createFileWithUser(String userName, String password, String accountId) {
        try {
            // Formatting the string for the proper format and creating the file
            String content = userName + " " + password + " " + accountId;
            Files.write(Paths.get("userLoginInfo.txt"), content.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException exception1) {
            System.out.println("A weird Random Error has occured");
            System.out.println("The Login File Does not Exist.");
            return false;
        }
        return true;
    }

    /**
     * parse username from a given line of loginInfo text file
     * @param s -> loginInfo line
     * @return return string
     */
    private static String getUserNameFromLoginInfo(String s) {
        return s.substring(0, s.indexOf(" "));
    }

    /**
     * parse password from a given line of loginInfo text file
     * @param s -> loginInfo line
     * @return password string
     */
    private static String getPasswordFromLoginInfo(String s) {
        return s.substring(s.indexOf(" ")+1, s.indexOf(" "));
    }

    /**
     * parse username from a given line of loginInfo text file
     * @param s -> loginInfo line
     * @return accountId string
     */
    private static String getAccountIdFromLoginInfo(String s) {
        return s.substring(s.indexOf(" ", s.indexOf(" ")+1)+1);
    }

    /**
     * Formatting the string for the account profile
     * @param firstName -> first name of customer
     * @param lastName -> last name of customer
     * @param accountBalance -> account balance of the customer
     * @param accountType -> account type of the customer
     * @return formatted string
     */
    private static String formatAccountProfile(String firstName, String lastName, double accountBalance, String accountType) {
        return firstName + " " + lastName +
                "\n" + accountBalance +
                "\n" + accountType;
    }
}
