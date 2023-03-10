//> using lib "org.typelevel::cats-core:2.9.0"
//> using lib "org.typelevel::cats-effect:3.4.8"
//> using lib "com.monovore::decline:2.4.1"
//> using lib "com.monovore::decline-effect:2.4.1"
//> using lib "com.lihaoyi::os-lib:0.9.1"

// create repository
// pull repository using ghq
// apply template using g8 (sbt new)
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect._
import cats.effect.{IO, ExitCode}
import scala.util.control.Exception._
import cats.data.Validated.Valid
import cats.data.Validated.Invalid

object ScalaNew
    extends CommandIOApp(
      name = "scalanew",
      header = "Create, pull, apply template!"
    ) {
  override def main: Opts[IO[ExitCode]] = {
    val template =
      Opts.argument[String]("template")

    template.map { (t) =>
      IO.println(s"using template $t !") >> ensuringSoftwares >>= {
        case Valid(a)   => ExitCode.Success.pure
        case Invalid(e) => IO.println(s"err: $e") >> ExitCode.Error.pure
      }
    }
  }

  // TODO: skip, parallel
  lazy val ensuringSoftwares = for {
    sbt <- ensuringSbt
    gh <- ensuringGh
    ghq <- ensuringGhq
  } yield sbt *> gh *> ghq

// ensuring softwares
  lazy val ensuringSbt = IO.println("checking sbt...") >> IO {
    allCatch opt {
      os.proc("sbt", "--version").call()
    } toValid ("cannot call sbt")
  }

  lazy val ensuringGh = IO.println("checking gh...") >> IO {
    allCatch opt { os.proc("gh").call() } toValid ("cannot call gh")
  }

  lazy val ensuringGhq = IO.println("checking ghq...") >> IO {
    allCatch opt { os.proc("ghq").call() } toValid ("cannot call ghq")
  }
}
