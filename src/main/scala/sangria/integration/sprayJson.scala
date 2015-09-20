package sangria.integration

import spray.json._


object sprayJson extends SprayJsonSupportLowPrioImplicits {

  implicit object SprayJsonResultMarshaller extends ResultMarshaller {
    type Node = JsValue

    def emptyMapNode = JsObject.empty
    def mapNode(keyValues: Seq[(String, JsValue)]) = JsObject(keyValues: _*)
    def addMapNodeElem(node: JsValue, key: String, value: JsValue) = JsObject(node.asInstanceOf[JsObject].fields + (key -> value))

    def emptyArrayNode = JsArray.empty
    def isEmptyArrayNode(array: JsValue) = array.asInstanceOf[JsArray].elements.isEmpty
    def arrayNode(values: Seq[JsValue]) = JsArray(values.toVector)
    def addArrayNodeElem(array: JsValue, elem: JsValue) = JsArray(array.asInstanceOf[JsArray].elements :+ elem)

    def stringNode(value: String) = JsString(value)
    def booleanNode(value: Boolean) = JsBoolean(value)
    def floatNode(value: Double) = JsNumber(value)
    def intNode(value: Int) = JsNumber(value)
    def bigIntNode(value: BigInt) = JsNumber(value)
    def bigDecimalNode(value: BigDecimal) = JsNumber(value)

    def nullNode = JsNull

    def renderCompact(node: JsValue) = node.compactPrint
    def renderPretty(node: JsValue) = node.prettyPrint
  }

  implicit object SprayJsonInputUnmarshaller extends InputUnmarshaller[JsValue] {
    def getRootMapValue(node: JsValue, key: String) = node.asInstanceOf[JsObject].fields get key

    def isArrayNode(node: JsValue) = node.isInstanceOf[JsArray]
    def getListValue(node: JsValue) = node.asInstanceOf[JsArray].elements

    def isMapNode(node: JsValue) = node.isInstanceOf[JsObject]
    def getMapValue(node: JsValue, key: String) = node.asInstanceOf[JsObject].fields get key
    def getMapKeys(node: JsValue) = node.asInstanceOf[JsObject].fields.keySet

    def isDefined(node: JsValue) = node != JsNull
    def getScalarValue(node: JsValue) = node match {
      case JsBoolean(b) => b
      case JsNumber(d) => d.toBigIntExact getOrElse d
      case JsString(s) => s
      case _ => throw new IllegalStateException(s"$node is not a scalar value")
    }
    def isScalarNode(node: JsValue) = node match {
      case _: JsBoolean | _: JsNumber | _: JsString => true
      case _ => false
    }

    def render(node: JsValue) = node.compactPrint
  }

  private object SprayJsonToInput extends ToInput[JsValue, JsValue] {
    def toInput(value: JsValue) = (value, SprayJsonInputUnmarshaller)
  }

  implicit def sprayJsonToInput[T <: JsValue]: ToInput[T, JsValue] =
    SprayJsonToInput.asInstanceOf[ToInput[T, JsValue]]
}

trait SprayJsonSupportLowPrioImplicits {
  implicit val SprayJsonInputUnmarshallerJObject =
    sprayJson.SprayJsonInputUnmarshaller.asInstanceOf[InputUnmarshaller[JsObject]]
}