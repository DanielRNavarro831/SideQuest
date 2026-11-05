package com.dnavarro.sidequest.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TextRulesTest {
    @Test fun `names keep letters digits and spaces only`() {
        assertEquals("The One Who Wanders", TextRules.filterName("The One Who Wanders!", 32))
        assertEquals("Hero42", TextRules.filterName("He<r>o_4-2", 32))
    }

    @Test fun `names are capped while typing`() {
        assertEquals("a".repeat(32), TextRules.filterName("a".repeat(40), 32))
    }

    @Test fun `blank names clean to empty`() {
        assertEquals("", TextRules.cleanName("   ", 32))
        assertEquals("", TextRules.cleanName("!!!", 32))
    }

    @Test fun `clean name trims and caps`() {
        assertEquals("Hero", TextRules.cleanName("  Hero  ", 32))
        assertEquals("x".repeat(16), TextRules.cleanName("x".repeat(20), 16))
    }

    @Test fun `descriptions allow punctuation but no angle brackets`() {
        assertEquals("Wash, dry & put away!", TextRules.filterDescription("Wash, dry & put away!"))
        assertEquals("scriptalert(1)/script", TextRules.filterDescription("<script>alert(1)</script>"))
        assertEquals("ab", TextRules.filterDescription("a\nb"))
    }

    @Test fun `descriptions may exceed the limit while typing but are cut on save`() {
        val long = "d".repeat(70)
        assertEquals(70, TextRules.filterDescription(long).length)
        assertEquals(64, TextRules.cleanDescription(long).length)
    }

    @Test fun `number fields take at most 4 digits`() {
        assertEquals("1234", TextRules.filterDigits("12a345"))
    }

    @Test fun `profile name rejects blank and keeps hero`() {
        val s = AppState().withName("   ")
        assertEquals("Hero", s.profile.name)
        assertEquals("Ada", AppState().withName("Ada").profile.name)
    }

    @Test fun `title can be cleared`() {
        val s = AppState().withTitle("The Brave").withTitle("")
        assertEquals("", s.profile.title)
    }
}
