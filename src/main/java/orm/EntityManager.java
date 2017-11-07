package orm;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import orm.strategies.SchemaInitializationStrategy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityManager<E> implements DbContext<E> {

    private Connection connection;
    private String dataSource;
    private SchemaInitializationStrategy strategy;


    public EntityManager(Connection connection, String dataSource, SchemaInitializationStrategy strategy) throws SQLException, IOException, ClassNotFoundException {
        this.connection = connection;
        this.dataSource = dataSource;
        this.strategy = strategy;
        this.strategy.execute();
    }

    @Override
    public boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        if (value == null || (long) value <= 0) {
            return this.doInsert(entity, primary);
        }

        return this.doUpdate(entity, primary);
    }

    @Override
    public boolean delete(Class<E> table, String where) throws SQLException {

        String query = "DELETE FROM " + this.getTableName(table);
        if (where != null) {
            query += " WHERE " + where;
        }
        return this.connection.prepareStatement(query).execute();
    }

    @Override
    public Iterable<E> find(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException {

        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM " + this.getTableName(table).toLowerCase();
        ResultSet rs = stmt.executeQuery(query);

        List<E> objectsFetched = new ArrayList<>();

        while (rs.next()) {
            E entity = table.newInstance();
            this.fillEntity(table, rs, entity);
            objectsFetched.add(entity);
        }
        return objectsFetched;
    }

    @Override
    public Iterable<E> find(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException {

        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM " + this.getTableName(table).toLowerCase() +
                " WHERE 1 " + (where != null ? " AND " + where : "");
        ResultSet rs = stmt.executeQuery(query);

        List<E> objectsFetched = new ArrayList<>();

        while (rs.next()) {
            E entity = table.newInstance();
            this.fillEntity(table, rs, entity);
            objectsFetched.add(entity);
        }
        return objectsFetched;
    }

    @Override
    public E findFirst(Class<E> table) throws SQLException, IllegalAccessException, InstantiationException {
        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM " + this.getTableName(table).toLowerCase() + " LIMIT 1";
        ResultSet rs = stmt.executeQuery(query);
        E entity = table.newInstance();

        if (!rs.isBeforeFirst()) {
            return null;
        }
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }


    public E findFirst(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException {
        Statement stmt = this.connection.createStatement();
        String query = "SELECT * FROM " + this.getTableName(table).toLowerCase() +
                " WHERE 1 " + (where != null ? " AND " + where : "") + " LIMIT 1";
        ResultSet rs = stmt.executeQuery(query);
        E entity = table.newInstance();
        if (!rs.isBeforeFirst()) {
            return null;
        }
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }

    private Field getId(Class entity) {
        return Arrays.stream(entity.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() ->
                        new UnsupportedOperationException("Entity does not have primary key"));
    }

    private boolean doInsert(E entity, Field primary) throws SQLException {
        String tableName = this.getTableName(entity.getClass());
        String query = "INSERT INTO " + tableName + " (";

        List<String> columnNames = new ArrayList<>();
        List<String> columnValues = new ArrayList<>();

        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .forEach(f -> {

                    if (!f.isAnnotationPresent(Id.class)) {
                        columnNames.add(this.getColumnName(f));
                        f.setAccessible(true);
                        try {
                            columnValues.add("'" + f.get(entity).toString() + "'");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                });

        query += String.join(", ", columnNames) + ") VALUES (" + String.join(", ", columnValues) + " )";
        PreparedStatement stmt = this.connection.prepareStatement(query);
        return stmt.execute();
    }


    private boolean doUpdate(E entity, Field primary) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName(entity.getClass());
        String query = "UPDATE " + tableName + " SET ";

        List<String> updateStatements = new ArrayList<>();

        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Column.class))
                .forEach(f -> {

                    if (!f.isAnnotationPresent(Id.class)) {
                        f.setAccessible(true);

                        try {
                            String value = f.get(entity).toString();
                            updateStatements.add(f.getAnnotation(Column.class).name() +
                                    " = '" + value + "'");
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                });

        query += String.join(", ", updateStatements);

        query += " WHERE " + this.getColumnName(primary) + "=?";

        PreparedStatement stmt = this.connection.prepareStatement(query);
        stmt.setLong(1, (Long) primary.get(entity));
        return stmt.execute();
    }

    private String getTableName(Class<?> entity) {


        if (entity.isAnnotationPresent(Entity.class)) {
            Entity entityAnnotation = entity.getAnnotation(Entity.class);
            return this.dataSource + "." + entityAnnotation.name();
        }

        throw new UnsupportedOperationException("Unrecognized entity!");
    }


    private String getColumnName(Field field) {

        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).name();
        }

        throw new UnsupportedOperationException("Unrecognized column!");
    }

    private void fillEntity(Class<E> table, ResultSet rs, E entity) throws SQLException, IllegalAccessException {
        Field[] fields = table.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            fillField(field, entity, rs, field.getAnnotation(Column.class).name());
        }
    }

    private static void fillField(Field field, Object instance, ResultSet rs, String fieldName) throws SQLException, IllegalAccessException {
        field.setAccessible(true);

        try {
            if (field.getType() == int.class || field.getType() == Integer.class) {
                field.set(instance, rs.getInt(fieldName));
            } else if (field.getType() == long.class || field.getType() == Long.class) {
                field.set(instance, rs.getLong(fieldName));
            } else if (field.getType().equals(String.class)) {
                field.set(instance, rs.getString(fieldName));
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                field.set(instance, rs.getBoolean(fieldName));
            } else if (field.getType().equals(LocalDate.class)) {
                field.set(instance, rs.getDate(fieldName).toLocalDate());
            }
        } catch (NullPointerException npe) {
            field.set(instance,null);
        }
    }
}
