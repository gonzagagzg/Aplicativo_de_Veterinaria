-- =========================================================================
-- Actualiza la clave de los usuarios de prueba a un hash BCrypt real.
--
-- El seed original (Script_Aplicacion_Veterinaria.sql) traia estos 5
-- usuarios con un hash falso ("$2b$12$ExampleHash1...") que nunca permite
-- iniciar sesion. Este script actualiza clave_hash sobre una base de datos
-- ya existente, sin necesidad de volver a sembrarla desde cero.
--
-- Contraseña resultante para los 5 usuarios: Prueba123!
-- Hash BCrypt cost 12, compatible con PasswordUtil.verificar del backend.
-- =========================================================================

UPDATE usuario SET clave_hash = '$2a$12$y8Cvy6PssoEjD3ockjHv.Or0Nm9ALRg6UC8QhUbALNi2o0D5GdamO'
WHERE usuario = 'admin_quito';

UPDATE usuario SET clave_hash = '$2a$12$AMw7LP.eKitsRLxZPlbM7uFUAAg.QJ/atKaiCgzXx2st1uztTL/v2'
WHERE usuario = 'doc_juarez';

UPDATE usuario SET clave_hash = '$2a$12$zO6jKrUO1d7KCheik.0GqOGVt0rjDw7sxGAbkLGtQ8ZEfGVb4m9e6'
WHERE usuario = 'admin_gye';

UPDATE usuario SET clave_hash = '$2a$12$naeqCZu1OnwtR7ni45ceteXhZm/S50Iix5Mijwgpwf2qLoHBQJOwi'
WHERE usuario = 'doc_silva';

UPDATE usuario SET clave_hash = '$2a$12$wpvM4sowy9ulD19fTgX1fu68j.ZbyirO1eo.eAOPVtUowv2m6tBOq'
WHERE usuario = 'recep_lucia';
