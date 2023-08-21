package org.avniproject.etl.domain.metadata;

import org.avniproject.etl.domain.metadata.diff.Diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class SchemaMetadata {
    private List<TableMetadata> tableMetadata;

    public SchemaMetadata(List<TableMetadata> tableMetadata) {
        this.tableMetadata = tableMetadata;
    }

    public List<TableMetadata> getTableMetadata() {
        return tableMetadata;
    }

    public void setTableMetadata(List<TableMetadata> tableMetadata) {
        this.tableMetadata = tableMetadata;
    }

    public List<Diff> findChanges(SchemaMetadata current) {
        List<TableMetadata> newTables = this.getTableMetadata();
        List<Diff> diffs = new ArrayList<>();
        newTables.forEach(newTable -> {
            diffs.addAll(findChanges(current, newTable));
        });
        return diffs;
    }

    public Optional<TableMetadata> findMatchingTable(TableMetadata newTable) {
        return this.getTableMetadata().stream().filter(currentTable -> currentTable.matches(newTable)).findFirst();
    }

    public List<TableMetadata> getAllSubjectTables() {
        return tableMetadata.stream().filter(TableMetadata::isSubjectTable).toList();
    }

    public List<TableMetadata> getAllProgramEnrolmentTables() {
        return tableMetadata.stream().filter(table -> table.getType() == TableMetadata.Type.ProgramEnrolment).toList();
    }

    public List<TableMetadata> getAllProgramEncounterTables() {
        return tableMetadata.stream().filter(table -> table.getType() == TableMetadata.Type.ProgramEncounter).toList();
    }

    public List<TableMetadata> getAllEncounterTables() {
        return tableMetadata.stream().filter(table -> table.getType() == TableMetadata.Type.Encounter).toList();
    }

    private List<Diff> findChanges(SchemaMetadata currentSchema, TableMetadata newTable) {
        List<Diff> diffs = new ArrayList<>();
        Optional<TableMetadata> optionalMatchingTable = currentSchema.findMatchingTable(newTable);
        if (optionalMatchingTable.isPresent()) {
            TableMetadata matchingTable = optionalMatchingTable.get();
            diffs.addAll(newTable.findChanges(matchingTable));
        } else {
            diffs.addAll(newTable.createNew());
        }
        return diffs;
    }

    public void mergeWith(SchemaMetadata oldSchemaMetadata) {
        getTableMetadata()
                .forEach(newTable ->
                        oldSchemaMetadata
                                .findMatchingTable(newTable)
                                .ifPresent(newTable::mergeWith));
    }

    public String getCountsByType() {
        Map<TableMetadata.Type, List<TableMetadata>> grouped = this.tableMetadata.stream().collect(groupingBy(TableMetadata::getType));
        List<String> strings = grouped.entrySet().stream().map(x -> String.format("%s-%d", x.getKey(), x.getValue().size())).collect(Collectors.toList());
        return String.join(";", strings);
    }
}
