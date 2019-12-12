create table sync_config(
id smallint,
config_name varchar(20),
config_run varchar(10),
constraint sync_config_pk primary key (id)
);

INSERT INTO sync_config(id,config_name,config_run) VALUES (1,'firstsearchquery','1680h');