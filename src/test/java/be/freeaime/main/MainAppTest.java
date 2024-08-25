package be.freeaime.main;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MainAppTest {
    @Test
    public void recordHasInvalidValuesNoValueTest() {
        assertTrue(MainApp.recordHasInvalidValues(new String[] {}));
    }

    @Test
    public void recordHasInvalidValuesEmptyStringTest() {
        assertTrue(MainApp.recordHasInvalidValues(new String[] { "", "" }));
        assertTrue(MainApp.recordHasInvalidValues(new String[] { "123", "" }));
        assertTrue(MainApp.recordHasInvalidValues(new String[] { "", "123" }));
        assertTrue(MainApp.recordHasInvalidValues(new String[] { null, "123" }));
        assertTrue(MainApp.recordHasInvalidValues(new String[] { "123.123", "123.123" }));
    }

    @Test
    public void recordHasInvalidValuesValidStringTest() {
        assertFalse(MainApp.recordHasInvalidValues(new String[] { "123", "123" }));
    }

}
