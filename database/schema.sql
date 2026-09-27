CREATE DATABASE IF NOT EXISTS biblioteca_universitaria;

USE biblioteca_universitaria;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS multas;
DROP TABLE IF EXISTS prestamos;
DROP TABLE IF EXISTS materiales;
DROP TABLE IF EXISTS usuarios;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE usuarios (
    id INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    tipo ENUM('ESTUDIANTE', 'DOCENTE') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE materiales (
    codigo VARCHAR(20) PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    tipo ENUM('LIBRO', 'REVISTA') NOT NULL,
    cantidad_total INT NOT NULL,
    cantidad_disponible INT NOT NULL,

    CONSTRAINT chk_material_total
        CHECK (cantidad_total > 0),

    CONSTRAINT chk_material_disponible
        CHECK (
            cantidad_disponible >= 0
            AND cantidad_disponible <= cantidad_total
        )
);

CREATE TABLE prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    material_codigo VARCHAR(20) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_devolucion DATE NULL,

    CONSTRAINT fk_prestamo_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_prestamo_material
        FOREIGN KEY (material_codigo)
        REFERENCES materiales(codigo),

    CONSTRAINT chk_fecha_devolucion
        CHECK (
            fecha_devolucion IS NULL
            OR fecha_devolucion >= fecha_inicio
        )
);

CREATE INDEX idx_prestamos_usuario
    ON prestamos(usuario_id);

CREATE INDEX idx_prestamos_material
    ON prestamos(material_codigo);

CREATE INDEX idx_prestamos_vencimiento
    ON prestamos(fecha_vencimiento);

CREATE TABLE multas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL UNIQUE,
    dias_atraso INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    pagada BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_multa_prestamo
        FOREIGN KEY (prestamo_id)
        REFERENCES prestamos(id),

    CONSTRAINT chk_dias_atraso
        CHECK (dias_atraso > 0),

    CONSTRAINT chk_monto_multa
        CHECK (monto > 0)
);

INSERT INTO usuarios (
    id, nombre, apellido, tipo, activo
) VALUES
    (1001, 'Emiliano', 'Funez', 'ESTUDIANTE', TRUE),
    (2001, 'Ana', 'Gómez', 'DOCENTE', TRUE);

INSERT INTO materiales (
    codigo, titulo, tipo, cantidad_total, cantidad_disponible
) VALUES
    ('L001', 'Programación Orientada a Objetos', 'LIBRO', 3, 3),
    ('L002', 'Bases de Datos', 'LIBRO', 2, 2),
    ('R001', 'Revista de Ingeniería de Software', 'REVISTA', 2, 2);
