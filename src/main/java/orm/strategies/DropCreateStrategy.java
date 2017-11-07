package orm.strategies;

import orm.utils.TableCreator;
import orm.scanner.ClassEntityScanner;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DropCreateStrategy extends SchemaInitializationStrategyAbstract {



    public DropCreateStrategy(Connection connection,
                              TableCreator creator,
                              String dataSource,
                              ClassEntityScanner entityScanner) {
        super(connection, creator, dataSource, entityScanner);
    }

    @Override
    public void execute() throws SQLException, IOException, ClassNotFoundException {
        String query="DROP DATABASE IF EXISTS `"+super.dataSource+"`;";
        this.connection.prepareStatement(query).executeUpdate();
        query="CREATE DATABASE `"+this.dataSource+"`;";
        this.connection.prepareStatement(query).execute();
        this.createTables(this.scanForEntities());
    }

    private void createTables(Map<String, Class> entities) throws SQLException {
        for (Class entity : entities.values()) {
            this.creator.doCreate(entity);
        }
    }
}
