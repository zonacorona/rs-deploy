#create the database
create database if not exists docs;

use docs;

#create the table
create table if not exists users(ldapname varchar(70) not null primary key, fname varchar(40), lname varchar(40), email varchar(70), status varchar(10));
create table if not exists groups(name varchar(90) not null primary key);
create table if not exists members(ldapname varchar(70) not null, groupname varchar(90) not null, constraint primary key(ldapname, groupname), foreign key(ldapname) references users(ldapname), foreign key(groupname) references groups(name));
create table if not exists status(value varchar(50) not null, constraint primary key(value));
create table if not exists deployjob(id bigint not null auto_increment,ldapname varchar(70) not null, groupid varchar(100) not null, artifactid varchar(100) not null, warname varchar(100) not null, pomname varchar(100), type enum('deploy','revert'), starttime bigint not null, endtime bigint, status varchar(50), failreason varchar(100), constraint primary key(id), foreign key(status) references status(value));
create table if not exists freeze(id smallint, shouldfreeze bool, constraint primary key(id)); 

#insert initial values users
insert into users(ldapname,fname,lname, email, status) SELECT 'thu4404','thu','doan', 'thu.doan@rackspace.com', 'active' from dual where not exists (select ldapname from users where ldapname='thu4404');
insert into users(ldapname,fname,lname, email, status) SELECT 'david.cramer','david','cramer', 'david.cramer@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='david.cramer');
insert into users(ldapname,fname,lname, email, status) SELECT 'davi4555','david','hendler', 'david.hendler@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='davi4555');
insert into users(ldapname,fname,lname, email, status) SELECT 'dia4454','Diane','Flemming', 'diane.flemming@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='dia4454');
insert into users(ldapname,fname,lname, email, status) SELECT 'mike.ashalter','Mike','Ashalter','mike.ashalter@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mike.ashalter');
insert into users(ldapname,fname,lname, email, status) SELECT 'mich6061','Michael','McGrail','michael.mcgrail@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'russell.haering','Russell','Haering','russell.haering@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'gary.dusbabek','Gary','Dusbabek','gary.dusbabek@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'raj.patel','Raj','Patel','raj.patel@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'rose.coste','Rose','Coste','rose.coste@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'raj.patel','Raj','Patel','raj.patel@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'tomaz.muraus','Tomas','Muraus','tomaz.muraus@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='mich6061');
insert into users(ldapname,fname,lname, email, status) SELECT 'rene5821','Renee','Rendon','renee.rendon@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='rene5821');
insert into users(ldapname,fname,lname, email, status) SELECT 'cath6184','Catherine','Richardson','catherine.richardson@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='cath6184');
insert into users(ldapname,fname,lname, email, status) SELECT 'cath6136','Cat','Lookabaugh','cat.lookabaugh@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='cath6136');
insert into users(ldapname,fname,lname, email, status) SELECT 'marg7175','Margaret','Eker','margaret.eker@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='marg7175');
insert into users(ldapname,fname,lname, email, status) SELECT 'cons6216','Constanze','Kratel','constanze.kratel@rackspace.com', 'active' from dual where not exists(select ldapname from users where ldapname='cons6216');


insert into users(ldapname,fname,lname, email, status) SELECT 'jonathan.stovall','jonathan','stovall', 'jonathan.stovall@rackspace.com', 'active' from dual where not exists (select ldapname from users where ldapname='jonathan.stovall');

#insert initial values into groups table
insert into groups(name) SELECT 'all' from dual where not exists (select name from groups where name='all');
insert into groups(name) SELECT 'admin' from dual where not exists (select name from groups where name='admin');
insert into groups(name) SELECT 'readonly' from dual where not exists (select name from groups where name='readonly');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---auth-doc-2x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---auth-doc-2x');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---auth-doc-2x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---auth-doc-2x');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---rcbu-api-docs' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---rcbu-api-docs');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-files' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-files');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-block-storage' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-block-storage');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-servers-1x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-servers-1x');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-servers-2x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-servers-2x');
insert into groups(name) SELECT 'com.rackspace.cloud.api---service-registry' from dual where not exists (select name from groups where name='com.rackspace.cloud.api---service-registry');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-servers-1x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-servers-1x');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---auth-doc-3x' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---auth-doc-3x');
insert into groups(name) SELECT 'zz-com.rackspace.cloud.api---auth-doc-1x---deploy' from dual where not exists (select name from groups where name='zz-com.rackspace.cloud.api---auth-doc-1x---deploy');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---cloud-keep-docs' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---cloud-keep-docs');
insert into groups(name) SELECT 'com.rackspace.usage---usage-schema' from dual where not exists (select name from groups where name='com.rackspace.usage---usage-schema');
insert into groups(name) SELECT 'com.rackspace.cloud.apidocs---orchestration-docs' from dual where not exists (select name from groups where name='com.rackspace.cloud.apidocs---orchestration-docs');

insert into groups(name) SELECT 'com.rackspace.feedback---rax-feedback-backend' from dual where not exists (select name from groups where name='com.rackspace.feedback---rax-feedback-backend');
insert into groups(name) SELECT 'com.rackspace.feedback---rax-feedback-services' from dual where not exists (select name from groups where name='com.rackspace.feedback---rax-feedback-services');
insert into groups(name) SELECT 'com.rackspace.header.service---rax-headerservice' from dual where not exists (select name from groups where name='com.rackspace.header.service---rax-headerservice');



