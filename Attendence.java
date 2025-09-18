// Smart Attendance Management System - Console Version
// Supports Teacher Approval of Student-Initiated Attendance

import java.time.LocalDate;
import java.util.*;

abstract class User {
    int id;
    String name;
    String password;
    User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }
    abstract void dashboard(Scanner scanner, AttendanceSystem system);
}

class Student extends User {
    Map<LocalDate, Boolean> attendanceRecord = new HashMap<>();
    Student(int id, String name, String password) {
        super(id, name, password);
    }
    
    double getAttendancePercentage() {
        if (attendanceRecord.isEmpty()) return 0;
        long presentDays = attendanceRecord.values().stream().filter(b -> b).count();
        return (presentDays * 100.0) / attendanceRecord.size();
    }

    @Override
    void dashboard(Scanner scanner, AttendanceSystem system) {
        while (true) {
            System.out.println("\nWelcome, " + name + " (Student)");
            System.out.println("1. View Attendance %");
            System.out.println("2. View Detailed Attendance");
            System.out.println("3. Request Attendance for Today");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 1) {
                System.out.printf("Your attendance: %.2f%%\n", getAttendancePercentage());
                if (getAttendancePercentage() < 75) {
                    System.out.println("⚠️ Warning: Attendance below 75%!");
                }
            } else if (choice == 2) {
                System.out.println("Date-wise Attendance:");
                attendanceRecord.forEach((date, status) -> 
                    System.out.println(date + ": " + (status ? "Present" : "Absent")));
            } else if (choice == 3) {
                system.addAttendanceRequest(new AttendanceRequest(id, LocalDate.now()));
                System.out.println(" Attendance request submitted for today.");
            } else {
                break;
            }
        }
    }
}

class Teacher extends User {
    Teacher(int id, String name, String password) {
        super(id, name, password);
    }

    @Override
    void dashboard(Scanner scanner, AttendanceSystem system) {
        while (true) {
            System.out.println("\nWelcome, " + name + " (Teacher)");
            System.out.println("1. Approve Attendance Requests");
            System.out.println("2. View All Students Attendance");
            System.out.println("3. Add Student");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 1) {
                system.approveRequests(scanner);
            } else if (choice == 2) {
                for (Student s : system.students) {
                    System.out.printf("%s (%d): %.2f%%\n", s.name, s.id, s.getAttendancePercentage());
                }
            } else if (choice == 3) {
                System.out.print("Enter new student ID: ");
                int sid = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Enter name: ");
                String sname = scanner.nextLine();
                System.out.print("Enter password: ");
                String spass = scanner.nextLine();
                system.students.add(new Student(sid, sname, spass));
                System.out.println("✅ Student added.");
            } else {
                break;
            }
        }
    }
}

class AttendanceRequest {
    int studentId;
    LocalDate date;
    boolean approved = false;

    AttendanceRequest(int studentId, LocalDate date) {
        this.studentId = studentId;
        this.date = date;
    }
}

class AttendanceSystem {
    List<Student> students = new ArrayList<>();
    List<Teacher> teachers = new ArrayList<>();
    List<AttendanceRequest> pendingRequests = new ArrayList<>();

    void addAttendanceRequest(AttendanceRequest req) {
        pendingRequests.add(req);
    }

    void approveRequests(Scanner scanner) {
        Iterator<AttendanceRequest> iterator = pendingRequests.iterator();
        while (iterator.hasNext()) {
            AttendanceRequest r = iterator.next();
            Student s = findStudentById(r.studentId);
            if (s == null) continue;
            System.out.printf("Approve attendance for %s (%d) on %s? (y/n): ", s.name, s.id, r.date);
            String ans = scanner.nextLine();
            if (ans.equalsIgnoreCase("y")) {
                s.attendanceRecord.put(r.date, true);
                r.approved = true;
                iterator.remove();
                System.out.println("✅ Approved.");
            }
        }
    }

    Student findStudentById(int id) {
        return students.stream().filter(s -> s.id == id).findFirst().orElse(null);
    }

    User login(int id, String password) {
        for (Teacher t : teachers) {
            if (t.id == id && t.password.equals(password)) return t;
        }
        for (Student s : students) {
            if (s.id == id && s.password.equals(password)) return s;
        }
        return null;
    }

    void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Smart Attendance System");

        while (true) {
            System.out.print("\nEnter ID (or 0 to exit): ");
            int id = scanner.nextInt();
            if (id == 0) break;
            scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            User user = login(id, password);
            if (user != null) {
                user.dashboard(scanner, this);
            } else {
                System.out.println("❌ Invalid login");
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        AttendanceSystem system = new AttendanceSystem();
        system.teachers.add(new Teacher(999, "Admin", "admin123"));
        system.start();
    }
}
