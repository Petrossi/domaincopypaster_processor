package db.migration;

import java.nio.charset.StandardCharsets;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.util.FileCopyUtils;

public abstract class BaseMigration extends BaseJavaMigration {
    protected abstract String path();

    @Override
    public void migrate(Context context) throws Exception {
        String filePath = String.format("migrations/%s/%s.sql", path(), getVersion().toString());

        ClassPathResource classPathResource = new ClassPathResource(filePath);

        byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());

        String sql = new String(bytes, StandardCharsets.UTF_8);

        new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true)).execute(sql);
    }
}