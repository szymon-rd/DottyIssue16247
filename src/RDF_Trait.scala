package interf_based

import scala.util.Try
import RDF.URI
import scala.reflect.TypeTest

trait RDF:
  rdf =>

  type R = rdf.type

  type Node <: Matchable
  type URI <: Node
  type BNode <: Node
  type Literal <: Node

  given rops: ROps[R]

end RDF

object RDF:

  type Node[R <: RDF] <: Matchable = R match
    case GetNode[n] => n & Matchable

  type URI[R <: RDF] <: Node[R] = R match
    case GetURI[u] => u & Node[R]

  type BNode[R <: RDF] <: Node[R] = R match
    case GetBNode[bn] => bn & Node[R]

  type Literal[R <: RDF] <: Node[R] = R match
    case GetLiteral[l] => l & Node[R]

  private type GetNode[N <: Matchable] = RDF { type Node = N }
  private type GetURI[U] = RDF { type URI = U }
  private type GetBNode[N] = RDF { type BNode = N }
  private type GetLiteral[L] = RDF { type Literal = L }

end RDF

trait ROps[R <: RDF]:
  def mkUri(str: String): Try[RDF.URI[R]]
  def mkBNode(): RDF.BNode[R]
  def mkLit(str: String): RDF.Literal[R]

  given node2Uri: TypeTest[RDF.Node[R], RDF.URI[R]]
  given node2BN: TypeTest[RDF.Node[R], RDF.BNode[R]]
  given node2Lit: TypeTest[RDF.Node[R], RDF.Literal[R]]

  // def mkRelURI(str: String): Try[RDF.rURI[R]]
  protected def nodeVal(node: RDF.Node[R]): String

  extension (nd: RDF.Node[R])
    def value: String = nodeVal(nd)
    def authority(uri: RDF.URI[R]): Try[String] =
      Try(java.net.URI.create(nodeVal(uri)).getAuthority())
    def fold[T](
        uF: RDF.URI[R] => T,
        bF: RDF.BNode[R] => T,
        lF: RDF.Literal[R] => T
    ): T =
      nd match
        case u: RDF.URI[R]     => uF(u)
        case b: RDF.BNode[R]   => bF(b)
        case l: RDF.Literal[R] => lF(l)

end ROps

object SomeObject:
  def calculate[R <: RDF](node: RDF.Node[R])(using ops: ROps[R]): String =
    node.value

//Here we place classes for an implementation of RDF completely
// based on interfaces, similar to rdf4j does https://rdf4j.org/    
object InterfaceRDF {
  trait Node extends java.io.Serializable:
    def value: String

  trait Uri extends Node
  trait BNode extends Node
  trait Lit extends Node

  trait IFactory:
    def mkBNode(): BNode
    def mkUri(u: String): Uri
    def mkLit(u: String): Lit

  def getFactory: IFactory = AFactory

  private object AFactory extends IFactory:
    var bnode: Int = 0
    def mkBNode(): BNode =
      bnode = bnode + 1
      new BNode { def value = bnode.toString }
    def mkUri(u: String): Uri =
      new Uri { def value = u }
    def mkLit(u: String): Lit =
      new Lit { def value = u }
}
///*
object IRDF extends RDF:
  import InterfaceRDF as ir
  override opaque type Node <: Matchable = ir.Node
  override opaque type URI <: Node = ir.Uri
  override opaque type BNode <: Node = ir.BNode
  override opaque type Literal <: Node = ir.Lit

  given rops: ROps[R] with
    override def mkUri(str: String): Try[RDF.URI[R]] = Try(
      ir.getFactory.mkUri(str)
    )
    override def mkBNode(): RDF.BNode[R] = ir.getFactory.mkBNode()
    override def mkLit(str: String): RDF.Literal[R] = ir.getFactory.mkLit(str)
    override protected def nodeVal(node: RDF.Node[R]): String = node.value

    given node2Uri: TypeTest[RDF.Node[R], RDF.URI[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.URI[R]] =
        x match
          case u: (x.type & ir.Uri) => Some(u)
          case _                    => None

    given node2BN: TypeTest[RDF.Node[R], RDF.BNode[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.BNode[R]] =
        x match
          case u: (x.type & ir.BNode) => Some(u)
          case _                      => None

    given node2Lit: TypeTest[RDF.Node[R], RDF.Literal[R]] with
      def unapply(x: RDF.Node[R]): Option[x.type & RDF.Literal[R]] =
        x match
          case u: (x.type & ir.Lit) => Some(u)
          case _                    => None

end IRDF

class Test[R <: RDF](using rops: ROps[R]):
  import rops.given
  val uri: Try[RDF.URI[R]] = rops.mkUri("https://bblfish.net/#i")
  val x: Try[String] = uri.map((u: URI[R]) => SomeObject.calculate(u))
  val nodeU: RDF.Node[R] = uri.get
  val folded = nodeU.fold(
    u => "<" + u.value + ">",
    b => "_:" + b.value,
    l => l
  )
  val matched = nodeU match
    case bn: RDF.BNode[R]  => "isBNode " + bn
    case l: RDF.Literal[R] => "literal " + l
    case u: RDF.URI[R]     => "isURI " + u

end Test

@main def run =
  val test = Test[IRDF.type]
  println(test.x)
  println("folded=" + test.folded)
  println("matched should be uri" + test.matched)
