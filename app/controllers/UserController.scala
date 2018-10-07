package controllers

import domain.UserService
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import repositories.User

/**
  * ユーザコントローラクラス
  *
  * @param cc ControllerComponentes
  * @param service UserService DIによって外から注入する。
  * @param ec ExecutionContext
  */
class UserController @Inject()(cc: ControllerComponents, service: UserService)
                              (implicit ec: ExecutionContext) extends AbstractController(cc) {


  /**
    * ユーザ一覧取得
    */
  def list: Action[AnyContent] = Action.async { implicit request =>
    import UserController.usersWritesFormat
    service.getUsers().map { users =>
      Ok(Json.obj("users" -> Json.toJson(users)))
    }
  }

  /**
    * ユーザ情報取得
    */
  def show(id: Long): Action[AnyContent] = Action.async { implicit request =>
    import UserController.userDetailWriteFormat
    service.getUser(id).map {
      case Some(u) => Ok(Json.obj("result" -> Json.toJson(u)))
      case None => Ok(Json.obj("result" -> "User not found"))
    }
  }

  /**
    * ユーザ新規登録
    */
  def create = TODO

  /**
    * ユーザ情報更新
    */
  def update = TODO

  /**
    * ユーザ削除
    */
  def remove(id: Long) = TODO

}

/**
  * UserControllerのコンパニオンオブジェクト
  */
object UserController {

  implicit val usersWritesFormat: Writes[User] = new Writes[User] {
    override def writes(user: User): JsValue = {
      Json.obj(
        "id" -> user.id,
        "name" -> user.name,
        "companyId" -> user.companyId
      )
    }
  }

  implicit val userDetailWriteFormat: Writes[User] = new Writes[User] {
    override def writes(user: User): JsValue = {
      Json.obj(
        "id" -> user.id,
        "name" -> user.name,
        "companyId" -> user.companyId,
        "companyName" -> user.companyName
      )
    }
  }

}