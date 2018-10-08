package services

import org.mindrot.jbcrypt.BCrypt

trait PasswordService {
  def hashPassword(rawPassword: String): String

  def checkPassword(rawPassword: String, hashedPassword: String): Boolean
}

trait PasswordServiceImpl extends PasswordService {
  override def hashPassword(rawPassword: String): String =
    BCrypt.hashpw(rawPassword, BCrypt.gensalt())

  override def checkPassword(rawPassword: String, hashedPassword: String): Boolean =
    BCrypt.checkpw(rawPassword, hashedPassword)
}
