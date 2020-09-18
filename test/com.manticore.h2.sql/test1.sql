/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  are
 * Created: Sep 19, 2020
 */

drop table B;
drop table A;

CREATE TABLE a
  (
     field1 varchar(1)
  );

CREATE TABLE b
  (
     field2 varchar(1)
  );

ALTER TABLE b
  ADD FOREIGN KEY (field2) REFERENCES a(field1);

