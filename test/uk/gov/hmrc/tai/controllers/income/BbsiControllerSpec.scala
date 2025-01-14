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

package uk.gov.hmrc.tai.controllers.income

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsNull, Json}
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.auth.core.MissingBearerToken
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.tai.controllers.benefits.BenefitsController
import uk.gov.hmrc.tai.controllers.predicates.AuthenticationPredicate
import uk.gov.hmrc.tai.mocks.MockAuthenticationPredicate
import uk.gov.hmrc.tai.model.api.ApiResponse
import uk.gov.hmrc.tai.model.domain.BankAccount
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.service.benefits.BenefitsService
import uk.gov.hmrc.tai.service.{BankAccountNotFound, BbsiService}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

class BbsiControllerSpec extends PlaySpec with MockitoSugar with MockAuthenticationPredicate {

  "Bbsi details" must {
    "return NOT AUTHORISED" when {
      "the user is not logged in" in {
        val sut = createSUT(mock[BbsiService], notLoggedInAuthenticationPredicate)
        val result = sut.bbsiDetails(nino)(FakeRequest())
        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }
    "return OK" in {
      val expectedJson =
        Json.obj(
          "data" -> Json.arr(
            Json.obj(
              "id"                     -> 0,
              "accountNumber"          -> "*****5566",
              "sortCode"               -> "112233",
              "bankName"               -> "ACCOUNT ONE",
              "grossInterest"          -> 1500.5,
              "source"                 -> "Customer",
              "numberOfAccountHolders" -> 1
            ),
            Json.obj(
              "id"                     -> 0,
              "accountNumber"          -> "*****5566",
              "sortCode"               -> "112233",
              "bankName"               -> "ACCOUNT ONE",
              "grossInterest"          -> 1500.5,
              "source"                 -> "Customer",
              "numberOfAccountHolders" -> 1
            ),
            Json.obj(
              "id"                     -> 0,
              "accountNumber"          -> "*****5566",
              "sortCode"               -> "112233",
              "bankName"               -> "ACCOUNT ONE",
              "grossInterest"          -> 1500.5,
              "source"                 -> "Customer",
              "numberOfAccountHolders" -> 1
            )
          ),
          "links" -> Json.arr()
        )

      val bankAccount = BankAccount(
        accountNumber = Some("*****5566"),
        sortCode = Some("112233"),
        bankName = Some("ACCOUNT ONE"),
        grossInterest = 1500.5,
        source = Some("Customer"),
        numberOfAccountHolders = Some(1)
      )

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.bbsiDetails(any(), any())(any()))
        .thenReturn(Future.successful(Seq(bankAccount, bankAccount, bankAccount)))

      val sut = createSUT(mockBbsiService)
      val result = sut.bbsiDetails(nino)(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe expectedJson
    }
  }

  "bbsiAccount" must {
    "return NOT AUTHORISED" when {
      "the user is not logged in" in {
        val sut = createSUT(mock[BbsiService], notLoggedInAuthenticationPredicate)
        val result = sut.bbsiAccount(nino, 1)(FakeRequest())
        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }
    "return bank account" in {
      val expectedJson =
        Json.obj(
          "data" -> Json.obj(
            "id"                     -> 1,
            "accountNumber"          -> "*****5566",
            "sortCode"               -> "112233",
            "bankName"               -> "ACCOUNT ONE",
            "grossInterest"          -> 1500.5,
            "source"                 -> "Customer",
            "numberOfAccountHolders" -> 1
          ),
          "links" -> Json.arr()
        )

      val bankAccount = BankAccount(
        1,
        accountNumber = Some("*****5566"),
        sortCode = Some("112233"),
        bankName = Some("ACCOUNT ONE"),
        grossInterest = 1500.5,
        source = Some("Customer"),
        numberOfAccountHolders = Some(1)
      )

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.bbsiAccount(any(), any())(any()))
        .thenReturn(Future.successful(Some(bankAccount)))

      val sut = createSUT(mockBbsiService)
      val result = sut.bbsiAccount(nino, 1)(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe expectedJson
    }

    "return not found" when {
      "account not found" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.bbsiAccount(any(), any())(any()))
          .thenReturn(Future.successful(None))

        val sut = createSUT(mockBbsiService)
        val result = sut.bbsiAccount(nino, 1)(FakeRequest())

        status(result) mustBe NOT_FOUND
      }

      "not found exception occur" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.bbsiAccount(any(), any())(any()))
          .thenReturn(Future.failed(new NotFoundException("Error")))

        val sut = createSUT(mockBbsiService)
        val result = sut.bbsiAccount(nino, 1)(FakeRequest())

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "close bank account" must {
    "return NOT AUTHORISED" when {
      "the user is not logged in" in {
        val sut = createSUT(mock[BbsiService], notLoggedInAuthenticationPredicate)
        val result = sut.closeBankAccount(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), JsNull)
            .withHeaders(("content-type", "application/json")))
        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }
    "return an envelope id" in {
      val envelopeId = "123456"

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.closeBankAccount(any(), any(), any())(any()))
        .thenReturn(Future.successful(envelopeId))

      val sut = createSUT(mockBbsiService)
      val result = sut.closeBankAccount(nino, 1)(
        FakeRequest("PUT", "/", FakeHeaders(), Json.obj("date" -> "2017-05-05", "closingInterest" -> 0))
          .withHeaders(("content-type", "application/json")))

      status(result) mustBe OK
      contentAsJson(result).as[ApiResponse[String]] mustBe ApiResponse(envelopeId, Nil)
    }

