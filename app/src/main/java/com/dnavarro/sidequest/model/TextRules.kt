package com.dnavarro.sidequest.model

object Limits {
    const val NAME_MAX = 32
    const val TITLE_MAX = 32
    const val SKILL_NAME_MAX = 16
    const val DESCRIPTION_MAX = 64
    const val THRESHOLD_MIN = 1
    const val THRESHOLD_MAX = 9999
    const val X_MIN = 2
    const val X_MAX = 9999
    const val NUMBER_DIGITS = 4
}

/**
 * Input rules shared by every text field. "filter" functions run on each keystroke/paste;
 * "clean" functions produce the value that actually gets saved.
 */
object TextRules {
    const val REQUIRED_ERROR = "* At least 1 character is required"

    /** Names, titles and skill names: letters, digits and spaces only. */
    fun isNameChar(c: Char): Boolean = c.isLetterOrDigit() || c == ' '

    fun filterName(input: String, max: Int): String = input.filter(::isNameChar).take(max)

    /** The value to store. An empty result means the user left it blank. */
    fun cleanName(input: String, max: Int): String =
        input.filter(::isNameChar).trim().take(max).trim()

    /**
     * Descriptions allow punctuation but not angle brackets (so nothing tag-like can be
     * entered) or control characters. Compose renders text literally, so this is belt-and-braces.
     */
    fun isDescriptionChar(c: Char): Boolean = !c.isISOControl() && c != '<' && c != '>'

    /** While typing, descriptions may run past the limit so the counter can show it. */
    fun filterDescription(input: String): String = input.filter(::isDescriptionChar)

    fun cleanDescription(input: String): String =
        filterDescription(input).trim().take(Limits.DESCRIPTION_MAX).trim()

    fun filterDigits(input: String): String = input.filter { it in '0'..'9' }.take(Limits.NUMBER_DIGITS)
}
