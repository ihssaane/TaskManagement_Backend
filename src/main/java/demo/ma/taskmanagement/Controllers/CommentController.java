package demo.ma.taskmanagement.Controllers;

import demo.ma.taskmanagement.dto.CommentDto;
import demo.ma.taskmanagement.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@Valid @RequestBody CommentDto commentDto,
                                                    Authentication authentication) {
        log.info("Comment creation request received from user: {}", authentication.getName());
        CommentDto createdComment = commentService.createComment(commentDto, authentication.getName());
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDto>> getCommentsByTask(@PathVariable Long taskId) {
        List<CommentDto> comments = commentService.getCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long id,
                                                    @Valid @RequestBody CommentDto commentDto,
                                                    Authentication authentication) {
        log.info("Comment update request received for ID: {} from user: {}", id, authentication.getName());
        CommentDto updatedComment = commentService.updateComment(id, commentDto, authentication.getName());
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Authentication authentication) {
        log.info("Comment deletion request received for ID: {} from user: {}", id, authentication.getName());
        commentService.deleteComment(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
