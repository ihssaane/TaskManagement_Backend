package demo.ma.taskmanagement.service;

import demo.ma.taskmanagement.entity.Comment;
import demo.ma.taskmanagement.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    public void sendTaskAssignedNotification(Task task) {
        if (task.getAssignedUser() != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "TASK_ASSIGNED");
            notification.put("message", "You have been assigned a new task: " + task.getTitle());
            notification.put("taskId", task.getId());
            notification.put("taskTitle", task.getTitle());
            notification.put("assignedBy", task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName());

            String destination = "/user/" + task.getAssignedUser().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);

            log.info("Task assignment notification sent to user: {}", task.getAssignedUser().getUsername());
        }
    }

    @Async
    public void sendTaskStatusChangeNotification(Task task, Task.Status oldStatus) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TASK_STATUS_CHANGED");
        notification.put("message", "Task status changed from " + oldStatus + " to " + task.getStatus());
        notification.put("taskId", task.getId());
        notification.put("taskTitle", task.getTitle());
        notification.put("newStatus", task.getStatus());
        notification.put("oldStatus", oldStatus);

        // Notification au créateur de la tâche
        if (task.getCreatedBy() != null) {
            String destination = "/user/" + task.getCreatedBy().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
        }

        // Notification à l'assigné si différent du créateur
        if (task.getAssignedUser() != null &&
                !task.getAssignedUser().getId().equals(task.getCreatedBy().getId())) {
            String destination = "/user/" + task.getAssignedUser().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
        }

        log.info("Task status change notification sent for task: {}", task.getId());
    }

    @Async
    public void sendCommentNotification(Comment comment) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "COMMENT_ADDED");
        notification.put("message", comment.getUser().getFirstName() + " commented on task: " + comment.getTask().getTitle());
        notification.put("taskId", comment.getTask().getId());
        notification.put("taskTitle", comment.getTask().getTitle());
        notification.put("commentContent", comment.getContent());
        notification.put("commentedBy", comment.getUser().getFirstName() + " " + comment.getUser().getLastName());

        // Notification au créateur de la tâche
        if (comment.getTask().getCreatedBy() != null &&
                !comment.getTask().getCreatedBy().getId().equals(comment.getUser().getId())) {
            String destination = "/user/" + comment.getTask().getCreatedBy().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
        }

        // Notification à l'assigné si différent du créateur et du commentateur
        if (comment.getTask().getAssignedUser() != null &&
                !comment.getTask().getAssignedUser().getId().equals(comment.getUser().getId()) &&
                !comment.getTask().getAssignedUser().getId().equals(comment.getTask().getCreatedBy().getId())) {
            String destination = "/user/" + comment.getTask().getAssignedUser().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
        }

        log.info("Comment notification sent for task: {}", comment.getTask().getId());
    }

    @Async
    public void sendTaskDueNotification(Task task) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "TASK_DUE_SOON");
        notification.put("message", "Task '" + task.getTitle() + "' is due soon!");
        notification.put("taskId", task.getId());
        notification.put("taskTitle", task.getTitle());
        notification.put("dueDate", task.getDueDate());

        if (task.getAssignedUser() != null) {
            String destination = "/user/" + task.getAssignedUser().getUsername() + "/notifications";
            messagingTemplate.convertAndSend(destination, notification);
        }

        log.info("Task due notification sent for task: {}", task.getId());
    }
}