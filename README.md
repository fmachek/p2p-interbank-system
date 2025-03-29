# p2p-interbank-system
This project simulates a node in a P2P (Peer-to-peer) interbank system.

## Documentation overview
- [Requirements](#requirements)
- [How to install and run](#how-to-install-and-run)
   * [Install required libraries](#install-required-libraries)
   * [Edit the configuration file](#edit-the-configuration-file)
   * [Import the database](#import-the-database)
   * [Run the program](#run-the-program)
- [Usage](#usage)
   * [Bank code - BC](#bank-code---bc)
   * [Bank amount - BA](#bank-amount---ba)
   * [Bank number - BN](#bank-number---bn)
   * [Account create - AC](#account-create---ac)
   * [Account removal - AR](#account-removal---ar)
   * [Account balance - AB](#account-balance---ab)
   * [Account deposit - AD](#account-deposit---ad)
   * [Account withdrawal - AW](#account-withdrawal---aw)
- [Logging](#logging)
   * [Log file example](#log-file-example)
- [Sources - Research](#sources---research)
- [Sources - ChatGPT](#sources---chatgpt)
- [Old project sources](#old-project-sources)

## Requirements
Please ensure that your PC meets the following requirements:

* **Java 21 or higher** installed

* **Microsoft JDBC Driver 12.8 for SQL Server** installed

* **Access to a Microsoft SQL Server database**

## How to install and run

### Install required libraries
Please install the libraries listed if you have not yet.

1. Download **Microsoft JDBC Driver 12.8 for SQL Server** from Microsoft [here](https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server?view=sql-server-ver16).

2. Unzip the folder you downloaded.

### Edit the configuration file
Before you attempt to run the program, you must set up the configuration file and provide it with your database credentials and P2P settings, for example the IP address the program will run on.
1. Navigate to the root folder of the project.

2. Open the **config.properties** file in a text editor.

3. For the following parameters, fill out your database credentials:

    * **server** - address of your database server

    * **database** - name of your database

    * **uid** - name of your database user

    * **pwd** - password for your database login

4. For the following parameters, fill out the peer to peer settings:

    * **address** - the IPv4 address the program will run on

    * **port** - the port the program will run on (65525 - 65535)

**Example of a correctly configured config.ini file**:
```
# Database credentials
address=192.168.0.100
database=bank
username=banker
password=p455w0rd

# P2P settings
host_address=192.168.0.10
port=65525
```

### Import the database
This program only requires one database table named BankAccount.
You can import it by copying the following query and executing it
in your own Microsoft SQL Server database. You can also find the query
in the **db/database.sql** file.

Make sure to replace 'your_database_name' in the first line with your
own database name.

```sql
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
        SELECT 1 FROM deleted WHERE balance <> 0
    )
    BEGIN
        THROW 50000, 'Cannot delete a bank account with balance greater than 0.', 1;
        RETURN;
    END

    DELETE FROM BankAccount WHERE id IN (SELECT id FROM deleted);
END;
```

To insert 3 test data bank accounts, execute this query:

```sql
INSERT INTO BankAccount (account_number, balance) VALUES (10000, 2500);
INSERT INTO BankAccount (account_number, balance) VALUES (10001, 5000);
INSERT INTO BankAccount (account_number, balance) VALUES (10002, 7500);
```

### Run the program
1. In the command line, navigate to the Microsoft JDBC Driver folder you unzipped.

2. Navigate to the **jars** directory.

3. Copy the absolute path to **mssql-jdbc-12.8.1.jre11.jar**.

4. Navigate to the root folder of this project.

5. Insert your own JDBC driver path and run the command:

    ```
    java -cp "p2p-interbank-system.jar;<path_to_jdbc_jar>" Main
    ```

   This command starts the program and the JDBC driver is included.

## Usage

If the configuration file is configured correctly, the program will start running on the assigned
IP address and port. It will start accepting peers and communicate with them.

The node hosting the program expects messages in a certain format. Messages which don't follow
the format will result in an invalid command message. When an invalid command call is sent, or an error occurs, a message
in this format is sent:

```
ER Something went wrong.
```

### Bank code - BC

The BC command returns the bank code of the node. The bank code is the IP address the node is running on.

```
BC
BC 192.168.0.10
```

### Bank amount - BA

The BA command returns the total amount of balance in the bank.

```
BA
BA 6028723
```

### Bank number - BN

The BN command returns the amount of bank accounts in the bank.

```
BN
BN 42
```

### Account create - AC

The AC command creates a new bank account and returns the assigned account number along
with the bank code.

```
AC
AC 42042/192.168.0.100
```

### Account removal - AR

The AR command removes a specified bank account (if it exists). For account removal
to be possible, the balance on that account must be zero.

The command parameters must be in this format:
```
AR <account_number>/<bank_code>
```

```
AR 42042/192.168.0.100
AR
```

### Account balance - AB

The AB command returns the current balance on a specified bank account.

The command parameters must be in this format:
```
AB <account_number>/<bank_code>
```

```
AB 42042/192.168.0.100
AB 314
```

### Account deposit - AD

The AD command deposits a given amount of money to a bank account.

The command parameters must be in this format:
```
AD <account_number>/<bank_code> <amount>
```

```
AD 42042/192.168.0.100 100
AD
```

### Account withdrawal - AW

The AW command withdraws a given amount of money from a bank account. There must be enough
balance to withdraw for the change to take effect.

The command parameters must be in this format:
```
AW <account_number>/<bank_code> <amount>
```

```
AW 42042/192.168.0.100 50
AW
```

## Logging
Most processes are logged in the **node.log** file. Every log has a severity level, timestamp, and more.

### Log file example
```
úno 07, 2025 7:57:04 ODP. peer.HostPeer start
INFO: Peer at /127.0.0.1:58720 connected.
úno 07, 2025 7:57:07 ODP. command.commands.bank.BankCodeCommand execute
INFO: Peer at /127.0.0.1:58720 used command BC.
```

## Sources - Research

### Stack Overflow

- https://stackoverflow.com/questions/130794/what-is-dependency-injection
- https://stackoverflow.com/questions/17837117/java-sending-multiple-parameters-to-method
- https://stackoverflow.com/questions/3570762/how-to-timeout-a-read-on-java-socket

### W3Schools

- https://www.w3schools.com/java/java_regex.asp
- https://www.w3schools.com/sql/sql_update.asp

### Regex101
I used this website to test my regular expressions.
- https://regex101.com/

### GitHub Docs
I used the information provided in the GitHub Docs to create this README.
- https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax

### martinfowler.com
- https://www.martinfowler.com/eaaCatalog/activeRecord.html

### Indirect sources
These are sources I used while originally working on this project in Python. Of course,
the project is in Java, so not many sources from this list were used at the end. Still,
I feel like they are worth mentioning.
- https://docs.python.org/3/howto/logging.html
- https://realpython.com/python-logging/
- https://www.w3schools.com/python/gloss_python_function_arbitrary_keyword_arguments.asp
- https://www.youtube.com/watch?v=Vh__2V2tXUM
- https://stackoverflow.com/questions/1507082/python-is-it-bad-form-to-raise-exceptions-within-init
- https://www.geeksforgeeks.org/python-circular-imports/
- https://refactoring.guru/design-patterns/command/python/example