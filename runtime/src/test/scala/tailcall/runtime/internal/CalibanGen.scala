package tailcall.runtime.internal

import caliban.{InputValue, ResponseValue, Value}
import zio.test.Gen

object CalibanGen {
  val probablePrime = BigInt("799058976649937674302168095891")

  val genName   = Gen.string1(Gen.alphaChar)
  val genBigInt = Gen.bigInt(BigInt(0), probablePrime)
  val genBigNum = Gen.bigDecimal(BigDecimal(0), BigDecimal(probablePrime))

  val genIntValue: Gen[Any, Value.IntValue] = Gen.oneOf(
    Gen.int.map(Value.IntValue.IntNumber),
    Gen.long.map(Value.IntValue.LongNumber),
    genBigInt.map(Value.IntValue.BigIntNumber),
  )

  val genFloatValue: Gen[Any, Value.FloatValue] = Gen.oneOf(
    Gen.float.map(Value.FloatValue.FloatNumber),
    Gen.double.map(Value.FloatValue.DoubleNumber),
    genBigNum.map(Value.FloatValue.BigDecimalNumber),
  )

  val genValue: Gen[Any, Value] = Gen.oneOf(
    Gen.const(Value.NullValue),
    genIntValue,
    genFloatValue,
    Gen.string.map(Value.StringValue),
    Gen.boolean.map(Value.BooleanValue),
  )

  val genInputValue: Gen[Any, InputValue] = Gen.suspend(Gen.oneOf(
    Gen.listOfBounded(0, 2)(genInputValue).map(InputValue.ListValue),
    Gen.mapOfBounded(0, 2)(genName, genInputValue).map(InputValue.ObjectValue),
    genValue,
  ))

  val genResponseValue: Gen[Any, ResponseValue] = Gen.suspend(Gen.oneOf(
    Gen.listOfBounded(0, 2)(genResponseValue).map(ResponseValue.ListValue),
    Gen.listOfBounded(0, 2)(for {
      key   <- genName
      value <- genResponseValue
    } yield key -> value).map(ResponseValue.ObjectValue),
    genValue,
  ))
}
