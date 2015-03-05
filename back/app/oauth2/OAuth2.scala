/**
 * User: Anh Nguyen
 * Date: 12/23/12
 * Time: 6:23 AM
 * from: https://github.com/iizmoo/scala-oauth2
 */
package oauth2

import dispatch._
import java.util.concurrent.ExecutionException
import org.scala_tools.time.Imports.DateTime
import play.api.Logger
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class AuthToken(token: String, expiry: DateTime)

/**
 * This is the root OAuth2 implementation class that handle web access to the OAuth2 Servers.  Specific OAuth2 API classes are customized for each service provider to provide streamlined access.
 */
class OAuth2
{
  /** OAuth2 Refresh Token to exchange for Access Token */
  var refreshToken: String = ""

  /** OAuth2 Access Token use to access API */
  var accessToken: String = ""

  /** HTTP Response from server after sending a request */
  var response: String = ""

  /** HTTP dispatch.request object */
  var request: dispatch.Req = _

  /** Dispatch object doing the actually HTTP request execution */
  val browser = new Http

  /** Store the last error from execution.  Use for debugging */
  var error: String = ""

  /**
   * Use this function to generate the URL for an OAuth2 Authorization server where user grant request to our app
   * @param providerURI URI for the server to send request to
   * @param params Querystring parameters to generate the request URI
   * @return URL of Authorization Server with parameter for the application
   */
  def getOAuth2AuthorizationURI (
    providerURI: String,
    params: Map[String, String] = Map()
  ) : String =
  {
    request = url(providerURI).GET
    (request <<? params).url
  }

  def getOAuth2Request (
    providerTokenURI: String,
    params: Map[String, String] = Map()
  ) : Option[String] =
  {
    response = ""
    request = url(providerTokenURI).GET <<? params
    try
    {
      response = Await.result(browser(request OK as.String), 10 seconds)
      return Some(response)
    }
    catch
    {
      case e: ExecutionException => this.error = "HTTP Error: " + e.getMessage
      case e: Exception => this.error = "General Exception: " + e.getMessage
    }

    None
  }


  /**
   * Request a piece of information from the authorization server via POST.  Usually used to get an access token.
   * @param providerTokenURI URI where to send request
   * @param params POST parameters to send along with the request
   * @return Server response or None
   */
  def postOAuth2Request (
    providerTokenURI: String,
    params: Map[String, String] = Map()
  ) : Option[String] =
  {
    response = ""
    request = url(providerTokenURI).POST
    request << params
    try
    {
      response = Await.result(browser(request OK as.String), 0 nanos)
      return Some(response)
    }
    catch
    {
      case e: ExecutionException => this.error = "HTTP Error: " + e.getMessage
      case e: Exception => this.error = "General Exception: " + e.getMessage
    }

    None
  }

  /**
   * Get an OAuth2 API Resource from the server after we have an Access Token
   * @param resourceURI URI of resource being request
   * @param params Extra parameters to send to the server
   * @param headers Extra headers to send to the server
   * @return server response string or None
   */
  def getOAuth2Resource (
    resourceURI: String,
    params: Map[String, String] = Map(),
    headers: Map[String, String] = Map()
  ) : Option[String] =
  {
    request = url(resourceURI).GET
    request.addQueryParameter("access_token", accessToken)
    response = ""
    request <<? params
    for ( header <- headers )
    {
      request.addHeader(header._1, header._2)
    }

    try
    {
      response = Await.result(browser(request OK as.String), 0 nanos)
      return Some(response)
    }
    catch
    {
      case ex: ExecutionException => this.error = "HTTP Error: " + ex.getMessage
      case e: Exception => this.error = "Generic Error " + e.getMessage
    }

    None
  }
}

object ServiceProvider {
  import models.Global

  val facebook = new oauth2.Facebook(Global.facebookAppID, Global.facebookSecret)
}
