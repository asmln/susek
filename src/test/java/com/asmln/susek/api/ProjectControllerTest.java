package com.asmln.susek.api;

import com.asmln.rest.model.*;
import com.asmln.susek.model.exception.ProjectGoneException;
import com.asmln.susek.model.exception.ProjectNotFoundException;
import com.asmln.susek.model.exception.ProjectUidAlreadyTakenException;
import com.asmln.susek.service.ProjectService;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {
    private static final String PROJECT_UID = "ads";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void listProjects_returns200AndProjects() throws Exception {
        var project = new Project(
                PROJECT_UID,
                "Сервис объявлений",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-11-24T01:23:00.123Z"),
                Instant.parse("2026-11-24T01:23:00.123Z"),
                null
        );

        when(projectService.list(
                ProjectStatus.ACTIVE,
                20,
                0
        )).thenReturn(new ProjectList(List.of(project), 20, 0, 1));

        mockMvc.perform(get("/projects")
                        .param("status", "ACTIVE")
                        .param("limit", "20")
                        .param("offset", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].uid").value(PROJECT_UID))
                .andExpect(jsonPath("$.items[0].name").value("Сервис объявлений"))
                .andExpect(jsonPath("$.items[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].createdAt")
                        .value("2026-11-24T01:23:00.123Z"))
                .andExpect(jsonPath("$.items[0].deletedAt").doesNotExist());
    }

    @Test
    void listProjects_returns400ForInvalidLimit() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("limit", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).list(any(), anyInt(), anyInt());
    }

    @Test
    void createProject_returns201AndLocation() throws Exception {
        var request = new ProjectCreate(PROJECT_UID, "Сервис объявлений");

        var created = new Project(
                PROJECT_UID,
                "Сервис объявлений",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-11-24T01:23:00.123Z"),
                Instant.parse("2026-11-24T01:23:00.123Z"),
                null
        );

        when(projectService.create(request)).thenReturn(created);

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/projects/" + PROJECT_UID
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").value(PROJECT_UID))
                .andExpect(jsonPath("$.name").value("Сервис объявлений"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());

        verify(projectService).create(request);
    }

    @Test
    void createProject_returns400WhenNameIsBlank() throws Exception {
        var request = new ProjectCreate(PROJECT_UID, "");

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(projectService, never()).create(any());
    }

    @Test
    void createProject_returns409WhenUidIsTaken() throws Exception {
        var request = new ProjectCreate(PROJECT_UID, "Сервис объявлений");

        when(projectService.create(request))
                .thenThrow(new ProjectUidAlreadyTakenException(PROJECT_UID));

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getProject_returns200() throws Exception {
        var project = new Project(
                PROJECT_UID,
                "Сервис объявлений",
                ProjectStatus.ACTIVE,
                Instant.parse("2026-11-24T01:23:00.123Z"),
                Instant.parse("2026-11-24T01:23:00.123Z"),
                null
        );

        when(projectService.getByUid(PROJECT_UID)).thenReturn(project);

        mockMvc.perform(get("/projects/{projectUid}", PROJECT_UID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").value(PROJECT_UID))
                .andExpect(jsonPath("$.name").value("Сервис объявлений"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getProject_returns404WhenProjectIsMissing() throws Exception {
        when(projectService.getByUid(PROJECT_UID))
                .thenThrow(new ProjectNotFoundException(PROJECT_UID));

        mockMvc.perform(get("/projects/{projectUid}", PROJECT_UID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProject_returns410WhenProjectIsDeleted() throws Exception {
        when(projectService.getByUid(PROJECT_UID))
                .thenThrow(new ProjectGoneException(PROJECT_UID));

        mockMvc.perform(get("/projects/{projectUid}", PROJECT_UID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());
    }

    @Test
    void updateProject_returns200() throws Exception {
        var request = new ProjectUpdate("Новое имя", ProjectUpdateStatus.INACTIVE);

        var updated = new Project(
                PROJECT_UID,
                "Новое имя",
                ProjectStatus.INACTIVE,
                Instant.parse("2026-11-24T01:23:00.123Z"),
                Instant.parse("2026-11-24T02:23:00.123Z"),
                null
        );

        when(projectService.update(PROJECT_UID, request)).thenReturn(updated);

        mockMvc.perform(patch("/projects/{projectUid}", PROJECT_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uid").value(PROJECT_UID))
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(projectService).update(PROJECT_UID, request);
    }

    @Test
    void updateProject_returns404WhenProjectIsMissing() throws Exception {
        var request = new ProjectUpdate("Новое имя", ProjectUpdateStatus.INACTIVE);

        when(projectService.update(PROJECT_UID, request))
                .thenThrow(new ProjectNotFoundException(PROJECT_UID));

        mockMvc.perform(patch("/projects/{projectUid}", PROJECT_UID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_returns204() throws Exception {
        doNothing().when(projectService).delete(PROJECT_UID);

        mockMvc.perform(delete("/projects/{projectUid}", PROJECT_UID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(projectService).delete(PROJECT_UID);
    }

    @Test
    void deleteProject_isIdempotentAndReturns204Again() throws Exception {
        doNothing().when(projectService).delete(PROJECT_UID);

        mockMvc.perform(delete("/projects/{projectUid}", PROJECT_UID))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/projects/{projectUid}", PROJECT_UID))
                .andExpect(status().isNoContent());

        verify(projectService, times(2)).delete(PROJECT_UID);
    }

    @Test
    void deleteProject_returns404WhenProjectIsMissing() throws Exception {
        doThrow(new ProjectNotFoundException(PROJECT_UID))
                .when(projectService)
                .delete(PROJECT_UID);

        mockMvc.perform(delete("/projects/{projectUid}", PROJECT_UID))
                .andExpect(status().isNotFound());
    }
    
}