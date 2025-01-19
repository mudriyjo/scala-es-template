package com.scconfig

import zio.{Duration, Task, ZIO, ZIOAppDefault, ZLayer}

import scala.language.postfixOps

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
//  val calcValue = ZIO.sleep(Duration.fromMillis(1000L)).flatMap(_ => zio.Random.nextIntBetween(0, 100))
//
//    override def run = {
//      val calcPar = (0 to 10).map(_ => calcValue.fork)
//      val calcSeq = (0 to 10).map(_ => calcValue)
//      val a = for {
//        start <- ZIO.succeed(System.nanoTime())
//        res <- ZIO.collectAllPar(calcPar)
//        result <- ZIO.foreach(res)(_.join)
//        end <- ZIO.succeed(System.nanoTime())
//        _ <- zio.Console.printLine(s"par result is: ${result}, time: ${end - start}")
//      } yield ()
//
//      val b = for {
//        start <- ZIO.succeed(System.nanoTime())
//        result <- ZIO.collectAll(calcSeq)
//        end <- ZIO.succeed(System.nanoTime())
//        _ <- zio.Console.printLine(s"seq result is: ${result}, time: ${end - start}")
//      } yield ()
//
//      a.flatMap(_ => b)
//    }

  // dependency
  case class User(name: String, email: String)
  class UserNotification(val emailService: EmailService, val userRepository: UserRepository) {
    def nofify(user: User): Task[Unit] = {
      for {
        _ <- emailService.send(user)
        _ <- userRepository.save(user)
        res <- ZIO.succeed(s"User notified $user")
        _ <- zio.Console.printLine(res)
      } yield ()
    }

  }

  object UserNotification {
    val live: ZLayer[EmailService with UserRepository, Nothing, UserNotification] =
      ZLayer.fromFunction((emailService, userRepository) => UserNotification(emailService, userRepository))
  }

  class EmailService() {
    def send(user: User): Task[Unit] = ZIO.succeed(s"User send $user").flatMap(msg => zio.Console.printLine(msg))
  }

  object EmailService {
    val live: ZLayer[Any, Nothing, EmailService] =
      ZLayer.succeed(EmailService())
  }

  class UserRepository(connectionPool: ConnectionPool) {
    def save(user: User): Task[Unit] = ZIO.succeed(s"User saved $user").flatMap(msg => zio.Console.printLine(msg))
  }

  object UserRepository {
    val live: ZLayer[ConnectionPool, Nothing, UserRepository] =
      ZLayer.fromFunction((pool) => UserRepository(pool))
  }

  class ConnectionPool(nConnection: Int) {
    def getConnection(): Task[Connection] = ZIO.succeed(Connection())
  }

  object ConnectionPool {
    def live(nConnection: Int): ZLayer[Any, Nothing, ConnectionPool] =
      ZLayer.succeed(ConnectionPool(nConnection))
  }

  case class Connection()

  def notify(user: User): ZIO[UserNotification, Throwable, Unit] = for {
    notification <- ZIO.service[UserNotification]
    _ <- notification.nofify(user)
  } yield()

  val program = for {
    _ <- notify(User("Alex", "test@test.com"))
    _ <- notify(User("Jhon", "test2@test.com"))
  } yield ()

  override def run = program.provide(
    ConnectionPool.live(5),
    UserRepository.live,
    UserNotification.live,
    EmailService.live
  )
}
