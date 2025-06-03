package demo.ma.taskmanagement.Repositories;


import demo.ma.taskmanagement.entity.Comment;
import demo.ma.taskmanagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
}
