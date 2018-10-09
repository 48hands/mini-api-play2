package controllers

import javax.inject.Inject
import models.Company
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.CompanyService

import scala.concurrent.{ExecutionContext, Future}


class CompanyController @Inject()(cc: ControllerComponents, service: CompanyService)
                                 (implicit ec: ExecutionContext) extends AbstractController(cc) {

  import CompanyController._

  def list: Action[AnyContent] = Action.async { implicit request =>
    service.getCompanies().map { companies =>
      Ok(Json.obj("companies" -> Json.toJson(companies)))
    }
  }

  def create: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CompanyForm].map { form =>
      // TODO 登録時にDBエラーが発生した場合のハンドリングを追加する
      service.registerCompany(Company(form.id, form.name)).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { error =>
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(error)))
      }
    }
  }

  def update: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[CompanyForm].map { form =>
      service.updateCompany(Company(form.id, form.name)).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { error =>
      Future {
        BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(error)))
      }
    }
  }

  def remove(id: Int): Action[AnyContent] = Action.async { implicit request =>
    // TODO 削除時にDBエラーが発生した場合のハンドリングを追加する
    service.removeCompany(id).map { _ =>
      Ok(Json.obj("result" -> "success"))
    }
  }
}

object CompanyController {

  case class CompanyForm(id: Int, name: String)

  implicit val companyWriteFormat: Writes[Company] = (c: Company) => {
    Json.obj(
      "id" -> c.id,
      "name" -> c.name
    )
  }

  implicit val companyReadFormat: Reads[CompanyForm] = (
    (__ \ "id").read[Int] and
      (__ \ "name").read[String]
    ) (CompanyForm)

}