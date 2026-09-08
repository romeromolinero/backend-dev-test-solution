package com.romeromolinero.similarproducts.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ServiceInfoController {

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceInfo serviceInfo() {
        return new ServiceInfo(
                "API de productos similares",
                "disponible",
                "GET /product/1/similar",
                "GET /actuator/health",
                "El catálogo simulado debe ejecutarse en localhost:3001 para consultar productos");
    }

    public record ServiceInfo(
            String service,
            String status,
            String tryEndpoint,
            String healthEndpoint,
            String note) {
    }
}
