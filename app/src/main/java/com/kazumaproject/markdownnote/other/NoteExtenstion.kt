package com.kazumaproject.markdownnote.other

import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity

fun NoteEntity.convertNoteBookMarkEntity(): NoteBookMarkEntity{
    return NoteBookMarkEntity(
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id
    )
}

fun NoteEntity.convertNoteDraftEntity(): NoteDraftEntity{
    return NoteDraftEntity(
        body = body,
        emojiUnicode = emojiUnicode,
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id
    )
}

fun NoteEntity.convertNoteTrashEntity(): NoteTrashEntity{
    return NoteTrashEntity(
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id
    )
}

fun NoteDraftEntity.convertNoteEntity(): NoteEntity{
    return NoteEntity(
        body = body,
        emojiUnicode = emojiUnicode,
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id
    )
}
