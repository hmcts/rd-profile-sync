package uk.gov.hmcts.reform.profilesync.domain;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import uk.gov.hmcts.reform.profilesync.constants.Source;

@Getter
@Setter
@Entity
@Table(name = "sync_job")
@SequenceGenerator(name = "sync_job_id_seq", sequenceName = "sync_job_id_seq", allocationSize = 1)
@NoArgsConstructor
public class SyncJobAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sync_job_id_seq")
    private Long id;

    private Integer response;

    private String status;

    private String errorMessage;

    @Enumerated(EnumType.STRING)
    private Source source;

    private Integer recordsUpdated;

    @CreationTimestamp
    private LocalDateTime auditTs;

    public SyncJobAudit(Integer response, String status, Source source) {

        this.response = response;
        this.status = status;
        this.source = source;
    }
}
