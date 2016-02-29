package br.ufsc.silq.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile(Constants.SPRING_PROFILE_HEROKU)
public class HerokuDatabaseConfiguration {

	private final Logger log = LoggerFactory.getLogger(HerokuDatabaseConfiguration.class);

	@Bean
	public DataSource dataSource(DataSourceProperties dataSourceProperties, JHipsterProperties jHipsterProperties) {
		this.log.debug("Configuring Heroku Datasource");

		String herokuUrl = System.getenv("JDBC_DATABASE_URL");
		if (herokuUrl != null) {
			HikariConfig config = new HikariConfig();

			// MySQL optimizations, see
			// https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
			if ("com.mysql.jdbc.jdbc2.optional.MysqlDataSource".equals(dataSourceProperties.getDriverClassName())) {
				config.addDataSourceProperty("cachePrepStmts", jHipsterProperties.getDatasource().isCachePrepStmts());
				config.addDataSourceProperty("prepStmtCacheSize",
						jHipsterProperties.getDatasource().getPrepStmtCacheSize());
				config.addDataSourceProperty("prepStmtCacheSqlLimit",
						jHipsterProperties.getDatasource().getPrepStmtCacheSqlLimit());
			}

			config.setDataSourceClassName(dataSourceProperties.getDriverClassName());
			config.addDataSourceProperty("url", herokuUrl);
			return new HikariDataSource(config);
		} else {
			throw new ApplicationContextException(
					"Heroku database URL is not configured, you must set $JDBC_DATABASE_URL");
		}
	}
}
