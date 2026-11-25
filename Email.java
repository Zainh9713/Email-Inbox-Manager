import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Email {

    private String emailId;
    private String sender;
    private String subject;
    private String priority;
    private boolean isRead;
    private LocalDateTime arrivalTime;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public Email(String emailId,
                 String sender,
                 String subject,
                 String priority,
                 LocalDateTime arrivalTime) {

        this.emailId = emailId;
        this.sender = sender;
        this.subject = subject;
        this.priority = (priority == null || priority.trim().isEmpty()) ? "Low" : priority.trim();
        this.arrivalTime = arrivalTime;
        this.isRead = false;
    }

    public String getEmailId() {
        return emailId;
    }

    public String getSender() {
        return sender;
    }

    public String getSubject() {
        return subject;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public int getPriorityValue() {
        switch (priority.toLowerCase()) {
            case "high":
                return 3;
            case "medium":
                return 2;
            default:
                return 1;
        }
    }

    @Override
    public String toString() {
        return emailId +
                " | " + sender +
                " | " + subject +
                " | " + priority +
                " | Read=" + isRead +
                " | Time=" + arrivalTime.format(FORMATTER);
    }
}