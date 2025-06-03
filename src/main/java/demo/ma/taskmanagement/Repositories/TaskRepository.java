package demo.ma.taskmanagement.Repositories;


import demo.ma.taskmanagement.entity.Task;
import demo.ma.taskmanagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByAssignedUser(User assignedUser, Pageable pageable);
    Page<Task> findByCreatedBy(User createdBy, Pageable pageable);
    Page<Task> findByStatus(Task.Status status, Pageable pageable);
    Page<Task> findByPriority(Task.Priority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignedUser = :user OR t.createdBy = :user")
    Page<Task> findTasksRelatedToUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end")
    List<Task> findTasksWithDueDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'DONE' AND t.status != 'CANCELLED'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedUser = :user AND t.status = :status")
    long countTasksByUserAndStatus(@Param("user") User user, @Param("status") Task.Status status);

    @Query("SELECT t FROM Task t WHERE " +
            "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:assignedUserId IS NULL OR t.assignedUser.id = :assignedUserId) AND " +
            "(:projectId IS NULL OR t.project.id = :projectId)")
    Page<Task> findTasksWithFilters(@Param("title") String title,
                                    @Param("status") Task.Status status,
                                    @Param("priority") Task.Priority priority,
                                    @Param("assignedUserId") Long assignedUserId,
                                    @Param("projectId") Long projectId,
                                    Pageable pageable);
}
