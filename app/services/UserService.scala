package services

import javax.inject.Inject
import models.User
import repositories.UserRepository
import scala.concurrent.Future


trait UserService {
  def getUsers(): Future[Seq[User]]

  def getUser(id: Long): Future[Option[User]]

  def registerUser(user: User): Future[Long]

  def updateUser(user: User): Future[Long]

  def removeUser(id: Long): Future[Long]
}

/**
  * UserServiceの実装クラス
  *
  * @param repository UserRepository DIで注入する
  */
class UserServiceImpl @Inject()(repository: UserRepository) extends UserService with PasswordServiceImpl {
  override def getUsers(): Future[Seq[User]] = {
    // do something
    repository.list()
  }

  override def getUser(id: Long): Future[Option[User]] = {
    // do something
    repository.find(id)
  }

  override def registerUser(user: User): Future[Long] = {
    // do something
    repository.insert(user)
  }

  override def updateUser(user: User): Future[Long] = {
    // do something
    repository.update(user)
  }

  override def removeUser(id: Long): Future[Long] = {
    // do something
    repository.delete(id)
  }
}