    "return Ok when closingInterest is provided" in {
      val envelopeId = "123456"

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.closeBankAccount(any(), any(), any())(any()))
        .thenReturn(Future.successful(envelopeId))

      val sut = createSUT(mockBbsiService)
      val result = sut.closeBankAccount(nino, 1)(
        FakeRequest("PUT", "/", FakeHeaders(), Json.obj("date" -> "2017-05-05", "closingInterest" -> 0))
          .withHeaders(("content-type", "application/json")))

      status(result) mustBe OK
      contentAsJson(result).as[ApiResponse[String]] mustBe ApiResponse(envelopeId, Nil)
    }

    "return Ok when closingInterest is not provided" in {
      val envelopeId = "123456"

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.closeBankAccount(any(), any(), any())(any()))
        .thenReturn(Future.successful(envelopeId))

      val sut = createSUT(mockBbsiService)
      val result = sut.closeBankAccount(nino, 1)(
        FakeRequest("PUT", "/", FakeHeaders(), Json.obj("date" -> "2017-05-05"))
          .withHeaders(("content-type", "application/json")))

      status(result) mustBe OK
      contentAsJson(result).as[ApiResponse[String]] mustBe ApiResponse(envelopeId, Nil)
    }

    "return not found" when {
      "bank account not found" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.closeBankAccount(any(), any(), any())(any()))
          .thenReturn(Future.failed(BankAccountNotFound("""{"Error":"Error"}""")))

        val sut = createSUT(mockBbsiService)
        val result = sut.closeBankAccount(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), Json.obj("date" -> "2017-05-05", "closingInterest" -> 0))
            .withHeaders(("content-type", "application/json")))

        status(result) mustBe NOT_FOUND
      }
    }

    "return internal server" when {
      "exception occur while removing bank account" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.closeBankAccount(any(), any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("Error")))

        val sut = createSUT(mockBbsiService)
        val result = sut.closeBankAccount(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), Json.obj("date" -> "2017-05-05", "closingInterest" -> 0))
            .withHeaders(("content-type", "application/json")))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "remove bank account" must {
    "return NOT AUTHORISED" when {
      "the user is not logged in" in {
        val sut = createSUT(mock[BbsiService], notLoggedInAuthenticationPredicate)
        val result = sut.removeAccount(nino, 1)(FakeRequest("DELETE", "/"))
        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }
    "return an envelope id" in {
      val envelopeId = "123456"

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.removeIncorrectBankAccount(any(), any())(any()))
        .thenReturn(Future.successful(envelopeId))

      val sut = createSUT(mockBbsiService)
      val result = sut.removeAccount(nino, 1)(FakeRequest("DELETE", "/"))

      status(result) mustBe ACCEPTED
      contentAsJson(result).as[ApiResponse[String]] mustBe ApiResponse(envelopeId, Nil)
    }

    "return not found" when {
      "bank account not found" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.removeIncorrectBankAccount(any(), any())(any()))
          .thenReturn(Future.failed(BankAccountNotFound("""{"Error":"Error"}""")))

        val sut = createSUT(mockBbsiService)
        val result = sut.removeAccount(nino, 1)(FakeRequest("DELETE", "/"))

        status(result) mustBe NOT_FOUND
      }
    }

    "return internal server" when {
      "exception occur while removing bank account" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.removeIncorrectBankAccount(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("Error")))

        val sut = createSUT(mockBbsiService)
        val result = sut.removeAccount(nino, 1)(FakeRequest("DELETE", "/"))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "update bank account" must {
    "return NOT AUTHORISED" when {
      "the user is not logged in" in {
        val sut = createSUT(mock[BbsiService], notLoggedInAuthenticationPredicate)
        val result = sut.updateAccountInterest(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), JsNull)
            .withHeaders(("content-type", "application/json")))

        ScalaFutures.whenReady(result.failed) { e =>
          e mustBe a[MissingBearerToken]
        }
      }
    }
    "return an envelope id" in {
      val envelopeId = "123456"

      val mockBbsiService = mock[BbsiService]
      when(mockBbsiService.updateBankAccountInterest(any(), any(), any())(any()))
        .thenReturn(Future.successful(envelopeId))

      val sut = createSUT(mockBbsiService)
      val result = sut.updateAccountInterest(nino, 1)(
        FakeRequest("PUT", "/", FakeHeaders(), Json.obj("amount" -> 1000.12))
          .withHeaders(("content-type", "application/json")))

      status(result) mustBe OK
      contentAsJson(result).as[ApiResponse[String]] mustBe ApiResponse(envelopeId, Nil)
    }

    "return not found" when {
      "bank account not found" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.updateBankAccountInterest(any(), any(), any())(any()))
          .thenReturn(Future.failed(BankAccountNotFound("""{"Error":"Error"}""")))

        val sut = createSUT(mockBbsiService)
        val result = sut.updateAccountInterest(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), Json.obj("amount" -> 1000.12))
            .withHeaders(("content-type", "application/json")))

        status(result) mustBe NOT_FOUND
      }
    }

    "throw HttpException" when {
      "http exception occurs" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.updateBankAccountInterest(any(), any(), any())(any()))
          .thenReturn(Future.failed(new BadRequestException("sdsd")))

        val sut = createSUT(mockBbsiService)
        val result = the[BadRequestException] thrownBy Await.result(
          sut.updateAccountInterest(nino, 1)(FakeRequest("PUT", "/", FakeHeaders(), Json.obj("amount" -> 1000.12))
            .withHeaders(("content-type", "application/json"))),
          5.seconds
        )

        result.responseCode mustBe BAD_REQUEST
      }
    }

    "return internal server" when {
      "exception occur while removing bank account" in {
        val mockBbsiService = mock[BbsiService]
        when(mockBbsiService.updateBankAccountInterest(any(), any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException("Error")))

        val sut = createSUT(mockBbsiService)
        val result = sut.updateAccountInterest(nino, 1)(
          FakeRequest("PUT", "/", FakeHeaders(), Json.obj("amount" -> 1000.12))
            .withHeaders(("content-type", "application/json")))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  private implicit val hc = HeaderCarrier(sessionId = Some(SessionId("TEST")))
  private val nino = new Generator(new Random).nextNino

  private def createSUT(
    bbsiService: BbsiService,
    authentication: AuthenticationPredicate = loggedInAuthenticationPredicate) =
    new BbsiController(bbsiService, authentication)
}
