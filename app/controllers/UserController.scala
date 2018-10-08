package controllers

import controllers.UserController.UserForm
import services.UserService
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import models.User


/**
  * ユーザコントローラクラス
  *
  * @param cc      ControllerComponentes
  * @param service UserService DIによって外から注入する。
  * @param ec      ExecutionContext
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
  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      // バリデーションチェックがOKの場合
      val user = User(0, form.name, form.companyId)
      service.registerUser(user).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // バリデーションチェックがNGの場合
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ情報更新
    */
  def update: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      // バリデーションチェックがOKの場合
      val user = User(form.id.get, form.name, form.companyId)
      service.updateUser(user).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // バリデーションチェックがNGの場合
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ削除
    */
  def remove(id: Long): Action[AnyContent] = Action.async { implicit request =>
    service.removeUser(id).map { _ =>
      Ok(Json.obj("result" -> "success"))
    }
  }

}

/**
  * UserControllerのコンパニオンオブジェクト
  */
object UserController {

  case class UserForm(id: Option[Long], name: String, companyId: Int)

  implicit val userFormFormat: Reads[UserForm] = (
    (__ \ "id").readNullable[Long] and
      (__ \ "name").read[String] and
      (__ \ "companyId").read[Int]
    ) (UserForm)


  implicit val usersWritesFormat: Writes[User] = (user: User) => {
    Json.obj(
      "id" -> user.id,
      "name" -> user.name,
      "companyId" -> user.companyId
    )
  }

  implicit val userDetailWriteFormat: Writes[User] = (user: User) => {
    Json.obj(
      "id" -> user.id,
      "name" -> user.name,
      "companyId" -> user.companyId,
      "companyName" -> user.companyName
    )
  }

}