/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tai.service

import com.google.inject.{Inject, Singleton}
import play.Logger
import play.api.http.Status._
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto, Protected}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.tai.config.{FeatureTogglesConfig, MongoConfig, NpsConfig}
import uk.gov.hmrc.tai.connectors.{CacheConnector, CitizenDetailsConnector, DesConnector, NpsConnector}
import uk.gov.hmrc.tai.metrics.Metrics
import uk.gov.hmrc.tai.model._
import uk.gov.hmrc.tai.model.enums.APITypes
import uk.gov.hmrc.tai.model.nps2.MongoFormatter

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

@Singleton
class TaxAccountService @Inject()(
  taiService: TaiService,
  cacheConnector: CacheConnector,
  citizenDetailsConnector: CitizenDetailsConnector,
  nps: NpsConnector,
  des: DesConnector,
  metrics: Metrics,
  hodConfig: NpsConfig,
  mongoConfig: MongoConfig,
  featureTogglesConfig: FeatureTogglesConfig)
    extends MongoFormatter {

  def taiData(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[SessionData] =
    if (mongoConfig.mongoEnabled) {
      unencryptedCachedSession(nino, year)
    } else {
      sessionData(nino, year)
    }

  def version(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[Option[Int]] =
    taiData(nino, year).map(_.taiRoot.map(_.version))

  def fetchSessionId(headerCarrier: HeaderCarrier): String =
    headerCarrier.sessionId.map(_.value).getOrElse(throw new RuntimeException("Error while fetching session id"))

  private def unencryptedCachedSession(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[SessionData] =
    cacheConnector.find[SessionData](fetchSessionId(hc)).flatMap {
      case Some(sd) => Future.successful(sd)
      case _        => newCachedSession(nino, year)
    }

  def invalidateTaiCacheData()(implicit hc: HeaderCarrier): Unit =
    if (mongoConfig.mongoEnabled) {
      cacheConnector.removeById(fetchSessionId(hc))
    } else {
      ()
    }

  private def newCachedSession(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[SessionData] =
    for {
      sessionData       <- sessionData(nino, year)
      cachedSessionData <- updateTaiData(nino, sessionData)
    } yield cachedSessionData

  def updateTaiData(nino: Nino, sessionData: SessionData)(implicit hc: HeaderCarrier): Future[SessionData] =
    if (mongoConfig.mongoEnabled) {
      cacheConnector.createOrUpdate[SessionData](fetchSessionId(hc), sessionData)
    } else {
      Future.successful(sessionData)
    }

  def taxSummaryDetails(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[TaxSummaryDetails] = {
    val timer = metrics.startTimer(APITypes.NpsTaxAccountAPI)

    taiService.getAutoUpdateResults(nino, year).flatMap { autoUpdateResults =>
      calculatedTaxAccountRawResponse(nino, year).flatMap { taxAccountResponse =>
        timer.stop()
        taxAccountResponse.status match {
          case OK =>
            metrics.incrementSuccessCounter(APITypes.NpsTaxAccountAPI)
            val version = taxAccountResponse.header("ETag").map(_.toInt).getOrElse(-1)
            taiService.getCalculatedTaxAccount(nino, year, autoUpdateResults, (taxAccountResponse.json, version))
          case _ =>
            Logger.warn(
              s"Tax Account - Service failed while fetching data from NPS with nino: $nino " +
                s"status: ${taxAccountResponse.status} body: ${taxAccountResponse.body}")
            metrics.incrementFailedCounter(APITypes.NpsTaxAccountAPI)
            throw NpsError(taxAccountResponse.body, taxAccountResponse.status)
        }
      }
    }
  }

  def calculatedTaxAccountRawResponse(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    if (featureTogglesConfig.desEnabled)
      des.getCalculatedTaxAccountRawResponseFromDes(nino, year)
    else
      nps.getCalculatedTaxAccountRawResponse(nino, year)

  private[service] def sessionData(nino: Nino, year: Int)(implicit hc: HeaderCarrier): Future[SessionData] =
    withSessionDataDetails(nino, year, { taiRoot => taxSummaryDetails =>
      SessionData(nino = nino.nino, taiRoot = Some(taiRoot), taxSummaryDetailsCY = taxSummaryDetails)
    })

  def withSessionDataDetails(nino: Nino, year: Int, body: TaiRoot => TaxSummaryDetails => SessionData)(
    implicit hc: HeaderCarrier): Future[SessionData] =
    for {
      personDetails <- personDetails(nino)
      taxSummary    <- taxSummaryFromPerson(personDetails, year)
    } yield {
      body(personDetails)(taxSummary)
    }

  def taxSummaryFromPerson(personDetails: TaiRoot, year: Int)(implicit hc: HeaderCarrier): Future[TaxSummaryDetails] =
    if (personDetails.manualCorrespondenceInd) {
      Future.successful(
        TaxSummaryDetails(nino = personDetails.nino, version = 0, gateKeeper = Some(GateKeeper.withMciRule)))
    } else {
      taxSummaryDetails(Nino(personDetails.nino), year)
    }

  def personDetails(nino: Nino)(implicit hc: HeaderCarrier): Future[TaiRoot] =
    citizenDetailsConnector.getPersonDetails(nino).map(_.toTaiRoot)

}

case class NpsError(message: String, reasonCode: Int) extends RuntimeException(s"error message :$message")
