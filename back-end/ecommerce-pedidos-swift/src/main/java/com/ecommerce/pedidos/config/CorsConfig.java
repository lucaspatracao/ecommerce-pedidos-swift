package com.ecommerce.pedidos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS num lugar só (Aula 15), em vez de um {@code @CrossOrigin} em cada controller.
 *
 * <p>Libera o front-end do Vite ({@code npm run dev}, porta 5173) para chamar
 * {@code /api/**}. O cabeçalho {@code Location} é exposto para o navegador
 * deixar o JavaScript ler o endereço do recurso criado num POST.</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    static final String[] ORIGENS_PERMITIDAS = {"http://localhost:5173", "http://127.0.0.1:5173"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(ORIGENS_PERMITIDAS)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .exposedHeaders(HttpHeaders.LOCATION);
    }
}
