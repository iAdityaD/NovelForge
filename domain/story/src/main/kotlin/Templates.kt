package com.novelforge.domain.story
val kinds =
    listOf("Resource", "External source",
        "Chapter",
        "Novel Profile",
        "Guide",
        "Arc",
        "Character",
        "Location",
        "Backstory",
        "Worldbuilding",
        "Plot thread",
        "Research",
        "Theme",
        "Prop",
        "Screenplay",
        "Timeline event",
        "Note",
    )


fun template(kind: String): String =
    when (kind) {
        "Chapter" -> ""
        "Novel Profile" -> "## Working title\n\n## Genre\n\n## Core premise\n\n## Short synopsis\n\n## Main characters\n\n## Current writing status\n\n## Next steps\n"

        "Character" ->
            "## Name and role\n\n## Personality and background\n\n## Goals and motivations\n\n## Strengths, flaws and fears\n\n## Relationships\n\n## Character development\n"
        "Theme" -> "## Central idea\n\n## Questions to explore\n\n## Moments that express it\n"
        "Prop" -> "## Description\n\n## Who owns it\n\n## Story significance\n"
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
