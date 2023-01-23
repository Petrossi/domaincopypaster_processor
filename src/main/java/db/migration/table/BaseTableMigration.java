package db.migration.table;

import db.migration.BaseMigration;

public abstract class BaseTableMigration extends BaseMigration {
    protected String path() {
        return "table";
    }
}