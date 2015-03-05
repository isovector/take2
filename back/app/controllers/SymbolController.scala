package controllers

import play.api._
import play.api.mvc._

object SymbolController extends Controller {

  def getSymbol(symbolId: Int) = Action {
    Ok(
      views.html.symbol(symbolId)
    )
  }
}
