package uk.gov.hmcts.reform.profilesync.domain;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Embeddable
@Getter
@NoArgsConstructor
public class ProfileSyncAuditDetailsId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "id",insertable = false,
            updatable = false, nullable = false)
    private ProfileSyncAudit profileSyncAudit;

    @NonNull
    private String userIdentifier;

    public ProfileSyncAuditDetailsId(ProfileSyncAudit profileSyncAudit, String userIdentifier) {
        this.profileSyncAudit = profileSyncAudit;
        this.userIdentifier = userIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfileSyncAuditDetailsId that = (ProfileSyncAuditDetailsId)o;

        if (!userIdentifier.equals(that.userIdentifier)) {

            return false;
        }

        return userIdentifier.equals(that.userIdentifier);
    }

    @Override
    public int hashCode() {
        int result = userIdentifier.hashCode();
        result = 31 * result + profileSyncAudit.hashCode();
        return result;
    }
}
