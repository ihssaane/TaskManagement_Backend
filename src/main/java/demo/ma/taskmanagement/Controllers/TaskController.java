package demo.ma.taskmanagement.Controllers;

import demo.ma.taskmanagement.dto.TaskDto;
import demo.ma.taskmanagement.dto.TaskStatisticsDto;
import demo.ma.taskmanagement.entity.Task;
import demo.ma.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto,
                                              Authentication authentication) {
        log.info("Task creation request received from user: {}", authentication.getName());
        TaskDto createdTask = taskService.createTask(taskDto, authentication.getName());
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long projectId) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TaskDto> tasks = taskService.getTasksWithFilters(
                title, status, priority, assignedUserId, projectId, pageable);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<Page<TaskDto>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TaskDto> tasks = taskService.getTasksByUser(authentication.getName(), pageable);
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id,
                                              @Valid @RequestBody TaskDto taskDto,
                                              Authentication authentication) {
        log.info("Task update request received for ID: {} from user: {}", id, authentication.getName());
        TaskDto updatedTask = taskService.updateTask(id, taskDto, authentication.getName());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        log.info("Task deletion request received for ID: {} from user: {}", id, authentication.getName());
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskDto>> getOverdueTasks() {
        List<TaskDto> overdueTasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(overdueTasks);
    }

    @GetMapping("/statistics")
    public ResponseEntity<TaskStatisticsDto> getTaskStatistics(Authentication authentication) {
        String username = authentication.getName();

        TaskStatisticsDto statistics = new TaskStatisticsDto();
        statistics.setTodoTasks(taskService.getTaskCountByUserAndStatus(username, Task.Status.TODO));
        statistics.setInProgressTasks(taskService.getTaskCountByUserAndStatus(username, Task.Status.IN_PROGRESS));
        statistics.setDoneTasks(taskService.getTaskCountByUserAndStatus(username, Task.Status.DONE));
        statistics.setTotalTasks(statistics.getTodoTasks() + statistics.getInProgressTasks() + statistics.getDoneTasks());

        return ResponseEntity.ok(statistics);
    }
}
