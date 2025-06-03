package demo.ma.taskmanagement.Repositories;


import demo.ma.taskmanagement.entity.Project;
import demo.ma.taskmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByIsActiveTrue();
    List<Project> findByCreatedBy(User createdBy);

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m = :user AND p.isActive = true")
    List<Project> findProjectsByMember(@Param("user") User user);

    @Query("SELECT p FROM Project p WHERE p.createdBy = :user OR :user MEMBER OF p.members")
    List<Project> findProjectsRelatedToUser(@Param("user") User user);

    boolean existsByName(String name);
}
