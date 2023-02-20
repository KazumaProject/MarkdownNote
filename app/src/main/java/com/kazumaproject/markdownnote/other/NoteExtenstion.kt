package com.kazumaproject.markdownnote.other

import com.kazumaproject.markdownnote.database.note.NoteEntity
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkEntity
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftEntity
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashEntity

fun NoteBookMarkEntity.convertNoteEntity(): NoteEntity{
    return NoteEntity(
        body = body,
        emojiUnicode = emojiUnicode,
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

fun NoteTrashEntity.convertNoteEntity(): NoteEntity{
    return NoteEntity(
        body = body,
        emojiUnicode = emojiUnicode,
        createdAt = createdAt,
        updatedAt = updatedAt,
        id = id
    )
}