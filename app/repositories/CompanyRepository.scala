package repositories

import javax.inject.Inject
import models.Tables._
import models.Company
import play.api.MarkerContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait CompanyRepository {

  def list()(implicit mc: MarkerContext): Future[Seq[Company]]

  def insert(company: Company)(implicit mc: MarkerContext): Future[Long]

  def update(company: Company)(implicit mc: MarkerContext): Future[Long]

  def delete(id: Int)(implicit mc: MarkerContext): Future[Long]
}


class CompanyRepositoryImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                     (implicit ec: UserRepositoryExecutionContext)
  extends CompanyRepository with HasDatabaseConfigProvider[JdbcProfile] {

  import slick.jdbc.MySQLProfile.api._

  override def list()(implicit mc: MarkerContext): Future[Seq[Company]] =
    db.run(Companies.sortBy(_.id).result).map(_.map { r =>
      Company(r.id, r.name)
    })

  override def insert(company: Company)(implicit mc: MarkerContext): Future[Long] = {
    db.run(Companies += CompaniesRow(company.id, company.name))
  }.map(_.toLong)

  override def update(company: Company)(implicit mc: MarkerContext): Future[Long] = {
    db.run(Companies.filter(_.id === company.id.bind)
      .update(CompaniesRow(company.id, company.name)))
  }.map(_.toLong)

  override def delete(id: Int)(implicit mc: MarkerContext): Future[Long] = {
    db.run(Companies.filter(_.id === id.bind).delete)
  }.map(_.toLong)

}
