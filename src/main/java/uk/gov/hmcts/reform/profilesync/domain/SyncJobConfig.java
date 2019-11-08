package uk.gov.hmcts.reform.profilesync.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sync_config")
@NoArgsConstructor
public class SyncJobConfig {
    @Id
    private int id;

    private String configName;

    private String configRun;

    public SyncJobConfig(String configName, String configRun) {

        this.configName = configName;
        this.configRun = configRun;

    }
}
