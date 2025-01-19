package com.scconfig

import zio.{Duration, ZIO, ZIOAppDefault}

object ZioRecap extends ZIOAppDefault {

  val meanningOfLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)
  val failuere: ZIO[Any, String, Nothing] = ZIO.fail("Something wrong happend")
  val suspension = ZIO.suspend(meanningOfLife)

  val mapMOL = meanningOfLife.map(num => num * 2)
  val flatmapMOL = meanningOfLife.flatMap(num => ZIO.succeed(num * 2))

  val attempt = ZIO.attempt {
    val a: Array[_] = null
    a.length
  }.catchAll(e => ZIO.succeed(s"Catch error ${e}"))

  // fiber
  val calcValue = ZIO.sleep(Duration.fromMillis(1000L)).flatMap(_ => zio.Random.nextIntBetween(0, 100))

    override def run = {
      val calcPar = (0 to 10).map(_ => calcValue.fork)
      val calcSeq = (0 to 10).map(_ => calcValue)
      val a = for {
        start <- ZIO.succeed(System.nanoTime())
        res <- ZIO.collectAllPar(calcPar)
        result <- ZIO.foreach(res)(_.join)
        end <- ZIO.succeed(System.nanoTime())
        _ <- zio.Console.printLine(s"par result is: ${result}, time: ${end - start}")
      } yield ()

      val b = for {
        start <- ZIO.succeed(System.nanoTime())
        result <- ZIO.collectAll(calcSeq)
        end <- ZIO.succeed(System.nanoTime())
        _ <- zio.Console.printLine(s"seq result is: ${result}, time: ${end - start}")
      } yield ()

      a.flatMap(_ => b)
    }
}
