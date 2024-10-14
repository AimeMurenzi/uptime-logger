package be.freeaime.app.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RecordManagerTest {
    @Test
    public void recordHasInvalidValuesNoValueTest() {
        assertTrue(RecordManager.recordHasInvalidValues(new String[] {}));
    }

    @Test
    public void recordHasInvalidValuesEmptyStringTest() {
        assertTrue(RecordManager.recordHasInvalidValues(new String[] { "", "" }));
        assertTrue(RecordManager.recordHasInvalidValues(new String[] { "123", "" }));
        assertTrue(RecordManager.recordHasInvalidValues(new String[] { "", "123" }));
        assertTrue(RecordManager.recordHasInvalidValues(new String[] { null, "123" }));
        assertTrue(RecordManager.recordHasInvalidValues(new String[] { "123.123", "123.123" }));
    }

    @Test
    public void recordHasInvalidValuesValidStringTest() {
        assertFalse(RecordManager.recordHasInvalidValues(new String[] { "123", "123" }));
    }

}
