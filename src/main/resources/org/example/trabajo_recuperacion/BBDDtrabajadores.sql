 drop database trabajadores;
 create database trabajadores;
 use trabajadores;
SET SQL_SAFE_UPDATES = 0;
CREATE TABLE trabajador
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    nombre     VARCHAR(45) NULL,
    puesto     VARCHAR(45) NULL,
    salario    INT NULL,
    fecha_alta DATE DEFAULT (CURDATE())

);
