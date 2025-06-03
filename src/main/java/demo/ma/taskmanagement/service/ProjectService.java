package demo.ma.taskmanagement.service;


import demo.ma.taskmanagement.Repositories.ProjectRepository;
import demo.ma.taskmanagement.Repositories.UserRepository;
import demo.ma.taskmanagement.dto.ProjectDto;
import demo.ma.taskmanagement.entity.Project;
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ProjectDto createProject(ProjectDto projectDto, String createdByUsername) {
        log.info("Creating project: {} by user: {}", projectDto.getName(), createdByUsername);

        if (projectRepository.existsByName(projectDto.getName())) {
            throw new RuntimeException("Project with name '" + projectDto.getName() + "' already exists");
        }

        User createdBy = userRepository.findByUsername(createdByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + createdByUsername));

        Project project = Project.builder()
                .name(projectDto.getName())
                .description(projectDto.getDescription())
                .createdBy(createdBy)
                .isActive(true)
                .build();

        // Ajouter les membres si spécifiés
        if (projectDto.getMemberIds() != null && !projectDto.getMemberIds().isEmpty()) {
            List<User> members = userRepository.findAllById(projectDto.getMemberIds());
            project.setMembers(members);
        }

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return convertToDto(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return projectRepository.findProjectsRelatedToUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto updateProject(Long id, ProjectDto projectDto, String updatedByUsername) {
        log.info("Updating project with ID: {} by user: {}", id, updatedByUsername);

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User updatedBy = userRepository.findByUsername(updatedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + updatedByUsername));

        // Vérification des permissions
        if (!canUserModifyProject(existingProject, updatedBy)) {
            throw new RuntimeException("You don't have permission to modify this project");
        }

        existingProject.setName(projectDto.getName());
        existingProject.setDescription(projectDto.getDescription());

        // Mise à jour des membres
        if (projectDto.getMemberIds() != null) {
            List<User> members = userRepository.findAllById(projectDto.getMemberIds());
            existingProject.setMembers(members);
        }

        Project updatedProject = projectRepository.save(existingProject);
        log.info("Project updated successfully: {}", updatedProject.getId());

        return convertToDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id, String deletedByUsername) {
        log.info("Deleting project with ID: {} by user: {}", id, deletedByUsername);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        User deletedBy = userRepository.findByUsername(deletedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + deletedByUsername));

        if (!canUserModifyProject(project, deletedBy)) {
            throw new RuntimeException("You don't have permission to delete this project");
        }

        project.setIsActive(false);
        projectRepository.save(project);

        log.info("Project deactivated successfully: {}", id);
    }

    @Transactional
    public ProjectDto addMemberToProject(Long projectId, Long userId, String addedByUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User addedBy = userRepository.findByUsername(addedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + addedByUsername));

        if (!canUserModifyProject(project, addedBy)) {
            throw new RuntimeException("You don't have permission to modify this project");
        }

        if (!project.getMembers().contains(userToAdd)) {
            project.getMembers().add(userToAdd);
            projectRepository.save(project);
        }

        return convertToDto(project);
    }

    @Transactional
    public ProjectDto removeMemberFromProject(Long projectId, Long userId, String removedByUsername) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User removedBy = userRepository.findByUsername(removedByUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + removedByUsername));

        if (!canUserModifyProject(project, removedBy)) {
            throw new RuntimeException("You don't have permission to modify this project");
        }

        project.getMembers().remove(userToRemove);
        projectRepository.save(project);

        return convertToDto(project);
    }

    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = modelMapper.map(project, ProjectDto.class);

        if (project.getCreatedBy() != null) {
            dto.setCreatedByName(project.getCreatedBy().getFirstName() + " " +
                    project.getCreatedBy().getLastName());
        }

        if (project.getMembers() != null) {
            dto.setMemberIds(project.getMembers().stream()
                    .map(User::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private boolean canUserModifyProject(Project project, User user) {
        return user.getRole() == User.Role.ADMIN ||
                project.getCreatedBy().getId().equals(user.getId());
    }
}
