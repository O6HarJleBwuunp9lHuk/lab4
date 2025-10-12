package org.example.dto;

import jakarta.validation.constraints.*;

public class UpdateUserRequest {
    @Size(max = 100, message = "Name cannot be longer than 100 characters")
    private String name;

    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email cannot be longer than 150 characters")
    private String email;

    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 150, message = "Age cannot exceed 150")
    private Integer age;

    public UpdateUserRequest() {}

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }

    public void setAge(Integer age) { this.age = age; }
}
