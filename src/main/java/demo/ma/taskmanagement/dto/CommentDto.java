package demo.ma.taskmanagement.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
}
