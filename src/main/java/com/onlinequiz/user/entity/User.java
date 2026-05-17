package com.onlinequiz.user.entity;

import com.onlinequiz.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected User() {
    }

    private User(String fullName, String email, String passwordHash, UserRole role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = true;
    }

    public static User createRegularUser(String fullName, String email, String passwordHash) {
        return new User(fullName, email, passwordHash, UserRole.USER);
    }

    public static User createAdmin(String fullName, String email, String passwordHash) {
        return new User(fullName, email, passwordHash, UserRole.ADMIN);
    }

    public void disable() {
        enabled = false;
    }

    public void enable() {
        enabled = true;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
