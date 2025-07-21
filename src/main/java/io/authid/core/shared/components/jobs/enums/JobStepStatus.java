package io.authid.core.shared.components.jobs.enums;

import io.authid.core.shared.components.i18n.extractors.I18n;
import lombok.Getter;

@Getter
public enum JobStepStatus {
    QUEUED(I18n.extract("job.step.status.queued.label"), I18n.extract("job.step.status.queued.description")),           // Waiting in the queue
    SCHEDULED(I18n.extract("job.step.status.scheduled.label"), I18n.extract("job.step.status.scheduled.description")),        // Scheduled to start in the future
    STARTING(I18n.extract("job.step.status.starting.label"), I18n.extract("job.step.status.starting.description")),         // Preparing resources, bootstrapping
    STARTED(I18n.extract("job.step.status.started.label"), I18n.extract("job.step.status.started.description")),          // Actually running
    RUNNING(I18n.extract("job.step.status.running.label"), I18n.extract("job.step.status.running.description")),          // Mid-execution (optional if you want more granularity)
    WAITING_DEPENDENCY(I18n.extract("job.step.status.waiting_dependency.label"), I18n.extract("job.step.status.waiting_dependency.description")), // Waiting for dependency (e.g. data, external system)
    RETRYING(I18n.extract("job.step.status.retrying.label"), I18n.extract("job.step.status.retrying.description")),         // Retrying after a failure
    SKIPPED(I18n.extract("job.step.status.skipped.label"), I18n.extract("job.step.status.skipped.description")),          // Skipped intentionally
    CANCELLED(I18n.extract("job.step.status.cancelled.label"), I18n.extract("job.step.status.cancelled.description")),        // Canceled by user/system
    FAILED(I18n.extract("job.step.status.failed.label"), I18n.extract("job.step.status.failed.description")),           // Ended due to error
    TIMEOUT(I18n.extract("job.step.status.timeout.label"), I18n.extract("job.step.status.timeout.description")),          // Failed due to timeout
    COMPLETED(I18n.extract("job.step.status.completed.label"), I18n.extract("job.step.status.completed.description")),        // Finished successfully
    PARTIAL_SUCCESS(I18n.extract("job.step.status.partial_success.label"), I18n.extract("job.step.status.partial_success.description")),  // Some internal step failed but others succeeded
    ROLLED_BACK(I18n.extract("job.step.status.rolled_back.label"), I18n.extract("job.step.status.rolled_back.description"));       // Step was completed but rolled back due to later failure

    JobStepStatus(String label, String description){}

    public boolean isTerminal() {
        return this == FAILED || this == TIMEOUT || this == COMPLETED || this == SKIPPED || this == CANCELLED || this == PARTIAL_SUCCESS || this == ROLLED_BACK;
    }

    public boolean isRunning() {
        return this == STARTING || this == STARTED || this == RUNNING || this == WAITING_DEPENDENCY || this == RETRYING;
    }

    public boolean isFailed() {
        return this == FAILED || this == TIMEOUT;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isSkipped() {
        return this == SKIPPED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isQueued() {
        return this == QUEUED;
    }

    public boolean isScheduled() {
        return this == SCHEDULED;
    }

    public boolean isStarting() {
        return this == STARTING;
    }

    public boolean isStarted() {
        return this == STARTED;
    }

    public boolean isWaitingDependency() {
        return this == WAITING_DEPENDENCY;
    }

    public boolean isRetrying() {
        return this == RETRYING;
    }

    public boolean isPartialSuccess() {
        return this == PARTIAL_SUCCESS;
    }

    public boolean isRolledBack() {
        return this == ROLLED_BACK;
    }

    public boolean canTransitionTo(JobStepStatus newType){
        return switch (this) {
            case QUEUED -> newType == SCHEDULED || newType == STARTED || newType == WAITING_DEPENDENCY || newType == RETRYING || newType == SKIPPED;
            case SCHEDULED, STARTING -> newType == STARTED || newType == WAITING_DEPENDENCY || newType == RETRYING || newType == SKIPPED;
            case STARTED -> newType == RUNNING || newType == WAITING_DEPENDENCY || newType == FAILED || newType == TIMEOUT || newType == COMPLETED || newType == PARTIAL_SUCCESS || newType == ROLLED_BACK || newType == CANCELLED;
            case WAITING_DEPENDENCY, RETRYING -> newType == STARTED || newType == FAILED || newType == TIMEOUT || newType == CANCELLED;
            case SKIPPED, CANCELLED, FAILED, TIMEOUT, COMPLETED, PARTIAL_SUCCESS, ROLLED_BACK -> false; // Terminal states
            case RUNNING -> newType == FAILED || newType == TIMEOUT || newType == COMPLETED || newType == PARTIAL_SUCCESS || newType == ROLLED_BACK || newType == CANCELLED;
            default -> false;
        };
    }
    public static JobStepStatus getDefault() {
        return QUEUED;
    }
}
