package com.example.hallyuclass.auth.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "user_type")
public class UserType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên loại tài khoản: free, nạp tiền 1 tháng, nạp tiền 1 năm
    @Column(nullable = false)
    private String name;

    // Thời gian hết hạn của loại tài khoản này
    private LocalDate expiredAt;

    // Getter, Setter, Constructor...

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDate expiredAt) {
        this.expiredAt = expiredAt;
    }
}