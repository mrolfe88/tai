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

package uk.gov.hmrc.tai.service

import com.google.inject.{Inject, Singleton}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tai.model.domain.calculation.{BankInterestIncomeCategory, CodingComponent, IncomeCategory}
import uk.gov.hmrc.tai.model.domain.formatters.TaxAccountSummaryHodFormatters
import uk.gov.hmrc.tai.model.domain._
import uk.gov.hmrc.tai.model.tai.TaxYear
import uk.gov.hmrc.tai.repositories.TaxAccountSummaryRepository

import scala.concurrent.Future
import scala.language.postfixOps

@Singleton
class TaxAccountSummaryService @Inject()(taxAccountSummaryRepository: TaxAccountSummaryRepository,
                                         codingComponentService: CodingComponentService,
                                         incomeService: IncomeService,
                                         totalTaxService: TotalTaxService) extends TaxAccountSummaryHodFormatters {

  def taxAccountSummary(nino: Nino, year: TaxYear)(implicit hc: HeaderCarrier): Future[TaxAccountSummary] = {

    for {
      totalEstimatedTax <- taxAccountSummaryRepository.taxAccountSummary(nino, year)
      taxFreeAmountComponents <- codingComponentService.codingComponents(nino, year)
      taxCodeIncomes <- incomeService.taxCodeIncomes(nino, year)
      totalTax <- totalTaxService.totalTax(nino, year)
      taxFreeAllowance <- totalTaxService.taxFreeAllowance(nino, year)
    } yield {

      val taxFreeAmount = taxFreeAmountCalculation(taxFreeAmountComponents)
      val totalIyaIntoCY = taxCodeIncomes map (_.inYearAdjustmentIntoCY) sum
      val totalIya = taxCodeIncomes map (_.totalInYearAdjustment) sum
      val totalIyatIntoCYPlusOne = taxCodeIncomes map (_.inYearAdjustmentIntoCYPlusOne) sum
      val totalTaxableIncome = totalTax.incomeCategories.map(_.totalTaxableIncome).sum

      val incomeCategoriesWithoutBankInterest=totalTax.incomeCategories.withFilter(_.incomeCategoryType != BankInterestIncomeCategory)
      val totalEstimatedIncome = if (totalTaxableIncome == 0) {
        incomeCategoriesWithoutBankInterest.map(_.totalIncome).sum
      } else {
        val totalBBSI =  totalBBSIAmount(totalTax.incomeCategories)
        val incomeCategoriesSumWithoutBBSI = incomeCategoriesWithoutBankInterest.map(_.totalTaxableIncome).sum
        incomeCategoriesSumWithoutBBSI + taxFreeAllowance + totalBBSI
      }

      TaxAccountSummary(totalEstimatedTax, taxFreeAmount, totalIyaIntoCY, totalIya, totalIyatIntoCYPlusOne, totalEstimatedIncome, taxFreeAllowance)
    }
  }

  private[service] def taxFreeAmountCalculation(codingComponents: Seq[CodingComponent]): BigDecimal = {
    codingComponents.foldLeft(BigDecimal(0))((total: BigDecimal, component: CodingComponent) =>
      component.componentType match {
        case _: AllowanceComponentType => total + component.amount.abs
        case _ => total - component.amount.abs
      })
  }

  private def totalBBSIAmount(incomeCategories: Seq[IncomeCategory]):BigDecimal={
    val bankInterestCategoryOnly=incomeCategories.withFilter(_.incomeCategoryType == BankInterestIncomeCategory)
    val bbsiTotalTax=bankInterestCategoryOnly.map(_.totalTax).sum
    if (bbsiTotalTax == 0) {
      0
    }
    else {
      bankInterestCategoryOnly.map(_.totalTaxableIncome).sum
    }
  }

}