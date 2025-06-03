package demo.ma.taskmanagement.dto;


import demo.ma.taskmanagement.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
}