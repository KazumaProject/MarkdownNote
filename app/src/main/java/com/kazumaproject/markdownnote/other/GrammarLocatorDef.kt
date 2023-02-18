package com.kazumaproject.markdownnote.other

import com.kazumaproject.markdownnote.other.languages.*
import io.noties.prism4j.GrammarLocator
import io.noties.prism4j.Prism4j

class GrammarLocatorDef : GrammarLocator{
    override fun grammar(prism4j: Prism4j, language: String): Prism4j.Grammar? {
        return when(language){

            "c","C"-> Prism_c.create(prism4j)

            "clike","Clike","c like","CLike",
            "c Like","C like", "C Like" -> Prism_clike.create(prism4j)

            "clojure","Clojure", "CLOJURE" -> Prism_clojure.create(prism4j)

            "cpp","Cpp","CPP","c++","C++"
            ,"c ++","C ++"-> Prism_cpp.create(prism4j)

            "csharp","Csharp","cSharp","CSharp",
            "C#","C #","c#","c #" -> Prism_csharp.create(prism4j)

            "css","CSS","Css" -> Prism_css.create(prism4j)

            "css_extra","CSS_extra",
            "css_Extra","CSS_Extra"-> Prism_css_extras.create(prism4j)

            "dart","Dart","DART" -> Prism_dart.create(prism4j)

            "git","Git","GIT" -> Prism_git.create(prism4j)

            "go","Go","GO" -> Prism_go.create(prism4j)

            "groovy","Groovy","GROOVY" -> Prism_groovy.create(prism4j)

            "java","Java","JAVA" -> Prism_java.create(prism4j)

            "javascript","JavaScript","java script",
            "Java Script","Java script","java Script" -> Prism_javascript.create(prism4j)

            "json","Json","JSON" -> Prism_json.create(prism4j)

            "kotlin","Kotlin","KOTLIN" -> Prism_kotlin.create(prism4j)

            "latex","Latex","LATEX" -> Prism_latex.create(prism4j)

            "makefile","Makefile","MakeFile","MAKEFILE" -> Prism_makefile.create(prism4j)

            "markdown","Markdown","markDown",
            "MarkDown", "MARKDOWN" -> Prism_markdown.create(prism4j)

            "markup","Markup","markUp",
            "MarkUp","MARKUP" -> Prism_markup.create(prism4j)

            "python","Python","PYTHON" -> Prism_python.create(prism4j)

            "scala","Scala","SCALA" -> Prism_scala.create(prism4j)

            "sql","SQL","Sql" -> Prism_sql.create(prism4j)

            "swift","Swift","SWIFT" -> Prism_swift.create(prism4j)

            "yaml","Yaml","YAML" -> Prism_yaml.create(prism4j)

            else ->{
                null
            }
        }
    }

    override fun languages(): MutableSet<String> {
        return mutableSetOf()
    }
}