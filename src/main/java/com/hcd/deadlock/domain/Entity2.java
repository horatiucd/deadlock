package com.hcd.deadlock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entity2")
public class Entity2 {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "text", nullable = false)
    private String text;

    public Entity2() {}

    public Entity2(long id) {
        this.id = id;
        this.text = "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Entity2 {id=" + id + ", text=" + text + "}";
    }
}
