package database.tables;

import java.sql.*;
import java.util.Objects;

/**
 * This class represents the BankAccount table in the database, with properties id, accountNumber
 * (account_number in the database) and balance. It contains CRUD methods such as insert, update and delete,
 * but also other helpful methods.
 */
public class BankAccount {
    private int id;
    private final int accountNumber;
    private long balance;

    /**
     * This constructor sets the account number, balance, and sets the
     * id to 0 by default.
     * @param accountNumber Bank account number
     * @param balance Bank account balance
     */
    private BankAccount(int accountNumber, long balance) {
        this.id = 0;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    /**
     * Attempts to create a new BankAccount instance.
     * @param id Bank account id
     * @param accountNumber Bank account number
     * @param balance Bank account balance
     * @return New BankAccount instance
     * @throws IllegalArgumentException Invalid parameters
     */
    public static BankAccount create(int id, int accountNumber, long balance) throws IllegalArgumentException {
        if (id < 0) {
            throw new IllegalArgumentException("Bank account ID must be equal to or greater than 0.");
        }
        if (accountNumber < 10000 || accountNumber > 99999) {
            throw new IllegalArgumentException("Bank account number must be between 10000 and 99999.");
        }
        if (balance < 0) {
            throw new IllegalArgumentException("Bank account balance must be equal to or greater than 0.");
        }
        BankAccount bankAccount = new BankAccount(accountNumber, balance);
        bankAccount.setId(id);
        return bankAccount;
    }

    /**
     * Saves the object to the database. It calls the insert() function if the id is equal to 0
     * (that means that the object has not been saved to the database yet, or it has been removed), or
     * the update() function.
     * @param connection Database connection
     * @throws SQLException Error occurred while inserting/updating
     */
    public synchronized void save(Connection connection) throws SQLException {
        if (id == 0) {
            insert(connection);
        } else {
            update(connection);
        }
    }

    /**
     * Executes an insert query on the BankAccount table. The bank account is saved to the database.
     * @param connection Database connection
     * @throws SQLException Error occurred while inserting
     */
    private synchronized void insert(Connection connection) throws SQLException {
        String insertQuery = "INSERT INTO BankAccount (account_number, balance) VALUES (?, ?)";
        connection.setAutoCommit(false);

        try (PreparedStatement statement = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, accountNumber);
            statement.setLong(2, balance);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet ids = statement.getGeneratedKeys()) {
                    if (ids.next()) {
                        this.id = ids.getInt(1);
                    }
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            System.out.println("Failed to insert bank account, rolling back.");
            connection.rollback();
            throw ex; // Propagate the exception
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * The bank account updates itself in the database.
     * @param connection Database connection
     * @throws SQLException Error occurred while updating
     */
    private synchronized void update(Connection connection) throws SQLException {
        String selectQuery = "SELECT * FROM BankAccount WITH (UPDLOCK, ROWLOCK) WHERE id = ?"; // Ensures concurrency safety
        String updateQuery = "UPDATE BankAccount SET balance = ? WHERE id = ?";

        connection.setAutoCommit(false);

        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
            selectStatement.setInt(1, this.id);

            ResultSet rs = selectStatement.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Account not found.");
            }

            try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                statement.setLong(1, this.balance);
                statement.setInt(2, this.id);
                statement.executeUpdate();
                connection.commit();
            }

        } catch (SQLException ex) {
            System.out.println("Failed to update bank account, rolling back.");
            connection.rollback();
            throw ex; // Propagate the exception
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * The bank account is deleted from the database and its id is set to 0.
     * @param connection Database connection
     * @throws SQLException Error occurred while deleting
     */
    public synchronized void delete(Connection connection) throws SQLException {
        String deleteQuery = "DELETE FROM BankAccount WHERE id = ?";
        connection.setAutoCommit(false);

        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, id);
            statement.executeUpdate();
            connection.commit();
            this.id = 0;
        } catch (SQLException ex) {
            System.out.println("Failed to delete bank account, rolling back.");
            connection.rollback();
            throw ex; // Propagate the exception
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Deposits a given amount of balance to the bank account.
     * The amount must be greater than 0.
     * @param amount Amount of money being deposited
     * @throws IllegalArgumentException Invalid deposit amount
     */
    public synchronized void deposit(long amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than 0.");
        }
        balance += amount;
    }

    /**
     * Withdraws a given amount of balance from the bank account.
     * The amount must be greater than zero, but also cannot be
     * greater than the balance.
     * @param amount Amount of money being withdrawn
     * @throws IllegalArgumentException Invalid withdraw amount
     */
    public synchronized void withdraw(long amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than 0.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Not enough balance on the bank account.");
        }
        balance -= amount;
    }

    /**
     * Retrieves the total amount of balance from the database. It is the sum of all
     * bank account balance. If no sum was found, 0 is returned.
     * @param connection Database connection
     * @return Total bank balance
     * @throws SQLException Error occurred while retrieving total balance
     */
    public static int getTotalBalance(Connection connection) throws SQLException {
        String selectQuery = "SELECT SUM(balance) AS total FROM BankAccount";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("total");
            } else {
                return 0;
            }
        }
    }

    /**
     * Retrieves the total amount of bank accounts in the database.
     * @param connection Database connection
     * @return Amount of bank accounts in the database
     * @throws SQLException Database error occurred while retrieving amount of accounts
     */
    public static int getAccountAmount(Connection connection) throws SQLException {
        String selectQuery = "SELECT COUNT(*) AS accounts FROM BankAccount";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("accounts");
            } else {
                return 0;
            }
        }
    }

    /**
     * Retrieves the current maximum account number in the BankAccount table. If there are no accounts,
     * 0 is returned instead.
     * @param connection Database connection
     * @return Highest account number, or 0 if not found
     * @throws SQLException Database error occurred while retrieving account number
     */
    public static int getMaxNumber(Connection connection) throws SQLException {
        String selectQuery = "SELECT MAX(account_number) AS max_number FROM BankAccount";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("max_number");
            } else {
                return 0;
            }
        }
    }

    /**
     * Retrieves an account with the given account number from the database. Returns a new BankAccount instance,
     * or null if not found.
     * @param accountNumber Bank account number
     * @param connection Database connection
     * @return New BankAccount instance, or null if not found
     * @throws SQLException Error occurred while retrieving bank account
     */
    public static BankAccount findByAccountNumber(int accountNumber, Connection connection) throws SQLException {
        String selectQuery = "SELECT id, balance FROM BankAccount WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                long balance = resultSet.getLong("balance");
                return BankAccount.create(id, accountNumber, balance);
            } else {
                return null;
            }
        }
    }

    /**
     * Sets the bank account id.
     * @param id New bank account id
     */
    public synchronized void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the bank account number.
     * @return Bank account number
     */
    public int getAccountNumber() {
        return this.accountNumber;
    }

    /**
     * Returns the bank account balance.
     * @return Bank account balance
     */
    public long getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount that = (BankAccount) o;
        return accountNumber == that.accountNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accountNumber);
    }
}
