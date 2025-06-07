import java.io.*;
import java.util.*;

// Model Classes
class Book implements Serializable {
    int bookId;
    String title;
    String author;
    int availableCopies;

    public Book(int bookId, String title, String author, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.availableCopies = availableCopies;
    }

    @Override
    public String toString() {
        return bookId + ": " + title + " by " + author + " (Available: " + availableCopies + ")";
    }
}

class User implements Serializable {
    String userId;
    String name;
    String password;

    public User(String userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
    }
}

class Transaction implements Serializable {
    String userId;
    int bookId;
    Date issuedDate;
    Date returnDate;

    public Transaction(String userId, int bookId, Date issuedDate) {
        this.userId = userId;
        this.bookId = bookId;
        this.issuedDate = issuedDate;
        this.returnDate = null;
    }

    public void returnBook() {
        this.returnDate = new Date();
    }
}

// File Handler
class FileHandler {
    public static <T> void saveToFile(String fileName, List<T> data) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
        oos.writeObject(data);
        oos.close();
    }

    public static <T> List<T> readFromFile(String fileName) throws IOException, ClassNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) return new ArrayList<>();

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        List<T> data = (List<T>) ois.readObject();
        ois.close();
        return data;
    }
}

// Main System
public class SmartLibrarySystem {
    static Scanner scanner = new Scanner(System.in);
    static List<Book> books;
    static List<User> users;
    static List<Transaction> transactions;

    public static void main(String[] args) throws Exception {
        books = FileHandler.readFromFile("books.dat");
        users = FileHandler.readFromFile("users.dat");
        transactions = FileHandler.readFromFile("transactions.dat");

        System.out.println("Welcome to Smart Library System");
        while (true) {
            System.out.println("1. Register\n2. Login\n3. Admin\n4. Exit");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: registerUser(); break;
                case 2: loginUser(); break;
                case 3: adminPanel(); break;
                case 4:
                    FileHandler.saveToFile("books.dat", books);
                    FileHandler.saveToFile("users.dat", users);
                    FileHandler.saveToFile("transactions.dat", transactions);
                    System.exit(0);
            }
        }
    }

    static void registerUser() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pwd = scanner.nextLine();
        users.add(new User(id, name, pwd));
        System.out.println("User registered successfully!");
    }

    static void loginUser() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Password: ");
        String pwd = scanner.nextLine();

        for (User u : users) {
            if (u.userId.equals(id) && u.password.equals(pwd)) {
                userMenu(u);
                return;
            }
        }
        System.out.println("Invalid credentials.");
    }

    static void userMenu(User user) {
        while (true) {
            System.out.println("1. View Books\n2. Borrow Book\n3. Return Book\n4. Logout");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: viewBooks(); break;
                case 2: borrowBook(user); break;
                case 3: returnBook(user); break;
                case 4: return;
            }
        }
    }

    static void viewBooks() {
        for (Book b : books) {
            System.out.println(b);
        }
    }

    static void borrowBook(User user) {
        viewBooks();
        System.out.print("Enter Book ID to borrow: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        for (Book b : books) {
            if (b.bookId == bookId && b.availableCopies > 0) {
                b.availableCopies--;
                transactions.add(new Transaction(user.userId, bookId, new Date()));
                System.out.println("Book borrowed successfully.");
                return;
            }
        }
        System.out.println("Book not available.");
    }

    static void returnBook(User user) {
        for (Transaction t : transactions) {
            if (t.userId.equals(user.userId) && t.returnDate == null) {
                System.out.println("Borrowed Book ID: " + t.bookId);
            }
        }
        System.out.print("Enter Book ID to return: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        for (Transaction t : transactions) {
            if (t.userId.equals(user.userId) && t.bookId == bookId && t.returnDate == null) {
                t.returnBook();
                for (Book b : books) {
                    if (b.bookId == bookId) b.availableCopies++;
                }
                System.out.println("Book returned successfully.");
                return;
            }
        }
        System.out.println("No such borrowed book found.");
    }

    static void adminPanel() {
        System.out.println("Admin Mode - Enter Admin Password: ");
        String pwd = scanner.nextLine();
        if (!pwd.equals("admin123")) {
            System.out.println("Wrong password.");
            return;
        }
        while (true) {
            System.out.println("1. Add Book\n2. View All Transactions\n3. Back");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("Book ID: ");
                    int id = Integer.parseInt(scanner.nextLine());
                    System.out.print("Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Author: ");
                    String author = scanner.nextLine();
                    System.out.print("Copies: ");
                    int copies = Integer.parseInt(scanner.nextLine());
                    books.add(new Book(id, title, author, copies));
                    System.out.println("Book added.");
                    break;
                case 2:
                    for (Transaction t : transactions) {
                        System.out.println("User: " + t.userId + " Book: " + t.bookId +
                                " Issued: " + t.issuedDate + " Returned: " + t.returnDate);
                    }
                    break;
                case 3:
                    return;
            }
        }
    }
}
