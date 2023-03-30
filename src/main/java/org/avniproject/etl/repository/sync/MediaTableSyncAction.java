package org.avniproject.etl.repository.sync;

import org.avniproject.etl.domain.ContextHolder;
import org.avniproject.etl.domain.NullObject;
import org.avniproject.etl.domain.metadata.ColumnMetadata;
import org.avniproject.etl.domain.metadata.SchemaMetadata;
import org.avniproject.etl.domain.metadata.TableMetadata;
import org.avniproject.etl.domain.result.SyncRegistrationConcept;
import org.avniproject.etl.repository.AvniMetadataRepository;
import org.avniproject.etl.service.EtlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.avniproject.etl.repository.JdbcContextWrapper.runInOrgContext;
import static org.avniproject.etl.repository.dynamicInsert.SqlFile.readFile;

@Repository
public class MediaTableSyncAction implements EntitySyncAction {
    private final JdbcTemplate jdbcTemplate;
    private final AvniMetadataRepository avniMetadataRepository;
    private static final Logger log = LoggerFactory.getLogger(EtlService.class);

    @Autowired
    public MediaTableSyncAction(JdbcTemplate jdbcTemplate, AvniMetadataRepository metadataRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.avniMetadataRepository = metadataRepository;
    }

    @Override
    public boolean supports(TableMetadata tableMetadata) {
        return tableMetadata.getType().equals(TableMetadata.Type.Media);
    }

    @Override
    public void perform(TableMetadata tableMetadata, Date lastSyncTime, Date dataSyncBoundaryTime, SchemaMetadata currentSchemaMetadata) {
        if (!this.supports(tableMetadata)) {
            return;
        }

        currentSchemaMetadata.getTableMetadata().stream().forEach(thisTableMetadata -> {
            List<ColumnMetadata> mediaColumns = thisTableMetadata.findColumnsMatchingConceptType(ColumnMetadata.ConceptType.Image, ColumnMetadata.ConceptType.Video);
            mediaColumns.forEach(mediaColumn -> {
                insertData(tableMetadata, thisTableMetadata, mediaColumn, lastSyncTime, dataSyncBoundaryTime);
            });
        });
    }

    private void insertData(TableMetadata mediaTableMetadata, TableMetadata tableMetadata, ColumnMetadata mediaColumn, Date lastSyncTime, Date dataSyncBoundaryTime) {
        String fromTableName = tableMetadata.getName();
        String subjectTypeName = avniMetadataRepository.subjectTypeName(tableMetadata.getSubjectTypeUuid());
        String programName = avniMetadataRepository.programName(tableMetadata.getProgramUuid());
        String encounterTypeName = avniMetadataRepository.encounterTypeName(tableMetadata.getEncounterTypeUuid());
        String conceptName = avniMetadataRepository.conceptName(mediaColumn.getConceptUuid());
        String conceptColumnName = mediaColumn.getName();
        SyncRegistrationConcept[] syncRegistrationConcepts = avniMetadataRepository.findSyncRegistrationConcepts(tableMetadata.getSubjectTypeUuid());


        tableMetadata.getColumnMetadataList().forEach(columnMetadata -> {
            if (equalsButNotBothNull(columnMetadata.getConceptUuid(), syncRegistrationConcepts[0].getUuid())) {
                syncRegistrationConcepts[0].setColumnName(columnMetadata.getName());
            }

            if (equalsButNotBothNull(columnMetadata.getConceptUuid(), syncRegistrationConcepts[1].getUuid())) {
                syncRegistrationConcepts[1].setColumnName(columnMetadata.getName());
            }
        });

        String templatePath = "/insertSql/media.sql";
        String sql = readFile(templatePath)
                .replace("${schema_name}", wrapInQuotes(ContextHolder.getDbSchema()))
                .replace("${table_name}", wrapInQuotes(mediaTableMetadata.getName()))
                .replace("${conceptColumnName}", wrapInQuotes(conceptColumnName))
                .replace("${syncRegistrationConcept1Name}", wrapStringValue(syncRegistrationConcepts[0].getName()))
                .replace("${syncRegistrationConcept1ColumnName}", wrapInQuotes(syncRegistrationConcepts[0].getColumnName()))
                .replace("${syncRegistrationConcept2Name}", wrapStringValue(syncRegistrationConcepts[1].getName()))
                .replace("${syncRegistrationConcept2ColumnName}", wrapInQuotes(syncRegistrationConcepts[0].getColumnName()))
                .replace("${subjectTypeName}", wrapStringValue(subjectTypeName))
                .replace("${encounterTypeName}", wrapStringValue(encounterTypeName))
                .replace("${programName}", wrapStringValue(programName))
                .replace("${conceptName}", wrapStringValue(conceptName))
                .replace("${fromTableName}", wrapInQuotes(fromTableName))
                .replace("${start_time}", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(lastSyncTime))
                .replace("${end_time}", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(dataSyncBoundaryTime));

        log.debug("Insert sql");
        log.debug(sql);

        runInOrgContext(() -> {
            jdbcTemplate.execute(sql);
            return NullObject.instance();
        }, jdbcTemplate);
    }

    private String wrapInQuotes(String parameter) {
        return parameter == null ? "null" : "\"" + parameter + "\"";

    }

    private String wrapStringValue(String parameter) {
        return parameter == null ? "null" : "'" + parameter + "'";
    }

    public static boolean equalsButNotBothNull(Object a, Object b) {
        return a != null && b != null && a.equals(b);
    }
}
