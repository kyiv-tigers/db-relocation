package kyiv.tigers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PersistenceConfiguration {

    @Bean(name = "publicbidDB")
    @ConfigurationProperties(prefix = "spring.datasource.public-bid")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "publicbidJdbcTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("publicbidDB") DataSource dsPublicbid) {
        return new JdbcTemplate(dsPublicbid);
    }

    @Bean(name = "opentenderDB")
    @ConfigurationProperties(prefix = "spring.datasource.open-tender")
    public DataSource postgresDataSource() {
        return  DataSourceBuilder.create().build();
    }

    @Bean(name = "opentenderJdbcTemplate")
    public JdbcTemplate postgresJdbcTemplate(@Qualifier("opentenderDB")
                                                     DataSource dsOpentender) {
        return new JdbcTemplate(dsOpentender);
    }
}
