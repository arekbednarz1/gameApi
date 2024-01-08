package pl.arekbednarz.gameshopapi.testcontainers;

import org.jboss.logging.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static pl.arekbednarz.gameshopapi.testcontainers.TestContainerNetwork.NETWORK;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MySqlContainerResource implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOG = Logger.getLogger(MySqlContainerResource.class);
    private static final int PORT = 3306;
    private static final String DATABASE = "gamestock_base_db";
    private static final String USERNAME = "test.user";
    private static final String PASSWORD = "test.password";

    @Container
    private MySQLContainer<?> mySQLContainer;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.mySQLContainer = new MySqlContainerConfig<>("mysql:8.0")
                .withExposedPorts(PORT)
                .withNetwork(NETWORK)
                .withNetworkAliases("mysql.docker")
                .withDatabaseName(DATABASE)
                .withUsername(USERNAME)
                .withPassword(PASSWORD)
                .withUrlParam("characterEncoding", "utf8")
                .withUrlParam("characterSetResults", "utf8")
                .withUrlParam("connectionCollation", "utf8_general_ci")
                .withUrlParam("useUnicode", "true")
                .withUrlParam("useJDBCCompliantTimezoneShift", "true")
                .withUrlParam("useLegacyDatetimeCode", "false")
                .withUrlParam("serverTimezone", "UTC")
                .withUrlParam("useSSL", "false")
                .withUrlParam("allowPublicKeyRetrieval", "true")
                .withCommand(
                        "--character-set-server=utf8",
                        "--collation-server=utf8_general_ci")
                .waitingFor(Wait.forListeningPort());

        LOG.infof("Starting MySQL container");

        mySQLContainer.start();
        mySQLContainer.followOutput(OutputUtils::forward);

        final var address = mySQLContainer.getJdbcUrl();
        final var username = mySQLContainer.getUsername();
        final var password = mySQLContainer.getPassword();

        final var properties = TestPropertyValues.of(
                "spring.datasource.url="+address,
                "spring.datasource.username="+username,
                "spring.datasource.password="+password
                );
        properties.applyTo(applicationContext.getEnvironment());
    }


    private static class MySqlContainerConfig<S extends MySQLContainer<S>> extends MySQLContainer<S> {

        public MySqlContainerConfig(String dockerImageName) {
            super(dockerImageName);
        }

        @Override
        public String getDriverClassName() {
            return com.mysql.cj.jdbc.Driver.class.getName();
        }
    }
}
