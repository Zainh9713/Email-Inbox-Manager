import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class EmailInboxManager {

    private static PriorityQueue<Email> inbox;
    private static Stack<Email> recentEmails;
    private static HashMap<String, Email> emailLookup;
    private static final String FILE_NAME = "inbox.txt";

    // Reusable comparator: Higher priority first, then earlier arrival time
    private static final Comparator<Email> PRIORITY_COMPARATOR = (e1, e2) -> {
        int priorityCompare = Integer.compare(e2.getPriorityValue(), e1.getPriorityValue());
        if (priorityCompare != 0)
            return priorityCompare;
        return e1.getArrivalTime().compareTo(e2.getArrivalTime());
    };

    static {
        inbox = new PriorityQueue<>(PRIORITY_COMPARATOR);
        recentEmails = new Stack<>();
        emailLookup = new HashMap<>();
    }

    public static void main(String[] args) {
        loadEmails();
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n===== EMAIL INBOX =====");
            System.out.println("1. Receive Email");
            System.out.println("2. Open Next Email");
            System.out.println("3. Search Email");
            System.out.println("4. Display Inbox");
            System.out.println("5. Sort By Sender");
            System.out.println("6. Sort By Date");
            System.out.println("7. Recently Opened");
            System.out.println("8. Exit");
            System.out.print("Choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number (1-8).");
                continue;
            }

            switch (choice) {
                case 1: receiveEmail(sc); break;
                case 2: openEmail(); break;
                case 3: searchEmail(sc); break;
                case 4: displayInbox(); break;
                case 5: sortBySender(); break;
                case 6: sortByDate(); break;
                case 7: showRecentEmails(); break;
                case 8:
                    saveEmails();
                    System.out.println("Data Saved. Exiting...");
                    break;
                default:
                    System.out.println("Invalid Choice");
            }
        } while (choice != 8);

        sc.close();
    }

    private static void receiveEmail(Scanner sc) {
        System.out.print("Email ID: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty() || emailLookup.containsKey(id)) {
            System.out.println("Invalid or duplicate Email ID.");
            return;
        }

        System.out.print("Sender: ");
        String sender = sc.nextLine().trim();

        System.out.print("Subject: ");
        String subject = sc.nextLine().trim();

        System.out.print("Priority (High/Medium/Low): ");
        String priority = sc.nextLine().trim();

        if (sender.isEmpty() || subject.isEmpty()) {
            System.out.println("Sender and Subject Required.");
            return;
        }

        Email email = new Email(id, sender, subject, priority, LocalDateTime.now());
        inbox.add(email);
        emailLookup.put(id, email);
        saveEmails(); // Auto-save immediately

        System.out.println("Email received: " + id + " | " + sender + " | " + priority);
    }

    private static void openEmail() {
        if (inbox.isEmpty()) {
            System.out.println("Inbox Empty.");
            return;
        }

        Email email = inbox.poll();
        email.setRead(true);
        recentEmails.push(email);
        saveEmails(); // Auto-save read status immediately

        System.out.println("Opened: " + email.getEmailId() + " | " + email.getSender() + " | " + email.getSubject());
    }

    private static void searchEmail(Scanner sc) {
        System.out.print("Enter Email ID: ");
        String id = sc.nextLine().trim();

        Email email = emailLookup.get(id);
        if (email == null) {
            System.out.println("Email " + id + " not found.");
            return;
        }
        System.out.println(email);
    }

    private static void displayInbox() {
        if (inbox.isEmpty()) {
            System.out.println("Inbox Empty.");
            return;
        }

        // Copy to a list and sort so it prints in true priority order
        List<Email> sortedInbox = new ArrayList<>(inbox);
        sortedInbox.sort(PRIORITY_COMPARATOR);

        for (Email email : sortedInbox) {
            System.out.println(email);
        }
    }

    private static void sortBySender() {
        if (emailLookup.isEmpty()) {
            System.out.println("No emails available.");
            return;
        }
        List<Email> list = new ArrayList<>(emailLookup.values());
        list.sort(Comparator.comparing(Email::getSender, String.CASE_INSENSITIVE_ORDER));
        for (Email email : list) {
            System.out.println(email);
        }
    }

    private static void sortByDate() {
        if (emailLookup.isEmpty()) {
            System.out.println("No emails available.");
            return;
        }
        List<Email> list = new ArrayList<>(emailLookup.values());
        list.sort(Comparator.comparing(Email::getArrivalTime));
        for (Email email : list) {
            System.out.println(email);
        }
    }

    private static void showRecentEmails() {
        if (recentEmails.isEmpty()) {
            System.out.println("No recent emails.");
            return;
        }
        for (int i = recentEmails.size() - 1; i >= 0; i--) {
            System.out.println(recentEmails.get(i));
        }
    }

    private static void saveEmails() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Email email : emailLookup.values()) {
                // Replace commas to prevent breaking CSV format
                String safeSender = email.getSender().replace(",", " ");
                String safeSubject = email.getSubject().replace(",", " ");

                pw.println(
                    email.getEmailId() + "," +
                    safeSender + "," +
                    safeSubject + "," +
                    email.getPriority() + "," +
                    email.isRead() + "," +
                    email.getArrivalTime()
                );
            }
        } catch (IOException e) {
            System.out.println("Error saving emails: " + e.getMessage());
        }
    }

    private static void loadEmails() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length < 6) continue;

                Email email = new Email(
                    data[0],
                    data[1],
                    data[2],
                    data[3],
                    LocalDateTime.parse(data[5])
                );

                boolean isRead = Boolean.parseBoolean(data[4]);
                email.setRead(isRead);

                emailLookup.put(email.getEmailId(), email);

                // Route to Stack if already read, otherwise to PriorityQueue
                if (isRead) {
                    recentEmails.push(email);
                } else {
                    inbox.add(email);
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading emails: " + e.getMessage());
        }
    }
}