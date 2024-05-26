package com.hafidtech.springmvcapplication.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.util.Collection;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @NaturalId(mutable = true)
    private String email;
    private String password;
    private boolean isEnabled = false;
    @Column(name = "account_non_locked")
    private boolean accountNonLocked;
    @Column(name = "failed_attempt")
    private int failedAttempt;
    @Column(name = "lock_time")
    private Date lockTime;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    public User(String firstName, String lastName, String email,
                String password, boolean isEnabled,int failedAttempt, boolean accountNonLocked, Date lockTime, Collection<Role> roles) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isEnabled = isEnabled;
        this.failedAttempt = failedAttempt;
        this.accountNonLocked = accountNonLocked;
        this.lockTime = lockTime;
        this.roles = roles;
    }
}