package orm;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Connector {

    private EntityManagerBuilder builder;
    private String adapter;
    private String driver;
    private String host;
    private String port;
    private String user;
    private String pass;


    public Connector(EntityManagerBuilder builder) {
        this.builder = builder;
    }

    public Connector setAdapter(String adapter) {
        this.adapter = adapter;
        return this;
    }

    public Connector setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public Connector setHost(String host) {
        this.host = host;
        return this;
    }

    public Connector setPort(String port) {
        this.port = port;
        return this;
    }

    public Connector setUser(String user) {
        this.user = user;
        return this;
    }

    public Connector setPass(String pass) {
        this.pass = pass;
        return this;
    }


    public EntityManagerBuilder createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", this.user);
        props.setProperty("password", this.pass);
        Connection connection = DriverManager.getConnection(this.driver + ":" +
                this.adapter + "://" +
                this.host + ":" +
                this.port, props);

        this.builder.setConnection(connection);

        return this.builder;
    }
}
