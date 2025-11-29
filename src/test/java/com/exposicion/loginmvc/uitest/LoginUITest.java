package com.exposicion.loginmvc.uitest;

import java.net.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginUITest {

    private WebDriver driver;

    @LocalServerPort
    private int port;

    private String baseUrl;

    private static final String SELENIUM_HUB_URL = "http://selenium-hub:4444/wd/hub";

    @BeforeAll
    static void setupClass() {
        // No se usa WebDriverManager en Selenium Grid
    }

    @BeforeEach
    void setupTest() throws Exception {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");

        driver = new RemoteWebDriver(new URL(SELENIUM_HUB_URL), options);

        driver.manage().window().maximize();

        // *** CAMBIO IMPORTANTE ***
        // "localhost" dentro del contenedor Selenium NO apunta a tu app.
        // host.docker.internal sí apunta al host donde corre Spring Boot.
        baseUrl = "http://host.docker.internal:" + port + "/";
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

        String pageTitle = driver.getTitle();
        assertTrue(pageTitle.contains("Bienvenido"),
                "El login exitoso debe llevar a la página de Bienvenida.");
    }

    @Test
    void testLogin_CredencialesInvalidas_DebeMostrarMensajeDeError() {
        driver.get(baseUrl);

        driver.findElement(By.name("username")).sendKeys("usuario-malo");
        driver.findElement(By.name("password")).sendKeys("clave-incorrecta");
        driver.findElement(By.tagName("button")).click();

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Credenciales incorrectas"),
                "El login fallido debe mostrar el mensaje de 'Credenciales incorrectas'.");
    }
}
