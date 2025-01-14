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

import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.auth.core.MissingBearerToken
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UnauthorizedException}
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.tai.controllers.predicates.AuthenticationPredicate
import uk.gov.hmrc.tai.mocks.MockAuthenticationPredicate
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.domain.calculation.CodingComponent
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.service.CodingComponentService
import uk.gov.hmrc.tai.util.{NpsExceptions, RequestQueryFilter}

import scala.concurrent.Future
import scala.util.Random

class CodingComponentControllerSpec
    extends PlaySpec with MockitoSugar with RequestQueryFilter with NpsExceptions with MockAuthenticationPredicate {

  "codingComponentsForYear" must {
    "return OK with sequence of coding components" when {
      "coding component service returns a sequence of coding components" in {
        val codingComponentSeq = Seq(
          CodingComponent(EmployerProvidedServices, Some(12), 12321, "Some Description"),
          CodingComponent(PersonalPensionPayments, Some(31), 12345, "Some Description Some")
        )

        val mockCodingComponentService = mock[CodingComponentService]
        when(mockCodingComponentService.codingComponents(Matchers.eq(nino), Matchers.eq(TaxYear().next))(any()))
          .thenReturn(Future.successful(codingComponentSeq))

        val sut = createSUT(mockCodingComponentService)
        val result = sut.codingComponentsForYear(nino, TaxYear().next)(FakeRequest())
        status(result) mustBe OK
        val expectedJson = Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "componentType" -> "EmployerProvidedServices",
              "employmentId"  -> 12,
              "amount"        -> 12321,
              "description"   -> "Some Description",
              "iabdCategory"  -> "Benefit"),
            Json.obj(
              "componentType" -> "PersonalPensionPayments",
              "employmentId"  -> 31,
              "amount"        -> 12345,
              "description"   -> "Some Description Some",
              "iabdCategory"  -> "Allowance"
            )
          ),
          "links" -> Json.arr()
        )
        contentAsJson(result) mustBe expectedJson
      }
    }

    "return NOT AUTHORISED" when {
      "the user is not logged in" in {

        val sut = createSUT(mock[CodingComponentService], notLoggedInAuthenticationPredicate)
        val result = sut.codingComponentsForYear(nino, TaxYear().next)(FakeRequest())
        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }

    "throw an exception" when {
      "an exception is thrown by the handler which is not a BadRequestException" in {
        val mockCodingComponentService = mock[CodingComponentService]
        val sut = createSUT(mockCodingComponentService)
        when(mockCodingComponentService.codingComponents(Matchers.eq(nino), Matchers.eq(TaxYear().next))(any()))
          .thenReturn(Future.failed(new UnauthorizedException("")))

        intercept[UnauthorizedException] {
          await(sut.codingComponentsForYear(nino, TaxYear().next)(FakeRequest()))
        }
      }
    }

    "return a bad request" when {
      "a BadRequestException is thrown" in {
        val mockCodingComponentService = mock[CodingComponentService]
        val sut = createSUT(mockCodingComponentService)
        when(mockCodingComponentService.codingComponents(Matchers.eq(nino), Matchers.eq(TaxYear().next))(any()))
          .thenReturn(Future.failed(new BadRequestException("bad request exception")))
        val result = sut.codingComponentsForYear(nino, TaxYear().next)(FakeRequest())
        status(result) mustBe 400
        val expectedJson = """{"reason":"bad request exception"}"""
        contentAsString(result) mustBe expectedJson
      }
    }

    "return a not found request" when {
      "a NotFoundException is thrown" in {
        val mockCodingComponentService = mock[CodingComponentService]
        val sut = createSUT(mockCodingComponentService)

        when(mockCodingComponentService.codingComponents(Matchers.eq(nino), Matchers.eq(TaxYear().next))(any()))
          .thenReturn(Future.failed(new NotFoundException("not found exception")))

        val result = sut.codingComponentsForYear(nino, TaxYear().next)(FakeRequest())
        status(result) mustBe 404

        val expectedJson = """{"reason":"not found exception"}"""
        contentAsString(result) mustBe expectedJson
      }
    }
  }

  val nino = new Generator(new Random).nextNino

  private implicit val hc = HeaderCarrier(sessionId = Some(SessionId("TEST")))

  private def createSUT(
    codingComponentService: CodingComponentService,
    predicate: AuthenticationPredicate = loggedInAuthenticationPredicate) =
    new CodingComponentController(predicate, codingComponentService)
}
