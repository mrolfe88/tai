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

package uk.gov.hmrc.tai.connectors

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Format
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.tai.audit.Auditor
import uk.gov.hmrc.tai.metrics.Metrics
import uk.gov.hmrc.tai.model.enums.APITypes
import uk.gov.hmrc.tai.model.nps.PersonDetails

import scala.concurrent.Future

@Singleton
class CitizenDetailsConnector @Inject()(
  metrics: Metrics,
  httpClient: HttpClient,
  auditor: Auditor,
  urls: CitizenDetailsUrls)
    extends BaseConnector(auditor, metrics, httpClient) {

  override val originatorId: String = ""

  def getPersonDetails(nino: Nino)(implicit hc: HeaderCarrier, formats: Format[PersonDetails]): Future[PersonDetails] =
    getPersonDetailsFromCitizenDetails(urls.designatoryDetailsUrl(nino), nino, APITypes.NpsPersonAPI)
}
