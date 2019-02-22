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

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.config.{DesConfig, FeatureTogglesConfig, NpsConfig}
import uk.gov.hmrc.tai.model.IabdUpdateAmountFormats
import uk.gov.hmrc.tai.model.IabdUpdateAmount
import uk.gov.hmrc.tai.model.domain.response.{HodUpdateFailure, HodUpdateResponse, HodUpdateSuccess}
import uk.gov.hmrc.tai.model.enums.APITypes
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.util.{HodsSource, TaiConstants}

import scala.concurrent.Future

@Singleton
class TaxAccountConnector @Inject()(npsConfig: NpsConfig,
                                    desConfig: DesConfig,
                                    taxAccountUrls: TaxAccountUrls,
                                    iabdUrls: IabdUrls,
                                    IabdUpdateAmountFormats: IabdUpdateAmountFormats,
                                    httpHandler: HttpHandler,
                                    featureTogglesConfig: FeatureTogglesConfig) extends HodsSource {

  def taxAccount(nino:Nino, taxYear:TaxYear)(implicit hc:HeaderCarrier): Future[JsValue] = {

    val currentYear = Json.parse(
      """{
        "taxAccountId":null,
        "date":"21/02/2019",
        "nino":"AA000000",
        "noCYEmployment":false,
        "taxYear":2018,
        "previousTaxAccountId":33,
        "previousYearTaxAccountId":30,
        "nextTaxAccountId":null,
        "nextYearTaxAccountId":34,
        "totalEstTax":7071.20,
        "totalEstPay":{
          "npsDescription":null,
          "amount":40128.00,
          "type":null,
          "iabdSummaries":[
        {
          "amount":40128,
          "type":27,
          "npsDescription":"New Estimated Pay",
          "employmentId":8,
          "defaultEstimatedPay":null,
          "estimatedPaySource":49
        }
          ],
          "sourceAmount":null
        },
        "rtiPayToDateEstPays":[
        {
          "employmentId":8,
          "taxablePay":33440,
          "rtiPayDate":"25/01/2019",
          "startDate":null,
          "rtiTaxablePayInPeriod":3440,
          "paymentFrequency":4,
          "ptdWkNum":null,
          "ptdMthNum":10,
          "occupationalPensionInd":false,
          "irregularEmploymentInd":false,
          "annualPenAmnt":null
        }
        ],
        "inYearCalcResult":1,
        "inYearCalcAmount":0,
        "adjustedNetIncome":{
          "npsDescription":null,
          "amount":43014,
          "type":null,
          "iabdSummaries":[
        {
          "amount":40128,
          "type":27,
          "npsDescription":"New Estimated Pay",
          "employmentId":8,
          "defaultEstimatedPay":null,
          "estimatedPaySource":49
        }
          ],
          "sourceAmount":null
        },
        "totalLiability":{
          "nonSavings":{
          "totalIncome":{
          "npsDescription":null,
          "amount":43014,
          "type":null,
          "iabdSummaries":[
        {
          "amount":40128,
          "type":27,
          "npsDescription":"New Estimated Pay",
          "employmentId":8,
          "defaultEstimatedPay":null,
          "estimatedPaySource":49
        },
        {
          "amount":2886,
          "type":19,
          "npsDescription":"Non-Coded Income",
          "employmentId":null
        }
          ],
          "sourceAmount":null
        },
          "allowReliefDeducts":{
          "npsDescription":null,
          "amount":11850,
          "type":null,
          "iabdSummaries":[
        {
          "amount":11850,
          "type":118,
          "npsDescription":"Personal Allowance (PA)",
          "employmentId":null
        }
          ],
          "sourceAmount":null
        },
          "totalTax":6232.80,
          "totalTaxableIncome":31164,
          "taxBands":[
        {
          "bandType":"B",
          "taxCode":"BR",
          "isBasicRate":true,
          "income":31164,
          "tax":6232.80,
          "lowerBand":0,
          "upperBand":34500,
          "rate":20.00
        },
        {
          "bandType":"D0",
          "taxCode":"D0",
          "isBasicRate":false,
          "income":null,
          "tax":null,
          "lowerBand":34500,
          "upperBand":150000,
          "rate":40.00
        },
        {
          "bandType":"D1",
          "taxCode":"D1",
          "isBasicRate":false,
          "income":null,
          "tax":null,
          "lowerBand":150000,
          "upperBand":0,
          "rate":45.00
        }
          ]
        },
          "untaxedInterest":null,
          "bankInterest":null,
          "ukDividends":null,
          "foreignInterest":null,
          "foreignDividends":null,
          "basicRateExtensions":{
          "npsDescription":null,
          "amount":0,
          "type":null,
          "iabdSummaries":null,
          "sourceAmount":null,
          "personalPensionPayment":null,
          "giftAidPayments":null,
          "personalPensionPaymentRelief":null,
          "giftAidPaymentsRelief":null
        },
          "reliefsGivingBackTax":null,
          "otherTaxDue":null,
          "alreadyTaxedAtSource":null,
          "addTaxRefunded":null,
          "lessTaxReceived":null,
          "totalLiability":6232.80
        },
        "incomeSources":[
        {
          "employmentId":8,
          "employmentType":1,
          "employmentStatus":1,
          "employmentTaxDistrictNumber":120,
          "employmentPayeRef":"ABC123",
          "pensionIndicator":false,
          "otherIncomeSourceIndicator":false,
          "jsaIndicator":false,
          "name":"Employer 1",
          "taxCode":"519L",
          "basisOperation":2,
          "potentialUnderpayment":0,
          "totalInYearAdjustment":0,
          "inYearAdjustmentIntoCY":0,
          "inYearAdjustmentIntoCYPlusOne":0,
          "inYearAdjustmentFromPreviousYear":0.00,
          "actualPUPCodedInCYPlusOneTaxYear":0,
          "rtiPayToDatePups":[
          {
            "employmentId":8,
            "taxablePay":33440,
            "totalTaxToDate":6039.6,
            "rtiPayDate":"25/01/2019",
            "ptdWkNum":null,
            "ptdMthNum":10
          }
          ],
          "allowances":[
          {
            "npsDescription":"personal allowance",
            "amount":11850,
            "type":11,
            "iabdSummaries":[
            {
              "amount":11850,
              "type":118,
              "npsDescription":"Personal Allowance (PA)",
              "employmentId":null
            }
            ],
            "sourceAmount":11850
          }
          ],
          "deductions":[
          {
            "npsDescription":"Underpayment amount ",
            "amount":6657,
            "type":35,
            "iabdSummaries":null,
            "sourceAmount":1418.4
          }
          ],
          "payAndTax":{
            "totalIncome":{
            "npsDescription":null,
            "amount":40128.00,
            "type":null,
            "iabdSummaries":[
          {
            "amount":40128,
            "type":27,
            "npsDescription":"New Estimated Pay",
            "employmentId":8,
            "defaultEstimatedPay":null,
            "estimatedPaySource":49
          }
            ],
            "sourceAmount":null
          },
            "allowReliefDeducts":{
            "npsDescription":null,
            "amount":0.00,
            "type":null,
            "iabdSummaries":null,
            "sourceAmount":null
          },
            "totalTax":7071.20,
            "totalTaxableIncome":34928.00,
            "taxBands":[
          {
            "bandType":"B",
            "taxCode":"BR",
            "isBasicRate":true,
            "income":34500.00,
            "tax":6900.00,
            "lowerBand":0,
            "upperBand":34500,
            "rate":20.00
          },
          {
            "bandType":"D0",
            "taxCode":"D0",
            "isBasicRate":false,
            "income":428.00,
            "tax":171.20,
            "lowerBand":34500,
            "upperBand":150000,
            "rate":40.00
          },
          {
            "bandType":"D1",
            "taxCode":"D1",
            "isBasicRate":false,
            "income":null,
            "tax":null,
            "lowerBand":150000,
            "upperBand":0,
            "rate":45.00
          }
            ]
          }
        }
        ]
      }"""
    )
    val nextYear = Json.parse(
      """{"taxAccountId":null,"date":"21/02/2019","nino":"AA000000","noCYEmployment":false,"taxYear":2019,"previousTaxAccountId":34,"previousYearTaxAccountId":33,"nextTaxAccountId":null,"nextYearTaxAccountId":null,"totalEstTax":5497.80,"totalEstPay":{"npsDescription":null,"amount":39999.00,"type":null,"iabdSummaries":[{"amount":39999,"type":27,"npsDescription":"New Estimated Pay","employmentId":8,"defaultEstimatedPay":null,"estimatedPaySource":26}],"sourceAmount":null},"rtiPayToDateEstPays":[],"inYearCalcResult":1,"inYearCalcAmount":0,"adjustedNetIncome":{"npsDescription":null,"amount":42885,"type":null,"iabdSummaries":[{"amount":39999,"type":27,"npsDescription":"New Estimated Pay","employmentId":8,"defaultEstimatedPay":null,"estimatedPaySource":26}],"sourceAmount":null},"totalLiability":{"nonSavings":{"totalIncome":{"npsDescription":null,"amount":42885,"type":null,"iabdSummaries":[{"amount":39999,"type":27,"npsDescription":"New Estimated Pay","employmentId":8,"defaultEstimatedPay":null,"estimatedPaySource":26},{"amount":2886,"type":19,"npsDescription":"Non-Coded Income","employmentId":null}],"sourceAmount":null},"allowReliefDeducts":{"npsDescription":null,"amount":12500,"type":null,"iabdSummaries":[{"amount":12500,"type":118,"npsDescription":"Personal Allowance (PA)","employmentId":null}],"sourceAmount":null},"totalTax":6077.00,"totalTaxableIncome":30385,"taxBands":[{"bandType":"B","taxCode":"BR","isBasicRate":true,"income":30385,"tax":6077.00,"lowerBand":0,"upperBand":37500,"rate":20.00},{"bandType":"D0","taxCode":"D0","isBasicRate":false,"income":null,"tax":null,"lowerBand":37500,"upperBand":150000,"rate":40.00},{"bandType":"D1","taxCode":"D1","isBasicRate":false,"income":null,"tax":null,"lowerBand":150000,"upperBand":0,"rate":45.00}]},"untaxedInterest":null,"bankInterest":null,"ukDividends":null,"foreignInterest":null,"foreignDividends":null,"basicRateExtensions":{"npsDescription":null,"amount":0,"type":null,"iabdSummaries":null,"sourceAmount":null,"personalPensionPayment":null,"giftAidPayments":null,"personalPensionPaymentRelief":null,"giftAidPaymentsRelief":null},"reliefsGivingBackTax":null,"otherTaxDue":null,"alreadyTaxedAtSource":null,"addTaxRefunded":null,"lessTaxReceived":null,"totalLiability":6077.00},"incomeSources":[{"employmentId":8,"employmentType":1,"employmentStatus":1,"employmentTaxDistrictNumber":120,"employmentPayeRef":"ABC123","pensionIndicator":false,"otherIncomeSourceIndicator":false,"jsaIndicator":false,"name":"Employer 1","taxCode":"1250L","basisOperation":2,"potentialUnderpayment":0,"totalInYearAdjustment":0,"inYearAdjustmentIntoCY":null,"inYearAdjustmentIntoCYPlusOne":null,"inYearAdjustmentFromPreviousYear":0.00,"actualPUPCodedInCYPlusOneTaxYear":0,"rtiPayToDatePups":[],"allowances":[{"npsDescription":"personal allowance","amount":12500,"type":11,"iabdSummaries":[{"amount":12500,"type":118,"npsDescription":"Personal Allowance (PA)","employmentId":null}],"sourceAmount":12500}],"deductions":[],"payAndTax":{"totalIncome":{"npsDescription":null,"amount":39999.00,"type":null,"iabdSummaries":[{"amount":39999,"type":27,"npsDescription":"New Estimated Pay","employmentId":8,"defaultEstimatedPay":null,"estimatedPaySource":26}],"sourceAmount":null},"allowReliefDeducts":{"npsDescription":null,"amount":0.00,"type":null,"iabdSummaries":null,"sourceAmount":null},"totalTax":5497.80,"totalTaxableIncome":27489.00,"taxBands":[{"bandType":"B","taxCode":"BR","isBasicRate":true,"income":27489.00,"tax":5497.80,"lowerBand":0,"upperBand":37500,"rate":20.00},{"bandType":"D0","taxCode":"D0","isBasicRate":false,"income":null,"tax":null,"lowerBand":37500,"upperBand":150000,"rate":40.00},{"bandType":"D1","taxCode":"D1","isBasicRate":false,"income":null,"tax":null,"lowerBand":150000,"upperBand":0,"rate":45.00}]}}]}"""
    )

    if(taxYear.year == 2018){
      println(Console.YELLOW + s"current year $taxYear" + Console.WHITE)
      Future.successful(currentYear)
    }
    else{
      println(Console.YELLOW + s"next year $taxYear" + Console.WHITE)
      Future.successful(nextYear)
    }

//    if(featureTogglesConfig.desEnabled) {
//      implicit val hc: HeaderCarrier = createHeader.withExtraHeaders("Gov-Uk-Originator-Id" -> desConfig.originatorId)
//      val url = taxAccountUrls.taxAccountUrlDes(nino, taxYear)
//      httpHandler.getFromApi(url, APITypes.DesTaxAccountAPI)
//    }
//    else{
//      val hcWithHodHeaders = hc.withExtraHeaders("Gov-Uk-Originator-Id" -> npsConfig.originatorId)
//      val url = taxAccountUrls.taxAccountUrlNps(nino, taxYear)
//      httpHandler.getFromApi(url, APITypes.NpsTaxAccountAPI)(hcWithHodHeaders)
//    }
  }

  def taxAccountHistory(nino: Nino, iocdSeqNo: Int)(implicit hc:HeaderCarrier): Future[JsValue] = {
    implicit val hc: HeaderCarrier = createHeader.withExtraHeaders("Gov-Uk-Originator-Id" -> desConfig.originatorId)
    val url = taxAccountUrls.taxAccountHistoricSnapshotUrl(nino, iocdSeqNo)
    httpHandler.getFromApi(url, APITypes.DesTaxAccountAPI)
  }

  def updateTaxCodeAmount(nino: Nino, taxYear: TaxYear, employmentId: Int, version: Int, iabdType: Int, amount: Int)
                         (implicit hc: HeaderCarrier): Future[HodUpdateResponse] = {

    if(featureTogglesConfig.desUpdateEnabled) {
      val url = iabdUrls.desIabdEmploymentUrl(nino, taxYear, iabdType)
      val amountList =  List(
        IabdUpdateAmount(employmentSequenceNumber = employmentId, grossAmount = amount, source = Some(DesSource))
      )
      val requestHeader = headersForUpdate(hc, version, sessionOrUUID, desConfig.originatorId)

      httpHandler.postToApi[List[IabdUpdateAmount]](url, amountList, APITypes.DesIabdUpdateEstPayAutoAPI)(
        requestHeader, IabdUpdateAmountFormats.formatList
      ).map { _ => HodUpdateSuccess}.recover { case _ => HodUpdateFailure }
    }
    else {
      val url = iabdUrls.npsIabdEmploymentUrl(nino, taxYear, iabdType)
      val amountList = List(
          IabdUpdateAmount(employmentSequenceNumber = employmentId, grossAmount = amount, source = Some(NpsSource))
      )
      val requestHeader = headersForUpdate(hc, version, sessionOrUUID, npsConfig.originatorId)

      httpHandler.postToApi[List[IabdUpdateAmount]](url, amountList, APITypes.NpsIabdUpdateEstPayManualAPI)(
        requestHeader, IabdUpdateAmountFormats.formatList
      ).map { _ => HodUpdateSuccess}.recover{ case _ => HodUpdateFailure}
    }

  }

  def sessionOrUUID(implicit hc: HeaderCarrier): String = {
    hc.sessionId match {
      case Some(sessionId) => sessionId.value
      case None => UUID.randomUUID().toString.replace("-", "")
    }
  }

  def headersForUpdate(hc: HeaderCarrier, version: Int, txId: String, originatorId: String): HeaderCarrier = {
    hc.withExtraHeaders("ETag" -> version.toString, "X-TXID" -> txId, "Gov-Uk-Originator-Id" -> originatorId)
  }

  val createHeader = HeaderCarrier(extraHeaders =
    Seq(
      "Environment" -> desConfig.environment,
      "Authorization" -> desConfig.authorization,
      "Content-Type" -> TaiConstants.contentType))


}