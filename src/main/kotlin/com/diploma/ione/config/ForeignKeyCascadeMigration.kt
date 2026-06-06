package com.diploma.ione.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class ForeignKeyCascadeMigration(
    private val jdbcTemplate: JdbcTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    @Order(100)
    fun applyCascadeToAllForeignKeys() {
        val updated = jdbcTemplate.queryForList(FIND_NON_CASCADE_FKS_SQL)
        if (updated.isEmpty()) {
            log.info("All foreign keys already use ON DELETE CASCADE")
            return
        }

        log.info("Updating {} foreign key(s) to ON DELETE CASCADE", updated.size)
        updated.forEach { row ->
            val schema = row["schema_name"] as String
            val table = row["table_name"] as String
            val constraint = row["constraint_name"] as String
            val columns = row["column_names"] as String
            val foreignTable = row["foreign_table_name"] as String
            val foreignColumns = row["foreign_column_names"] as String

            jdbcTemplate.execute(
                "ALTER TABLE \"$schema\".\"$table\" DROP CONSTRAINT \"$constraint\""
            )
            jdbcTemplate.execute(
                """
                ALTER TABLE "$schema"."$table"
                ADD CONSTRAINT "$constraint"
                FOREIGN KEY ($columns) REFERENCES "$foreignTable"($foreignColumns)
                ON DELETE CASCADE
                """.trimIndent()
            )
            log.info("Updated FK {}.{}.{}", schema, table, constraint)
        }
        log.info("Foreign key cascade migration completed")
    }

    companion object {
        private val FIND_NON_CASCADE_FKS_SQL = """
            SELECT
                src_ns.nspname AS schema_name,
                src.relname AS table_name,
                con.conname AS constraint_name,
                tgt.relname AS foreign_table_name,
                (
                    SELECT string_agg(quote_ident(attname), ', ' ORDER BY array_position(con.conkey, attnum))
                    FROM pg_attribute
                    WHERE attrelid = con.conrelid AND attnum = ANY(con.conkey)
                ) AS column_names,
                (
                    SELECT string_agg(quote_ident(attname), ', ' ORDER BY array_position(con.confkey, attnum))
                    FROM pg_attribute
                    WHERE attrelid = con.confrelid AND attnum = ANY(con.confkey)
                ) AS foreign_column_names
            FROM pg_constraint con
            JOIN pg_class src ON src.oid = con.conrelid
            JOIN pg_namespace src_ns ON src_ns.oid = src.relnamespace
            JOIN pg_class tgt ON tgt.oid = con.confrelid
            WHERE con.contype = 'f'
              AND src_ns.nspname = 'public'
              AND con.confdeltype <> 'c'
        """.trimIndent()
    }
}
