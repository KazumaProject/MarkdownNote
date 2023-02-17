package com.kazumaproject.markdownnote.di

import android.content.Context
import com.kazumaproject.markdownnote.other.GrammarLocatorDef
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
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
    fun provideMarkWon(@ApplicationContext context: Context) =
        Markwon.builder(context)
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(
                SyntaxHighlightPlugin.create(
                    Prism4j(GrammarLocatorDef()),
                    Prism4jThemeDarkula.create(0))
            )
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create((context.resources.displayMetrics.widthPixels)/ (context.resources.displayMetrics.density), object : JLatexMathPlugin.BuilderConfigure{
                override fun configureBuilder(builder: JLatexMathPlugin.Builder) {
                    builder.inlinesEnabled(true)
                }

            }))
            .usePlugin(StrikethroughPlugin.create())
            .build()
}