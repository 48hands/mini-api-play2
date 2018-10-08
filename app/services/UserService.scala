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

class UserServiceImpl @Inject()(repository: UserRepository) extends UserService {
  override def getUsers(): Future[Seq[User]] = {
    repository.list()
  }

  override def getUser(id: Long): Future[Option[User]] = {
    repository.find(id)
  }

  override def registerUser(user: User): Future[Long] = {
    repository.insert(user)
  }

  override def updateUser(user: User): Future[Long] = {
    repository.update(user)
  }

  override def removeUser(id: Long): Future[Long] = {
    repository.delete(id)
  }
}
