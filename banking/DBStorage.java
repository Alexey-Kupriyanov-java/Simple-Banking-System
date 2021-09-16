package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Random;

public class DBStorage implements Storage {
    String dbName;
    String url;
    SQLiteDataSource dataSource;

    public DBStorage(String dbName) {
        this.dbName = dbName;
        this.url = "jdbc:sqlite:" + dbName;
        this.dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL," +
                        "balance INTEGER DEFAULT 0)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String create(int bin) {
        String number;
        int accountIdentifier;
        int checksum;
        String pin;

        accountIdentifier = getRandomIdentifier();
        checksum = getChecksum(bin, accountIdentifier);
        pin = getRandomPin();

        number = bin + String.format("%09d", accountIdentifier) + checksum;

        String sql = "INSERT INTO card (number, pin) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                statement.setString(2, pin);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return number;
    }

    @Override
    public String getPin(String number) {
        String pin = null;
        String sql = "SELECT pin FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        pin = resultSet.getString(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pin;
    }

    @Override
    public int getBalance(String number) {
        int balance = -1;
        String sql = "SELECT balance FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        balance = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean logIn(String number, String pin) {
        String sql = "SELECT pin FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        return pin.equals(resultSet.getString(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addIncome(String number, int income) {
        int balance = getBalance(number);
        String sql = "UPDATE card SET balance = ? WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, balance + income);
                statement.setString(2, number);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean transfer(String number, String toNumber, int money) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            addIncome(number, money * -1);
            addIncome(toNumber, money);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;
    }

    @Override
    public boolean consist(String number) {
        int id = -1;
        String sql = "SELECT id FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        id = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id != -1;
    }

    @Override
    public void closeAccount(String number) {
        String sql = "DELETE FROM card WHERE number = ?";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, number);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getRandomIdentifier() {
        Random rnd = new Random();
        return rnd.nextInt(1_000_000_000);
    }

    private static int getChecksum(int bin, int accountIdentifier) {
        String[] cardNumberWithoutChecksum = (bin + String.format("%09d", accountIdentifier)).split("");
        int sum = 0;
        for (int i = 0; i < cardNumberWithoutChecksum.length; i++) {
            int digit = Integer.parseInt(cardNumberWithoutChecksum[i]);
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        for (int i = 0; i < 10; i++) {
            if ((sum + i) % 10 == 0) {
                return i;
            }
        }
        return 0;
    }

    private static String getRandomPin() {
        Random rnd = new Random();
        return String.format("%04d", rnd.nextInt(10_000));
    }
}
