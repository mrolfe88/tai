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

import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.http.{BadRequestException, NotFoundException}
import play.api.mvc.Results._
import scala.concurrent.Future

trait ControllerErrorHandler {

  def taxAccountErrorHandler(): PartialFunction[Throwable, Future[Result]] = {
    case ex: BadRequestException => Future.successful(BadRequest(ex.message))
    case ex: NotFoundException   => Future.successful(NotFound(ex.message))
    case ex                      => throw ex
  }
}
