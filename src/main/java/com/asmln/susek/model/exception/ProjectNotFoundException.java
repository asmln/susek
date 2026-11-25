package com.asmln.susek.model.exception;

public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String projectUid) {
        super("Project not found: " + projectUid);
    }
}
