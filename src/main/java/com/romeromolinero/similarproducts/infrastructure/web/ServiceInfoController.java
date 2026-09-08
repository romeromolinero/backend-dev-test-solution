package com.romeromolinero.similarproducts.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public final class ServiceInfoController {

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ServiceInfo serviceInfo() {
        return new ServiceInfo(
                "Similar Products API",
                "ready",
                "GET /product/1/similar",
                "GET /actuator/health",
                "The catalog mock must be running on localhost:3001 for product requests");
    }

    public record ServiceInfo(
            String service,
            String status,
            String tryEndpoint,
            String healthEndpoint,
            String note) {
    }
}
