

 Project Overview**

* **Name:** Java Swing-Based Library Management System
* **Type:** Desktop Application
* **Language:** Java (Core + Swing)
* **Data Storage:** File-based serialization (`.dat` files)
* **Purpose:** Manage books, members, and loan records in a library.

---

## **🛠 Features**

1. **Book Management**

   * Add new books with title, author, ISBN, and total copies.
   * Edit existing book details.
   * Delete books from the system.
   * Track total and available copies automatically.

2. **Member Management**

   * Add new members with name, email, and phone number.
   * Edit existing member details.
   * Delete members.
   * View full member list.

3. **Issue / Return System**

   * Issue a book to a member.
   * Return a book and automatically update availability.
   * Fine calculation for late returns (default ₹10/day overdue).

4. **Loan Records**

   * View all loan transactions in a table.
   * Track issue date, due date, and return date.
   * Delete old loan records if needed.

5. **Search Function**

   * Search books by title, author, ISBN, or ID.
   * Search members by name, email, or ID.

6. **Data Persistence**

   * Stores books, members, and loan data in `.dat` files.
   * Automatically loads data on startup.
   * Saves changes immediately after operations.

7. **User Interface**

   * Java Swing-based GUI.
   * Tabbed layout for easy navigation.
   * Adaptive window size and resizable tables.

8. **Extensibility**

   * Easy to integrate with MySQL or SQLite.
   * Can be expanded for multi-user and online access.

---
 Project Structure**

```
LibraryApp.java      # Main application file
books.dat            # Serialized book data
members.dat          # Serialized member data
loans.dat            # Serialized loan data
```

---
 Installation & Setup**

1. **Install Java**

   * Make sure Java JDK 8 or later is installed.
   * Check version:

     ```bash
     java -version
     javac -version
     ```

2. **Download Source Code**

   * Save `LibraryApp.java` to a folder.

3. **Compile the Application**

   ```bash
   javac LibraryApp.java
   ```

4. **Run the Application**

   ```bash
   java LibraryApp
   ```

5. **Data Storage**

   * The program creates `books.dat`, `members.dat`, and `loans.dat` automatically in the same folder.

---

 How It Works**

* **On Startup:** Reads `.dat` files into in-memory lists.
* **When Adding / Editing / Deleting:** Updates the in-memory list and saves to file.
* **Issue Book:** Checks availability, reduces count, creates loan record.
* **Return Book:** Increases available count, updates return date, calculates fine.
* **Search:** Filters in-memory data without affecting files.

