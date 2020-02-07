package com.github.pdalpra.computerdb.model

import com.github.pdalpra.computerdb.ComputerDatabaseSpec

class PageSpec extends ComputerDatabaseSpec {

  "Page.previous" should "return the previous page number if there is a page available" in {
    forAll { pageNumber: Int =>
      whenever(pageNumber > 1) {

        val page = Page(Nil, Page.Number.unsafeFrom(pageNumber), 0, 0)

        page.previous shouldBe Some(Page.Number.unsafeFrom(pageNumber - 1))
      }
    }
  }

  it should "return None if there is no previous page available" in {
    val page = Page(Nil, Page.Number.unsafeFrom(1), 0, 0)
    page.previous shouldBe None
  }
}
