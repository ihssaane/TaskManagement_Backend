package demo.ma.taskmanagement.service;


import demo.ma.taskmanagement.Repositories.CommentRepository;
import demo.ma.taskmanagement.Repositories.TaskRepository;
import demo.ma.taskmanagement.Repositories.UserRepository;
import demo.ma.taskmanagement.dto.CommentDto;
import demo.ma.taskmanagement.entity.Comment;
import demo.ma.taskmanagement.entity.Task;
import demo.ma.taskmanagement.entity.User;
import demo.ma.taskmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public CommentDto createComment(CommentDto commentDto, String createdByUsername) {
        log.info("Creating comment for task: {} by user: {}", commentDto.getTaskId(), createdByUsername);

        Task task = taskRepository.findById(commentDto.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + commentDto.getTaskId()));

        User user = userRepository.findByUsername(createdByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUsername));

        Comment comment = Comment.builder()
                .content(commentDto.getContent())
                .task(task)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // Notification pour le créateur de la tâche et l'assigné
        notificationService.sendCommentNotification(savedComment);

        log.info("Comment created successfully with ID: {}", savedComment.getId());
        return convertToDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto updateComment(Long id, CommentDto commentDto, String updatedByUsername) {
        log.info("Updating comment with ID: {} by user: {}", id, updatedByUsername);

        Comment existingComment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User updatedBy = userRepository.findByUsername(updatedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + updatedByUsername));

        // Vérification des permissions
        if (!canUserModifyComment(existingComment, updatedBy)) {
            throw new RuntimeException("You don't have permission to modify this comment");
        }

        existingComment.setContent(commentDto.getContent());
        Comment updatedComment = commentRepository.save(existingComment);

        log.info("Comment updated successfully: {}", updatedComment.getId());
        return convertToDto(updatedComment);
    }

    @Transactional
    public void deleteComment(Long id, String deletedByUsername) {
        log.info("Deleting comment with ID: {} by user: {}", id, deletedByUsername);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        User deletedBy = userRepository.findByUsername(deletedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + deletedByUsername));

        if (!canUserModifyComment(comment, deletedBy)) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        commentRepository.deleteById(id);
        log.info("Comment deleted successfully: {}", id);
    }

    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = modelMapper.map(comment, CommentDto.class);
        dto.setTaskId(comment.getTask().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        return dto;
    }

    private boolean canUserModifyComment(Comment comment, User user) {
        return user.getRole() == User.Role.ADMIN ||
                comment.getUser().getId().equals(user.getId());
    }
}
