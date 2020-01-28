package com.github.pdalpra.computerdb.http.html

import scalatags.Text._
import scalatags.Text.all._

private[html] object PageTemplate {

  def pageTemplate(contents: Frag*): TypedTag[String] =
    html(
      head(
        tags2.title("Computers database"),
        cssStyleSheet("/assets/css/bootstrap.min.css"),
        cssStyleSheet("/assets/css/main.css")
      ),
      body(
        header(`class` := "topbar")(
          h1(`class` := "fill")(
            a(`class` := "fill", href := "/computers")("Computer database")
          )
        ),
        tag("section")(id := "main")(
          contents
        )
      )
    )

  private def cssStyleSheet(url: String) =
    link(rel := "stylesheet", `type` := "text/css", media := "screen", href := url)

}
