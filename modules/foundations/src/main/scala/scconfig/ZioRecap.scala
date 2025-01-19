package com.scconfig

import zio.{ZIO, ZIOAppDefault}

object ZioRecap extends ZIOAppDefault {

  val meanningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  val failuere: ZIO[Any, String, Nothing] = ZIO.fail("Something wrong happend")
  val suspension = ZIO.suspend(meanningOfLife)

  val mapMOL = meanningOfLife.map(num => num * 2)
  val flatmapMOL = meanningOfLife.flatMap(num => ZIO.succeed(num * 2))

  val attempt = ZIO.attempt{
    val a: Array[_] = null
    a.length
  }.catchAll(e => ZIO.succeed(s"Catch error ${e}"))

  override def run = {
    for {
      res <- attempt
      _ <- zio.Console.printLine(res)
    } yield ()
  }
}
