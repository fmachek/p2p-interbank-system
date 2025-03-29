USE [database_name]

-- Bank account table
CREATE TABLE BankAccount(
id INT PRIMARY KEY IDENTITY(1, 1),
account_number INT NOT NULL UNIQUE,
balance BIGINT NOT NULL,
CHECK(account_number >= 10000 AND account_number <= 99999),
CHECK(balance >= 0),
);

-- Trigger that checks if a bank account has balance
-- before deleting it.
CREATE TRIGGER CheckBalanceBeforeDelete
ON BankAccount
INSTEAD OF DELETE
AS
BEGIN
    IF EXISTS (
        SELECT 1 FROM deleted WHERE balance > 0
    )
    BEGIN
        THROW 50000, 'Cannot delete a bank account with balance greater than 0.', 1;
        RETURN;
    END

    DELETE FROM BankAccount WHERE id IN (SELECT id FROM deleted);
END;