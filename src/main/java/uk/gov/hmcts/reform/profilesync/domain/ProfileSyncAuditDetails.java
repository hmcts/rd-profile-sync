package uk.gov.hmcts.reform.profilesync.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "profile_sync_audit_details")
@NoArgsConstructor
public class ProfileSyncAuditDetails {

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
