/**
 * Customer.java
 * This class contains the structure of the Customer.
 * A customer has name, id, password, balance and account type.
 */
public class Customer {

    // Fields
    String firstName, lastName;
    String userId, password;
    double accountBalance;
    String accountType;
    private String accountId;

    /**
     * Customer constructor for creating a NEW customer
     */
    public Customer(String fName, String lName, String userId, String password, double accountBalance, String accountType) {
        this.firstName = fName;
        this.lastName = lName;
        this.userId = userId;
        this.password = password;
        this.accountBalance = accountBalance;
        this.accountType = accountType;
        this.accountId = BankHelper.generateAccountID();
        boolean addedUserToLoginInfo = BankHelper.addUserToUserLoginInfo(userId, password, accountId);
        if(addedUserToLoginInfo) {
            BankHelper.createAccountFiles(this);
            System.out.println("Successfully created the customer Profile.");
        } else {
            this.accountId = "";
            System.out.println("There was error creating the customer. Please Re-enter Info.");
        }
    }

    /**
     * Customer Constructor to create a pre-existing customer
     */
    public Customer(String firstName, String lastName, String userId, String password, double accountBalance, String accountType, String accountId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userId = userId;
        this.password = password;
        this.accountBalance = accountBalance;
        this.accountType = accountType;
        this.accountId = accountId;
    }

    /**
     * This returns the account ID
     * @return accountId -> The account's ID number
     */
    public String getAccountId() {
        return accountId;
    }

    public String getAccountDetails() {
        return "\tHello " + firstName + "!" +
                "\n\tAccount Number: " + accountId +
                "\n\tAccount Type: " + accountType +
                "\n\tAccount Balance: $" + accountBalance;
    }

    /**
     * This method will handle the deposit
     * @param amount -> The amount being deposited
     */
    public void depositMoney(double amount) {
        boolean b = BankHelper.depositMoney(accountId, amount, false, null);
        if (b) {
            accountBalance = accountBalance + amount;
            System.out.println("The deposit was successful!");
        }
    }

    /**
     * This method will handle a withdraw
     * @param amount -> The amount being withdrawn
     */
    public void withdrawMoney(double amount) {
        boolean b = BankHelper.withdrawMoney(accountId, amount, false, null);
        if (b) {
            accountBalance = accountBalance - Math.abs(amount);
            System.out.println("The withdraw was successful!");
        }
    }

    /**
     * This method wil handle a transfer between 2 different accounts
     * @param amount -> The amount to be transferred
     * @param otherAccountId -> The other account the amount is transfered to
     */
    public void transferMoney(double amount, String otherAccountId) {
        boolean b = BankHelper.transferMoney(accountId, amount,otherAccountId);
        if (b) {
            accountBalance = accountBalance - amount;
            System.out.println("The transfer was successful!");
        }
    }
}
