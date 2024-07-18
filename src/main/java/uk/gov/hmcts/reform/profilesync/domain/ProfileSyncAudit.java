package uk.gov.hmcts.reform.profilesync.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "profile_sync_audit")
@SequenceGenerator(name = "scheduler_id_seq", sequenceName = "scheduler_id_seq", allocationSize = 1)
@NoArgsConstructor
public class ProfileSyncAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduler_id_seq")
    @Column(name = "scheduler_id")
    private Long schedulerId;

    @Column(name = "scheduler_status")
    private String schedulerStatus;

    @Column(name = "scheduler_start_time")
    private LocalDateTime schedulerStartTime;

    @CreationTimestamp
    @Column(name = " scheduler_end_time")
    private LocalDateTime schedulerEndTime;

    @OneToMany(targetEntity = ProfileSyncAuditDetails.class, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", referencedColumnName = "scheduler_id")
    private List<ProfileSyncAuditDetails> profileSyncAuditDetails = new ArrayList<>();

    public ProfileSyncAudit(LocalDateTime schedulerStartTime, String schedulerStatus) {
        this.schedulerStartTime = schedulerStartTime;
        this.schedulerStatus = schedulerStatus;
    }
}

