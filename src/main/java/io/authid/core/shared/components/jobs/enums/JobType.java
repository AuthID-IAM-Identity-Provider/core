package io.authid.core.shared.components.jobs.enums;

import io.authid.core.shared.components.i18n.extractors.I18n;
import lombok.Getter;

@Getter
public enum JobType {
    MANUAL(I18n.extract("job.type.manual.label"), I18n.extract("job.type.manual.description")),
    SCHEDULED(I18n.extract("job.type.scheduled.label"), I18n.extract("job.type.scheduled.description")),
    SYSTEM(I18n.extract("job.type.system.label"), I18n.extract("job.type.system.description")),
    EVENT(I18n.extract("job.type.event.label"), I18n.extract("job.type.event.description"));

    JobType(String label, String description) {
    }

    public boolean isManual(){
        return this == MANUAL;
    }

    public boolean isScheduled(){
        return this == SCHEDULED;
    }

    public boolean isSystem(){
        return this == SYSTEM;
    }

    public boolean isEvent(){
        return this == EVENT;
    }

    public boolean canTransitionTo(JobType newType){
        return switch (this) {
            case MANUAL -> newType == SCHEDULED || newType == SYSTEM || newType == EVENT;
            case SCHEDULED -> newType == MANUAL || newType == SYSTEM || newType == EVENT;
            case SYSTEM -> newType == MANUAL || newType == SCHEDULED || newType == EVENT;
            case EVENT -> newType == MANUAL || newType == SCHEDULED || newType == SYSTEM;
            default -> false;
        };
    }
    public static JobType getDefault() {
        return MANUAL;
    }
}
