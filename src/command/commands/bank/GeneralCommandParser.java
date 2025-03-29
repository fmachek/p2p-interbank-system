package command.commands.bank;

import command.exceptions.InvalidParameterException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This interface contains some default methods for parsing the Command parameters.
 */
public interface GeneralCommandParser {
    /**
     * Parses the string of parameters. This method
     * expects parameters in this format: [account_number]/[bank_code]
     * @param paramString String of parameters to be parsed
     * @return Array of Objects (account number (int) and bank code (String))
     * @throws InvalidParameterException Invalid or no parameters
     */
    default Object[] parseAccountNumberAndBankCode(String paramString, String commandName) throws InvalidParameterException {
        if (paramString == null) {
            throw new InvalidParameterException("Invalid parameters (usage: " + commandName + " <account_number>/<bank_code>).");
        }

        Pattern pattern = Pattern.compile("^(\\d{5})/(\\d{1,4}\\.\\d{1,4}\\.\\d{1,4}\\.\\d{1,4})$");
        Matcher matcher = pattern.matcher(paramString);

        if (matcher.find()) {
            String accountNumberString = matcher.group(1);
            String bankCode = matcher.group(2);

            int accountNumber = Integer.parseInt(accountNumberString);

            Object[] params = new Object[2];
            params[0] = accountNumber;
            params[1] = bankCode;
            return params;
        } else {
            throw new InvalidParameterException("Invalid parameters (usage: " + commandName + " <account_number>/<bank_code>).");
        }
    }

    /**
     * Parses the string of parameters. This method
     * expects parameters in this format: [account_number]/[bank_code] [number]
     * @param paramString String of parameters to be parsed
     * @return Array of Objects (account number (int), bank code (String) and amount (long))
     * @throws InvalidParameterException Invalid or no parameters
     */
    default Object[] parseAmount(String paramString, String commandName) throws InvalidParameterException {
        if (paramString == null) {
            throw new InvalidParameterException("Invalid parameters (usage: " + commandName + " <account_number>/<bank_code> <amount>).");
        }

        Pattern pattern = Pattern.compile("^(\\d{5})/(\\d{1,4}\\.\\d{1,4}\\.\\d{1,4}\\.\\d{1,4}) (\\d{1,19})$");
        Matcher matcher = pattern.matcher(paramString);

        if (matcher.find()) {
            String accountNumberString = matcher.group(1);
            String bankCode = matcher.group(2);
            String amountString = matcher.group(3);

            int accountNumber = Integer.parseInt(accountNumberString);
            long amount = Long.parseLong(amountString);

            Object[] params = new Object[3];
            params[0] = accountNumber;
            params[1] = bankCode;
            params[2] = amount;
            return params;
        } else {
            throw new InvalidParameterException("Invalid parameters (usage: " + commandName + " <account_number>/<bank_code> <amount>).");
        }
    }
}
