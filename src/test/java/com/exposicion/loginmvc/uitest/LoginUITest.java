package com.exposicion.loginmvc.uitest;

import java.net.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Usamos DEFINED_PORT para asegurar que Spring Boot levante la app en 8082
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginUITest {

    private WebDriver driver;
    
    private final int fixedPort = 8082;
    
    // CORRECCIÓN CLAVE: Usamos @Value para inyectar el host definido por Maven (app-test)
    // El valor por defecto ahora es host.docker.internal, aunque 'app-test' es el deseado
    @Value("${app.host.name:host.docker.internal}")
    private String appHostName; 

    @Value("${selenium.hub.host:selenium-hub}")
    private String seleniumHubHost;
    
    private String SELENIUM_HUB_URL;
    private String baseUrl;

    @BeforeEach
    void setupTest() throws Exception {
        
        // 1. Configurar la URL del Hub
        SELENIUM_HUB_URL = "http://" + seleniumHubHost + ":4444/wd/hub";
        
        // 2. Configurar el WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        options.setAcceptInsecureCerts(true);

        // Intentar hasta 3 veces para conectar al Selenium Hub
        int maxAttempts = 3;
        int currentAttempt = 0;
        Exception lastException = null;

        while (currentAttempt < maxAttempts) {
            try {
                driver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), options);
                break; // Éxito
            } catch (Exception e) {
                lastException = e;
                currentAttempt++;
                System.out.println("ADVERTENCIA: Falló conexión con Selenium Hub. Intento " + currentAttempt + "/" + maxAttempts + ". Esperando 2 segundos...");
                Thread.sleep(2000);
            }
        }

        if (driver == null) {
            throw new RuntimeException("Fallo al inicializar RemoteWebDriver después de " + maxAttempts + " intentos.", lastException);
        }

        driver.manage().window().maximize();

        // 3. Configurar la Base URL para la prueba
        baseUrl = "http://" + appHostName + ":" + fixedPort + "/";
        
        System.out.println("DEBUG: App Base URL para la prueba UI: " + baseUrl);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testLogin_CredencialesValidas_DebeIrAHome() {
        driver.get(baseUrl);
        // ... (resto del test)
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.tagName("button")).click();

        String titulo = driver.getTitle();
        assertTrue(titulo.contains("Bienvenido"), "Debe redirigir a la página de bienvenida después del login exitoso.");
    }

    @Test
    void testLogin_CredencialesInvalidas_DebeMostrarMensajeDeError() {
        driver.get(baseUrl);
        // ... (resto del test)
        driver.findElement(By.name("username")).sendKeys("usuario-malo");
        driver.findElement(By.name("password")).sendKeys("clave-incorrecta");
        driver.findElement(By.tagName("button")).click();

        String src = driver.getPageSource();
        assertTrue(src.contains("Credenciales incorrectas"), "Debe mostrar un mensaje de error por credenciales inválidas.");
    }
}