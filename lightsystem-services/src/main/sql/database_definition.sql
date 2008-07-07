-- Drop the database if it exists and re-create it.
drop database  if exists lightcontrol;
create database lightcontrol;
use lightcontrol;

-- Drivers.
create table boarddrivers (
	id integer(2) unsigned not null auto_increment,
	classname varchar(255) not null,
	constraint pk_boarddrivers primary key (id)
) type = InnoDB;

-- Boards.
create table boards
(
	id integer(3) unsigned not null,
	type enum ('DIGITAL', 'DIMMER') not null,
	number_of_channels integer(2) unsigned not null,
	constraint pk_boards primary key (id)
) type = InnoDB;

-- Lights.
create table lights
(
	id varchar(50) not null,
	board_id integer(3) unsigned not null,
	channel_number integer(2) unsigned not null,
	description varchar(255) not null,
	index idx_board_id (board_id),
	CONSTRAINT uk_board_id_channel_number unique index idx_board_id_channel_number (board_id, channel_number),
	constraint pk_lights primary key (id),
	constraint fk_lights_boards foreign key (board_id) references boards(id)
) type = InnoDB;

-- First we need a way to generate errors from triggers and such. We need this to ensure data integrity.
-- For this we use the UDF method as suggested on http://rpbouman.blogspot.com/2005/11/using-udf-to-raise-errors-from-inside.html
-- The shared lib needs to be in /usr/lib for this to work.
create function raise_error returns int soname 'libmysql_raise_error.so';

-- Also create an on insert trigger on lights making sure that no light gets inserted on the wrong channel number.
delimiter $$

create trigger trg_insert_light_check_channel_number 
before insert on lights
for each row
begin
	declare number_of_channels_on_board integer(2) unsigned;
	declare dummy long;

	select number_of_channels from boards where id = new.board_id into number_of_channels_on_board;
	
	if new.channel_number > number_of_channels_on_board - 1
	then
		select raise_error('CHANNEL_NOT_AVAILABLE') into dummy;
	end if;
end$$

delimiter ;

-- Tables for the spheres
create table spheres
(
	id varchar(50) not null,
	description varchar(255) not null,
	constraint pk_spheres primary key (id)
) type = InnoDB;

-- Digital lights associated to the spheres.
create table sphere_digital_lights
(
	sphere_id varchar(50) not null,
	light_id varchar(50) not null,
	light_on enum('Y', 'N') not null,
	constraint pk_sphere_digital_lights primary key (sphere_id, light_id),
	constraint fk_sphere_digital_lights_spheres foreign key (sphere_id) references spheres(id),
	constraint fk_sphere_digital_lights_lights foreign key (light_id) references lights(id)
) type = InnoDB;

-- Trigger making sure that only digital lights are inserted here.
delimiter $$

create trigger trg_insert_sphere_digital_light_ensure_digital_light 
before insert on sphere_digital_lights
for each row
begin
	declare light_type enum('DIGITAL', 'DIMMER');
	declare dummy long;

	select type from boards where id = (select board_id from lights where id = new.light_id) into light_type;
	
	if type <> 'DIGITAL'
	then
		select raise_error('LIGHT_NOT_DIGITAL') into dummy;
	end if;
end$$

delimiter ;

-- Dimmer lights associated to the spheres.
create table sphere_dimmer_lights
(
	sphere_id varchar(50) not null,
	light_id varchar(50) not null,
	light_on enum('Y', 'N') not null,
	percentage integer(3) not null,
	constraint pk_sphere_dimmer_lights primary key (sphere_id, light_id),
	constraint fk_sphere_dimmer_lights_spheres foreign key (sphere_id) references spheres(id),
	constraint fk_sphere_dimmer_lights_lights foreign key (light_id) references lights(id)
) type = InnoDB;

-- Trigger making sure that only dimmer lights are inserted here.
delimiter $$

create trigger trg_insert_sphere_digital_light_ensure_dimmer_light 
before insert on sphere_dimmer_lights
for each row
begin
	declare light_type enum('DIGITAL', 'DIMMER');
	declare dummy long;

	select type from boards where id = (select board_id from lights where id = new.light_id) into light_type;
	
	if type <> 'DIMMER'
	then
		select raise_error('LIGHT_NOT_DIMMER') into dummy;
	end if;
end$$

delimiter ;