#insert initial values into members table
insert into members(ldapname, groupname) SELECT 'thu4404','all' from dual where not exists (select groupname from members where groupname='all');
insert into members(ldapname, groupname) SELECT 'david.cramer','all' from dual where not exists (select groupname from members where groupname='all');

insert into members(ldapname, groupname) SELECT 'davi4555','com.rackspace.cloud.apidocs---rcbu-api-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---rcbu-api-docs');
insert into members(ldapname, groupname) SELECT 'davi4555','com.rackspace.cloud.apidocs---cloud-files' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-files');
insert into members(ldapname, groupname) SELECT 'davi4555','com.rackspace.cloud.apidocs---cloud-block-storage' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-block-storage');
insert into members(ldapname, groupname) SELECT 'davi4555','com.rackspace.cloud.apidocs---cloud-servers-1x' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-servers-1x');
insert into members(ldapname, groupname) SELECT 'davi4555','com.rackspace.cloud.apidocs---cloud-servers-2x' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-servers-2x');

insert into members(ldapname, groupname) SELECT 'dia4454','all' from dual where not exists (select groupname from members where groupname='all');

insert into members(ldapname, groupname) SELECT 'gary.dusbabek','com.rackspace.cloud.api---service-registry' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.api---service-registry');

insert into members(ldapname, groupname) SELECT 'tomaz.muraus','com.rackspace.cloud.api---service-registry' from dual where not exists (select groupname from members where ldapname='com.rackspace.cloud.api---service-registry');

insert into members(ldapname, groupname) SELECT 'dan.dispaltro','com.rackspace.cloud.apidocs---cloud-monitoring' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-monitoring');

insert into members(ldapname, groupname) SELECT 'russell.haering','com.rackspace.cloud.apidocs---cloud-monitoring' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-monitoring');

insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.apidocs---loadbalancers-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---loadbalancers-docs');
insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.apidocs---sdks' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---sdks');
insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.apidocs---sdks-apiref' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---sdks-apiref');
insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.dbaas---dbaas-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.dbaas---dbaas-docs');
insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.dns.api---dns-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.dns.api---dns-docs');
insert into members(ldapname, groupname) SELECT 'mike.asthalter','com.rackspace.cloud.apidocs---orchestration-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---orchestration-docs');
insert into members(ldapname, groupname) SELECT 'raj.patel','foundation-api---incident' from dual where not exists (select groupname from members where groupname='foundation-api---incident');

insert into members(ldapname, groupname) SELECT 'marg7175','com.rackspace.cloud.api---auth-doc-1x' from dual where not exists (select groupname from members where groupname='foundation-api---incident');
insert into members(ldapname, groupname) SELECT 'marg7175','com.rackspace.cloud.apidocs---auth-doc-2x' from dual where not exists (select groupname fromfee members where groupname='com.rackspace.cloud.apidocs---auth-doc-2x');
insert into members(ldapname, groupname) SELECT 'marg7175','com.rackspace.cloud.apidocs---auth-doc-3x' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---auth-doc-3x');
insert into members(ldapname, groupname) SELECT 'marg7175','com.rackspace.cloud.apidocs---auth-wadls' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---auth-wadls');
insert into members(ldapname, groupname) SELECT 'marg7175','zz-com.rackspace.cloud.api---auth-doc-1x---deploy' from dual where not exists (select groupname from members where groupname='zz-com.rackspace.cloud.api---auth-doc-1x---deploy');

insert into members(ldapname, groupname) SELECT 'cons6216','com.rackspace.cloud.apidocs---cloud-keep-docs' from dual where not exists (select groupname from members where groupname='com.rackspace.cloud.apidocs---cloud-keep-docs');
insert into members(ldapname, groupname) SELECT 'cons6216','com.rackspace.usage---usage-schema' from dual where not exists (select groupname from members where groupname='com.rackspace.usage---usage-schema');

insert into members(ldapname, groupname) SELECT 'jonathan.stovall','com.rackspace.feedback---rax-feedback-backend' from dual where not exists (select groupname from members where groupname='com.rackspace.feedback---rax-feedback-backend');
insert into members(ldapname, groupname) SELECT 'jonathan.stovall','com.rackspace.feedback---rax-feedback-services' from dual where not exists (select groupname from members where groupname='com.rackspace.feedback---rax-feedback-services');
insert into members(ldapname, groupname) SELECT 'jonathan.stovall','com.rackspace.header.service---rax-headerservice' from dual where not exists (select groupname from members where groupname='com.rackspace.usage---usage-schema');

#insert initial values into status table
insert into status(value) SELECT 'started' from dual where not exists (select value from status where value='started');
insert into status(value) SELECT 'node1 started' from dual where not exists (select value from status where value='node1 started');
insert into status(value) SELECT 'other nodes started' from dual where not exists (select value from status where value='other nodes started');
insert into status(value) SELECT 'done' from dual where not exists (select value from status where value='done');
insert into status(value) SELECT 'failed' from dual where not exists (select value from status where value='failed');
insert into status(value) SELECT 'aborted' from dual where not exists (select value from status where value='aborted');
insert into status(value) SELECT 'deploy to internal from external' from dual where not exists (select value from status where value='deploy to internal from external');


