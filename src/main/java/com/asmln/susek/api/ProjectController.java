package com.asmln.susek.api;

import com.asmln.rest.api.ProjectApi;
import com.asmln.rest.model.Project;
import com.asmln.rest.model.ProjectCreate;
import com.asmln.rest.model.ProjectList;
import com.asmln.rest.model.ProjectStatus;
import com.asmln.rest.model.ProjectUpdate;

import com.asmln.susek.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class ProjectController implements ProjectApi {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@Override
	public ResponseEntity<ProjectList> listProjects(
			ProjectStatus status,
			@DecimalMin("1") @DecimalMax("100") Integer limit,
			@DecimalMin("0") Integer offset
	) {
		return ResponseEntity.ok(projectService.list(status, limit, offset));
	}

	@Override
	public ResponseEntity<Project> createProject(@RequestBody @Valid @NotNull ProjectCreate body) {
		Project created = projectService.create(body);
		URI location = URI.create("/projects/" + created.uid());

		return ResponseEntity.created(location).body(created);
	}

	@Override
	public ResponseEntity<Project> getProject(
			@NotNull @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$") String projectUid) {
		return ResponseEntity.ok(projectService.getByUid(projectUid));
	}

	@Override
	public ResponseEntity<Project> updateProject(
			@NotNull @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$") String projectUid,
			@Valid @NotNull ProjectUpdate body) {
		return ResponseEntity.ok(projectService.update(projectUid, body));
	}

	@Override
	public ResponseEntity<Void> deleteProject(
			@NotNull @Size(min = 1, max = 64) @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._-]*$") String projectUid) {
		projectService.delete(projectUid);
		return ResponseEntity.noContent().build();
	}
    
}