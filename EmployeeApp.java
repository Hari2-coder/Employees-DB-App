import java.math.BigDecimal;
import java.sql.*;
import java.util.Scanner;

public class EmployeeApp {

    // PostgreSQL DB credentials
    private static final String URL = "jdbc:postgresql://localhost:5432/employeesdb";
    private static final String USER = "hari2";        // change if needed
    private static final String PASSWORD = "12345678"; // <-- put your password

    private Connection conn;
    private Scanner scanner;

    public EmployeeApp() {
        scanner = new Scanner(System.in);
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            // Connect
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connected to PostgreSQL database.");

            // Create table if not exists (Postgres style: SERIAL instead of AUTO_INCREMENT)
            String createSql = "CREATE TABLE IF NOT EXISTS employee (" +
                               "id SERIAL PRIMARY KEY, " +
                               "name VARCHAR(100) NOT NULL, " +
                               "email VARCHAR(100) UNIQUE NOT NULL, " +
                               "salary NUMERIC(10,2) NOT NULL)";
            try (Statement st = conn.createStatement()) {
                st.execute(createSql);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEmployee() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            System.out.print("Enter salary: ");
            BigDecimal salary = new BigDecimal(scanner.nextLine());

            String sql = "INSERT INTO employee (name, email, salary) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setBigDecimal(3, salary);
                ps.executeUpdate();
                System.out.println("✅ Employee added.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error adding employee: " + e.getMessage());
        }
    }

    private void viewEmployees() {
        try {
            String sql = "SELECT * FROM employee";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                System.out.println("\n--- Employee List ---");
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " | " +
                                       rs.getString("name") + " | " +
                                       rs.getString("email") + " | " +
                                       rs.getBigDecimal("salary"));
                }
                System.out.println("----------------------\n");
            }
        } catch (Exception e) {
            System.out.println("❌ Error viewing employees: " + e.getMessage());
        }
    }

    private void updateEmployee() {
        try {
            System.out.print("Enter employee ID to update: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("New name (leave blank to keep current): ");
            String name = scanner.nextLine();
            System.out.print("New email (leave blank to keep current): ");
            String email = scanner.nextLine();
            System.out.print("New salary (leave blank to keep current): ");
            String salaryStr = scanner.nextLine();

            // Build dynamic SQL
            StringBuilder sb = new StringBuilder("UPDATE employee SET ");
            boolean first = true;

            if (!name.isBlank()) {
                sb.append("name = ?");
                first = false;
            }
            if (!email.isBlank()) {
                if (!first) sb.append(", ");
                sb.append("email = ?");
                first = false;
            }
            if (!salaryStr.isBlank()) {
                if (!first) sb.append(", ");
                sb.append("salary = ?");
            }
            sb.append(" WHERE id = ?");

            String sql = sb.toString();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                if (!name.isBlank()) ps.setString(idx++, name);
                if (!email.isBlank()) ps.setString(idx++, email);
                if (!salaryStr.isBlank()) ps.setBigDecimal(idx++, new BigDecimal(salaryStr));
                ps.setInt(idx, id);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Employee updated.");
                } else {
                    System.out.println("⚠️ No employee found with ID " + id);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error updating employee: " + e.getMessage());
        }
    }

    private void deleteEmployee() {
        try {
            System.out.print("Enter employee ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            String sql = "DELETE FROM employee WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("✅ Employee deleted.");
                } else {
                    System.out.println("⚠️ No employee found with ID " + id);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error deleting employee: " + e.getMessage());
        }
    }

    private void close() {
        try {
            conn.close();
            scanner.close();
            System.out.println("🔒 Connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Main menu loop ---
    public void run() {
        while (true) {
            System.out.println("\n--- Employee Database Menu ---");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Update Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1": addEmployee(); break;
                case "2": viewEmployees(); break;
                case "3": updateEmployee(); break;
                case "4": deleteEmployee(); break;
                case "5": close(); return;
                default: System.out.println("❌ Invalid choice");
            }
        }
    }

    public static void main(String[] args) {
        EmployeeApp app = new EmployeeApp();
        app.run();
    }
}
