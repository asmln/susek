package com.asmln.susek.model.exception;

public class ProjectUidAlreadyTakenException extends RuntimeException {
    public ProjectUidAlreadyTakenException(String projectUid) {
        super("Project uid is already taken: " + projectUid);
    }
}
