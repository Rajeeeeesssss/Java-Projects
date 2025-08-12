import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.sql.*;



// Single-file Library Management System (file-backed persistence, Swing UI)
public class LibraryApp extends JFrame {

    // --- Data files
    private static final String BOOKS_FILE = "books.dat";
    private static final String MEMBERS_FILE = "members.dat";
    private static final String LOANS_FILE = "loans.dat";

    // --- In-memory data
    private List<Book> books = new ArrayList<>();
    private List<Member> members = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    // --- UI components (tables & models)
    private DefaultTableModel booksModel;
    private DefaultTableModel membersModel;
    private DefaultTableModel loansModel;

    // Fine per day late (example)
    private static final double FINE_PER_DAY = 10.0;

    public LibraryApp() {
        super("Library Management System");
        loadAll();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", buildBooksPanel());
        tabs.addTab("Members", buildMembersPanel());
        tabs.addTab("Issue / Return", buildIssueReturnPanel());
        tabs.addTab("Loans", buildLoansPanel());
        tabs.addTab("Search", buildSearchPanel());

        add(tabs, BorderLayout.CENTER);

        // Save on close
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                saveAll();
            }
        });
    }

    // -----------------------
    // Panels
    // -----------------------
    private JPanel buildBooksPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        booksModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "ISBN", "Total", "Available"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(booksModel);
        refreshBooksModel();

        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Book");
        JButton editBtn = new JButton("Edit Book");
        JButton delBtn = new JButton("Delete Book");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> showBookDialog(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showInfo("Select a book to edit."); return; }
            String id = (String) booksModel.getValueAt(r, 0);
            Book b = findBookById(id);
            showBookDialog(b);
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showInfo("Select a book to delete."); return; }
            String id = (String) booksModel.getValueAt(r, 0);
            if (confirm("Delete selected book?")) {
                deleteBook(id);
                refreshBooksModel();
            }
        });
        refreshBtn.addActionListener(e -> refreshBooksModel());

        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(delBtn);
        controls.add(refreshBtn);

        p.add(controls, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildMembersPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        membersModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Phone"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(membersModel);
        refreshMembersModel();

        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Member");
        JButton editBtn = new JButton("Edit Member");
        JButton delBtn = new JButton("Delete Member");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> showMemberDialog(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showInfo("Select a member to edit."); return; }
            String id = (String) membersModel.getValueAt(r,0);
            Member m = findMemberById(id);
            showMemberDialog(m);
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showInfo("Select a member to delete."); return; }
            String id = (String) membersModel.getValueAt(r,0);
            if (confirm("Delete selected member?")) {
                deleteMember(id);
                refreshMembersModel();
            }
        });
        refreshBtn.addActionListener(e -> refreshMembersModel());

        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(delBtn);
        controls.add(refreshBtn);

        p.add(controls, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildIssueReturnPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblMember = new JLabel("Member ID or Name:");
        JTextField txtMember = new JTextField();
        JLabel lblBook = new JLabel("Book ID or Title:");
        JTextField txtBook = new JTextField();
        JButton issueBtn = new JButton("Issue Book");
        JButton returnBtn = new JButton("Return Book");

        gbc.gridx=0; gbc.gridy=0; p.add(lblMember, gbc);
        gbc.gridx=1; gbc.gridy=0; p.add(txtMember, gbc);
        gbc.gridx=0; gbc.gridy=1; p.add(lblBook, gbc);
        gbc.gridx=1; gbc.gridy=1; p.add(txtBook, gbc);
        gbc.gridx=0; gbc.gridy=2; p.add(issueBtn, gbc);
        gbc.gridx=1; gbc.gridy=2; p.add(returnBtn, gbc);

        issueBtn.addActionListener(e -> {
            String mem = txtMember.getText().trim();
            String bk = txtBook.getText().trim();
            if (mem.isEmpty() || bk.isEmpty()) { showInfo("Enter member and book."); return; }
            Member m = findMemberByText(mem);
            Book b = findBookByText(bk);
            if (m==null) { showError("Member not found."); return; }
            if (b==null) { showError("Book not found."); return; }
            try {
                issueBook(b.getId(), m.getId());
                showInfo("Issued '" + b.getTitle() + "' to " + m.getName());
                refreshAllTables();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        returnBtn.addActionListener(e -> {
            String mem = txtMember.getText().trim();
            String bk = txtBook.getText().trim();
            if (mem.isEmpty() || bk.isEmpty()) { showInfo("Enter member and book."); return; }
            Member m = findMemberByText(mem);
            Book b = findBookByText(bk);
            if (m==null) { showError("Member not found."); return; }
            if (b==null) { showError("Book not found."); return; }
            try {
                double fine = returnBook(b.getId(), m.getId());
                if (fine > 0) {
                    showInfo(String.format("Book returned. Fine due: ₹%.2f", fine));
                } else {
                    showInfo("Book returned. No fine.");
                }
                refreshAllTables();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        return p;
    }

    private JPanel buildLoansPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        loansModel = new DefaultTableModel(new String[]{"LoanID","BookID","Book","MemberID","Member","IssuedOn","DueOn","ReturnedOn"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(loansModel);
        refreshLoansModel();

        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh");
        JButton deleteBtn = new JButton("Delete Loan Record");
        refreshBtn.addActionListener(e -> refreshLoansModel());
        deleteBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { showInfo("Select a loan record."); return; }
            String loanId = (String) loansModel.getValueAt(r,0);
            if (confirm("Delete loan record (this won't change book availability)?")) {
                deleteLoan(loanId);
                refreshLoansModel();
            }
        });

        controls.add(refreshBtn);
        controls.add(deleteBtn);
        p.add(controls, BorderLayout.NORTH);
        return p;
    }

    private JPanel buildSearchPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtQuery = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Books", "Members"});
        JButton searchBtn = new JButton("Search");

        gbc.gridx=0; gbc.gridy=0; gbc.weightx=0.1; top.add(new JLabel("Search:"), gbc);
        gbc.gridx=1; gbc.gridy=0; gbc.weightx=0.7; top.add(txtQuery, gbc);
        gbc.gridx=2; gbc.gridy=0; gbc.weightx=0.1; top.add(cbType, gbc);
        gbc.gridx=3; gbc.gridy=0; gbc.weightx=0.1; top.add(searchBtn, gbc);

        JTextArea results = new JTextArea();
        results.setEditable(false);
        JScrollPane sp = new JScrollPane(results);

        searchBtn.addActionListener(e -> {
            String q = txtQuery.getText().trim().toLowerCase();
            if (q.isEmpty()) { results.setText("Enter query."); return; }
            if (cbType.getSelectedItem().equals("Books")) {
                StringBuilder sb = new StringBuilder();
                for (Book b: books) {
                    if (b.getTitle().toLowerCase().contains(q) ||
                        b.getAuthor().toLowerCase().contains(q) ||
                        b.getId().toLowerCase().contains(q) ||
                        b.getIsbn().toLowerCase().contains(q)) {
                        sb.append(b).append("\n");
                    }
                }
                results.setText(sb.length()==0? "No books found." : sb.toString());
            } else {
                StringBuilder sb = new StringBuilder();
                for (Member m: members) {
                    if (m.getName().toLowerCase().contains(q) ||
                        m.getEmail().toLowerCase().contains(q) ||
                        m.getId().toLowerCase().contains(q)) {
                        sb.append(m).append("\n");
                    }
                }
                results.setText(sb.length()==0? "No members found." : sb.toString());
            }
        });

        p.add(top, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // -----------------------
    // Business logic
    // -----------------------
    private void issueBook(String bookId, String memberId) throws Exception {
        Book b = findBookById(bookId);
        Member m = findMemberById(memberId);
        if (b == null) throw new Exception("Book not found.");
        if (m == null) throw new Exception("Member not found.");
        if (b.getAvailable() <= 0) throw new Exception("No available copies to issue.");

        // create loan (14 days)
        LocalDate issued = LocalDate.now();
        LocalDate due = issued.plusDays(14);
        Loan loan = new Loan(UUID.randomUUID().toString(), bookId, memberId, issued, due, null);
        loans.add(loan);
        b.setAvailable(b.getAvailable() - 1);
        saveAll();
    }

    private double returnBook(String bookId, String memberId) throws Exception {
        // find active loan
        Loan found = null;
        for (Loan l : loans) {
            if (l.getBookId().equals(bookId) && l.getMemberId().equals(memberId) && l.getReturnedOn() == null) {
                found = l; break;
            }
        }
        if (found == null) throw new Exception("No active loan found for that book and member.");

        LocalDate returned = LocalDate.now();
        found.setReturnedOn(returned);

        // update book availability
        Book b = findBookById(bookId);
        if (b != null) b.setAvailable(b.getAvailable() + 1);

        // compute fine if overdue
        long daysLate = ChronoUnit.DAYS.between(found.getDueOn(), returned);
        double fine = daysLate > 0 ? daysLate * FINE_PER_DAY : 0.0;

        saveAll();
        return fine;
    }

    // CRUD operations
    private void saveAll() {
        saveList(BOOKS_FILE, books);
        saveList(MEMBERS_FILE, members);
        saveList(LOANS_FILE, loans);
    }
    private void loadAll() {
        books = loadList(BOOKS_FILE);
        members = loadList(MEMBERS_FILE);
        loans = loadList(LOANS_FILE);
    }

    private <T extends Serializable> void saveList(String fname, List<T> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fname))) {
            oos.writeObject(list);
        } catch (Exception e) {
            System.err.println("Failed to save " + fname + ": " + e.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private <T extends Serializable> List<T> loadList(String fname) {
        File f = new File(fname);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Failed to load " + fname + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void refreshBooksModel() {
        booksModel.setRowCount(0);
        for (Book b : books) {
            booksModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getTotalCopies(), b.getAvailable()});
        }
    }
    private void refreshMembersModel() {
        membersModel.setRowCount(0);
        for (Member m : members) {
            membersModel.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone()});
        }
    }
    private void refreshLoansModel() {
        loansModel.setRowCount(0);
        for (Loan l : loans) {
            Book b = findBookById(l.getBookId());
            Member m = findMemberById(l.getMemberId());
            loansModel.addRow(new Object[]{
                l.getId(), l.getBookId(), (b==null? "—" : b.getTitle()),
                l.getMemberId(), (m==null? "—" : m.getName()),
                l.getIssuedOn(), l.getDueOn(), l.getReturnedOn()
            });
        }
    }

    private void refreshAllTables() {
        refreshBooksModel();
        refreshMembersModel();
        refreshLoansModel();
    }

    private Book findBookById(String id) {
        for (Book b: books) if (b.getId().equals(id)) return b;
        return null;
    }
    private Member findMemberById(String id) {
        for (Member m: members) if (m.getId().equals(id)) return m;
        return null;
    }
    private Book findBookByText(String txt) {
        txt = txt.toLowerCase();
        for (Book b: books) if (b.getId().toLowerCase().equals(txt) || b.getTitle().toLowerCase().contains(txt)) return b;
        return null;
    }
    private Member findMemberByText(String txt) {
        txt = txt.toLowerCase();
        for (Member m: members) if (m.getId().toLowerCase().equals(txt) || m.getName().toLowerCase().contains(txt)) return m;
        return null;
    }

    private void deleteBook(String id) {
        books.removeIf(b -> b.getId().equals(id));
        saveAll();
    }
    private void deleteMember(String id) {
        members.removeIf(m -> m.getId().equals(id));
        saveAll();
    }
    private void deleteLoan(String id) {
        loans.removeIf(l -> l.getId().equals(id));
        saveAll();
    }

    // -----------------------
    // Dialogs for add/edit
    // -----------------------
    private void showBookDialog(Book book) {
        boolean isEdit = book != null;
        JTextField title = new JTextField(isEdit? book.getTitle(): "");
        JTextField author = new JTextField(isEdit? book.getAuthor(): "");
        JTextField isbn = new JTextField(isEdit? book.getIsbn(): "");
        JTextField total = new JTextField(isEdit? String.valueOf(book.getTotalCopies()): "1");

        JPanel panel = new JPanel(new GridLayout(0,1,6,6));
        panel.add(new JLabel("Title:")); panel.add(title);
        panel.add(new JLabel("Author:")); panel.add(author);
        panel.add(new JLabel("ISBN:")); panel.add(isbn);
        panel.add(new JLabel("Total copies:")); panel.add(total);

        int res = JOptionPane.showConfirmDialog(this, panel, (isEdit? "Edit Book":"Add Book"), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String t = title.getText().trim();
                String a = author.getText().trim();
                String i = isbn.getText().trim();
                int tot = Integer.parseInt(total.getText().trim());
                if (t.isEmpty()) { showError("Title required."); return; }
                if (isEdit) {
                    book.setTitle(t); book.setAuthor(a); book.setIsbn(i);
                    // adjust available based on change in total copies
                    int diff = tot - book.getTotalCopies();
                    book.setTotalCopies(tot);
                    book.setAvailable(book.getAvailable() + diff);
                } else {
                    Book b = new Book(UUID.randomUUID().toString(), t, a, i, tot, tot);
                    books.add(b);
                }
                saveAll();
                refreshBooksModel();
            } catch (NumberFormatException ex) {
                showError("Total copies must be a number.");
            }
        }
    }

    private void showMemberDialog(Member member) {
        boolean isEdit = member != null;
        JTextField name = new JTextField(isEdit? member.getName(): "");
        JTextField email = new JTextField(isEdit? member.getEmail(): "");
        JTextField phone = new JTextField(isEdit? member.getPhone(): "");

        JPanel panel = new JPanel(new GridLayout(0,1,6,6));
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Email:")); panel.add(email);
        panel.add(new JLabel("Phone:")); panel.add(phone);

        int res = JOptionPane.showConfirmDialog(this, panel, (isEdit? "Edit Member":"Add Member"), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String n = name.getText().trim();
            String e = email.getText().trim();
            String p = phone.getText().trim();
            if (n.isEmpty()) { showError("Name required."); return; }

            if (isEdit) {
                member.setName(n); member.setEmail(e); member.setPhone(p);
            } else {
                Member m = new Member(UUID.randomUUID().toString(), n, e, p);
                members.add(m);
            }
            saveAll();
            refreshMembersModel();
        }
    }
    // -----------------------
    // DBConnection
    // -----------------------

    public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USER = "root"; // your MySQL username
    private static final String PASS = "Rg@#1000"; // your MySQL password

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
    
    // -----------------------
    // DBConnection
    // -----------------------



public void loadBooks() {
    try (Connection con = DBConnection.getConnection()) {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM books");
        while (rs.next()) {
            System.out.println(rs.getString("title") + " by " + rs.getString("author"));
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
     
    public void addBook(String id, String title, String author, String isbn, int total, int available) {
    try (Connection con = DBConnection.getConnection()) {
        String sql = "INSERT INTO books (id, title, author, isbn, totalCopies, available) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, id);
        ps.setString(2, title);
        ps.setString(3, author);
        ps.setString(4, isbn);
        ps.setInt(5, total);
        ps.setInt(6, available);
        ps.executeUpdate();
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // -----------------------
    // Utils
    // -----------------------
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private boolean confirm(String message) {
        int c = JOptionPane.showConfirmDialog(this, message, "Confirm", JOptionPane.YES_NO_OPTION);
        return c == JOptionPane.YES_OPTION;
    }

    // -----------------------
    // Main
    // -----------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // optional look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            LibraryApp app = new LibraryApp();
            app.setVisible(true);
        });
    }

    // -----------------------
    // Data model classes
    // -----------------------
    static class Book implements Serializable {
        private String id;
        private String title;
        private String author;
        private String isbn;
        private int totalCopies;
        private int available;

        public Book(String id, String title, String author, String isbn, int totalCopies, int available) {
            this.id = id; this.title = title; this.author = author; this.isbn = isbn;
            this.totalCopies = totalCopies; this.available = available;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getIsbn() { return isbn; }
        public int getTotalCopies() { return totalCopies; }
        public int getAvailable() { return available; }

        public void setTitle(String title) { this.title = title; }
        public void setAuthor(String author) { this.author = author; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
        public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
        public void setAvailable(int available) { this.available = available; }

        public String toString() {
            return String.format("%s | %s | %s | Available: %d/%d", id, title, author, available, totalCopies);
        }
    }

    static class Member implements Serializable {
        private String id;
        private String name;
        private String email;
        private String phone;

        public Member(String id, String name, String email, String phone) {
            this.id = id; this.name = name; this.email = email; this.phone = phone;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }

        public void setName(String name) { this.name = name; }
        public void setEmail(String email) { this.email = email; }
        public void setPhone(String phone) { this.phone = phone; }

        public String toString() {
            return String.format("%s | %s | %s | %s", id, name, email, phone);
        }
    }

    static class Loan implements Serializable {
        private String id;
        private String bookId;
        private String memberId;
        private LocalDate issuedOn;
        private LocalDate dueOn;
        private LocalDate returnedOn;

        public Loan(String id, String bookId, String memberId, LocalDate issuedOn, LocalDate dueOn, LocalDate returnedOn) {
            this.id = id; this.bookId = bookId; this.memberId = memberId; this.issuedOn = issuedOn; this.dueOn = dueOn; this.returnedOn = returnedOn;
        }

        public String getId() { return id; }
        public String getBookId() { return bookId; }
        public String getMemberId() { return memberId; }
        public LocalDate getIssuedOn() { return issuedOn; }
        public LocalDate getDueOn() { return dueOn; }
        public LocalDate getReturnedOn() { return returnedOn; }
        public void setReturnedOn(LocalDate d) { this.returnedOn = d; }
    }
}
