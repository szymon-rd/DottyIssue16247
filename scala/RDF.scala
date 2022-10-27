package generic 

import scala.util.Try
import RDF.URI
import scala.reflect.TypeTest

// here is the structural code that which is then implemented in 
// a number of ways... 

trait RDF:
  rdf =>

  type R = rdf.type
  
  type rNode <: Matchable
  type rURI <: rNode
  type Node <: rNode
  type URI <: Node & rURI
  type BNode <: Node
  type Literal <: Node

  given rops: ROps[R]

end RDF

object RDF:
  type rNode[R <: RDF] <: Matchable = R match
    case GetRelNode[n] => n & Matchable

  type rURI[R <: RDF] <: rNode[R] = R match
    case GetRelURI[n] => n & rNode[R]

  type Node[R <: RDF] <: rNode[R] = R match
    case GetNode[n] => n & rNode[R]

  type URI[R <: RDF] <: Node[R] = R match
    case GetURI[u] => u & Node[R] & rURI[R]

  type BNode[R <: RDF] <: Node[R] = R match
    case GetBNode[bn] => bn & Node[R]

  type Literal[R <: RDF] <: Node[R] = R match
    case GetLiteral[l] => l & Node[R]

  private type GetRelNode[N <: Matchable] = RDF { type rNode = N }
  private type GetNode[N] = RDF { type Node = N }
  private type GetRelURI[U] = RDF { type rURI = U }
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
  protected def auth(uri: RDF.URI[R]): Try[String]
   
  extension (uri: URI[R])
    def authority: Try[String] = auth(uri)

  extension (nd: RDF.Node[R])
    def value: String = nodeVal(nd)
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
    case u: RDF.URI[R]     => "isURI " + u + "with authority " + u.authority

end Test


