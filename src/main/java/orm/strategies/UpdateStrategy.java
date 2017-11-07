package orm.strategies;

import annotations.Column;
import orm.utils.TableCreator;
import orm.scanner.ClassEntityScanner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class UpdateStrategy extends SchemaInitializationStrategyAbstract {


    public UpdateStrategy(Connection connection,
                          TableCreator creator,
                          String dataSource,
                          ClassEntityScanner entityScanner) {
        super(connection, creator, dataSource, entityScanner);
    }

    @Override
    public void execute() throws SQLException, IOException, ClassNotFoundException {

        for (Map.Entry<String,Class> entry : this.scanForEntities().entrySet()) {

            if(!this.checkIfTableExists(entry.getKey())){
                this.creator.doCreate(entry.getValue());
            }

            this.checkTableFields(entry.getValue(), this.creator.getTableName(entry.getValue()));
        }

    }


    private void checkTableFields(Class entity, String tableName) throws SQLException {

        for (Field field : entity.getDeclaredFields()) {
            field.setAccessible(true);

            if(!this.checkIfFieldExistsInDb(field,tableName)){
                this.addFieldToTable(tableName,field);
            }
        }
    }

    private void addFieldToTable(String tableName, Field field) throws SQLException {

        String query = "ALTER TABLE "+ tableName;
        query += " ADD " + this.creator.getFieldName(field) + " " + this.creator.getDatabaseType(field);

        this.connection.prepareStatement(query).execute();
    }

    private boolean checkIfTableExists(String tableName) throws SQLException {
        String query = "SHOW TABLES FROM `" + this.dataSource
                + "` WHERE `Tables_in_" + this.dataSource + "` LIKE '" + tableName + "';";
        ResultSet rs = this.connection.createStatement().executeQuery(query);
        return rs.isBeforeFirst();
    }

    private boolean checkIfFieldExistsInDb(Field field, String tableName) throws SQLException {
        String fieldName = field.getAnnotation(Column.class).name();
        String query = "SHOW COLUMNS FROM " + tableName + " LIKE '" + fieldName + "' ;";

        ResultSet rs = this.connection.createStatement().executeQuery(query);
        return rs.isBeforeFirst();
    }

}
