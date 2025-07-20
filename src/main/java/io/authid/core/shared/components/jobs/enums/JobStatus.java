package io.authid.core.shared.components.jobs.enums;

import io.authid.core.shared.components.i18n.I18n;
import lombok.Getter;

@Getter
public enum JobStatus {
    STARTING(I18n.extract("job.status.starting.label"), I18n.extract("job.status.starting.description")),
    STARTED(I18n.extract("job.status.started.label"), I18n.extract("job.status.started.description")),
    FAILED(I18n.extract("job.status.failed.label"), I18n.extract("job.status.failed.description")),
    COMPLETED(I18n.extract("job.status.completed.label"), I18n.extract("job.status.completed.description")),
    STOPPED(I18n.extract("job.status.stopped.label"), I18n.extract("job.status.stopped.description"));

    JobStatus(String label, String description){}

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == STOPPED;
    }

    public boolean isRunning() {
        return this == STARTING || this == STARTED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }


    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isStopped() {
        return this == STOPPED;
    }

    public boolean isStarting() {
        return this == STARTING;
    }

    public boolean isStarted() {
        return this == STARTED;
    }

    public boolean canTransitionTo(JobStatus newStatus) {
        return switch (this) {
            case STARTING -> newStatus == STARTED || newStatus == FAILED || newStatus == STOPPED;
            case STARTED -> newStatus == COMPLETED || newStatus == FAILED || newStatus == STOPPED;
            case FAILED -> newStatus == COMPLETED || newStatus == STOPPED;
            case COMPLETED, STOPPED -> false;
        };
    }
    public static JobStatus getDefault() {
        return STARTING;
    }
}
