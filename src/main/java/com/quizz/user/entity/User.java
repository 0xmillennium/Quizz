package com.quizz.user.entity;

import com.quizz.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Application account aggregate used by domain services and the security adapter.
 *
 * <p>The account stores role, enablement, and password hash state. Email lookup
 * is case-insensitive through repository methods and database constraints, but
 * this entity does not normalize the field by itself. The password hash should
 * only leave the user boundary for authentication adapters; {@code User} does
 * not implement Spring Security {@code UserDetails}.</p>
 */
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
