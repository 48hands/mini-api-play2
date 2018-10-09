import com.google.inject.AbstractModule
import services._
import javax.inject._
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import repositories._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
  extends AbstractModule
    with ScalaModule {

  override def configure(): Unit = {
    // UserRepositoryのDI
    bind[UserRepository].to[UserRepositoryImplWithSlick].in[Singleton]
    // UserServiceのDI
    bind[UserService].to[UserServiceImpl].in[Singleton]

    // CompanyRepositoryのDI
    bind[CompanyRepository].to[CompanyRepositoryImpl].in[Singleton]
    // CompanyServiceのDI
    bind[CompanyService].to[CompanyServiceImpl].in[Singleton]

  }
}
