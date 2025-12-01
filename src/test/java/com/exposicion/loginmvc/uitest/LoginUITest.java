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
    
    // Puerto fijo para la aplicación
    private final int fixedPort = 8082;
    
    // CORRECCIÓN CLAVE: El valor por defecto es ahora 'app-test', 
    // el nombre resoluble del contenedor de la aplicación en la red de Docker.
    @Value("${app.host.name:app-test}") 
    private String appHostName; 

    // Host del Selenium Hub, pasado desde Jenkins
    @Value("${selenium.hub.host:selenium-hub}")
    private String seleniumHubHost;
    
    private String SELENIUM_HUB_URL;
    private String baseUrl;

    @BeforeEach
    void setupTest() throws Exception {
        
        // 1. Configurar la URL del Hub de Selenium
        SELENIUM_HUB_URL = "http://" + seleniumHubHost + ":4444/wd/hub";
        
        // 2. Configurar el WebDriver para Chrome Headless
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        options.setAcceptInsecureCerts(true);

        // 3. Inicializar RemoteWebDriver con reintentos para manejar el arranque de Selenium Grid
        int maxAttempts = 3;
        int currentAttempt = 0;
        Exception lastException = null;

        while (currentAttempt < maxAttempts) {
            try {
                driver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), options);
                break; // Conexión exitosa
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

        // 4. Configurar la Base URL de la aplicación
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
        // Lógica de interacción para credenciales válidas
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.tagName("button")).click();

        String titulo = driver.getTitle();
        assertTrue(titulo.contains("Bienvenido"), "Debe redirigir a la página de bienvenida después del login exitoso.");
    }

    @Test
    void testLogin_CredencialesInvalidas_DebeMostrarMensajeDeError() {
        driver.get(baseUrl);
        // Lógica de interacción para credenciales inválidas
        driver.findElement(By.name("username")).sendKeys("usuario-malo");
        driver.findElement(By.name("password")).sendKeys("clave-incorrecta");
        driver.findElement(By.tagName("button")).click();

        String src = driver.getPageSource();
        assertTrue(src.contains("Credenciales incorrectas"), "Debe mostrar un mensaje de error por credenciales inválidas.");
    }
}