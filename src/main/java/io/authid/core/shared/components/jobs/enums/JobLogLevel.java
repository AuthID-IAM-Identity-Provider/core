package io.authid.core.shared.components.jobs.enums;

import io.authid.core.shared.components.i18n.extractors.I18n;
import lombok.Getter;

@Getter
public enum JobLogLevel {
    TRACE(I18n.extract("job.log.level.trace.label"), I18n.extract("job.log.level.trace.description"), "white", 0),
    DEBUG(I18n.extract("job.log.level.debug.label"), I18n.extract("job.log.level.debug.description"), "blue", 1),
    INFO(I18n.extract("job.log.level.info.label"), I18n.extract("job.log.level.info.description"), "green", 2),
    NOTICE(I18n.extract("job.log.level.notice.label"), I18n.extract("job.log.level.notice.description"), "yellow", 3),
    WARN(I18n.extract("job.log.level.warn.label"), I18n.extract("job.log.level.warn.description"), "orange", 4),
    ERROR(I18n.extract("job.log.level.error.label"), I18n.extract("job.log.level.error.description"), "red", 5),
    CRITICAL(I18n.extract("job.log.level.critical.label"), I18n.extract("job.log.level.critical.description"), "magenta", 6),
    ALERT(I18n.extract("job.log.level.alert.label"), I18n.extract("job.log.level.alert.description"), "red", 7),
    EMERGENCY(I18n.extract("job.log.level.emergency.label"), I18n.extract("job.log.level.emergency.description"), "red", 8);

    JobLogLevel(String label, String description, String color, int priority){}

    public boolean isTrace(){
        return this == TRACE;
    }

    public boolean isDebug(){
        return this == DEBUG;
    }

    public boolean isInfo(){
        return this == INFO;
    }

    public boolean isNotice(){
        return this == NOTICE;
    }

    public boolean isWarn(){
        return this == WARN;
    }

    public boolean isError(){
        return this == ERROR;
    }

    public boolean isCritical(){
        return this == CRITICAL;
    }

    public boolean isAlert(){
        return this == ALERT;
    }

    public boolean isEmergency(){
        return this == EMERGENCY;
    }

    public boolean canTransitionTo(JobLogLevel newType){
        return switch (this) {
            case TRACE -> newType == DEBUG || newType == INFO || newType == NOTICE || newType == WARN || newType == ERROR || newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case DEBUG -> newType == INFO || newType == NOTICE || newType == WARN || newType == ERROR || newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case INFO -> newType == NOTICE || newType == WARN || newType == ERROR || newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case NOTICE -> newType == WARN || newType == ERROR || newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case WARN -> newType == ERROR || newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case ERROR -> newType == CRITICAL || newType == ALERT || newType == EMERGENCY;
            case CRITICAL -> newType == ALERT || newType == EMERGENCY;
            case ALERT -> newType == EMERGENCY;
            default -> false;
        };
    }
    public static JobLogLevel getDefault() {
        return INFO;
    }
}
