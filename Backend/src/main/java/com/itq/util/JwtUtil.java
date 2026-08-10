package com.itq.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

public final class JwtUtil {

    private static final String SECRET =
            "VETERINARIAITQ_CAMBIAR_ESTA_CLAVE_EN_PRODUCCION_2026";

    private static final Algorithm ALGORITHM =
            Algorithm.HMAC256(SECRET);

    private static final JWTVerifier VERIFIER =
            JWT.require(ALGORITHM)
                    .withIssuer("VETERINARIAITQ")
                    .build();

    private JwtUtil() {
    }

    public static String generarToken(
            UUID idUsuario,
            UUID idEmpresa,
            Integer idRol,
            String rol
    ) {

        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(8, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer("VETERINARIAITQ")
                .withSubject(idUsuario.toString())
                .withClaim(
                        "idEmpresa",
                        idEmpresa == null ? null : idEmpresa.toString()
                )
                .withClaim("idRol", idRol)
                .withClaim("rol", rol)
                .withIssuedAt(Date.from(ahora))
                .withExpiresAt(Date.from(expiracion))
                .sign(ALGORITHM);
    }

    public static DecodedJWT verificarToken(String token) {
        return VERIFIER.verify(token);
    }
}