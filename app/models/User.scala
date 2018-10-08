package models

/**
  * User情報を保持するケースクラス
  *
  * @param id          ユーザID
  * @param name        ユーザ名
  * @param companyId   会社のID
  * @param companyName 会社名
  */
final case class User(id: Long, name: String, companyId: Int, companyName: Option[String] = None)
