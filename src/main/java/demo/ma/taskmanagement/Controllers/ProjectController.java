package demo.ma.taskmanagement.Controllers;

import demo.ma.taskmanagement.dto.ProjectDto;
import demo.ma.taskmanagement.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto,
                                                    Authentication authentication) {
        log.info("Project creation request received from user: {}", authentication.getName());
        ProjectDto createdProject = projectService.createProject(projectDto, authentication.getName());
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDto>> getMyProjects(Authentication authentication) {
        List<ProjectDto> projects = projectService.getProjectsByUser(authentication.getName());
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id,
                                                    @Valid @RequestBody ProjectDto projectDto,
                                                    Authentication authentication) {
        log.info("Project update request received for ID: {} from user: {}", id, authentication.getName());
        ProjectDto updatedProject = projectService.updateProject(id, projectDto, authentication.getName());
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, Authentication authentication) {
        log.info("Project deletion request received for ID: {} from user: {}", id, authentication.getName());
        projectService.deleteProject(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> addMemberToProject(@PathVariable Long projectId,
                                                         @PathVariable Long userId,
                                                         Authentication authentication) {
        log.info("Add member request received for project: {} and user: {}", projectId, userId);
        ProjectDto project = projectService.addMemberToProject(projectId, userId, authentication.getName());
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> removeMemberFromProject(@PathVariable Long projectId,
                                                              @PathVariable Long userId,
                                                              Authentication authentication) {
        log.info("Remove member request received for project: {} and user: {}", projectId, userId);
        ProjectDto project = projectService.removeMemberFromProject(projectId, userId, authentication.getName());
        return ResponseEntity.ok(project);
    }
}
