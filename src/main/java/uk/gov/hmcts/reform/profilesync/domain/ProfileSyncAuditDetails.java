package uk.gov.hmcts.reform.profilesync.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "profile_sync_audit_details")
@NoArgsConstructor
public class ProfileSyncAuditDetails implements Serializable {

    @EmbeddedId
    private ProfileSyncAuditDetailsId  profileSyncAuditDetailsId;

    @Column(name = "status_code")
    private int statusCode;

    @Column(name = "error_description")
    private String errorDescription;

    @Column(name = "created_timestamp")
    private LocalDateTime created;

    public ProfileSyncAuditDetails(ProfileSyncAuditDetailsId  profileSyncAuditDetailsId, int statusCode,String
            errorDescription, LocalDateTime created) {
        this.profileSyncAuditDetailsId = profileSyncAuditDetailsId;
        this.statusCode = statusCode;
        this.errorDescription = errorDescription;
        this.created = created;

    }

}
