package org.avniproject.etl.repository.rowMappers.tableMappers;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TableTest {
    @Test
    void shouldGetAllColumns() {
        assertThat(new SubjectTable().columns().size(), is(13));
        assertThat(new PersonTable().columns().size(), is(16));

        assertThat(new EncounterTable().columns().size(), is(16));
        assertThat(new ProgramEncounterCancellationTable().columns().size(), is(17));
        assertThat(new ProgramEncounterTable().columns().size(), is(17));

        assertThat(new ProgramEnrolmentTable().columns().size(), is(15));
        assertThat(new ProgramExitTable().columns().size(), is(15));
    }
}