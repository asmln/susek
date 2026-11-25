package com.asmln.susek.model.exception;

public class ProjectGoneException extends RuntimeException {
    public ProjectGoneException(String projectUid) {
        super("Project is deleted: " + projectUid);
    }
}
