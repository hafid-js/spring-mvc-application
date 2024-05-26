package com.hafidtech.springmvcapplication.registration;

import com.hafidtech.springmvcapplication.user.Role;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RegistrationRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private boolean isEnabled;
    private int failedAttempt = 0;
    private boolean accountNonLocked = true;
    private Date lockTime;
    private List<Role> roles;
}
