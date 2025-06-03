package demo.ma.taskmanagement.service;


import demo.ma.taskmanagement.Repositories.ProjectRepository;
import demo.ma.taskmanagement.Repositories.TaskRepository;
import demo.ma.taskmanagement.Repositories.UserRepository;
import demo.ma.taskmanagement.dto.TaskDto;
import demo.ma.taskmanagement.entity.Project;
import demo.ma.taskmanagement.entity.Task;
import demo.ma.taskmanagement.entity.User;
import demo.ma.taskmanagement.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Transactional
    public TaskDto createTask(TaskDto taskDto, String createdByUsername) {
        log.info("Creating task: {} by user: {}", taskDto.getTitle(), createdByUsername);

        User createdBy = userRepository.findByUsername(createdByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUsername));

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus())
                .priority(taskDto.getPriority())
                .dueDate(taskDto.getDueDate())
                .createdBy(createdBy)
                .tags(taskDto.getTags())
                .build();

        // Assigner l'utilisateur si spécifié
        if (taskDto.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(taskDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
            task.setAssignedUser(assignedUser);
        }

        // Assigner le projet si spécifié
        if (taskDto.getProjectId() != null) {
            Project project = projectRepository.findById(taskDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            task.setProject(project);
        }

        Task savedTask = taskRepository.save(task);

        // Notification si la tâche est assignée
        if (savedTask.getAssignedUser() != null &&
                !savedTask.getAssignedUser().getId().equals(createdBy.getId())) {
            notificationService.sendTaskAssignedNotification(savedTask);
        }

        log.info("Task created successfully with ID: {}", savedTask.getId());
        return convertToDto(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksWithFilters(String title, Task.Status status,
                                             Task.Priority priority, Long assignedUserId,
                                             Long projectId, Pageable pageable) {
        return taskRepository.findTasksWithFilters(title, status, priority, assignedUserId, projectId, pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByUser(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return taskRepository.findTasksRelatedToUser(user, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto, String updatedByUsername) {
        log.info("Updating task with ID: {} by user: {}", id, updatedByUsername);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        User updatedBy = userRepository.findByUsername(updatedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + updatedByUsername));

        // Vérification des permissions
        if (!canUserModifyTask(existingTask, updatedBy)) {
            throw new RuntimeException("You don't have permission to modify this task");
        }

        // Sauvegarder l'ancien assigné pour notification
        User oldAssignedUser = existingTask.getAssignedUser();

        // Mise à jour des champs
        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setDueDate(taskDto.getDueDate());
        existingTask.setTags(taskDto.getTags());

        // Mise à jour de l'utilisateur assigné
        if (taskDto.getAssignedUserId() != null) {
            User newAssignedUser = userRepository.findById(taskDto.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));
            existingTask.setAssignedUser(newAssignedUser);

            // Notification si l'assignation a changé
            if (oldAssignedUser == null ||
                    !oldAssignedUser.getId().equals(newAssignedUser.getId())) {
                notificationService.sendTaskAssignedNotification(existingTask);
            }
        } else {
            existingTask.setAssignedUser(null);
        }

        // Mise à jour du projet
        if (taskDto.getProjectId() != null) {
            Project project = projectRepository.findById(taskDto.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            existingTask.setProject(project);
        } else {
            existingTask.setProject(null);
        }

        Task updatedTask = taskRepository.save(existingTask);
        log.info("Task updated successfully: {}", updatedTask.getId());

        return convertToDto(updatedTask);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @taskService.isTaskOwnerOrAssigned(#id, authentication.name)")
    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        taskRepository.deleteById(id);
        log.info("Task deleted successfully: {}", id);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDateTime.now());
        return overdueTasks.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTaskCountByUserAndStatus(String username, Task.Status status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return taskRepository.countTasksByUserAndStatus(user, status);
    }

    // Méthodes utilitaires
    private TaskDto convertToDto(Task task) {
        TaskDto dto = modelMapper.map(task, TaskDto.class);

        if (task.getAssignedUser() != null) {
            dto.setAssignedUserId(task.getAssignedUser().getId());
            dto.setAssignedUserName(task.getAssignedUser().getFirstName() + " " +
                    task.getAssignedUser().getLastName());
        }

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        if (task.getCreatedBy() != null) {
            dto.setCreatedByName(task.getCreatedBy().getFirstName() + " " +
                    task.getCreatedBy().getLastName());
        }

        return dto;
    }

    private boolean canUserModifyTask(Task task, User user) {
        return user.getRole() == User.Role.ADMIN ||
                task.getCreatedBy().getId().equals(user.getId()) ||
                (task.getAssignedUser() != null && task.getAssignedUser().getId().equals(user.getId()));
    }

    public boolean isTaskOwnerOrAssigned(Long taskId, String username) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return false;

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        return task.getCreatedBy().getId().equals(user.getId()) ||
                (task.getAssignedUser() != null && task.getAssignedUser().getId().equals(user.getId()));
    }
}
