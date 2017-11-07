package orm.strategies;

import orm.scanner.ClassEntityScanner;
import orm.utils.TableCreator;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

public abstract class SchemaInitializationStrategyAbstract implements SchemaInitializationStrategy {

    private ClassEntityScanner entityScanner;
    TableCreator creator;
    Connection connection;
    String dataSource;


    public SchemaInitializationStrategyAbstract(Connection connection, TableCreator creator,
                                         String dataSource, ClassEntityScanner entityScanner) {
        this.entityScanner = entityScanner;
        this.creator = creator;
        this.connection = connection;
        this.dataSource = dataSource;
    }

    Map<String, Class> scanForEntities() throws ClassNotFoundException, IOException {
        return this.entityScanner.listFilesForFolder(System.getProperty("user.dir"));
    }
}
