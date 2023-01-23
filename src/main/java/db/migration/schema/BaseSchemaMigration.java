package db.migration.schema;

import db.migration.BaseMigration;

public abstract class BaseSchemaMigration extends BaseMigration {
    protected String path() {
        return "schema";
    }
}