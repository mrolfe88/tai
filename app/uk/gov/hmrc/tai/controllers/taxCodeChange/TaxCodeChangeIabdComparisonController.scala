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

package uk.gov.hmrc.tai.controllers.taxCodeChange

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.tai.controllers.predicates.AuthenticationPredicate
import uk.gov.hmrc.tai.model.TaxFreeAmountComparison
import uk.gov.hmrc.tai.service.TaxFreeAmountComparisonService
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.tai.model.api.ApiResponse

class TaxCodeChangeIabdComparisonController @Inject()(
  taxFreeAmountComparisonService: TaxFreeAmountComparisonService,
  authentication: AuthenticationPredicate)
    extends BaseController {

  def taxCodeChangeIabdComparison(nino: Nino): Action[AnyContent] = authentication.async { implicit request =>
    taxFreeAmountComparisonService.taxFreeAmountComparison(nino).map { comparison: TaxFreeAmountComparison =>
      Ok(Json.toJson(ApiResponse(Json.toJson(comparison), Seq.empty)))
    } recover {
      case ex: Exception => BadRequest(Json.toJson(Map("reason" -> ex.getMessage)))
    }
  }
}
