package repositories

import models.Tables._
import akka.actor.ActorSystem
import javax.inject.Inject
import models.Tables
import play.api.MarkerContext
import play.api.libs.concurrent.CustomExecutionContext
import play.api.db.slick._
import slick.jdbc.JdbcProfile
import scalikejdbc._
import scalikejdbc.config._

import scala.concurrent.Future

// ユーザ情報を保持するケースクラス
final case class User(id: Long,
                      name: String,
                      companyId: Int,
                      companyName: Option[String] = None)

/**
  * UserRepositoryのトレイト
  * 実装クラスはこのトレイトを継承する。
  */
trait UserRepository {
  def list()(implicit mc: MarkerContext): Future[Seq[User]]

  def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]]

  def insert(user: User)(implicit mc: MarkerContext): Future[Long]

  def update(user: User)(implicit mc: MarkerContext): Future[Long]

  def delete(id: Long)(implicit mc: MarkerContext): Future[Long]
}

/**
  * ダミーデータを返却するためのUserRepositoryの実装クラス
  *
  * @param ec UserRepository用のExecutionContext
  */
class UserRepositoryImplWithDummy @Inject()()(implicit ec: UserRepositoryExecutionContext) extends UserRepository {

  override def list()(implicit mc: MarkerContext): Future[Seq[User]] = Future {
    Seq(
      User(1000, "Baki Hanma", 1),
      User(1001, "Yujiro Hanma", 1),
      User(1002, "Doppo Orochi", 2),
      User(1003, "Izo Motobe", 2),
    )
  }

  override def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]] = Future {
    Some(User(id, "Pickle", 1, Some("NTTDATA")))
  }

  override def insert(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    1000
  }

  override def update(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    1001
  }

  override def delete(id: Long)(implicit mc: MarkerContext): Future[Long] = Future {
    1003
  }
}

/**
  * ScalaikeJDBCで実装したUserRepositoryの実装クラス
  *
  * @param ec UserRepository用のExecutionContext
  */
class UserRepositoryImplWithScalikeJDBC @Inject()()(implicit ec: UserRepositoryExecutionContext)
  extends UserRepository {

  DBs.setupAll()

  override def list()(implicit mc: MarkerContext): Future[Seq[User]] = Future {
    DB.readOnly { implicit session =>
      sql"""select id, name, company_id from users"""
        .map(createUser).list().apply()
    }
  }

  override def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]] = Future {
    DB.readOnly { implicit session =>
      sql"""
          select u.id, u.name, u.company_id, c.name as company_name
          from (select * from users where id = $id) as u
          left outer join companies as c
          on u.company_id = c.id
         """
        .map(getUser).single().apply()
    }
  }

  override def insert(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    DB.localTx { implicit session =>
      sql"""insert into users (name, company_id) values (${user.name}, ${user.companyId})"""
        .updateAndReturnGeneratedKey().apply()
    }
  }


  override def update(user: User)(implicit mc: MarkerContext): Future[Long] = Future {
    DB.localTx { implicit session =>
      sql"""update users set name = ${user.name}, company_id = ${user.companyId}"""
        .update().apply()
    }
    user.id
  }

  override def delete(id: Long)(implicit mc: MarkerContext): Future[Long] = Future {
    DB.localTx { implicit session =>
      sql"""delete from users where id = $id"""
        .update().apply()
    }
  }

  private[this] def createUser(rs: WrappedResultSet): User =
    User(rs.long("id"), rs.string("name"), rs.int("company_id"))

  private[this] def getUser(rs: WrappedResultSet): User =
    User(rs.long("id"), rs.string("name"), rs.int("company_id"), rs.stringOpt("company_name"))

}

/**
  * Slickで実装するUserRepositoryの実装クラス
  *
  * @param dbConfigProvider DatabaseConfigProvider
  * @param ec               UserRepository用のExecutionContext
  */
class UserRepositoryImplWithSlick @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                           (implicit ec: UserRepositoryExecutionContext)
  extends UserRepository with HasDatabaseConfigProvider[JdbcProfile] {

  import slick.jdbc.MySQLProfile.api._

  override def list()(implicit mc: MarkerContext): Future[Seq[User]] =
    db.run(Users.sortBy(_.id).result)
      .map(_.map { r =>
        User(r.id, r.name, r.companyId)
      })

  override def find(id: Long)(implicit mc: MarkerContext): Future[Option[User]] = {
    val query = for {
      (u, c) <- Users.filter(_.id === id.bind) join Companies on (_.companyId === _.id)
    } yield (u.id, u.name, u.companyId, c.name)

    db.run(query.result.headOption)
      .map(_.map { r =>
        User(r._1, r._2, r._3, Some(r._4))
      })
  }

  override def insert(data: User)(implicit mc: MarkerContext): Future[Long] = ???

  override def update(data: User)(implicit mc: MarkerContext): Future[Long] = ???

  override def delete(id: Long)(implicit mc: MarkerContext): Future[Long] = ???
}


class UserRepositoryExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "user.repository.dispatcher")
