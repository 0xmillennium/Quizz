package com.quizz.category.entity;

import com.quizz.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected Category() {
    }

    private Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.active = true;
    }

    public static Category create(String name, String description) {
        return new Category(name, description);
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}
