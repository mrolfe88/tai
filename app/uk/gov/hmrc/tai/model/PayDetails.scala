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

package uk.gov.hmrc.tai.model

import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.tai.model.enums.PayFreq._

case class PayDetails(
  paymentFrequency: PayFreq,
  pay: Option[BigDecimal] = None,
  taxablePay: Option[BigDecimal] = None,
  days: Option[Int] = None,
  bonus: Option[BigDecimal] = None,
  startDate: Option[LocalDate] = None)

object PayDetails {
  implicit val formats = Json.format[PayDetails]
}
