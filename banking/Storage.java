package banking;

public interface Storage {
    String create(int bin);

    String getPin(String number);
    int getBalance(String number);

    boolean logIn(String number, String pin);

    void addIncome(String number, int income);
    boolean transfer(String number, String toNumber, int money);

    default boolean checkValidNumber(String number) {
        String[] digits = number.split("");
        int sum = 0;
        for (int i = 0; i < digits.length - 1; i++) {
            int digit = Integer.parseInt(digits[i]);
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (sum + Integer.parseInt(digits[digits.length - 1])) % 10 == 0;
    }

    boolean consist(String number);

    void closeAccount(String number);

    //Account select(String cardNumber, String pin);
    
    //void update(Account acount);

    //boolean consist(Account account);
}
