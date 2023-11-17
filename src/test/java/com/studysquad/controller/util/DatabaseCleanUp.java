package com.studysquad.controller.util;

import static com.google.common.base.CaseFormat.*;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleanUp implements InitializingBean {

	private final EntityManager em;
	private List<String> tableNames;

	public DatabaseCleanUp(EntityManager em) {
		this.em = em;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		tableNames = em.getMetamodel().getEntities().stream()
			.filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
			.map(e -> UPPER_CAMEL.to(LOWER_UNDERSCORE, e.getName()))
			.collect(Collectors.toList());
	}

	@Transactional
	public void cleanUp() {
		em.flush();
		em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

		for (String tableName : tableNames) {
			String validateTableName = validateTableName(tableName);
			String columnName = getColumnName(tableName);

			em.createNativeQuery("TRUNCATE TABLE " + validateTableName).executeUpdate();
			em.createNativeQuery("ALTER TABLE " + validateTableName
					+ " ALTER COLUMN " + columnName
					+ " RESTART WITH 1")
				.executeUpdate();
		}
		em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
	}

	private String validateTableName(String tableName) {
		return isReservedKeyword(tableName) ? makePlural(tableName) : tableName;
	}

	private boolean isReservedKeyword(String tableName) {
		return tableName.equalsIgnoreCase("user");
	}

	private String makePlural(String tableName) {
		return createStringBuilder()
			.append(tableName)
			.append("s")
			.toString();
	}

	private String getColumnName(String tableName) {
		return createStringBuilder()
			.append(tableName)
			.append("_id")
			.toString();
	}

	private StringBuilder createStringBuilder() {
		return new StringBuilder();
	}
}
