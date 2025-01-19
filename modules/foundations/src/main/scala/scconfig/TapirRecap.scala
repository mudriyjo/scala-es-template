package scconfig

import zio.*
import zio.http.{Server, ServerConfig}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.ztapir.*

object TapirRecap extends ZIOAppDefault {

  val simpleEndpoint = endpoint
    .tag("hello")
    .name("hello")
    .description("Hello world endpoint")
    .get
    .in("")
    .out(stringBody)

  val server = Server.serve(
    ZioHttpInterpreter().toHttp(simpleEndpoint.zServerLogic[Any](_ => ZIO.succeed("Hello there!")))
  )
  override def run = server.provide(
    Server.live,
    ZLayer.succeed(ServerConfig.default.binding("localhost", 8080)),
  )
}
