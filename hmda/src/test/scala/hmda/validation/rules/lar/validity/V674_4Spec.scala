package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PrimarilyBusinessOrCommercialPurpose
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V674_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V674_4

  property("If loan is for business, origination charges must be NA") {
    forAll(larGen) { lar =>
      whenever(
        lar.businessOrCommercialPurpose != PrimarilyBusinessOrCommercialPurpose) {
        lar.mustPass
      }

      val appLar = lar.copy(
        businessOrCommercialPurpose = PrimarilyBusinessOrCommercialPurpose)
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(originationCharges = "-10.0"))
        .mustFail
      appLar
        .copy(
          loanDisclosure =
            appLar.loanDisclosure.copy(originationCharges = "10.0"))
        .mustFail
      appLar
        .copy(loanDisclosure =
          appLar.loanDisclosure.copy(originationCharges = "NA"))
        .mustPass
    }
  }
}
