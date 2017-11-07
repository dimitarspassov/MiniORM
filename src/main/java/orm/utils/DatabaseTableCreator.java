package orm.utils;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class DatabaseTableCreator implements TableCreator {

    private Connection connection;
    private String dataSource;

    public DatabaseTableCreator(Connection connection, String dataSource) {
        this.connection = connection;
        this.dataSource = dataSource;
    }

    @Override
    public void doCreate(Class entity) throws SQLException {
        String tableName = this.getTableName(entity);
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + "( ";

        Field[] fields = entity.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            if (!field.isAnnotationPresent(Column.class)) {
                continue;
            }
            query += field.getName() + " " + this.getDatabaseType(field);

            if (field.isAnnotationPresent(Id.class)) {
                query += " PRIMARY KEY AUTO_INCREMENT";
            }

            if (i < fields.length - 1) {
                query += ", ";
            }
        }

        query += ")";
        this.connection.prepareStatement(query).execute();
    }

    @Override
    public String getFieldName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).name();
        }

        throw new UnsupportedOperationException("Unrecognized column!");
    }

    @Override
    public String getTableName(Class<?> entity) {

        if (entity.isAnnotationPresent(Entity.class)) {
            Entity entityAnnotation = entity.getAnnotation(Entity.class);
            return "`"+this.dataSource+"`.`"+entityAnnotation.name()+"`";
        }

        throw new UnsupportedOperationException("Unrecognized entity!");
    }

    @Override
    public String getDatabaseType(Field field) {
        field.setAccessible(true);

        if (field.getType() == int.class || field.getType() == Integer.class) {
            return "INT";
        } else if (field.getType() == long.class || field.getType() == Long.class) {
            return "INT";
        } else if (field.getType().equals(String.class)) {
            return "VARCHAR(200)";
        } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
            return "BIT";
        } else if (field.getType().equals(LocalDate.class)) {
            return "DATE";
        } else if (field.getType() == byte.class || field.getType() == Byte.class) {
            return "TINYINT";
        } else if (field.getType() == double.class || field.getType() == Double.class) {
            return "DOUBLE";
        } else if (field.getType() == float.class || field.getType() == Float.class) {
            return "FLOAT";
        }
        return "";
    }
}
