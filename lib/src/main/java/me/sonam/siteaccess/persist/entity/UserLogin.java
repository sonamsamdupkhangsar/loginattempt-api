package me.sonam.siteaccess.persist.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserLogin implements Persistable<String> {
    public enum Status {
        SUCCESS, FAILED
    }
    @Id
    private String username;

    private UUID userId;

    private int attemptCount;
    private String ip;
    private String status;
    private LocalDateTime dateTime;

    @Transient
    private boolean newRow;

    public UserLogin(String username, UUID userId, String ip, String status, LocalDateTime localDateTime) {
        this.username = username;
        this.userId = userId;
        this.ip = ip;
        this.attemptCount = 0;
        this.status = status;
        this.dateTime = localDateTime;

        this.newRow = true;
    }

    public UserLogin() {

    }

    public String getId() {
        return username;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getIp() {
        return ip;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
        this.newRow = false;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isNew() {
        return newRow;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDateTime() {
        return  dateTime;
    }

    public void setStatus(UserLogin.Status status) {
        this.status = status.name();
    }

    public void loginSuccess() {
        this.status = Status.SUCCESS.name();
        this.attemptCount = 0;
        this.newRow = false;
    }

    public void incrementAttemptCount() {
        this.status = Status.FAILED.name();
        this.attemptCount++;
    }

    public void loginFailed() {
        this.status = Status.FAILED.name();
        this.attemptCount++;
        this.newRow = false;
    }

    public void resetAttempt() {
        this.attemptCount = 0;
    }

    @Override
    public String toString() {
        return "UserLogin{" +
                "username='" + username + '\'' +
                ", userId=" + userId +
                ", attemptCount=" + attemptCount +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", dateTime=" + dateTime +
                ", newRow=" + newRow +
                '}';
    }
}
