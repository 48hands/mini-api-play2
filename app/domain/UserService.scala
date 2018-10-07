package domain

import javax.inject.Inject
import repositories.{User, UserRepository}

import scala.concurrent.Future


trait UserService {
  def getUsers(): Future[Seq[User]]

  def getUser(id: Long): Future[Option[User]]
}

class UserServiceImpl @Inject()(repository: UserRepository) extends UserService {
  override def getUsers(): Future[Seq[User]] = {
    repository.list()
  }

  override def getUser(id: Long): Future[Option[User]] = {
    repository.find(id)
  }
}
