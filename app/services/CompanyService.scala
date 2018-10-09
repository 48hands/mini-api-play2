package services

import javax.inject.Inject
import models.Company
import repositories.CompanyRepository

import scala.concurrent.Future

trait CompanyService {
  def getCompanies(): Future[Seq[Company]]

  def registerCompany(company: Company): Future[Long]

  def updateCompany(company: Company): Future[Long]

  def removeCompany(id: Int): Future[Long]
}

class CompanyServiceImpl @Inject()(repository: CompanyRepository) extends CompanyService {
  override def getCompanies(): Future[Seq[Company]] = repository.list()

  override def registerCompany(company: Company): Future[Long] = repository.insert(company)

  override def updateCompany(company: Company): Future[Long] = repository.update(company)

  override def removeCompany(id: Int): Future[Long] = repository.delete(id)
}
