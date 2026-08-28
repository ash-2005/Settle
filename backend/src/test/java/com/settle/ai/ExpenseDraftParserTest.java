package com.settle.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExpenseDraftParserTest {

    @Test
    void readsAmountAndGroceryDescription() {
        UUID me = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID rahul = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var draft = ExpenseDraftParser.parse(
                "I paid 1850 for groceries, split between me Rahul",
                me,
                List.of(
                        new ExpenseDraftParser.NamedPerson(me, "Ashmit"),
                        new ExpenseDraftParser.NamedPerson(rahul, "Rahul")));
        assertEquals("1850.00", draft.get("amount"));
        assertEquals("Groceries", draft.get("description"));
        assertTrue(((List<?>) draft.get("participantIds")).contains(rahul.toString()));
    }
}
