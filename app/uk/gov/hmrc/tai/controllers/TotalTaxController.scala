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

package uk.gov.hmrc.tai.controllers

import com.google.inject.{Inject, Singleton}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.tai.controllers.predicates.AuthenticationPredicate
import uk.gov.hmrc.tai.model.api.{ApiFormats, ApiResponse}
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.service.TotalTaxService
import uk.gov.hmrc.tai.util.NpsExceptions

import scala.concurrent.Future

@Singleton
class TotalTaxController @Inject()(totalTaxService: TotalTaxService, authentication: AuthenticationPredicate)
    extends BaseController with ApiFormats with NpsExceptions with ControllerErrorHandler {

  def totalTax(nino: Nino, year: TaxYear): Action[AnyContent] = authentication.async { implicit request =>
    totalTaxService.totalTax(nino, year) map { totalTax =>
      Ok(Json.toJson(ApiResponse(totalTax, Nil)))
    } recoverWith taxAccountErrorHandler
  }
}
