CREATE TABLE tb_contadores (
    contador_id BIGINT PRIMARY KEY,
    numero_registro VARCHAR(50),
    direccion_servicio VARCHAR(255),
    estado INT,
    cliente_id BIGINT,
    tipo_servicio_id BIGINT
);

CREATE TABLE tb_lecturas (
    lectura_id BIGINT PRIMARY KEY,
    lectura_anterior DECIMAL(12,2),
    lectura_actual DECIMAL(12,2),
    consumo_litros DECIMAL(12,2),
    litros_exceso DECIMAL(12,2),
    monto_base DECIMAL(12,2),
    monto_exceso DECIMAL(12,2),
    monto_total DECIMAL(12,2),
    fecha DATE,
    contador_id BIGINT,
    usuario_lector_id BIGINT,
    tarifa_base_id BIGINT,
    tarifa_exceso_id BIGINT
);

INSERT INTO tb_contadores VALUES
(1, 'CTR-001', 'Sector Centro', 1, 1, 1),
(2, 'CTR-002', 'Sector Norte', 1, 2, 1);

INSERT INTO tb_lecturas VALUES
(1, 100, 120, 20, 0, 60, 0, 60, '2026-10-21', 1, 1, 1, 2),
(2, 200, 235, 35, 5, 60, 10, 70, '2026-10-21', 2, 1, 1, 2);

-- Restringir el usuario del microservicio a lectura
REVOKE ALL PRIVILEGES ON `oficina\_agua`.* FROM 'f04_docker_ro'@'%';
GRANT SELECT ON `oficina\_agua`.* TO 'f04_docker_ro'@'%';
