create table profile_sync_audit(
scheduler_id bigint,
scheduler_start_time timestamp,
scheduler_end_time timestamp,
scheduler_status varchar(7),
constraint scheduler_id_pk primary key (scheduler_id)
);

create sequence scheduler_id_seq
 increment by 1
 minvalue 0
 maxvalue 2147483647
 start with 1
 cache 1
 no cycle;

create table profile_sync_audit_details(
id bigint,
user_identifier varchar(50),
status_code bigint,
error_description varchar(1024),
created_timestamp timestamp,
constraint sync_audit_mapping_pk primary key (id,user_identifier)
);

ALTER TABLE sync_job RENAME TO sync_job_backup;



