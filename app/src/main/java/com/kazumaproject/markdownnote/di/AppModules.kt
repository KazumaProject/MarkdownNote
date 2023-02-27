package com.kazumaproject.markdownnote.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.kazumaproject.markdownnote.preferences.SharedPreferences
import com.kazumaproject.markdownnote.database.note.NoteDatabase
import com.kazumaproject.markdownnote.database.note_bookmark.NoteBookMarkDatabase
import com.kazumaproject.markdownnote.database.note_draft.NoteDraftDatabase
import com.kazumaproject.markdownnote.database.note_trash.NoteTrashDatabase
import com.kazumaproject.markdownnote.other.Constants.NOTE_BOOKMARK_DATABASE_NAME
import com.kazumaproject.markdownnote.other.Constants.NOTE_DATABASE_NAME
import com.kazumaproject.markdownnote.other.Constants.NOTE_DRAFT_DATABASE_NAME
import com.kazumaproject.markdownnote.other.Constants.NOTE_TRASH_DATABASE_NAME
import com.kazumaproject.markdownnote.other.FileManageUtil
import com.kazumaproject.markdownnote.other.GrammarLocatorDef
import com.kazumaproject.markdownnote.other.TaskListToggleSpan
import com.kazumaproject.markdownnote.preferences.PreferenceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.*
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListItem
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tasklist.TaskListSpan
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.file.FileSchemeHandler
import io.noties.markwon.image.gif.GifMediaDecoder
import io.noties.markwon.image.svg.SvgPictureMediaDecoder
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.syntax.Prism4jThemeDarkula
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.Prism4j
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    @Singleton
    @Provides
    fun providesFileManageUtil(
        @ApplicationContext context: Context
    ) = FileManageUtil(context)

    @Provides
    @Singleton
    fun providesSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        SharedPreferences.init(context)
        return SharedPreferences
    }

    @Provides
    @Singleton
    fun providesPreferenceImpl(
        sharedPreferences: SharedPreferences
    ): PreferenceImpl = PreferenceImpl(sharedPreferences)

    @Singleton
    @Provides
    fun provideGson() = GsonBuilder().disableHtmlEscaping().create()

    @Singleton
    @Provides
    fun provideMarkWon(@ApplicationContext context: Context): Markwon =
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(ImagesPlugin.create {
                it.addSchemeHandler(FileSchemeHandler.create())
                it.addMediaDecoder(GifMediaDecoder.create(true))
                it.addMediaDecoder(SvgPictureMediaDecoder.create())
            })
            .usePlugin(
                SyntaxHighlightPlugin.create(
                    Prism4j(GrammarLocatorDef()),
                    Prism4jThemeDarkula.create(0))
            )
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(50f
            ) { builder ->
                builder.apply {
                    inlinesEnabled(true)
                    blocksEnabled(true)
                    blocksLegacy(true)
                }
            })
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(object : AbstractMarkwonPlugin(){
                override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                    super.configureSpansFactory(builder)
                    val origin = builder.getFactory(TaskListItem::class.java) ?: return
                    builder.setFactory(TaskListItem::class.java
                    ) { configuration, props ->
                        val span = origin.getSpans(configuration, props) as TaskListSpan
                        arrayOf(
                            span,
                            TaskListToggleSpan(span)
                        )
                    }
                }
            })
            .build()

    @Singleton
    @Provides
    fun providesNoteDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NoteDatabase::class.java, NOTE_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesNoteDao(db: NoteDatabase) = db.noteDao()

    @Singleton
    @Provides
    fun providesNoteDraftDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NoteDraftDatabase::class.java, NOTE_DRAFT_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesNoteDraftDao(db: NoteDraftDatabase) = db.noteDraftDao()

    @Singleton
    @Provides
    fun providesNoteTrashDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NoteTrashDatabase::class.java, NOTE_TRASH_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesNoteTrashDao(db: NoteTrashDatabase) = db.noteTrashDao()

    @Singleton
    @Provides
    fun providesNoteBookmarkDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NoteBookMarkDatabase::class.java, NOTE_BOOKMARK_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun providesNoteBookmarkDao(db: NoteBookMarkDatabase) = db.noteBookmarkDao()
}