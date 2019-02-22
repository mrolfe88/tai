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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.config.{DesConfig, FeatureTogglesConfig, NpsConfig}
import uk.gov.hmrc.tai.model.enums.APITypes
import uk.gov.hmrc.tai.model.nps2.IabdType
import uk.gov.hmrc.tai.model.tai.TaxYear

import scala.concurrent.Future

@Singleton
class IabdConnector @Inject()(npsConfig: NpsConfig,
                              desConfig: DesConfig,
                              httpHandler: HttpHandler,
                              iabdUrls: IabdUrls,
                              featureTogglesConfig: FeatureTogglesConfig){

  def iabds(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): Future[JsValue] = {

    if(taxYear > TaxYear()){
      Future.successful(Json.arr())
    }
    else{
      Future.successful(Json.parse(
        """[
          {
            "nino":"AA000000",
            "sequenceNumber":201800005,
            "taxYear":2018,
            "type":72,
            "source":33,
            "grossAmount":0,
            "receiptDate":"14/05/2018",
            "captureDate":"14/05/2018",
            "typeDescription":"Profit",
            "netAmount":null
          },
          {
            "nino":"AA000000",
            "sequenceNumber":201800007,
            "taxYear":2018,
            "type":82,
            "source":33,
            "grossAmount":0,
            "receiptDate":"14/05/2018",
            "captureDate":"14/05/2018",
            "typeDescription":"Untaxed Interest",
            "taxDeducted":null,
            "netAmount":0
          },
          {
            "nino":"AA000000",
            "sequenceNumber":201800010,
            "taxYear":2018,
            "type":19,
            "source":33,
            "grossAmount":2886,
            "receiptDate":"04/07/2018",
            "captureDate":"04/07/2018",
            "typeDescription":"Non-Coded Income",
            "netAmount":null
          },
          {
            "nino":"AA000000",
            "sequenceNumber":201800011,
            "taxYear":2018,
            "type":118,
            "source":null,
            "grossAmount":11850,
            "receiptDate":null,
            "captureDate":null,
            "typeDescription":"Personal Allowance (PA)",
            "netAmount":null
          },
          {
            "nino":"AA000000",
            "sequenceNumber":201800012,
            "taxYear":2018,
            "type":27,
            "source":49,
            "grossAmount":38400,
            "receiptDate":"25/07/2018",
            "captureDate":"25/07/2018",
            "typeDescription":"New Estimated Pay",
            "netAmount":null,
            "employmentSequenceNumber":8,
            "defaultEstimatedPay":null
          }
          ]"""
      ))
    }
//    else if (featureTogglesConfig.desEnabled){
//        val hcWithHodHeaders = hc.withExtraHeaders("Gov-Uk-Originator-Id" -> desConfig.originatorId)
//        val urlDes = iabdUrls.desIabdUrl(nino, taxYear)
//        httpHandler.getFromApi(urlDes, APITypes.DesIabdAllAPI)(hcWithHodHeaders)
//    }
//    else {
//        val hcWithHodHeaders = hc.withExtraHeaders("Gov-Uk-Originator-Id" -> npsConfig.originatorId)
//        val urlNps = iabdUrls.npsIabdUrl(nino, taxYear)
//        httpHandler.getFromApi(urlNps, APITypes.NpsIabdAllAPI)(hcWithHodHeaders)
//    }

  }

  def iabdByType(nino: Nino, taxYear: TaxYear, iabdType: IabdType)(implicit hc: HeaderCarrier): Future[JsValue] = {
    val hcWithHodHeaders = hc.withExtraHeaders("Gov-Uk-Originator-Id" -> desConfig.originatorId)
    val urlDes = iabdUrls.desIabdByTypeUrl(nino, taxYear, iabdType)
    httpHandler.getFromApi(urlDes, APITypes.DesIabdGetFlatRateExpensesAPI)(hcWithHodHeaders)
  }
}
