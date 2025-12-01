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

// CORRECCIÓN CLAVE: Usamos DEFINED_PORT para forzar el uso de server.port=8082
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginUITest {

    private WebDriver driver;
    
    // El puerto ahora es fijo (8082) y ya no se inyecta con @LocalServerPort
    private final int fixedPort = 8082; 
    
    // Lee el host (que es "jenkins_practica" pasado por el script bash)
    // private final String appHostName = "host.docker.internal";
    private final String appHostName = System.getProperty("app.host.name", "localhost");


    @Value("${selenium.hub.host:selenium-hub}")
    private String seleniumHubHost;
    
    private String SELENIUM_HUB_URL;
    private String baseUrl; // Se inicializará con el puerto fijo

    @BeforeEach
    void setupTest() throws Exception {
        
        // 1. Configurar la URL del Hub
        SELENIUM_HUB_URL = "http://" + seleniumHubHost + ":4444/wd/hub";
        
        // 2. Configurar el WebDriver
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        options.setAcceptInsecureCerts(true);

        driver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), options);
        driver.manage().window().maximize();

        // 3. Configurar la Base URL para la prueba
        // CORRECCIÓN: Usamos el puerto fijo 8082
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

        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("password123");
        driver.findElement(By.tagName("button")).click();

        String titulo = driver.getTitle();
        assertTrue(titulo.contains("Bienvenido"), "Debe redirigir a la página de bienvenida después del login exitoso.");
    }

    @Test
    void testLogin_CredencialesInvalidas_DebeMostrarMensajeDeError() {
        driver.get(baseUrl);

        driver.findElement(By.name("username")).sendKeys("usuario-malo");
        driver.findElement(By.name("password")).sendKeys("clave-incorrecta");
        driver.findElement(By.tagName("button")).click();

        String src = driver.getPageSource();
        assertTrue(src.contains("Credenciales incorrectas"), "Debe mostrar un mensaje de error por credenciales inválidas.");
    }
}