package io.authid.core.containers.authorization.domain.entities;

import io.authid.core.containers.authorization.domain.factories.RoleFactory;
import io.authid.core.shared.components.database.factory.HasFactory;
import io.authid.core.shared.components.database.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "role")
public class RoleEntity extends BaseEntity<UUID> implements HasFactory<RoleFactory> {
    @Getter
    @Setter
    @Column(name = "name")
    private String name;

    @Getter
    @Setter
    @Column(name = "display_name")
    private String displayName;

    @Getter
    @Setter
    @Column(name = "description")
    private String description;

    @Getter
    @Setter
    @Column(name = "code")
    private String code;

    @Getter
    @Setter
    @Column(name = "group")
    private String group;

    @Getter
    @Setter
    @Column(name = "guard_name")
    private String guardName;

    @Getter
    @Setter
    @Column(name = "is_locked")
    private Boolean isLocked;


    public static RoleFactory factory() {
        return new RoleFactory();
    }
}
