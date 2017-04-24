package playasophy.wonderdome.mode

import playasophy.wonderdome.util.Color
import clojure.lang.Associative
import clojure.lang.Keyword
import clojure.java.api.Clojure

case class ScalaRedMode() extends Mode[ScalaRedMode] {

  override def update(event: Associative): ScalaRedMode = this

  override def render(pixel: Associative): Int = Color.RED

}
