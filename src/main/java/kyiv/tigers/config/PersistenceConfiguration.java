package kyiv.tigers.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:importer.properties")
public class PersistenceConfiguration {

    @Bean(name = "publicbidDB")
    @ConfigurationProperties(prefix = "spring.datasource.public-bid")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "publicbidJdbcTemplate")
    public NamedParameterJdbcTemplate jdbcTemplate(@Qualifier("publicbidDB") DataSource dsPublicbid) {
        return new NamedParameterJdbcTemplate(dsPublicbid);
    }

    @Bean(name = "opentenderDB")
    @ConfigurationProperties(prefix = "spring.datasource.open-tender")
    public DataSource postgresDataSource() {
        return  DataSourceBuilder.create().build();
    }

    @Bean(name = "opentenderJdbcTemplate")
    public NamedParameterJdbcTemplate postgresJdbcTemplate(@Qualifier("opentenderDB")
                                                     DataSource dsOpentender) {
        return new NamedParameterJdbcTemplate(dsOpentender);
    }
}
