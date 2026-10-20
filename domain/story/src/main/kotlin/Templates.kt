package com.novelforge.domain.story
val kinds =
    listOf(
        "Chapter",
        "Arc",
        "Character",
        "Location",
        "Backstory",
        "Worldbuilding",
        "Plot thread",
        "Research",
        "Screenplay",
        "Timeline event",
        "Note",
    )
val statuses = listOf("Draft", "In progress", "Revised", "Final")

fun template(kind: String): String =
    when (kind) {
        "Character" ->
            "## Role in the story\n\n## Desire & motivation\n\n## Fear & flaw\n\n## Appearance & voice\n\n## Backstory\n\n## Relationships\n\n## Character arc\n"
        "Timeline event" ->
            "## When\n\n## What happens\n\n## People involved\n\n## Cause\n\n## Consequence\n"
        "Location" ->
            "## Atmosphere\n\n## Geography\n\n## History\n\n## People & culture\n\n## Story significance\n"
        "Arc" ->
            "## Central conflict\n\n## Beginning\n\n## Turning point\n\n## Climax & resolution\n"
        "Backstory" ->
            "## What happened?\n\n## Who was involved?\n\n## When did it happen?\n\n## Consequences in the present\n"
        "Plot thread" -> "## Setup\n\n## Foreshadowing\n\n## Development\n\n## Payoff\n"
        "Screenplay" ->
            "INT. LOCATION - DAY\n\nDescribe the action.\n\nCHARACTER\nDialogue begins here.\n"
        "Worldbuilding" -> "## Rules\n\n## Culture & history\n\n## Limitations\n\n## Connections\n"
        else -> ""
    }
