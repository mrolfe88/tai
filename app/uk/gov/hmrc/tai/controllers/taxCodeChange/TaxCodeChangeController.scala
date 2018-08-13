/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tai.controllers.taxCodeChange

import com.google.inject.Inject
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.tai.config.FeatureTogglesConfig
import uk.gov.hmrc.tai.controllers.predicates.AuthenticationPredicate
import uk.gov.hmrc.tai.model.api.ApiResponse
import uk.gov.hmrc.tai.service.TaxCodeChangeService

import scala.concurrent.Future

class TaxCodeChangeController @Inject()(authentication: AuthenticationPredicate,
                                        taxCodeChangeService: TaxCodeChangeService,
                                        toggleConfig: FeatureTogglesConfig) extends BaseController  {

  def hasTaxCodeChanged(nino: Nino) = authentication.async {
    implicit request =>
        if (toggleConfig.taxCodeChangeEnabled) {
          taxCodeChangeService.hasTaxCodeChanged(nino).map{
            taxCodeChanged => Ok(Json.toJson(taxCodeChanged))
          }
        }
        else{
          Future.successful(Ok(Json.toJson(false)))
        }
  }

  def taxCodeHistory(nino: Nino) = authentication.async {
    implicit request =>
      taxCodeChangeService.taxCodeHistory(nino) map { taxCodeHistory =>
        Ok(Json.toJson(ApiResponse(taxCodeHistory, Nil)))
      }
  }
}